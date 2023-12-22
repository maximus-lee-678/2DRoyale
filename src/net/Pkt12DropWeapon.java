package net;

public class Pkt12DropWeapon extends Packet {

	private int playerWeapIndex;
	private int weapType;
	private int weapId;
	private int worldX;
	private int worldY;

	public Pkt12DropWeapon(String username, int playerWeapIndex, int weapType, int weapId, int worldX, int worldY) {
		super(12, username);
		this.playerWeapIndex = playerWeapIndex;
		this.weapType = weapType;
		this.weapId = weapId;
		this.worldX = worldX;
		this.worldY = worldY;
	}

	public Pkt12DropWeapon(byte[] data) {
		super(12);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.playerWeapIndex = Integer.parseInt(dataArr[1]);
		this.weapType = Integer.parseInt(dataArr[2]);
		this.weapId = Integer.parseInt(dataArr[3]);
		this.worldX = Integer.parseInt(dataArr[4]);
		this.worldY = Integer.parseInt(dataArr[5]);
	}

	@Override
	public byte[] getData() {
		return ("12" + getUsername() + "," + getPlayerWeapIndex() + "," + getWeapType() + "," + getWeapId() + "," + getWorldX() + "," + getWorldY()).getBytes();
	}

	public int getPlayerWeapIndex() {
		return playerWeapIndex;
	}

	public int getWeapType() {
		return weapType;
	}

	public int getWeapId() {
		return weapId;
	}

	public int getWorldX() {
		return worldX;
	}

	public int getWorldY() {
		return worldY;
	}

}
