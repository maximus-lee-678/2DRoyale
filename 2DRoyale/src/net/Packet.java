package net;

import main.Game;

public class Packet {

	private int id;
	private String username;
	private String data;
	
	public Packet(int id, String username){
		this.id = id;
		this.username = username;
		this.data = "";
	}
	
	public Packet(int id, String username, int x, int y){
		this.id = id;
		this.username = username;
		this.data = "," + x + "," + y;
	}
	
	public Packet(int id, String username, double x, double y){
		this.id = id;
		this.username = username;
		this.data = "," + x + "," + y;
	}
	
	public byte[] getPacket() {
		String idStr = String.format("%02d", id);
		return (idStr + username + data).getBytes();
	}
	
	
}
