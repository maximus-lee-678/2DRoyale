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

public class GameServer extends Thread  {

	private DatagramSocket socket;
	private Game game;
	private long seed;
	private List<PlayerMP> connectedPlayers = new ArrayList<PlayerMP>();

	private int gameTicks;
	private int countDownSeq;
	private int weaponIdCount;
	private int playerRemaining;

	private int gameState;
	
	public GameServer(Game game, long seed) {
		this.game = game;
		this.seed = seed;
		this.weaponIdCount = 0;
		this.gameTicks = 0;
		this.gameState = Game.waitState;
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
			if (gameState != Game.playState)
				return;
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
			if (gameState != Game.playState)
				return;
			Pkt10PickupWeapon pickUpPacket = new Pkt10PickupWeapon(data);
			handlePickUpWeapon(pickUpPacket);
			break;
		case 11:
			// OPEN CRATE
			if (gameState != Game.playState)
				return;
			Pkt11CrateOpen crateOpenPacket = new Pkt11CrateOpen(data);
			handleCrateOpen(crateOpenPacket);
			break;
		case 12:
			// WEAPON DROP
			if (gameState != Game.playState)
				return;
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
		
		if (gameState == Game.playState) {
			//gas speed
			if(gameTicks % 120 == 0) 
				handleCloseGas();
			// Check if no remaining players
			if (countDownSeq < 0 && playerRemaining <= 1) {
				String lastPlayer = findPlayerInPlayState();
				new Pkt18Winner(lastPlayer).sendData(this);
				getPlayers().get(playerIndex(lastPlayer)).setPlayerState(Game.endState);
				gameState = Game.waitState;
			}
			// Check for bullet hit
			for (PlayerMP p : getPlayers()) {
				for (SuperWeapon weap : p.getWeapons()) {
					if (weap != null) {
						weap.checkPlayerHit(this);
						weap.update();
					}
				}
			}
		}
		
	}

	private void handleGasDamage(Pkt20GasDamage gasDmgPacket) {
		int playerI = playerIndex(gasDmgPacket.getUsername());
		if(playerI == -1) return;
		PlayerMP gasVictim = getPlayers().get(playerI);
		if (gasVictim.getPlayerState() != Game.playState)
			return;
		// Decrease player hp
		gasVictim.updatePlayerHP(-1);
		gasDmgPacket.sendData(this);
		// Check player death
		if (gasVictim.getHealth() == 0) {
			if (gasVictim.getWeapons()[gasVictim.getPlayerWeapIndex()] != null) {
				SuperWeapon dropWeap = gasVictim.getWeapons()[gasVictim.getPlayerWeapIndex()];
				new Pkt12DropWeapon(gasVictim.getUsername(), gasVictim.getPlayerWeapIndex(), dropWeap.getTypeId(), dropWeap.getId(),
						gasVictim.getWorldX() - dropWeap.getImgIconWidth() / 2 + Game.playerSize / 2, gasVictim.getWorldY() - dropWeap.getImgIconHeight() / 2 + Game.playerSize / 2)
								.sendData(this);
			}
			gasVictim.setPlayerState(Game.endState);
			new Pkt16Death("Gas", gasVictim.getUsername(), playerRemaining--).sendData(this);
		}
	}

	private void handleBackToLobby(Pkt17BackToLobby backToLobbyPacket) {
		int playerI = playerIndex(backToLobbyPacket.getUsername());
		if(playerI == -1) return;
		PlayerMP playerBTL = getPlayers().get(playerI);
		// Decrement player remaining if player leaves during the game
		if (getPlayers().get(playerI).getPlayerState() == Game.playState)
			playerRemaining--;
		// Reset player
		playerBTL.setPlayerDefault();
		backToLobbyPacket.sendData(this);
		// Send player the newly generated seed
		Pkt07ServerSeed seedPacket = new Pkt07ServerSeed(this.seed);
		sendData(seedPacket.getData(), playerBTL.getIpAddress(), playerBTL.getPort());
	}

	private void handleDropWeapon(Pkt12DropWeapon dropPacket) {
		int playerI = playerIndex(dropPacket.getUsername());
		if(playerI == -1) return;
		getPlayers().get(playerI).dropWeapon(dropPacket.getPlayerWeapIndex());
		dropPacket.sendData(this);
	}

	private void handleStartGame() {
		// Host can't start again if game is in progress
		if (gameState == Game.playState)
			return;
		if (findPlayerInPlayState() != null)
			return;
		gameState = Game.playState;
		playerRemaining = 0;
		countDownSeq = 5;
		// Tell all players who entered a new game
		for (PlayerMP p : getPlayers()) {
			if (p.getPlayerState() == Game.endState)
				continue;
			playerRemaining++;
			p.setPlayerState(Game.playState);
			new Pkt14StartGame(p.getUsername()).sendData(this);
		}
		// Generate new seed
		this.seed = System.currentTimeMillis();
	}

	private void handleCrateOpen(Pkt11CrateOpen crateOpenPacket) {
		// Generate a random value to spawn a random weapon
		Random r = new Random();
		crateOpenPacket.setWeapType(r.nextInt(game.itemM.getWeaponsArr().length));
		crateOpenPacket.setWeapId(weaponIdCount++);
		crateOpenPacket.sendData(this);
	}

	private void handlePickUpWeapon(Pkt10PickupWeapon pickUpPacket) {
		int playerI = playerIndex(pickUpPacket.getUsername());
		if(playerI == -1) return;
		getPlayers().get(playerI).addWeapon(pickUpPacket.getPlayerWeapIndex(), pickUpPacket.getWeapType(), pickUpPacket.getWeapId());
		pickUpPacket.sendData(this);
	}

