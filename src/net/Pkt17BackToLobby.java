package net;

public class Pkt17BackToLobby extends Packet {

	public Pkt17BackToLobby(String username) {
		super(17, username);
	}

	public Pkt17BackToLobby(byte[] data) {
		super(17);
		String message = new String(data).trim().substring(2);
		this.username = message;
	}

	@Override
	public byte[] getData() {
		return ("17" + getUsername()).getBytes();
	}

}
