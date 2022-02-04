package net;

public abstract class Packet {

	public int id;
	public String username;
	
	public Packet(int id) {
		this.id = id;
	}

	public Packet(int id, String username) {
		this.id = id;
		this.username = username;
	}

	public void sendData(GameClient client) {
		client.sendData(getData());
	}

	public void sendData(GameServer server) {
		server.sendDataToAllClients(getData());
	}

	public String getUsername() {
		return username;
	}

	public abstract byte[] getData();

}
