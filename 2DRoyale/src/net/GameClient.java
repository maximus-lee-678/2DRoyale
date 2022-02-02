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
		String message = new String(data).trim(); // first 2 values of the String is the packet type/id
		int type = lookupPacket(message.substring(0, 2)); // check if that packet type/id exist
		String msgData = message.substring(2); // snip away first two values (aka the id)
		String[] dataArr;

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
			Pkt06Shoot shootPacket = new Pkt06Shoot(data);
			PlayerMP p = game.getPlayers().get(playerIndex(shootPacket.getUsername()));
			p.playerWeap.get(weapIndex(p, shootPacket.getWeapon())).updateMPProjectiles(shootPacket.getProjAngle(), shootPacket.getWorldX(), shootPacket.getWorldY());
			break;
		default:
		case 0:
			break;
		}

	}

	private int weapIndex(PlayerMP player, String name) {
		int index = 0;

		for (SuperWeapon w : player.playerWeap) {
			if (w.name.equals(name)) {
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

	private int lookupPacket(String message) { // check if packet exist
		int packetType;

		try {
			packetType = Integer.parseInt(message);
		} catch (NumberFormatException e) {
			packetType = 0;
		}

		return packetType;
	}

	public void sendData(byte[] data) { // send data from client to server
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, 2207); // 2207 is a random port number
																						// i thought of
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
