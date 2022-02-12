package net;

public class Pkt20GasDamage extends Packet{
	
	public Pkt20GasDamage(String username) {
		super(20, username);
	}

	public Pkt20GasDamage(byte[] data) {
		super(20);
		String message = new String(data).trim().substring(2);
		this.username = message;
	}

	public byte[] getData() {
		return ("20"+getUsername()).getBytes();
	}

}
