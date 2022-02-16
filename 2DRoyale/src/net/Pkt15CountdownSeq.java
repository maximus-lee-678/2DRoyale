package net;

public class Pkt15CountdownSeq extends Packet {

	private int countDown;

	public Pkt15CountdownSeq(int countDown) {
		super(15, null);
		this.countDown = countDown;
	}

	public Pkt15CountdownSeq(byte[] data) {
		super(15);
		String message = new String(data).trim().substring(2);
		this.countDown = Integer.parseInt(message);
	}

	public byte[] getData() {
		return ("15" + getCountDown()).getBytes();
	}

	public int getCountDown() {
		return countDown;
	}
}
