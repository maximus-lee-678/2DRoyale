package main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import net.Pkt05MouseScroll;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {

	private double x;
	private double y;
	private boolean mousePressed;
	private Game game;

	public MouseHandler(Game game) {
		this.game = game;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (game.getGameState() == Game.waitState || game.getGameState() == Game.playState) {
			this.x = e.getX();
			this.y = e.getY();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (game.getGameState() == Game.waitState || game.getGameState() == Game.playState)
			new Pkt05MouseScroll(game.player.getUsername(), e.getWheelRotation()).sendData(game.socketClient);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (game.getGameState() == Game.waitState || game.getGameState() == Game.playState)
			mousePressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (game.getGameState() == Game.waitState || game.getGameState() == Game.playState)
			mousePressed = false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (game.getGameState() == Game.waitState || game.getGameState() == Game.playState) {
			this.x = e.getX();
			this.y = e.getY();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public boolean isMousePressed() {
		return mousePressed;
	}

	
}
