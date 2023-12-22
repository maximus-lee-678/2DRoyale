package net;

public class Pkt04MouseMove extends Packet {

	private double mouseX;
	private double mouseY;

	public Pkt04MouseMove(String username, double mouseX, double mouseY) {
		super(4, username);
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public Pkt04MouseMove(byte[] data) {
		super(4);
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.mouseX = Double.parseDouble(dataArr[1]);
		this.mouseY = Double.parseDouble(dataArr[2]);
	}

	@Override
	public byte[] getData() {
		return ("04" + getUsername() + "," + getMouseX() + "," + getMouseY()).getBytes();
	}

	public double getMouseX() {
		return mouseX;
	}

	public double getMouseY() {
		return mouseY;
	}

}
