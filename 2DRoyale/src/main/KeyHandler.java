package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
	
	Game gp;
	public boolean up, down, left, right = false;
	
	public KeyHandler(Game gp) {
		this.gp = gp;
	}

	public void keyTyped(KeyEvent e) {}; //default func, delete will throw warning :(

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		if(gp.gameState == gp.titleState) {
			if(gp.ui.titleScreenState == 0) {
				if(code == KeyEvent.VK_W) {
					gp.ui.commandNum--;
					if (gp.ui.commandNum < 0) {
						gp.ui.commandNum = 3;
					}
				}
				if(code == KeyEvent.VK_S) {
					gp.ui.commandNum++;
					if (gp.ui.commandNum > 3) {
						gp.ui.commandNum = 0;
					}
				}
				if(code == KeyEvent.VK_ENTER) {
					if (gp.ui.commandNum == 0) {
						gp.gameState = gp.playState;
					}
					if (gp.ui.commandNum == 1) {
						gp.ui.titleScreenState = 1;
					}
					if (gp.ui.commandNum == 2) {
						gp.ui.titleScreenState = 2;
					}
					if (gp.ui.commandNum == 3) {
						System.exit(0);
					}
				}
			}
			else if(gp.ui.titleScreenState == 1) {
				if(code == KeyEvent.VK_ENTER) {
					gp.ui.titleScreenState = 0;
				}
			}
			else if(gp.ui.titleScreenState == 2) {
				if(code == KeyEvent.VK_ENTER) {
					gp.ui.titleScreenState = 0;
				}
			}
			
		}
		//true if user presses button
		if(code == KeyEvent.VK_W) up = true;
		if(code == KeyEvent.VK_A) left = true;
		if(code == KeyEvent.VK_S) down = true;
		if(code == KeyEvent.VK_D) right = true;
	}

	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		
		//true if user un-press button
		if(code == KeyEvent.VK_W) up = false;
		if(code == KeyEvent.VK_A) left = false;
		if(code == KeyEvent.VK_S) down = false;
		if(code == KeyEvent.VK_D) right = false;
	}

}
