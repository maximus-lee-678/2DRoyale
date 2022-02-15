package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import entity.PlayerMP;
import item.SuperWeapon;
import main.Game;

public class GameServer extends Thread {

	private DatagramSocket socket;
	private Game game;
	private long seed;
	public List<PlayerMP> connectedPlayers = new ArrayList<PlayerMP>();

	private int gameTicks = 0;
	private int gameState;
	public final int waitState = 1;
	public final int playState = 2;
	public final int endState = 3;

	private int countDownSeq;
	private int weaponIdCount;
	public int playerRemaining;

	public GameServer(Game game, long seed) {
		this.game = game;
		this.seed = seed;
		this.weaponIdCount = 0;
		this.gameState = waitState;
		this.countDownSeq = -1;
		this.playerRemaining = 0;
		try {
			this.socket = new DatagramSocket(2207);
		} catch (SocketException e) {
			e.printStackTrace();
		}

	}

	public void run() {
		while (!socket.isClosed()) { // listen for new packets
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			readPacket(packet.getData(), packet.getAddress(), packet.getPort()); // call readPacket() to handle packet
		}
	}

	private void readPacket(byte[] data, InetAddress address, int port) {
		int type = lookupPacket(data);

		switch (type) {
		case 1:
			// LOGIN
			Pkt01Login loginPacket = new Pkt01Login(data);
			PlayerMP player = new PlayerMP(game, loginPacket.getUsername(), loginPacket.getWorldX(), loginPacket.getWorldY(), loginPacket.getPlayerWeapIndex(), address, port);
			if (addConnection(player, loginPacket)) 
				System.out.println("Server: [" + address.getHostAddress() + ":" + port + "] " + loginPacket.getUsername() + " has connected...");
			break;
		case 2:
			// DISCONNECT
			Pkt02Disconnect disconnectPacket = new Pkt02Disconnect(data);
			System.out.println("Server: [" + address.getHostAddress() + ":" + port + "] " + disconnectPacket.getUsername() + " has left the game...");
			removeConnection(disconnectPacket);
			break;
		case 3:
			// MOVEMENT
			Pkt03Move movePacket = new Pkt03Move(data);
			handleMove(movePacket);
			break;
		case 4:
			// MOUSEMOVE
			Pkt04MouseMove mouseMovePacket = new Pkt04MouseMove(data);
			handleMouseMove(mouseMovePacket);
			break;
		case 5:
			// MOUSESCROLL
			Pkt05MouseScroll mouseScrollPacket = new Pkt05MouseScroll(data);
			handleMouseScroll(mouseScrollPacket);
			break;
		case 6:
			// SHOOT
			if (gameState != playState) return;
			Pkt06Shoot shootPacket = new Pkt06Shoot(data);
			handleShoot(shootPacket);
			break;
		case 8:
			// SERVER TICK
			Pkt08ServerPing pingPacket = new Pkt08ServerPing();
			sendData(pingPacket.getData(), address, port);
			break;
		case 10:
			// WEAPON PICK UP
			if (gameState != playState) return;
			Pkt10PickupWeapon pickUpPacket = new Pkt10PickupWeapon(data);
			handlePickUpWeapon(pickUpPacket);
			break;
		case 11:
			// OPEN CRATE
			if (gameState != playState) return;
			Pkt11CrateOpen crateOpenPacket = new Pkt11CrateOpen(data);
			handleCrateOpen(crateOpenPacket);
			break;
		case 12:
			// WEAPON DROP
			if (gameState != playState) return;
			Pkt12DropWeapon dropPacket = new Pkt12DropWeapon(data);
			handleDropWeapon(dropPacket);
			break;
		case 14:
			// START GAME
			handleStartGame();
			break;
		case 17:
			// PLAYER BACK TO LOBBY
			Pkt17BackToLobby backToLobbyPacket = new Pkt17BackToLobby(data);
			handleBackToLobby(backToLobbyPacket);
			break;
		case 20:
			// GAS DAMAGE
			Pkt20GasDamage gasDmgPacket = new Pkt20GasDamage(data);
			handleGasDamage(gasDmgPacket);
			break;
		default:
		case 0:
			break;
		}
	}
	
	public void update() {
		gameTicks++;

		if (countDownSeq >= 0 && gameTicks % 60 == 0) {
			new Pkt15CountdownSeq(countDownSeq).sendData(this);
			countDownSeq--;
		}
		if (gameState == playState && gameTicks % 120 == 0) { // gas speed
			handleCloseGas();
		}
		// Check if no remaining players
		if (gameState == playState && countDownSeq < 0 && playerRemaining == 1) {
			String lastPlayer = findPlayerInPlayState();
			new Pkt18Winner(lastPlayer).sendData(this);
			connectedPlayers.get(playerIndex(lastPlayer)).playerState = endState;
			gameState = waitState;
		}
		// Check for bullet hit
		for (PlayerMP p : connectedPlayers) {
			for (SuperWeapon weap : p.getWeapons()) {
				if (weap != null) {
					weap.checkPlayerHit(this);
					weap.update();
				}
			}
		}
	}

