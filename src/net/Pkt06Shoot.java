package net;

public class Pkt06Shoot extends Packet {

	private int weapId;
	private double projAngle;
	private int worldX;
	private int worldY;

	public Pkt06Shoot(String username, int weapId, double projAngle, int worldX, int worldY) {
		super(6, username);
		this.weapId = weapId;
		this.projAngle = projAngle;
		this.worldX = worldX;
		this.worldY = worldY;
	}

	public Pkt06Shoot(byte[] data) {
		super(6);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.weapId = Integer.parseInt(dataArr[1]);
		this.projAngle = Double.parseDouble(dataArr[2]);
		this.worldX = Integer.parseInt(dataArr[3]);
		this.worldY = Integer.parseInt(dataArr[4]);
	}

	@Override
	public byte[] getData() {
		return ("06" + getUsername() + "," + getWeapId() + "," + getProjAngle() + "," + getWorldX() + "," + getWorldY()).getBytes();
	}

	public int getWeapId() {
		return weapId;
	}

	public double getProjAngle() {
		return projAngle;
	}

	public int getWorldX() {
		return worldX;
	}

	public int getWorldY() {
		return worldY;
	}

}
