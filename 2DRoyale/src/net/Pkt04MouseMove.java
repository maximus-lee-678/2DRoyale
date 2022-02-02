package net;

public class Pkt04MouseMove implements Packet {

	public int id = 4;
	private String username;

	private double mouseX;
	private double mouseY;

	public Pkt04MouseMove(String username, double mouseX, double mouseY) {
		this.username = username;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}

	public Pkt04MouseMove(byte[] data) {
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.mouseX = Double.parseDouble(dataArr[1]);
		this.mouseY = Double.parseDouble(dataArr[2]);	
	}
	
	@Override
	public void sendData(GameClient client) {
		client.sendData(getData());
	}

	@Override
	public void sendData(GameServer server) {
		server.sendDataToAllClients(getData());
	}

	@Override
	public byte[] getData() {
		return ("04"+getUsername()+","+getMouseX()+","+getMouseY()).getBytes();
	}
	
	public String getUsername() {
		return username;
	}
	
	public double getMouseX() {
		return mouseX;
	}

	public double getMouseY() {
		return mouseY;
	}

}
