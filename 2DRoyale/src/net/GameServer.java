package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import entity.PlayerMP;
import main.Game;

public class GameServer extends Thread {

	private DatagramSocket socket;
	private Game game;
	private List<PlayerMP> connectedPlayers = new ArrayList<PlayerMP>();

	public GameServer(Game game) {
		this.game = game;
		try {
			this.socket = new DatagramSocket(2207);					//create socket, open on port 2207. To send data to server, all clients must send to port 2207
		} catch (SocketException e) {
			e.printStackTrace();
		}

	}

	public void run() {
		while (true) {												//listen for new packets
			byte[] data = new byte[1024];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			readPacket(packet.getData(), packet.getAddress(), packet.getPort());	//call readPacket() to handle packet
		}
	}

	private void readPacket(byte[] data, InetAddress address, int port) {
		String message = new String(data).trim();			//first 2 values of the String is the packet type/id
		int type = lookupPacket(message.substring(0, 2));	//check if that packet type/id exist
		String msgData = message.substring(2);				//snip away first two values (aka the id)
		String[] dataArr;
		
		switch (type) {
		case 1:
			// LOGIN
			PlayerMP player = new PlayerMP(game, msgData, address, port);
			System.out.println("Server: [" + address.getHostAddress() + ":" + port + "] " + msgData + " has connected...");
			addConnection(player);							//call addConnection()
			break;
		case 2:
			// DISCONNECT
			System.out.println("Server: [" + address.getHostAddress() + ":" + port + "] " + msgData + " has left the game...");
			removeConnection(msgData);						//call removeConnection()
			break;
		case 3:
			// MOVEMENT
			dataArr = msgData.split(",");			//Eg: Bob,1000,800 -> arr[0]username: Bob, arr[1]x: 1000, arr[2]y: 800
			handleMove(dataArr);							//call handleMove()
			break;
		case 4:
			// MOUSE
			dataArr = msgData.split(",");			//Eg: Bob,1000,800 -> arr[0]username: Bob, arr[1]x: 1000, arr[2]y: 800
			handleMouse(dataArr);							//call handleMove()
			break;
		default:
		case 0:
			break;
		}
	}
	
	private void handleMouse(String[] dataArr) {				
		int index = 0;
		for (PlayerMP p : connectedPlayers) {				//find player by username
			if (dataArr[0].equals(p.getUsername())) {
				break;
			}
			index++;
		}
		
		Packet mousePacket = new Packet(4, dataArr[0], Double.parseDouble(dataArr[1]), Double.parseDouble(dataArr[2]));			//Create a move packet [\net\Packet] to send to all clients
		sendDataToAllClients(mousePacket.getPacket());						//Send the move packet of the player to everyone (Server -> All clients). GameClient will handle this packet
	}

	private void handleMove(String[] dataArr) {				
		int index = 0;
		for (PlayerMP p : connectedPlayers) {				//find player by username
			if (dataArr[0].equals(p.getUsername())) {
				break;
			}
			index++;
		}
		connectedPlayers.get(index).worldX = Integer.parseInt(dataArr[1]);	//update player coordinates on server's playerlist, currently got no purpose
		connectedPlayers.get(index).worldY = Integer.parseInt(dataArr[2]);
		Packet movePacket = new Packet(3, dataArr[0], Integer.parseInt(dataArr[1]), Integer.parseInt(dataArr[2]));			//Create a move packet [\net\Packet] to send to all clients
		sendDataToAllClients(movePacket.getPacket());						//Send the move packet of the player to everyone (Server -> All clients). GameClient will handle this packet
	}

	private void removeConnection(String msgData) {
		int index = 0;
		for (PlayerMP p : connectedPlayers) {				//find player by username
			if (msgData.equals(p.getUsername())) {
				break;
			}
			index++;
		}

		connectedPlayers.remove(index);								//remove player from server playerList
		Packet disconnectPacket = new Packet(2, msgData);			//Create a move packet [\net\Packet] to send to all clients
		sendDataToAllClients(disconnectPacket.getPacket());			//Send the disconnect packet of the player to everyone (Server -> All clients). GameClient will handle this packet
	}

	public void addConnection(PlayerMP player) {					
		boolean isConnected = false;
		for (PlayerMP p : connectedPlayers) {								//Loop thru each player in server playerList array
			if (player.getUsername().equalsIgnoreCase(p.getUsername())) {	//If the player is the user, update the user's ip and port
				if (p.ipAddress == null) {									//Remember line 56 in Game.java? we set it to null there and push into server player list array. so now we update
					p.ipAddress = player.ipAddress;
				}
				if (p.port == -1) {
					p.port = player.port;
				}
				isConnected = true;
			} else {																			//If player is not user,
				Packet userInfoPacket = new Packet(1, player.getUsername());	
				sendData(userInfoPacket.getPacket(), p.ipAddress, p.port);						//Tell (other players) that there's a new player that just logged in
				
				Packet otherPlayersInfoPacket = new Packet(1, p.getUsername());	
				sendData(otherPlayersInfoPacket.getPacket(), player.ipAddress, player.port);	//Tell (the new player) that other players exist
			}
		}
		if (!isConnected) {
			connectedPlayers.add(player);					//Add player to server player list array
		}
	}

	private int lookupPacket(String message) {				//match player username to get index
		int packetType;

		try {
			packetType = Integer.parseInt(message);
		} catch (NumberFormatException e) {
			packetType = 0;
		}

		return packetType;
	}

	public void sendData(byte[] data, InetAddress ipAddress, int port) {		//send data from server to client
		DatagramPacket packet = new DatagramPacket(data, data.length, ipAddress, port);
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendDataToAllClients(byte[] data) {								//send data from server to all clients
		for (PlayerMP p : connectedPlayers) {
			sendData(data, p.ipAddress, p.port);
		}
	}

}
