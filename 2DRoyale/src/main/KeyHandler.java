package main;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import entity.PlayerMP;
import net.GameClient;
import net.GameServer;
import net.Pkt01Login;
import net.Pkt02Disconnect;
import net.Pkt08ServerPing;
import net.Pkt14StartGame;
import net.Pkt17BackToLobby;

public class KeyHandler implements KeyListener {

	Game gp;
	public boolean up, down, left, right, interact = false, drop = false, map = false, ping = false;
	String pattern = "^[a-zA-Z0-9]*$";
	String ipPattern = "^[0-9\\.]*$";

	public KeyHandler(Game gp) {
		this.gp = gp;
	}

	public void keyTyped(KeyEvent e) {
	};

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		// when in menu page
		if (gp.gameState == gp.titleState) {
			if (gp.ui.titleScreenState == 0) {
				if (code == KeyEvent.VK_W) {
					gp.playSE(0);
					gp.ui.commandNum--;
					if (gp.ui.commandNum < 0) {
						gp.ui.commandNum = 3;
					}
				}
				if (code == KeyEvent.VK_S) {
					gp.playSE(0);
					gp.ui.commandNum++;
					if (gp.ui.commandNum > 3) {
						gp.ui.commandNum = 0;
					}
				}
				if (code == KeyEvent.VK_ENTER) {
					// when user click "start"
					if (gp.ui.commandNum == 0) {
						gp.ui.titleScreenState = 3;
					}
					//// when user click "how to play"
					if (gp.ui.commandNum == 1) {
						gp.ui.titleScreenState = 1;
					}
					// when user click "players control"
					if (gp.ui.commandNum == 2) {
						gp.ui.titleScreenState = 2;
					}
					// when user click "quit"
					if (gp.ui.commandNum == 3) {
						System.exit(0);
					}
				}
			}
			// when in "how to play" page
			else if (gp.ui.titleScreenState == 1) {
				if (code == KeyEvent.VK_ENTER) {
					gp.ui.titleScreenState = 0;
				}
			}
			// when in "players control page"
			else if (gp.ui.titleScreenState == 2) {
				if (code == KeyEvent.VK_ENTER) {
					gp.ui.titleScreenState = 0;
				}
			}
			// when in "do you want to host the server" page
			else if (gp.ui.titleScreenState == 3) {
				if (code == KeyEvent.VK_W) {
					gp.playSE(0);
					gp.ui.commandNum--;
					if (gp.ui.commandNum < 0) {
						gp.ui.commandNum = 2;
					}
				}
				if (code == KeyEvent.VK_S) {
					gp.playSE(0);
					gp.ui.commandNum++;
					if (gp.ui.commandNum > 2) {
						gp.ui.commandNum = 0;
					}
				}
				if (code == KeyEvent.VK_ENTER) {
					// user will create a server using his localhost
					if (gp.ui.commandNum == 0) {
						gp.ui.titleScreenState = 4;
						gp.socketServer = new GameServer(gp, gp.randSeed);
						gp.socketServer.start();
						gp.socketClient = new GameClient(gp, "localhost");
						gp.socketClient.start();
					} else if (gp.ui.commandNum == 1) {
						gp.ui.titleScreenState = 5;
						gp.ui.commandNum = 0;
					} else if (gp.ui.commandNum == 2) {
						gp.ui.titleScreenState = 0;
						gp.ui.commandNum = 0;
					}
				}
			}
			// when in "Enter your nickname" page
			else if (gp.ui.titleScreenState == 4) {
				char input = e.getKeyChar();
				String tempInput = "";
				tempInput += input;
				if (input == KeyEvent.VK_BACK_SPACE) {
					gp.ui.name = removeLastChar(gp.ui.name);
				} else if (tempInput.matches(pattern)) {
					gp.ui.name += input;
					gp.ui.name = maxLength(gp.ui.name, 15);
				}
				if (gp.ui.name != "" && code == KeyEvent.VK_ENTER) {
					gp.player.setUsername(gp.ui.name.trim());
					Pkt01Login loginPacket = new Pkt01Login(gp.player.getUsername(), gp.player.worldX, gp.player.worldY, gp.player.playerWeapIndex, gp.waitState);
					if (gp.socketServer != null) {
						gp.getPlayers().add(gp.player);
						PlayerMP clonePlayer = null;
						try {
							clonePlayer = (PlayerMP) gp.player.clone();
						} catch (CloneNotSupportedException e1) {
							e1.printStackTrace();
						}
						gp.socketServer.addConnection(clonePlayer, loginPacket);
						gp.gameState = gp.waitState;
						gp.player.playerState = gp.waitState;
						gp.loadDefaults();
						loginPacket.sendData(gp.socketClient);
						gp.player.generatePlayerXY();
					} else {
						loginPacket.sendData(gp.socketClient);
					}
				}

			}
			// when in "Type the server ip:" page
			else if (gp.ui.titleScreenState == 5) {
				if (code == KeyEvent.VK_W) {
					gp.playSE(0);
					gp.ui.commandNum--;
					if (gp.ui.commandNum < 0) {
						gp.ui.commandNum = 1;
					}
				}
				if (code == KeyEvent.VK_S) {
					gp.playSE(0);
					gp.ui.commandNum++;
					if (gp.ui.commandNum > 1) {
						gp.ui.commandNum = 0;
					}
				}
				if (gp.ui.commandNum == 0) {
					char input = e.getKeyChar();
					String tempInput = "";
					tempInput += input;
					if (input == KeyEvent.VK_BACK_SPACE) {
						gp.ui.ipAddress = removeLastChar(gp.ui.ipAddress);
					} else if (tempInput.matches(ipPattern)) {
						gp.ui.ipAddress += input;
						gp.ui.ipAddress = maxLength(gp.ui.ipAddress, 15);
					}
					if (code == KeyEvent.VK_ENTER) {
						// user type in server address
						gp.ui.ipAddress = gp.ui.ipAddress.trim();
						// if user leave it empty, the user will enter localhost alone without a server
						if (gp.ui.ipAddress.isEmpty() == true) {
							gp.ui.ipAddress = "localhost";
						}
						gp.ui.titleScreenState = 4;
						gp.socketClient = new GameClient(gp, gp.ui.ipAddress);
						gp.socketClient.start();

					}
				} else if (gp.ui.commandNum == 1) {
					if (code == KeyEvent.VK_ENTER) {
						try {
							String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
							if (data.matches(ipPattern)) {
								gp.ui.ipAddress = data;
								gp.ui.ipAddress = maxLength(gp.ui.ipAddress, 15);
							} else
								System.out.println("bad syntax");
						} catch (Exception x) {
							System.out.println(x);
						}
					}
				}
			}
		}
		// when in option screen
		if ((gp.gameState == gp.waitState || gp.gameState == gp.playState) && gp.ui.option == true) {
			if (code == KeyEvent.VK_W) {
				gp.playSE(0);
				gp.ui.commandNum--;
				if (gp.ui.commandNum < 0) {
					gp.ui.commandNum = 2;
				}
			}
			if (code == KeyEvent.VK_S) {
				gp.playSE(0);
				gp.ui.commandNum++;
				if (gp.ui.commandNum > 2) {
					gp.ui.commandNum = 0;
				}
			}
			if (code == KeyEvent.VK_ENTER) {
				// back to game
				if (gp.ui.commandNum == 0) {
					gp.ui.option = false;
				}
				// back to main menu
				else if (gp.ui.commandNum == 1) {
					gp.gameState = gp.titleState;
					gp.player.playerState = gp.titleState;
					gp.clearPlayers();
					gp.ui.titleScreenState = 0;
					gp.ui.commandNum = 0;
					new Pkt02Disconnect(gp.player.getUsername()).sendData(gp.socketClient);
				}
				// exit game
				else if (gp.ui.commandNum == 2) {
					System.exit(0);
				}
			}
			if (code == KeyEvent.VK_ESCAPE) {
				gp.playSE(0);
				gp.ui.option = false;
			}
		} else if (gp.gameState == gp.waitState || gp.gameState == gp.playState) {
			// true if user presses button
			if (code == KeyEvent.VK_W)
				up = true;
			if (code == KeyEvent.VK_A)
				left = true;
			if (code == KeyEvent.VK_S)
				down = true;
			if (code == KeyEvent.VK_D)
				right = true;
			if (code == KeyEvent.VK_E)
				interact = false;
			if (code == KeyEvent.VK_F) {
				if (gp.socketServer != null)
					new Pkt14StartGame(gp.player.getUsername()).sendData(gp.socketClient);

			}
			if (code == KeyEvent.VK_ESCAPE) {
				gp.playSE(0);
				gp.ui.option = true;
				gp.ui.commandNum = 0;
				;
			}

			if (code == KeyEvent.VK_Q) {
				drop = false;
			}
			if (code == KeyEvent.VK_M) {
				map = !map;
				gp.playSE(5);
			}

			if (code == KeyEvent.VK_1) {
				gp.player.playerWeapIndex = 0;
			}
			if (code == KeyEvent.VK_2) {
				gp.player.playerWeapIndex = 1;
			}
			if (code == KeyEvent.VK_3) {
				gp.player.playerWeapIndex = 2;
			}
			if (code == KeyEvent.VK_4) {
				gp.player.playerWeapIndex = 3;
			}

		}
		if (gp.gameState == gp.endState) {
			if (code == KeyEvent.VK_W) {
				gp.playSE(0);
				gp.ui.commandNum--;
				if (gp.ui.commandNum < 0) {
					gp.ui.commandNum = 1;
				}
			}
			if (code == KeyEvent.VK_S) {
				gp.playSE(0);
				gp.ui.commandNum++;
				if (gp.ui.commandNum > 1) {
					gp.ui.commandNum = 0;
				}
			}
			if (code == KeyEvent.VK_ENTER) {
				// back to lobby
				if (gp.ui.commandNum == 0) {
					new Pkt17BackToLobby(gp.player.getUsername()).sendData(gp.socketClient);
					// back to main menu
				} else if (gp.ui.commandNum == 1) {
					gp.gameState = gp.titleState;
					gp.ui.titleScreenState = 0;
					gp.ui.commandNum = 0;
					new Pkt02Disconnect(gp.player.getUsername()).sendData(gp.socketClient);
				}
			}
		}

	}

	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();

		// true if user un-press button
		if (code == KeyEvent.VK_W)
			up = false;
		if (code == KeyEvent.VK_A)
			left = false;
		if (code == KeyEvent.VK_S)
			down = false;
		if (code == KeyEvent.VK_D)
			right = false;
		if (code == KeyEvent.VK_E)
			interact = true;
		if (code == KeyEvent.VK_Q)
			drop = true;
		if (code == KeyEvent.VK_P) {
			gp.socketClient.latency = System.currentTimeMillis();
			new Pkt08ServerPing().sendData(gp.socketClient);
		}

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
