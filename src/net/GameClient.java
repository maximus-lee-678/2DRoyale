package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

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
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			System.out.println(e);
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
			if (game.getGameState() == Game.playState)
				handleShooting(shootPacket);
			break;
		case 7:
			// SERVER SEED
			Pkt07ServerSeed seedPacket = new Pkt07ServerSeed(data);
			handleSeed(seedPacket);
			break;
		case 8:
			// SERVER PING
			game.ui.setPing(System.currentTimeMillis() - latency);
			break;
		case 9:
			// SERVER BULLET HIT
			Pkt09ServerBulletHit serverHitPacket = new Pkt09ServerBulletHit(data);
			if (game.getGameState() == Game.playState)
				handleBulletHit(serverHitPacket);
			break;
		case 10:
			// PICK UP WEAPON
			Pkt10PickupWeapon pickUpPacket = new Pkt10PickupWeapon(data);
			if (game.getGameState() == Game.playState)
				handleWeapPickUp(pickUpPacket);
			break;
		case 11:
			// OPEN CRATE
			Pkt11CrateOpen crateOpenPacket = new Pkt11CrateOpen(data);
			if (game.getGameState() == Game.playState)
				handleCrateOpen(crateOpenPacket);
			break;
		case 12:
			// DROP WEAPON
			Pkt12DropWeapon dropPacket = new Pkt12DropWeapon(data);
			if (game.getGameState() == Game.playState)
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
			if (game.getGameState() == Game.playState)
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
		game.setGameState(Game.titleState);
		game.player.setPlayerState(Game.titleState);
		game.ui.setTitleScreenState(0);
		game.keys.setUserSelect(0);
		game.clearPlayers();
	}

	private void handleWinning(Pkt18Winner winnerPacket) {
		game.ui.addMessage(winnerPacket.getUsername() + " won!");
		// If player is winner, end the game
		if (winnerPacket.getUsername().equals(game.player.getUsername())) {
			game.ui.setPlayingPlayerCount(1);
			game.ui.setWin(true);
			game.setGameState(Game.endState);
			game.player.setPlayerState(Game.endState);
			game.soundHandler.playSound(10);
		}
	}

	private void handleBackToLobby(Pkt17BackToLobby backToLobbyPacket) {
		// Update players that went back to lobby
		game.getPlayers().get(playerIndex(backToLobbyPacket.getUsername())).setPlayerDefault();
		// If player is you, clear the number of kills
		if (backToLobbyPacket.getUsername().equals(game.player.getUsername()))
			game.ui.setKills(0);
	}

	private void handleDeath(Pkt16Death deathPacket) {
		game.ui.addMessage(deathPacket.getUsername() + " killed " + deathPacket.getVictim());
		game.getPlayers().get(playerIndex(deathPacket.getVictim())).setPlayerState(Game.endState);
		// If player is the shooter, increment kills
		if (deathPacket.getUsername().equals(game.player.getUsername()))
			game.ui.incrementKills();
		// If player is victim, end game
		if (deathPacket.getVictim().equals(game.player.getUsername())) {
			game.setGameState(Game.endState);
			game.ui.setWin(false);
			game.soundHandler.playSound(11);
		}
	}

	private void handleCountDown(Pkt15CountdownSeq countDownPacket) {
		game.ui.setCountdown(countDownPacket.getCountDown());
		game.soundHandler.playSound(6);
		// Show countdown on screen when game starts
		if (countDownPacket.getCountDown() > 0)
			System.out.println("Game Starting in " + countDownPacket.getCountDown());
		else {
			System.out.println("GO");
			game.player.setFreeze(false);
		}
	}
	

	private void handleGameStart(Pkt14StartGame startGamePacket) {
		// Check who entered the new game, if it's player, refresh the map to the new one
		if (startGamePacket.getUsername().equals(game.player.getUsername())) {
			game.setGameState(Game.playState);
			game.loadDefaults();
			game.player.generatePlayerXY();
			game.player.setPlayerDefault();
			game.player.setFreeze(true);
			game.player.setPlayerState(Game.playState);
		} else
			game.getPlayers().get(playerIndex(startGamePacket.getUsername())).setPlayerState(Game.playState);
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
		game.setRandSeed(seedPacket.getServerSeed());
		game.setGameState(Game.waitState);
		game.player.setPlayerState(Game.waitState);
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
			System.out.print(e + ": Please go back and enter a valid ip address!");
		} catch (IllegalArgumentException e) {
			System.out.print(e + ": Please go back and enter a valid ip address!");
		}
	}
	
	public void setLatency(long latency) {
		this.latency = latency;
	}
}
