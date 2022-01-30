package main;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import net.Packet;

public class WindowHandler implements WindowListener{

	private final Game game;
	
	public WindowHandler(Game game) {
		this.game = game;
		this.game.window.addWindowListener(this);
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		if(game.gameState == game.playState) {
			Packet disconnectPacket = new Packet(2, game.player.getUsername());
			game.socketClient.sendData(disconnectPacket.getPacket());
		}
		
	}
	
	//default funcs, delete will throw warning :(
	public void windowOpened(WindowEvent e) {}
	public void windowClosed(WindowEvent e) {}
	public void windowIconified(WindowEvent e) {}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e) {}
	public void windowDeactivated(WindowEvent e) {}
	
}
