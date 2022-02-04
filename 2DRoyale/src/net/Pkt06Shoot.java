package net;

public class Pkt06Shoot extends Packet {

	private String weapon;
	private double projAngle;
	private int worldX;
	private int worldY;

	public Pkt06Shoot(String username, String weapon, double projAngle, int worldX, int worldY) {
		super(6, username);
		this.weapon = weapon;
		this.projAngle = projAngle;
		this.worldX = worldX;
		this.worldY = worldY;
	}

	public Pkt06Shoot(byte[] data) {
		super(6);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.weapon = dataArr[1];
		this.projAngle = Double.parseDouble(dataArr[2]);
		this.worldX = Integer.parseInt(dataArr[3]);
		this.worldY = Integer.parseInt(dataArr[4]);
	}

	@Override
	public byte[] getData() {
		return ("06"+getUsername()+","+getWeapon()+","+getProjAngle()+","+getWorldX()+","+getWorldY()).getBytes();
	}

	public String getWeapon() {
		return weapon;
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
