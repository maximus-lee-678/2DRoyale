package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {

	public boolean up, down, left, right = false;

	public void keyTyped(KeyEvent e) {}; //default func, delete will throw warning :(

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		
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
