package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import net.GameClient;
import net.GameServer;
import net.Packet;

public class KeyHandler implements KeyListener {
	
	Game gp;
	public boolean up, down, left, right = false;
	String pattern= "^[a-zA-Z0-9]*$";
	String ipPattern= "^[0-9\\.]*$";
	
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
					//user will create a server using his localhost
					if (gp.ui.commandNum == 0) {
						gp.ui.titleScreenState = 4;
						gp.socketServer = new GameServer(gp);
						gp.socketServer.start();
						gp.socketClient = new GameClient(gp, "localhost");
						gp.socketClient.start();
					}
					else if (gp.ui.commandNum == 1) {
						gp.ui.titleScreenState = 5;
					}
					else if (gp.ui.commandNum == 2) {
						gp.ui.titleScreenState = 0;
						gp.ui.commandNum = 0;
					}
				}
			}
			//when in "Enter your nickname" page
			else if(gp.ui.titleScreenState == 4) {
				char input = e.getKeyChar();
				String tempInput = "";
				tempInput += input;
				if(input == KeyEvent.VK_BACK_SPACE)
			    {  
					gp.ui.name = removeLastChar(gp.ui.name);
			    }
				else if (tempInput.matches(pattern)){
					gp.ui.name += input;
					gp.ui.name = maxLength(gp.ui.name, 15);
				}
				if(code == KeyEvent.VK_ENTER) {
					gp.gameState = gp.playState;
					gp.player.setUsername(gp.ui.name.trim());
					gp.getPlayers().add(gp.player);
					if (gp.socketServer != null) {
						gp.socketServer.addConnection(gp.player);
					}
					Packet loginPacket = new Packet(1, gp.player.getUsername(), gp.player.worldX, gp.player.worldY, gp.player.playerWeapIndex);
					gp.socketClient.sendData(loginPacket.getPacket());
				}
			}
			//when in "Type the server ip:" page
			else if(gp.ui.titleScreenState == 5) {
				char input = e.getKeyChar();
				String tempInput = "";
				tempInput += input;
				if(input == KeyEvent.VK_BACK_SPACE)
			    {  
					gp.ui.ipAddress = removeLastChar(gp.ui.ipAddress);
			    }
				else if (tempInput.matches(ipPattern)){
					gp.ui.ipAddress += input;
					gp.ui.ipAddress = maxLength(gp.ui.ipAddress, 15);
				}
				if(code == KeyEvent.VK_ENTER) {
					//user type in server address
					gp.ui.ipAddress = gp.ui.ipAddress.trim();
					//if user leave it empty, the user will enter localhost alone without a server
					if (gp.ui.ipAddress.isEmpty() == true) {
						gp.ui.ipAddress = "localhost";
					}
					gp.ui.titleScreenState = 4;
					gp.socketClient = new GameClient(gp, gp.ui.ipAddress);
					gp.socketClient.start();
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
	
	public String maxLength(String str, int max) {
		if (str.length() > max) {
		    str = str.substring(0, max);
		}
		return str;
	}

}
