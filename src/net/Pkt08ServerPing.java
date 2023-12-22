package net;

public class Pkt08ServerPing extends Packet {

	public Pkt08ServerPing() {
		super(8, null);
	}

	public byte[] getData() {
		return ("08").getBytes();
	}
}
