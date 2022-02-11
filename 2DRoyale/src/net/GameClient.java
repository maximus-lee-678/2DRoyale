package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import javax.swing.text.Position;

import entity.PlayerMP;
import item.SuperWeapon;
import main.Game;
import structure.Crate;

public class GameClient extends Thread {

	private InetAddress ipAddress;
	private DatagramSocket socket;
	private Game game;

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
		int type = lookupPacket(data); // check if that packet type/id exist

		switch (type) {
		case 1:
			// LOGIN
			Pkt01Login loginPacket = new Pkt01Login(data);
			System.out.println("[" + address.getHostAddress() + ":" + port + "] " + loginPacket.getUsername() + " has joined the game...");
			PlayerMP player = new PlayerMP(game, loginPacket.getUsername(), loginPacket.getWorldX(), loginPacket.getWorldY(), loginPacket.getPlayerWeapIndex(), address, port);
			game.getPlayers().add(player); // add new player to playerList
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
			if (game.gameState != game.playState)
				return;
			Pkt06Shoot shootPacket = new Pkt06Shoot(data);
			if (game.player.getUsername() == shootPacket.getUsername())
				return;
			PlayerMP p = game.getPlayers().get(playerIndex(shootPacket.getUsername()));
			p.getWeapons()[weapIndex(p, shootPacket.getWeapId())].updateMPProjectiles(shootPacket.getProjAngle(), shootPacket.getWorldX(), shootPacket.getWorldY());
			break;
		case 7:
			// SERVER SEED
			Pkt07ServerSeed seedPacket = new Pkt07ServerSeed(data);
			game.randSeed = seedPacket.getServerSeed();
			game.rand = new Random(game.randSeed);
			game.gameState = game.waitState;
			game.loadDefaults();
			game.player.generatePlayerXY();
			break;
		case 9:
			// SERVER BULLET HIT
			if (game.gameState != game.playState)
				return;
			Pkt09ServerBulletHit serverHitPacket = new Pkt09ServerBulletHit(data);
			PlayerMP p2 = game.getPlayers().get(playerIndex(serverHitPacket.getUsername()));
			p2.getWeapons()[weapIndex(p2, serverHitPacket.getWeapId())].serverHit(serverHitPacket.getBullet());
			double dmg = p2.getWeapons()[weapIndex(p2, serverHitPacket.getWeapId())].damage;
			game.getPlayers().get(playerIndex(serverHitPacket.getVictim())).updatePlayerHP(-dmg);
			break;
		case 10:
			// PICK UP WEAPON
			if (game.gameState != game.playState)
				return;
			Pkt10PickupWeapon pickUpPacket = new Pkt10PickupWeapon(data);
			game.getPlayers().get(playerIndex(pickUpPacket.getUsername())).addWeapon(pickUpPacket.getPlayerWeapIndex(), pickUpPacket.getWeapType(), pickUpPacket.getWeapId());
			game.itemM.deleteWorldWeapon(pickUpPacket.getWeapId());
			break;
		case 11:
			// OPEN CRATE
			if (game.gameState != game.playState)
				return;
			Pkt11CrateOpen crateOpenPacket = new Pkt11CrateOpen(data);
			Crate crate = game.structM.deleteCrate(crateOpenPacket.getCrateIndex());
			game.itemM.spawnWeap(crate, crateOpenPacket.getWeapType(), crateOpenPacket.getWeapId());
			break;
		case 12:
			// DROP WEAPON
			if (game.gameState != game.playState)
				return;
			Pkt12DropWeapon dropPacket = new Pkt12DropWeapon(data);
			game.getPlayers().get(playerIndex(dropPacket.getUsername())).dropWeapon(dropPacket.getPlayerWeapIndex());
			game.itemM.dropWeap(dropPacket.getWeapType(), dropPacket.getWeapId(), dropPacket.getWorldX(), dropPacket.getWorldY());
			break;
		case 13:
			// CLOSE GAS
			game.tileM.closeGas();
			break;
		case 14:
			// GAME START
			game.gameState = game.playState;
			game.loadDefaults();
			game.player.generatePlayerXY();
			game.player.setPlayerDefault();
			game.player.freeze = true;
			updateAllPlayerState(game.playState);
			break;
		case 15:
			// COUNTDOWN SEQUENCE
			Pkt15CountdownSeq countDownPacket = new Pkt15CountdownSeq(data);
			game.ui.countdown = countDownPacket.getCountDown();
			if (countDownPacket.getCountDown() > 0) {
				System.out.println("Game Starting in " + countDownPacket.getCountDown());
			}
			else {
				System.out.println("GO");
				game.player.freeze = false;
			}
			break;
		case 16:
			// DEATH
			Pkt16Death deathPacket = new Pkt16Death(data);
			game.ui.position = deathPacket.getRemainingPlayers();
			game.getPlayers().get(playerIndex(deathPacket.getVictim())).playerState = game.endState;
			if (deathPacket.getUsername().equals(game.player.getUsername())) {
				game.ui.kills++;
			}
			if (deathPacket.getVictim().equals(game.player.getUsername())) {
				game.gameState = game.endState;
				game.ui.win = false;
			}
			break;
		case 17:
			// PLAYER BACK TO LOBBY
			Pkt17BackToLobby backToLobbyPacket = new Pkt17BackToLobby(data);
			game.getPlayers().get(playerIndex(backToLobbyPacket.getUsername())).playerState = game.waitState;
			if (backToLobbyPacket.getUsername().equals(game.player.getUsername())) {
				game.gameState = game.waitState;
				game.rand = new Random(game.randSeed);
				game.loadDefaults();
				game.player.generatePlayerXY();
				game.player.setPlayerDefault();
				game.ui.kills = 0;
			}
			break;
		case 18:
			// WINNER + GAME END
			Pkt18Winner winnerPacket = new Pkt18Winner(data);
			if (winnerPacket.getUsername().equals(game.player.getUsername())) {
				game.ui.position = 1;
				game.ui.win = true;
				game.gameState = game.endState;
				game.player.playerState = game.endState;
			}
		default:
		case 0:
		case 8:
			break;
		}
	}

	private void updateAllPlayerState(int state) {
		for (PlayerMP p : game.getPlayers()) {
			p.playerState = state;
		}
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
}
