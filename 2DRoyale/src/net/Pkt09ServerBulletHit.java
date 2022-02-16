package net;

public class Pkt09ServerBulletHit extends Packet {

	private int bullet;
	private int weapId;
	private String victim;

	public Pkt09ServerBulletHit(String username, String victim, int weapId, int bullet) {
		super(9, username);
		this.bullet = bullet;
		this.weapId = weapId;
		this.victim = victim;
	}

	public Pkt09ServerBulletHit(byte[] data) {
		super(9);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.victim = dataArr[1];
		this.weapId = Integer.parseInt(dataArr[2]);
		this.bullet = Integer.parseInt(dataArr[3]);
	}

	@Override
	public byte[] getData() {
		return ("09" + getUsername() + "," + getVictim() + "," + getWeapId() + "," + getBullet()).getBytes();
	}

	public int getWeapId() {
		return weapId;
	}

	public int getBullet() {
		return bullet;
	}

	public String getVictim() {
		return victim;
	}
}
