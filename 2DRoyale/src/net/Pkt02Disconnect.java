package net;

public class Pkt02Disconnect implements Packet {

	public int id = 2;
	private String username;

	public Pkt02Disconnect(String username) {
		this.username = username;
	}

	public Pkt02Disconnect(byte[] data) {
		String message = new String(data).trim().substring(2);
		this.username = message;
	}
	
	@Override
	public void sendData(GameClient client) {
		client.sendData(getData());
	}

	@Override
	public void sendData(GameServer server) {
		server.sendDataToAllClients(getData());
	}

	@Override
	public byte[] getData() {
		return ("02"+getUsername()).getBytes();
	}
	
	public String getUsername() {
		return username;
	}

}
