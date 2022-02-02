package net;

public interface Packet {
	public void sendData(GameClient client);
	public void sendData(GameServer server);
	public byte[] getData();
}
