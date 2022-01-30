package main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public class MouseHandler implements MouseListener, MouseMotionListener, MouseWheelListener {

	public double x;
	public double y;
	public boolean mousePressed;
	private Game game;

	public MouseHandler(Game game) {
		this.game = game;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (game.gameState == game.playState) {
			this.x = e.getX();
			this.y = e.getY();
		}		
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (game.gameState == game.playState) {
			game.player.playerMouseScroll(e.getWheelRotation());
		}		
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (game.gameState == game.playState)
			mousePressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (game.gameState == game.playState)
			mousePressed = false;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}
	@Override
	public void mouseDragged(MouseEvent e) {
		if (game.gameState == game.playState) {
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

}
