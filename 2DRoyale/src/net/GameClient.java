package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import entity.PlayerMP;
import item.SuperWeapon;
import main.Game;
import structure.Crate;

public class GameClient extends Thread {

	private InetAddress ipAddress;
	private DatagramSocket socket;
	private Game game;
	private long latency;

	public GameClient(Game game, String ipAddress) {
		this.game = game;
		try {
			this.socket = new DatagramSocket(); // create socket
			this.ipAddress = InetAddress.getByName(ipAddress);
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		// Listening for new packets
		while (true) {
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Handle packet
			readPacket(packet.getData(), packet.getAddress(), packet.getPort());
		}
	}

	private void readPacket(byte[] data, InetAddress address, int port) {
		int type = lookupPacket(data); // check if that packet type/id exist

		switch (type) {
		case 1:
			// LOGIN
			Pkt01Login loginPacket = new Pkt01Login(data);
			System.out.println("[" + address.getHostAddress() + ":" + port + "] " + loginPacket.getUsername() + " has joined the game...");
			PlayerMP player = new PlayerMP(game, loginPacket.getUsername(), loginPacket.getWorldX(), loginPacket.getWorldY(), loginPacket.getPlayerWeapIndex(), address, port);
			handleLogin(loginPacket, player);
			break;
		case 2:
			// DISCONNECT
			Pkt02Disconnect disconnectPacket = new Pkt02Disconnect(data);
			System.out.println("[" + address.getHostAddress() + ":" + port + "] " + disconnectPacket.getUsername() + " has left the game...");
			game.getPlayers().remove(playerIndex(disconnectPacket.getUsername()));
			break;
		case 3:
			// MOVEMENT
			Pkt03Move movePacket = new Pkt03Move(data);
			game.getPlayers().get(playerIndex(movePacket.getUsername())).updatePlayerXY(movePacket.getWorldX(), movePacket.getWorldY());
			break;
		case 4:
			// MOUSEMOVE
			Pkt04MouseMove mouseMovePacket = new Pkt04MouseMove(data);
			game.getPlayers().get(playerIndex(mouseMovePacket.getUsername())).updateMouseDirection(mouseMovePacket.getMouseX(), mouseMovePacket.getMouseY());
			break;
		case 5:
			// MOUSESCROLL
			Pkt05MouseScroll mouseScrollPacket = new Pkt05MouseScroll(data);
			game.getPlayers().get(playerIndex(mouseScrollPacket.getUsername())).playerMouseScroll(mouseScrollPacket.getMouseScrollDir());
			break;
		case 6:
			// SHOOTING
			Pkt06Shoot shootPacket = new Pkt06Shoot(data);
			if (game.gameState == game.playState)
				handleShooting(shootPacket);
			break;
		case 7:
			// SERVER SEED
			Pkt07ServerSeed seedPacket = new Pkt07ServerSeed(data);
			handleSeed(seedPacket);
			break;
		case 8:
			System.out.println("Ping: " + (System.currentTimeMillis() - latency) + "ms");
			break;
		case 9:
			// SERVER BULLET HIT
			Pkt09ServerBulletHit serverHitPacket = new Pkt09ServerBulletHit(data);
			if (game.gameState == game.playState)
				handleBulletHit(serverHitPacket);
			break;
		case 10:
			// PICK UP WEAPON
			Pkt10PickupWeapon pickUpPacket = new Pkt10PickupWeapon(data);
			if (game.gameState == game.playState)
				handleWeapPickUp(pickUpPacket);
			break;
		case 11:
			// OPEN CRATE
			Pkt11CrateOpen crateOpenPacket = new Pkt11CrateOpen(data);
			if (game.gameState == game.playState)
				handleCrateOpen(crateOpenPacket);
			break;
		case 12:
			// DROP WEAPON
			Pkt12DropWeapon dropPacket = new Pkt12DropWeapon(data);
			if (game.gameState == game.playState)
				handleWeapDrop(dropPacket);
			break;
		case 13:
			// CLOSE GAS
			game.tileM.closeGas();
			break;
		case 14:
			// GAME START
			Pkt14StartGame startGamePacket = new Pkt14StartGame(data);
			handleGameStart(startGamePacket);
			break;
		case 15:
			// COUNTDOWN SEQUENCE
			Pkt15CountdownSeq countDownPacket = new Pkt15CountdownSeq(data);
			handleCountDown(countDownPacket);
			break;
		case 16:
			// DEATH
			Pkt16Death deathPacket = new Pkt16Death(data);
			handleDeath(deathPacket);
			break;
		case 17:
			// PLAYER BACK TO LOBBY
			Pkt17BackToLobby backToLobbyPacket = new Pkt17BackToLobby(data);
			handleBackToLobby(backToLobbyPacket);
			break;
		case 18:
			// WINNER + GAME END
			Pkt18Winner winnerPacket = new Pkt18Winner(data);
			handleWinning(winnerPacket);
			break;
		case 19:
			// SERVER SHUTDOWN
			Pkt19ServerKick kickPacket = new Pkt19ServerKick(data);
			System.out.println("Server Closed!");
			handleServerShutdown(kickPacket);
			break;
		case 20:
			// GAS DAMAGE
			Pkt20GasDamage gasDmgPacket = new Pkt20GasDamage(data);
			if (game.gameState == game.playState)
				game.getPlayers().get(playerIndex(gasDmgPacket.getUsername())).updatePlayerHP(-1);
			break;
		default:
		case 0:
			break;
		}
	}

	private void handleServerShutdown(Pkt19ServerKick kickPacket) {
		// If player is host delete server
		if (kickPacket.getIsHost())
			game.socketServer = null;
		// Move to title screen and clear player array
		game.gameState = game.titleState;
		game.player.setPlayerState(game.titleState);
		game.ui.titleScreenState = 0;
		game.ui.commandNum = 0;
		game.clearPlayers();
	}

	private void handleWinning(Pkt18Winner winnerPacket) {
		game.ui.addMessage(winnerPacket.getUsername() + " won!");
		// If player is winner, end the game
		if (winnerPacket.getUsername().equals(game.player.getUsername())) {
			game.ui.playingPlayerCount = 1;
			game.ui.win = true;
			game.gameState = game.endState;
			game.player.setPlayerState(game.endState);
		}
	}

	private void handleBackToLobby(Pkt17BackToLobby backToLobbyPacket) {
		// Update players that went back to lobby
		game.getPlayers().get(playerIndex(backToLobbyPacket.getUsername())).setPlayerDefault();
		// If player is you, clear the number of kills
		if (backToLobbyPacket.getUsername().equals(game.player.getUsername()))
			game.ui.kills = 0;
	}

	private void handleDeath(Pkt16Death deathPacket) {
		game.ui.addMessage(deathPacket.getUsername() + " killed " + deathPacket.getVictim());
		game.getPlayers().get(playerIndex(deathPacket.getVictim())).setPlayerState(game.endState);
		// If player is the shooter, increment kills
		if (deathPacket.getUsername().equals(game.player.getUsername()))
			game.ui.kills++;
		// If player is victim, end game
		if (deathPacket.getVictim().equals(game.player.getUsername())) {
			game.gameState = game.endState;
			game.ui.win = false;
		}
	}

	private void handleCountDown(Pkt15CountdownSeq countDownPacket) {
		game.ui.countdown = countDownPacket.getCountDown();
		// Show countdown on screen when game starts
		if (countDownPacket.getCountDown() > 0)
			System.out.println("Game Starting in " + countDownPacket.getCountDown());
		else {
			System.out.println("GO");
			game.player.setFreeze(false);
			System.out.println(1);
		}
	}
	

	private void handleGameStart(Pkt14StartGame startGamePacket) {
		// Check who entered the new game, if it's player, refresh the map to the new one
		if (startGamePacket.getUsername().equals(game.player.getUsername())) {
			game.gameState = game.playState;
			game.loadDefaults();
			game.player.generatePlayerXY();
			game.player.setPlayerDefault();
			game.player.setFreeze(true);
			game.player.setPlayerState(game.playState);
		} else
			game.getPlayers().get(playerIndex(startGamePacket.getUsername())).setPlayerState(game.playState);
	}

	private void handleWeapDrop(Pkt12DropWeapon dropPacket) {
		game.getPlayers().get(playerIndex(dropPacket.getUsername())).dropWeapon(dropPacket.getPlayerWeapIndex());
		game.itemM.dropWeap(dropPacket.getWeapType(), dropPacket.getWeapId(), dropPacket.getWorldX(), dropPacket.getWorldY());
	}

	private void handleCrateOpen(Pkt11CrateOpen crateOpenPacket) {
		Crate crate = game.structM.deleteCrate(crateOpenPacket.getCrateIndex());
		game.itemM.spawnWeap(crate, crateOpenPacket.getWeapType(), crateOpenPacket.getWeapId());
	}

	private void handleWeapPickUp(Pkt10PickupWeapon pickUpPacket) {
		game.getPlayers().get(playerIndex(pickUpPacket.getUsername())).addWeapon(pickUpPacket.getPlayerWeapIndex(), pickUpPacket.getWeapType(), pickUpPacket.getWeapId());
		game.itemM.deleteWorldWeapon(pickUpPacket.getWeapId());
	}

	private void handleBulletHit(Pkt09ServerBulletHit serverHitPacket) {
		PlayerMP p2 = game.getPlayers().get(playerIndex(serverHitPacket.getUsername()));
		p2.getWeapons()[weapIndex(p2, serverHitPacket.getWeapId())].serverHit(serverHitPacket.getBullet());
		double dmg = p2.getWeapons()[weapIndex(p2, serverHitPacket.getWeapId())].getDamage();
		// Apply damage to victim
		game.getPlayers().get(playerIndex(serverHitPacket.getVictim())).updatePlayerHP(-dmg);
	}

	private void handleSeed(Pkt07ServerSeed seedPacket) {
		// After joining server, server will send the seed for player to generate the world
		game.randSeed = seedPacket.getServerSeed();
		game.rand = new Random(game.randSeed);
		game.gameState = game.waitState;
		game.player.setPlayerState(game.waitState);
		game.loadDefaults();
		game.player.generatePlayerXY();
	}

	private void handleShooting(Pkt06Shoot shootPacket) {
		if (game.player.getUsername() == shootPacket.getUsername())
			return;
		PlayerMP p = game.getPlayers().get(playerIndex(shootPacket.getUsername()));
		p.getWeapons()[weapIndex(p, shootPacket.getWeapId())].updateMPProjectiles(shootPacket.getProjAngle(), shootPacket.getWorldX(), shootPacket.getWorldY());
	}

	private void handleLogin(Pkt01Login loginPacket, PlayerMP player) {
		player.setPlayerState(loginPacket.getPlayerState());
		if (loginPacket.getUsername().equals(game.player.getUsername()))
			game.getPlayers().add(0, game.player);
		else
			game.getPlayers().add(player); // add new player to playerList
	}

	private int weapIndex(PlayerMP player, int weapId) {
		int index = 0;

		for (SuperWeapon w : player.getWeapons()) {
			if (w != null && w.getId() == weapId) {
				break;
			}
			index++;
		}
		return index;
	}

	private int playerIndex(String msgData) { // match player username to get index
		int index = 0;

		for (PlayerMP p : game.getPlayers()) {
			if (p.getUsername().equals(msgData)) {
				break;
			}
			index++;
		}
		return index;
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

	public void sendData(byte[] data) { // send data from client to server
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 2207);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setLatency(long latency) {
		this.latency = latency;
	}
}
