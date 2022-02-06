package net;

public class Pkt08ServerTick extends Packet{
	
	private int id;
	
	public Pkt08ServerTick() {
		super(8,null);
	}
	
	public byte[] getData() {
		return ("08").getBytes();
	}
}
