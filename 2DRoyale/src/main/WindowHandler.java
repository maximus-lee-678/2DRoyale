package main;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import net.Pkt02Disconnect;

public class WindowHandler implements WindowListener {

	private Game game;

	public WindowHandler(Game game) {
		this.game = game;
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (game.getGameState() == Game.waitState || game.getGameState() == Game.playState)
			new Pkt02Disconnect(game.player.getUsername()).sendData(game.socketClient);
	}

	// Functions from WindowListener Interface
	public void windowOpened(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

}
