package net;

public class Pkt16Death extends Packet {

	private String victim;
	private int remainingPlayers;

	public Pkt16Death(String username, String victim, int remainingPlayers) {
		super(16, username);
		this.victim = victim;
		this.remainingPlayers = remainingPlayers;
	}

	public Pkt16Death(byte[] data) {
		super(16);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.victim = dataArr[1];
		this.remainingPlayers = Integer.parseInt(dataArr[2]);
	}
	
	@Override
	public byte[] getData() {
		return ("16"+getUsername()+","+getVictim()+","+getRemainingPlayers()).getBytes();
	}

	public String getVictim() {
		return victim;
	}

	public int getRemainingPlayers() {
		return remainingPlayers;
	}
	
	

}
