package net;

public class Pkt11CrateOpen extends Packet {

	private int crateIndex;
	private int weapType;
	private int weapId;

	public Pkt11CrateOpen(String username, int crateIndex) {
		super(11, username);
		this.crateIndex = crateIndex;
	}

	public Pkt11CrateOpen(byte[] data) {
		super(11);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.crateIndex = Integer.parseInt(dataArr[1]);
		this.weapType = Integer.parseInt(dataArr[2]);
		this.weapId = Integer.parseInt(dataArr[3]);
	}

	@Override
	public byte[] getData() {
		return ("11" + getUsername() + "," + getCrateIndex() + "," + getWeapType() + "," + getWeapId()).getBytes();
	}

	public int getCrateIndex() {
		return crateIndex;
	}

	public int getWeapType() {
		return weapType;
	}

	public void setWeapType(int weapType) {
		this.weapType = weapType;
	}

	public int getWeapId() {
		return weapId;
	}

	public void setWeapId(int weapId) {
		this.weapId = weapId;
	}

}
