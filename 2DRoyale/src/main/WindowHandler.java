package main;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import net.Pkt02Disconnect;

public class WindowHandler implements WindowListener{

	private final Game game;
	
	public WindowHandler(Game game) {
		this.game = game;
		this.game.window.addWindowListener(this);
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		if(game.gameState == game.waitState || game.gameState == game.playState) {
			Pkt02Disconnect disconnectPacket = new Pkt02Disconnect(game.player.getUsername());
			disconnectPacket.sendData(game.socketClient);
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
