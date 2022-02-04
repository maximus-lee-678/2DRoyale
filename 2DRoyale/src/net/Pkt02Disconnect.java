package net;

public class Pkt02Disconnect extends Packet {
	
	public Pkt02Disconnect(String username) {
		super(2, username);
	}

	public Pkt02Disconnect(byte[] data) {
		super(2);
		String message = new String(data).trim().substring(2);
		this.username = message;
	}
	
	@Override
	public byte[] getData() {
		return ("02"+getUsername()).getBytes();
	}
}
