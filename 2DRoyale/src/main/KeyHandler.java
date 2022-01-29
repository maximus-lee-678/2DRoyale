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
		//when in menu page
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
					//when user click "start"
					if (gp.ui.commandNum == 0) {
						gp.ui.titleScreenState = 3;
					}
					////when user click "how to play"
					if (gp.ui.commandNum == 1) {
						gp.ui.titleScreenState = 1;
					}
					//when user click "players control"
					if (gp.ui.commandNum == 2) {
						gp.ui.titleScreenState = 2;
					}
					//when user click "quit"
					if (gp.ui.commandNum == 3) {
						System.exit(0);
					}
				}
			}
			//when in "how to play" page
			else if(gp.ui.titleScreenState == 1) {
				if(code == KeyEvent.VK_ENTER) {
					gp.ui.titleScreenState = 0;
				}
			}
			//when in "players control page"
			else if(gp.ui.titleScreenState == 2) {
				if(code == KeyEvent.VK_ENTER) {
					gp.ui.titleScreenState = 0;
				}
			}
			//when in "do you want to host the server" page
			else if(gp.ui.titleScreenState == 3) {
				if(code == KeyEvent.VK_W) {
					gp.ui.commandNum--;
					if (gp.ui.commandNum < 0) {
						gp.ui.commandNum = 2;
					}
				}
				if(code == KeyEvent.VK_S) {
					gp.ui.commandNum++;
					if (gp.ui.commandNum > 2) {
						gp.ui.commandNum = 0;
					}
				}
				if(code == KeyEvent.VK_ENTER) {
					if (gp.ui.commandNum == 0) {
						gp.ui.titleScreenState = 4;
					}
					else if (gp.ui.commandNum == 1) {
						gp.ui.titleScreenState = 4;
					}
					else if (gp.ui.commandNum == 2) {
						gp.ui.titleScreenState = 0;
						gp.ui.commandNum = 0;
					}
				}
			}
			else if(gp.ui.titleScreenState == 4) {
				char input = e.getKeyChar();
				if(input == KeyEvent.VK_BACK_SPACE)
			    {  
					gp.ui.name = removeLastChar(gp.ui.name);
			    }
				else {
					gp.ui.name += input;
					gp.ui.name = maxLength(gp.ui.name);
				}
				if(code == KeyEvent.VK_ENTER) {
					gp.gameState = gp.playState;
					gp.player.setUsername(gp.ui.name);
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
	

	public String removeLastChar(String str) {
	    if (str != null && str.length() > 0) {
	        str = str.substring(0, str.length() - 1);
	    }
	    System.out.println(str);
	    return str;
	}
	
	public String maxLength(String str) {
		if (str.length() > 15) {
		    str = str.substring(0, 15);
		}
		return str;
	}

}
