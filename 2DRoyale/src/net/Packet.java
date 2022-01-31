package net;

public class Packet {

	private int id;
	private String username;
	private String data;
	
	public Packet(int id, String username){
		this.id = id;
		this.username = username;
		this.data = "";
	}
	
	public Packet(int id, String username, int data){
		this.id = id;
		this.username = username;
		this.data = "," + data;
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
	
	public Packet(int id, String username, String weapon, double projAngle, int worldX, int worldY) {
		this.id = id;
		this.username = username;
		this.data = "," + weapon + "," + projAngle + "," + worldX + "," + worldY;
	}
	
	public byte[] getPacket() {
		String idStr = String.format("%02d", id);
		return (idStr + username + data).getBytes();
		
	}
	
	
}
