package net;

public class Pkt07ServerSeed extends Packet {

	private long serverSeed;

	public Pkt07ServerSeed(long serverSeed) {
		super(7, null);
		this.serverSeed = serverSeed;
	}

	public Pkt07ServerSeed(byte[] data) {
		super(7);
		String message = new String(data).trim().substring(2);
		this.serverSeed = Long.parseLong(message);
	}

	@Override
	public byte[] getData() {
		return ("07" + getServerSeed()).getBytes();
	}

	public long getServerSeed() {
		return serverSeed;
	}

}
