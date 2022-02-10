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
		while (true) { // listen for new packets
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
//			if(gameState != waitState) return;
			PlayerMP player = new PlayerMP(game, loginPacket.getUsername(), loginPacket.getWorldX(), loginPacket.getWorldY(), loginPacket.getPlayerWeapIndex(), address, port);
			System.out.println("Server: [" + address.getHostAddress() + ":" + port + "] " + loginPacket.getUsername() + " has connected...");
			addConnection(player, loginPacket);
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
			Pkt06Shoot shootPacket = new Pkt06Shoot(data);
			handleShoot(shootPacket);
			break;
		case 8:
			// SERVER TICK
			update();
			break;
		case 10:
			// WEAPON PICK UP
			Pkt10PickupWeapon pickUpPacket = new Pkt10PickupWeapon(data);
			handlePickUpWeapon(pickUpPacket);
			break;
		case 11:
			// OPEN CRATE
			Pkt11CrateOpen crateOpenPacket = new Pkt11CrateOpen(data);
			handleCrateOpen(crateOpenPacket);
			break;
		case 12:
			// WEAPON DROP
			Pkt12DropWeapon dropPacket = new Pkt12DropWeapon(data);
			handleDropWeapon(dropPacket);
			break;
		case 14:
			//START GAME
			handleStartGame();
			break;
		case 17:
			// PLAYER BACK TO LOBBY
			Pkt17BackToLobby backToLobbyPacket = new Pkt17BackToLobby(data);
			handleBackToLobby(backToLobbyPacket);
			break;
		default:
		case 0:
			break;
		}
	}

	private void handleBackToLobby(Pkt17BackToLobby backToLobbyPacket) {
		connectedPlayers.get(playerIndex(backToLobbyPacket.getUsername())).playerState = waitState;
		backToLobbyPacket.sendData(this);
	}

	private void handleDropWeapon(Pkt12DropWeapon dropPacket) {
		connectedPlayers.get(playerIndex(dropPacket.getUsername())).dropWeapon(dropPacket.getPlayerWeapIndex());
		dropPacket.sendData(this);
	}

	private void handleStartGame() {
		if(gameState == playState) return;
		if(findPlayerInPlayState() != null) return;
		gameState = playState;
		playerRemaining = connectedPlayers.size();
		updateAllPlayerState(game.playState);
		Pkt14StartGame startGamePacket = new Pkt14StartGame();
		startGamePacket.sendData(this);
		countDownSeq = 5;
	}

	private void handleCrateOpen(Pkt11CrateOpen crateOpenPacket) {
		Random r = new Random();
		crateOpenPacket.setWeapType(r.nextInt(game.itemM.weaponsArr.length));
		crateOpenPacket.setWeapId(weaponIdCount++);
		crateOpenPacket.sendData(this);
	}

	private void handlePickUpWeapon(Pkt10PickupWeapon pickUpPacket) {
		connectedPlayers.get(playerIndex(pickUpPacket.getUsername())).addWeapon(pickUpPacket.getPlayerWeapIndex(), pickUpPacket.getWeapType(), pickUpPacket.getWeapId());
		pickUpPacket.sendData(this);
	}

	private void update() {
		gameTicks++;
		
		if(countDownSeq >= 0 && gameTicks % 60 == 0) {
			Pkt15CountdownSeq countDownPacket = new Pkt15CountdownSeq(countDownSeq);
			countDownPacket.sendData(this);
			countDownSeq--;
		}
		if (gameState == playState && gameTicks % 60 == 0) {	//gas speed
			handleCloseGas();			
		}
		//Check if no remaining players
		if(gameState == playState && playerRemaining == 1) {
			String lastPlayer = findPlayerInPlayState();
			Pkt18Winner winnerPacket = new Pkt18Winner(lastPlayer);
			winnerPacket.sendData(this);
		}
		//Check for bullet hit
		for (PlayerMP p : connectedPlayers) {
			for (SuperWeapon weap : p.getWeapons()) {
				if (weap != null) {
					weap.checkPlayerHit(this);
					weap.update();
				}
			}
		}
	}

	private void handleCloseGas() {
		for (PlayerMP p : connectedPlayers) {
			if (p.playerState != playState)
				continue;
			Pkt13Gas gasPacket = new Pkt13Gas();
			sendData(gasPacket.getData(), p.ipAddress, p.port);
		}		
	}

	private void handleShoot(Pkt06Shoot shootPacket) {
		PlayerMP p = connectedPlayers.get(playerIndex(shootPacket.getUsername()));
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

	private void removeConnection(Pkt02Disconnect disconnectPacket) {
		connectedPlayers.remove(playerIndex(disconnectPacket.getUsername()));
		disconnectPacket.sendData(this);
	}

	public void addConnection(PlayerMP player, Pkt01Login loginPacket) {
		boolean isConnected = false;
		for (PlayerMP p : connectedPlayers) {
			if (player.getUsername().equalsIgnoreCase(p.getUsername())) {
				if (p.ipAddress == null) {
					p.ipAddress = player.ipAddress;
				}
				if (p.port == -1) {
					p.port = player.port;
				}
				isConnected = true;
			} else {
				sendData(loginPacket.getData(), p.ipAddress, p.port);

				Pkt01Login otherPlayersLoginPacket = new Pkt01Login(p.getUsername(), p.worldX, p.worldY, p.playerWeapIndex, p.playerState);
				sendData(otherPlayersLoginPacket.getData(), player.ipAddress, player.port);
			}
		}
		if (!isConnected) {
			connectedPlayers.add(player);
			if (!player.isLocal) {
				Pkt07ServerSeed seedPacket = new Pkt07ServerSeed(this.seed);
				sendData(seedPacket.getData(), player.ipAddress, player.port);
			}
		}
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
				break;
			}
			index++;
		}
		return index;
	}
	
	private void updateAllPlayerState(int state) {
		for (PlayerMP p : connectedPlayers) {
			p.playerState = state;
		}
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
