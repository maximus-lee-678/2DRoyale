package net;

public class Pkt14StartGame extends Packet{
	
	public Pkt14StartGame() {
		super(14,null);
	}
	
	public byte[] getData() {
		return ("14").getBytes();
	}
}
