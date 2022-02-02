package net;

public class Pkt03Move implements Packet {

	public int id = 3;
	private String username;
	
	private int worldX;
	private int worldY;

	public Pkt03Move(String username, int worldX, int worldY) {
		this.username = username;
		this.worldX = worldX;
		this.worldY = worldY;
	}

	public Pkt03Move(byte[] data) {
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.worldX = Integer.parseInt(dataArr[1]);
		this.worldY = Integer.parseInt(dataArr[2]);		
	}
	
	@Override
	public void sendData(GameClient client) {
		client.sendData(getData());
	}

	@Override
	public void sendData(GameServer server) {
		server.sendDataToAllClients(getData());
	}

	@Override
	public byte[] getData() {
		return ("03"+getUsername()+","+getWorldX()+","+getWorldY()).getBytes();
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getWorldX() {
		return worldX;
	}

	public int getWorldY() {
		return worldY;
	}

}