	private void handleCloseGas() {
		// Send all players in game info of the gas closing
		for (PlayerMP p : getPlayers()) {
			if (p.getPlayerState() != Game.playState)
				continue;
			Pkt13Gas gasPacket = new Pkt13Gas();
			sendData(gasPacket.getData(), p.getIpAddress(), p.getPort());
		}
	}

	private void handleShoot(Pkt06Shoot shootPacket) {
		int playerI = playerIndex(shootPacket.getUsername());
		if(playerI == -1) return;
		PlayerMP p = getPlayers().get(playerI);
		// Spawn bullet at player
		int weapI = weapIndex(p, shootPacket.getWeapId());
		if (weapI != -1) {
			p.getWeapons()[weapI].updateMPProjectiles(shootPacket.getProjAngle(), shootPacket.getWorldX(), shootPacket.getWorldY());
			shootPacket.sendData(this);
		}		
	}

	private void handleMouseScroll(Pkt05MouseScroll mouseScrollPacket) {
		int playerI = playerIndex(mouseScrollPacket.getUsername());
		if(playerI == -1) return;
		PlayerMP p = getPlayers().get(playerI);
		p.playerMouseScroll(mouseScrollPacket.getMouseScrollDir());
		mouseScrollPacket.sendData(this);
	}

	private void handleMouseMove(Pkt04MouseMove mouseMovePacket) {
		int playerI = playerIndex(mouseMovePacket.getUsername());
		if(playerI == -1) return;
		PlayerMP p = getPlayers().get(playerI);
		p.updateMouseDirection(mouseMovePacket.getMouseX(), mouseMovePacket.getMouseY());
		mouseMovePacket.sendData(this);
	}

	private void handleMove(Pkt03Move movePacket) {
		int playerI = playerIndex(movePacket.getUsername());
		if(playerI == -1) return;
		PlayerMP p = getPlayers().get(playerI);
		p.updatePlayerXY(movePacket.getWorldX(), movePacket.getWorldY());
		movePacket.sendData(this);
	}

	private void shutDownServer(PlayerMP isHost) {
		// Disconnect everyone before disconnecting the host
		for (int i = 1; i < getPlayers().size(); i++) {
			PlayerMP kickPlayer = getPlayers().get(1);
			Pkt19ServerKick playersKickPacket = new Pkt19ServerKick(false);
			sendData(playersKickPacket.getData(), kickPlayer.getIpAddress(), kickPlayer.getPort());
		}
		Pkt19ServerKick hostKickPacket = new Pkt19ServerKick(true);
		sendData(hostKickPacket.getData(), isHost.getIpAddress(), isHost.getPort());
		socket.close();
		return;
	}

	private void removeConnection(Pkt02Disconnect disconnectPacket) {
		PlayerMP isHost = getPlayers().get(0);
		// Check if host disconnects
		if (isHost.getUsername().equals(disconnectPacket.getUsername())) {
			shutDownServer(isHost);
			return;
		}
		int playerI = playerIndex(disconnectPacket.getUsername());
		if(playerI == -1) return;
		if (getPlayers().get(playerI).getPlayerState() == Game.playState)
			playerRemaining--;
		getPlayers().remove(playerI);
		disconnectPacket.sendData(this);
	}

	public boolean addConnection(PlayerMP player, Pkt01Login loginPacket) {
		boolean isConnected = false;

		for (PlayerMP p : getPlayers()) {
			if (player.getUsername().equalsIgnoreCase(p.getUsername())) {
				// Update host's ip and port
				if (p.getIpAddress() == null && p.getPort() == -1) {
					p.setIpAddress(player.getIpAddress());
					p.setPort(player.getPort());
				} else
					return false;
				isConnected = true;
			} else {
				// Send information of the new player to all connected players already in the server
				sendData(loginPacket.getData(), p.getIpAddress(), p.getPort());

				// Send information of already connected players to the new player
				Pkt01Login otherPlayersLoginPacket = new Pkt01Login(p.getUsername(), p.getWorldX(), p.getWorldY(), p.getPlayerWeapIndex(), p.getPlayerState());
				sendData(otherPlayersLoginPacket.getData(), player.getIpAddress(), player.getPort());
			}
		}
		if (!isConnected) {
			getPlayers().add(player);
			if (!player.isLocal()) {
				// If not host send them the seed
				sendData(loginPacket.getData(), player.getIpAddress(), player.getPort());
				Pkt07ServerSeed seedPacket = new Pkt07ServerSeed(this.seed);
				sendData(seedPacket.getData(), player.getIpAddress(), player.getPort());
			}
		}
		return true;
	}

	private int playerIndex(String username) {
		int index = 0;
		for (PlayerMP p : getPlayers()) {
			if (p.getUsername().equals(username)) {
				return index;
			}
			index++;
		}
		return -1;
	}

	private int weapIndex(PlayerMP player, int weapId) {
		int index = 0;

		for (SuperWeapon w : player.getWeapons()) {
			if (w != null && w.getId() == weapId) {
				return index;
			}
			index++;
		}
		return -1;
	}

	private String findPlayerInPlayState() {
		for (PlayerMP p : getPlayers()) {
			if (p.getPlayerState() == Game.playState) {
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
		for (PlayerMP p : getPlayers()) {
			sendData(data, p.getIpAddress(), p.getPort());
		}
	}
	
	public synchronized List<PlayerMP> getPlayers() {
		return connectedPlayers;
	}

	public int getPlayerRemaining() {
		return playerRemaining;
	}

	public void decrementPlayerRemaining() {
		this.playerRemaining--;
	}

	
}
