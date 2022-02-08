package net;

public class Pkt12DropWeapon extends Packet{
	
	private int weapIndex;
	
	public Pkt12DropWeapon(int weapIndex) {
		super(12, null);
		this.weapIndex = weapIndex;
	}

	public Pkt12DropWeapon(byte[] data) {
		super(12);
		String message = new String(data).trim().substring(2);
		this.weapIndex = Integer.parseInt(message);
	}

	@Override
	public byte[] getData() {
		return ("12" + getWeapIndex()).getBytes();
	}

	public int getWeapIndex() {
		return weapIndex;
	}

	
}
