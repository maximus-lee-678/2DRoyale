package net;

public class Pkt01Login extends Packet {

	private int worldX;
	private int worldY;
	private int playerWeapIndex;
	private int playerState;

	public Pkt01Login(String username, int worldX, int worldY, int playerWeapIndex, int playerState) {
		super(1, username);
		this.worldX = worldX;
		this.worldY = worldY;
		this.playerWeapIndex = playerWeapIndex;
		this.playerState = playerState;
	}

	public Pkt01Login(byte[] data) {
		super(1);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.worldX = Integer.parseInt(dataArr[1]);
		this.worldY = Integer.parseInt(dataArr[2]);
		this.playerWeapIndex = Integer.parseInt(dataArr[3]);
		this.playerState = Integer.parseInt(dataArr[4]);
	}
	
	@Override
	public byte[] getData() {
		return ("01"+getUsername()+","+getWorldX()+","+getWorldY()+","+getPlayerWeapIndex()+","+getPlayerState()).getBytes();
	}
	
	public int getWorldX() {
		return worldX;
	}

	public int getWorldY() {
		return worldY;
	}
	
	public int getPlayerWeapIndex() {
		return playerWeapIndex;
	}

	public int getPlayerState() {
		return playerState;
	}
	
	

}
