package net;

public class Pkt18Winner extends Packet {

	public Pkt18Winner(String username) {
		super(18, username);
	}

	public Pkt18Winner(byte[] data) {
		super(18);
		String message = new String(data).trim().substring(2);
		this.username = message;
	}
	
	@Override
	public byte[] getData() {
		return ("18"+getUsername()).getBytes();
	}	

}
