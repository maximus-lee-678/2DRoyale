package net;

public class Pkt13Gas extends Packet {

	public Pkt13Gas() {
		super(13, null);
	}

	public byte[] getData() {
		return ("13").getBytes();
	}
}
