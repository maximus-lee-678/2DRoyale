package net;

public class Pkt10PickupWeapon extends Packet{

	public Pkt10PickupWeapon(String username) {
		super(10, username);
	}

	public Pkt10PickupWeapon(byte[] data) {
		super(10);
		String message = new String(data).trim().substring(2);
		this.username = message;
	}

	@Override
	public byte[] getData() {
		return ("10" + getUsername()).getBytes();
	}
	
}
