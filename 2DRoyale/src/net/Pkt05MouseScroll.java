package net;

public class Pkt05MouseScroll implements Packet {

	public int id = 5;
	private String username;

	private int mouseScrollDir;

	public Pkt05MouseScroll(String username, int mouseScrollDir) {
		this.username = username;
		this.mouseScrollDir = mouseScrollDir;
	}

	public Pkt05MouseScroll(byte[] data) {
		String message = new String(data).trim().substring(2);
		String[] dataArr = message.split(",");
		this.username = dataArr[0];
		this.mouseScrollDir = Integer.parseInt(dataArr[1]);
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
		return ("05"+getUsername()+","+getMouseScrollDir()).getBytes();
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getMouseScrollDir() {
		return mouseScrollDir;
	}

}
