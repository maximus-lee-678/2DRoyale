package net;

public class Pkt19ServerKick extends Packet {

	private boolean isHost;

	public Pkt19ServerKick(boolean isHost) {
		super(19, null);
		this.isHost = isHost;
	}

	public Pkt19ServerKick(byte[] data) {
		super(19);
		String message = new String(data).trim().substring(2);
		this.isHost = Boolean.parseBoolean(message);
	}

	public byte[] getData() {
		return ("19" + getIsHost()).getBytes();
	}

	public boolean getIsHost() {
		return isHost;
	}

}
