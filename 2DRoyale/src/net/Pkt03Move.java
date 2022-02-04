package net;

public class Pkt03Move extends Packet {

	private int worldX;
	private int worldY;

	public Pkt03Move(String username, int worldX, int worldY) {
		super(3, username);
		this.worldX = worldX;
		this.worldY = worldY;
	}

	public Pkt03Move(byte[] data) {
		super(3);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.worldX = Integer.parseInt(dataArr[1]);
		this.worldY = Integer.parseInt(dataArr[2]);
	}

	@Override
	public byte[] getData() {
		return ("03" + getUsername() + "," + getWorldX() + "," + getWorldY()).getBytes();
	}

	public int getWorldX() {
		return worldX;
	}

	public int getWorldY() {
		return worldY;
	}

}
