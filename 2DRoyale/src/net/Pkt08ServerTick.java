package net;

public class Pkt08ServerTick extends Packet{
	
	public Pkt08ServerTick() {
		super(8,null);
	}
	
	public byte[] getData() {
		return ("08").getBytes();
	}
}
