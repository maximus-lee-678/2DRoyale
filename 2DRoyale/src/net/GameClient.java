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
			dataArr = msgData.split(",");
			System.out.println(
					"[" + address.getHostAddress() + ":" + port + "] " + dataArr[0] + " has joined the game...");
			PlayerMP player = new PlayerMP(game, dataArr[0], address, port);
			player.worldX = Integer.parseInt(dataArr[1]);
			player.worldY = Integer.parseInt(dataArr[2]);
			game.getPlayers().add(player); // add new player to playerList
			break;
		case 2:
			// DISCONNECT
			System.out.println("[" + address.getHostAddress() + ":" + port + "] " + msgData + " has left the game...");
			game.getPlayers().remove(playerIndex(msgData)); // delete new player to playerList. Got index to delete from
															// playerIndex function
			break;
		case 3:
			// MOVEMENT
			dataArr = msgData.split(","); // Eg: Bob,1000,800 -> arr[0]username: Bob, arr[1]x: 1000, arr[2]y: 800
			int x = Integer.parseInt(dataArr[1]); // x axis
			int y = Integer.parseInt(dataArr[2]); // y axis
			game.getPlayers().get(playerIndex(dataArr[0])).updatePlayerXY(x, y); // get player and call move() to update
																					// player world coordinates
			break;
		case 4:
			// MOUSEMOVE
			dataArr = msgData.split(","); // Eg: Bob,1000,800 -> arr[0]username: Bob, arr[1]x: 1000, arr[2]y: 800
			double mouseX = Double.parseDouble(dataArr[1]); // x axis
			double mouseY = Double.parseDouble(dataArr[2]); // y axis
			game.getPlayers().get(playerIndex(dataArr[0])).updateMouseDirection(mouseX, mouseY); // get player and call
																									// move() to update
																									// player world
																									// coordinates
			break;
		case 5:
			// MOUSESCROLL
			dataArr = msgData.split(",");
			int mouseScrollDir = Integer.parseInt(dataArr[1]);
			game.getPlayers().get(playerIndex(dataArr[0])).playerMouseScroll(mouseScrollDir);
			break;
		case 6:
			// SHOOTING
			dataArr = msgData.split(",");
			String weapon = dataArr[1];
			double projAngle = Double.parseDouble(dataArr[2]);
			int worldX = Integer.parseInt(dataArr[3]);
			int worldY = Integer.parseInt(dataArr[4]);
			PlayerMP p = game.getPlayers().get(playerIndex(dataArr[0]));
			p.playerWeap.get(weapIndex(p, weapon)).updateMPProjectiles(projAngle, worldX, worldY);
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
