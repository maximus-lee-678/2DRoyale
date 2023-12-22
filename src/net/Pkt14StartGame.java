package net;

public class Pkt14StartGame extends Packet {

	public Pkt14StartGame(String username) {
		super(14, username);
	}

	public Pkt14StartGame(byte[] data) {
		super(14);
		String message = new String(data).trim().substring(2);
		this.username = message;
	}

	public byte[] getData() {
		return ("14" + getUsername()).getBytes();
	}

}
