package net;

public class Pkt05MouseScroll extends Packet {

	private int mouseScrollDir;

	public Pkt05MouseScroll(String username, int mouseScrollDir) {
		super(5, username);
		this.mouseScrollDir = mouseScrollDir;
	}

	public Pkt05MouseScroll(byte[] data) {
		super(5);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.mouseScrollDir = Integer.parseInt(dataArr[1]);
	}

	@Override
	public byte[] getData() {
		return ("05"+getUsername()+","+getMouseScrollDir()).getBytes();
	}
	
	public int getMouseScrollDir() {
		return mouseScrollDir;
	}

}
