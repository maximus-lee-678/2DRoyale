package net;

public class Pkt10PickupWeapon extends Packet {

	private int playerWeapIndex;
	private int weapType;
	private int weapId;

	public Pkt10PickupWeapon(String username, int playerWeapIndex, int weapType, int weapId) {
		super(10, username);
		this.playerWeapIndex = playerWeapIndex;
		this.weapType = weapType;
		this.weapId = weapId;
	}

	public Pkt10PickupWeapon(byte[] data) {
		super(10);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.playerWeapIndex = Integer.parseInt(dataArr[1]);
		this.weapType = Integer.parseInt(dataArr[2]);
		this.weapId = Integer.parseInt(dataArr[3]);
	}

	@Override
	public byte[] getData() {
		return ("10" + getUsername() + "," + getPlayerWeapIndex() + "," + getWeapType() + "," + getWeapId()).getBytes();
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
}
