package net;

public class Pkt09ServerBulletHit extends Packet{

	private String weapon;
	private int bullet;
	private String victim;

	public Pkt09ServerBulletHit(String username, String victim, String weapon, int bullet) {
		super(9, username);
		this.victim = victim;
		this.weapon = weapon;
		this.bullet = bullet;
	}

	public Pkt09ServerBulletHit(byte[] data) {
		super(9);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.victim = dataArr[1];
		this.weapon = dataArr[2];		
		this.bullet = Integer.parseInt(dataArr[3]);		
	}

	@Override
	public byte[] getData() {
		return ("09" + getUsername() + "," + getVictim() + "," +getWeapon() + "," + getBullet()).getBytes();
	}

	public String getWeapon() {
		return weapon;
	}
	
	public int getBullet() {
		return bullet;
	}
	
	public String getVictim() {
		return victim;
	}
}