	private void handleGasDamage(Pkt20GasDamage gasDmgPacket) {
		PlayerMP gasVictim = connectedPlayers.get(playerIndex(gasDmgPacket.getUsername()));
		if (gasVictim.playerState != playState) return;
		// Decrease player hp
		gasVictim.updatePlayerHP(-1);
		gasDmgPacket.sendData(this);
		// Check player death
		if (gasVictim.health == 0) {
			if (gasVictim.getWeapons()[gasVictim.playerWeapIndex] != null) {
				SuperWeapon dropWeap = gasVictim.getWeapons()[gasVictim.playerWeapIndex];
				new Pkt12DropWeapon(gasVictim.getUsername(), gasVictim.playerWeapIndex, dropWeap.typeId, dropWeap.id, gasVictim.worldX - dropWeap.imgIconWidth / 2 + game.playerSize / 2, gasVictim.worldY - dropWeap.imgIconHeight / 2 + game.playerSize / 2).sendData(game.socketClient);
			}
			gasVictim.playerState = endState;
			new Pkt16Death("Gas", gasVictim.getUsername(), playerRemaining--).sendData(this);
		}
	}

	private void handleBackToLobby(Pkt17BackToLobby backToLobbyPacket) {
		PlayerMP playerBTL = connectedPlayers.get(playerIndex(backToLobbyPacket.getUsername()));
		// Decrement player remaining if player leaves during the game
		if (connectedPlayers.get(playerIndex(backToLobbyPacket.getUsername())).playerState == playState) 
			playerRemaining--;
		// Reset player
		playerBTL.setPlayerDefault();
		backToLobbyPacket.sendData(this);
		// Send player the newly generated seed
		Pkt07ServerSeed seedPacket = new Pkt07ServerSeed(this.seed);
		sendData(seedPacket.getData(), playerBTL.ipAddress, playerBTL.port);
	}

	private void handleDropWeapon(Pkt12DropWeapon dropPacket) {
		connectedPlayers.get(playerIndex(dropPacket.getUsername())).dropWeapon(dropPacket.getPlayerWeapIndex());
		dropPacket.sendData(this);
	}

	private void handleStartGame() {
		// Host can't start again if game is in progress
		if (gameState == playState) return;
		if (findPlayerInPlayState() != null) return;
		gameState = playState;
		playerRemaining = 0;
		countDownSeq = 5;
		// Tell all players who entered a new game
		for (PlayerMP p : connectedPlayers) {
			if (p.playerState == endState) continue;
			playerRemaining++;
			p.playerState = game.playState;
			new Pkt14StartGame(p.getUsername()).sendData(this);
		}
		// Generate new seed
		this.seed = System.currentTimeMillis();
	}

	private void handleCrateOpen(Pkt11CrateOpen crateOpenPacket) {
		// Generate a random value to spawn a random weapon
		Random r = new Random();
		crateOpenPacket.setWeapType(r.nextInt(game.itemM.weaponsArr.length));
		crateOpenPacket.setWeapId(weaponIdCount++);
		crateOpenPacket.sendData(this);
	}

	private void handlePickUpWeapon(Pkt10PickupWeapon pickUpPacket) {
		connectedPlayers.get(playerIndex(pickUpPacket.getUsername())).addWeapon(pickUpPacket.getPlayerWeapIndex(), pickUpPacket.getWeapType(), pickUpPacket.getWeapId());
		pickUpPacket.sendData(this);
	}

	
	private void handleCloseGas() {
		// Send all players in game info of the gas closing
		for (PlayerMP p : connectedPlayers) {
			if (p.playerState != playState) continue;
			Pkt13Gas gasPacket = new Pkt13Gas();
			sendData(gasPacket.getData(), p.ipAddress, p.port);
		}
	}

	private void handleShoot(Pkt06Shoot shootPacket) {
		PlayerMP p = connectedPlayers.get(playerIndex(shootPacket.getUsername()));
		// Spawn bullet at player
		p.getWeapons()[weapIndex(p, shootPacket.getWeapId())].updateMPProjectiles(shootPacket.getProjAngle(), shootPacket.getWorldX(), shootPacket.getWorldY());
		shootPacket.sendData(this);
	}

	private void handleMouseScroll(Pkt05MouseScroll mouseScrollPacket) {
		PlayerMP p = connectedPlayers.get(playerIndex(mouseScrollPacket.getUsername()));
		p.playerMouseScroll(mouseScrollPacket.getMouseScrollDir());
		mouseScrollPacket.sendData(this);
	}

	private void handleMouseMove(Pkt04MouseMove mouseMovePacket) {
		PlayerMP p = connectedPlayers.get(playerIndex(mouseMovePacket.getUsername()));
		p.updateMouseDirection(mouseMovePacket.getMouseX(), mouseMovePacket.getMouseY());
		mouseMovePacket.sendData(this);
	}

	private void handleMove(Pkt03Move movePacket) {
		PlayerMP p = connectedPlayers.get(playerIndex(movePacket.getUsername()));
		p.updatePlayerXY(movePacket.getWorldX(), movePacket.getWorldY());
		movePacket.sendData(this);
	}

	private void shutDownServer(PlayerMP isHost) {
		// Disconnect everyone before disconnecting the host
		for (int i = 1; i < connectedPlayers.size(); i++) {
			PlayerMP kickPlayer = connectedPlayers.get(1);
			Pkt19ServerKick playersKickPacket = new Pkt19ServerKick(false);
			sendData(playersKickPacket.getData(), kickPlayer.ipAddress, kickPlayer.port);
		}
		Pkt19ServerKick hostKickPacket = new Pkt19ServerKick(true);
		sendData(hostKickPacket.getData(), isHost.ipAddress, isHost.port);
		socket.close();
		return;
	}

	private void removeConnection(Pkt02Disconnect disconnectPacket) {
		PlayerMP isHost = connectedPlayers.get(0);
		// Check if host disconnects
		if (isHost.getUsername().equals(disconnectPacket.getUsername())) {
			shutDownServer(isHost);
			return;
		}
		if (connectedPlayers.get(playerIndex(disconnectPacket.getUsername())).playerState == playState) playerRemaining--;
		connectedPlayers.remove(playerIndex(disconnectPacket.getUsername()));
		disconnectPacket.sendData(this);
	}

	public boolean addConnection(PlayerMP player, Pkt01Login loginPacket) {
		boolean isConnected = false;

		for (PlayerMP p : connectedPlayers) {
			if (player.getUsername().equalsIgnoreCase(p.getUsername())) {
				// Update host's ip and port 
				if (p.ipAddress == null && p.port == -1) {
					p.ipAddress = player.ipAddress;
					p.port = player.port;
				} else
					return false;
				isConnected = true;
			} else {
				// Send information of the new player to all connected players already in the server
				sendData(loginPacket.getData(), p.ipAddress, p.port);
				
				// Send information of already connected players to the new player
				Pkt01Login otherPlayersLoginPacket = new Pkt01Login(p.getUsername(), p.worldX, p.worldY, p.playerWeapIndex, p.playerState);
				sendData(otherPlayersLoginPacket.getData(), player.ipAddress, player.port);
			}
		}
		if (!isConnected) {
			connectedPlayers.add(player);
			if (!player.isLocal) {
				// If not host send them the seed
				sendData(loginPacket.getData(), player.ipAddress, player.port);
				Pkt07ServerSeed seedPacket = new Pkt07ServerSeed(this.seed);
				sendData(seedPacket.getData(), player.ipAddress, player.port);
			}
		}
		return true;
	}

	private int playerIndex(String username) {
		int index = 0;
		for (PlayerMP p : connectedPlayers) {
			if (p.getUsername().equals(username)) {
				break;
			}
			index++;
		}
		return index;
	}

	private int weapIndex(PlayerMP player, int weapId) {
		int index = 0;

		for (SuperWeapon w : player.getWeapons()) {
			if (w != null && w.id == weapId) {
				return index;
			}
			index++;
		}
		return -1;
	}

	private String findPlayerInPlayState() {
		for (PlayerMP p : connectedPlayers) {
			if (p.playerState == playState) {
				return p.getUsername();
			}
		}
		return null;
	}

	private int lookupPacket(byte[] data) { // match player username to get index
		String message = new String(data).trim().substring(0, 2);
		int packetType;
		try {
			packetType = Integer.parseInt(message);
		} catch (NumberFormatException e) {
			packetType = 0;
		}
		return packetType;
	}

	public void sendData(byte[] data, InetAddress ipAddress, int port) { // send data from server to client
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendDataToAllClients(byte[] data) { // send data from server to all clients
		for (PlayerMP p : connectedPlayers) {
			sendData(data, p.ipAddress, p.port);
		}
	}

}
