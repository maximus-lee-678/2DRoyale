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

	Game game;
	public boolean up, down, left, right, interact = false, drop = false, map = false, ping = false;
	// regex for username
	String pattern = "^[a-zA-Z0-9]*$";
	// regex for ip address
	String ipPattern = "^[0-9\\.]*$";
	boolean isHost;

	public KeyHandler(Game game) {
		this.game = game;
	}

	public void keyTyped(KeyEvent e) {
	};

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		// when in menu page
		if (game.gameState == game.titleState) {
			if (game.ui.titleScreenState == 0) {
				if (code == KeyEvent.VK_UP) {
					game.playSE(0);
					game.ui.commandNum--;
					if (game.ui.commandNum < 0) {
						game.ui.commandNum = 3;
					}
				}
				if (code == KeyEvent.VK_DOWN) {
					game.playSE(0);
					game.ui.commandNum++;
					if (game.ui.commandNum > 3) {
						game.ui.commandNum = 0;
					}
				}
				if (code == KeyEvent.VK_ENTER) {
					// when user click "start"
					if (game.ui.commandNum == 0) {
						game.ui.titleScreenState = 3;
					}
					//// when user click "how to play"
					if (game.ui.commandNum == 1) {
						game.ui.titleScreenState = 1;
					}
					// when user click "players control"
					if (game.ui.commandNum == 2) {
						game.ui.titleScreenState = 2;
					}
					// when user click "quit"
					if (game.ui.commandNum == 3) {
						System.exit(0);
					}
				}
			}
			// when in "how to play" page
			else if (game.ui.titleScreenState == 1) {
				if (code == KeyEvent.VK_ENTER) {
					game.ui.titleScreenState = 0;
				}
			}
			// when in "players control page"
			else if (game.ui.titleScreenState == 2) {
				if (code == KeyEvent.VK_ENTER) {
					game.ui.titleScreenState = 0;
				}
			}
			// when in "do you want to host the server" page
			else if (game.ui.titleScreenState == 3) {
				if (code == KeyEvent.VK_UP) {
					game.playSE(0);
					game.ui.commandNum--;
					if (game.ui.commandNum < 0) {
						game.ui.commandNum = 2;
					}
				}
				if (code == KeyEvent.VK_DOWN) {
					game.playSE(0);
					game.ui.commandNum++;
					if (game.ui.commandNum > 2) {
						game.ui.commandNum = 0;
					}
				}
				if (code == KeyEvent.VK_ENTER) {
					// user will create a server using his/her ip address
					if (game.ui.commandNum == 0) {
						game.ui.titleScreenState = 4;
						isHost = true;
					} else if (game.ui.commandNum == 1) {
						game.ui.titleScreenState = 5;
						game.ui.commandNum = 0;
					} else if (game.ui.commandNum == 2) {
						game.ui.titleScreenState = 0;
						game.ui.commandNum = 0;
					}
				}
			}
			// when in "Enter your nickname" page
			else if (game.ui.titleScreenState == 4) {
				if (code == KeyEvent.VK_UP) {
					game.playSE(0);
					game.ui.commandNum--;
					if (game.ui.commandNum < 0) {
						game.ui.commandNum = 1;
					}
				}
				if (code == KeyEvent.VK_DOWN) {
					game.playSE(0);
					game.ui.commandNum++;
					if (game.ui.commandNum > 1) {
						game.ui.commandNum = 0;
					}
				}
				char input = e.getKeyChar();
				String tempInput = "";
				tempInput += input;
				if (input == KeyEvent.VK_BACK_SPACE) {
					game.ui.name = removeLastChar(game.ui.name);
				} else if (tempInput.matches(pattern)) {
					game.ui.name += input;
					game.ui.name = maxLength(game.ui.name, 15);
				}
				// check to see if nickname is empty
				if (game.ui.commandNum == 0) {
					if (game.ui.name != "" && code == KeyEvent.VK_ENTER) {
						if (isHost) {
							game.socketServer = new GameServer(game, game.randSeed);
							game.socketServer.start();
							game.socketClient = new GameClient(game, "localhost");
							game.socketClient.start();
						}
						else {
							game.socketClient = new GameClient(game, game.ui.ipAddress);
							game.socketClient.start();
						}
						game.player.setUsername(game.ui.name.trim());
						Pkt01Login loginPacket = new Pkt01Login(game.player.getUsername(), game.player.getWorldX(), game.player.getWorldY(), game.player.getPlayerWeapIndex(), game.waitState);
						if (game.socketServer != null) {
							game.getPlayers().add(game.player);
							PlayerMP clonePlayer = null;
							try {
								clonePlayer = (PlayerMP) game.player.clone();
							} catch (CloneNotSupportedException e1) {
								e1.printStackTrace();
							}
							game.socketServer.addConnection(clonePlayer, loginPacket);
							game.gameState = game.waitState;
							game.player.setPlayerState(game.waitState);
							game.loadDefaults();
							loginPacket.sendData(game.socketClient);
							game.player.generatePlayerXY();
						} else {
							loginPacket.sendData(game.socketClient);
						}
					}
				} else if (game.ui.commandNum == 1) {
					if(code == KeyEvent.VK_ENTER) {
						if (isHost) {
							game.ui.titleScreenState = 3;
							game.ui.commandNum = 0;
						}
						else {
							game.ui.titleScreenState = 5;
							game.ui.commandNum = 0;
						}
					}
				}
			}
			// when in "Type the server ip:" page
			else if (game.ui.titleScreenState == 5) {
				if (code == KeyEvent.VK_UP) {
					game.playSE(0);
					game.ui.commandNum--;
					if (game.ui.commandNum < 0) {
						game.ui.commandNum = 2;
					}
				}
				if (code == KeyEvent.VK_DOWN) {
					game.playSE(0);
					game.ui.commandNum++;
					if (game.ui.commandNum > 2) {
						game.ui.commandNum = 0;
					}
				}
				
				// user type in server address
				char input = e.getKeyChar();
				String tempInput = "";
				tempInput += input;
				if (input == KeyEvent.VK_BACK_SPACE) {
					game.ui.ipAddress = removeLastChar(game.ui.ipAddress);
				} else if (tempInput.matches(ipPattern)) {
					game.ui.ipAddress += input;
					game.ui.ipAddress = maxLength(game.ui.ipAddress, 15);
				}
				if (game.ui.commandNum == 0) {
					if (code == KeyEvent.VK_ENTER) {
						game.ui.ipAddress = game.ui.ipAddress.trim();
						// if user leave it empty, the user will enter his/her own ip address
						if (game.ui.ipAddress.isEmpty() == true) {
							game.ui.ipAddress = "localhost";
						}
						// go to "Enter your nickname" page
						game.ui.titleScreenState = 4;
						isHost = false;

					}
					// if user wants to copy from keyboard
				} else if (game.ui.commandNum == 1) {
					if (code == KeyEvent.VK_ENTER) {
						try {
							String data = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
							if (data.matches(ipPattern)) {
								game.ui.ipAddress = data;
								game.ui.ipAddress = maxLength(game.ui.ipAddress, 15);
							} else
								System.out.println("bad syntax");
						} catch (Exception x) {
							System.out.println(x);
						}
					}
					// go back
				} else if (game.ui.commandNum == 2) {
					if (code == KeyEvent.VK_ENTER) {
						game.ui.titleScreenState = 3;
						game.ui.commandNum = 0;
					}
				}
			}
		}
		// when in option screen
		if ((game.gameState == game.waitState || game.gameState == game.playState) && game.ui.option == true) {
			if (code == KeyEvent.VK_UP) {
				game.playSE(0);
				game.ui.commandNum--;
				if (game.ui.commandNum < 0) {
					game.ui.commandNum = 2;
				}
			}
			if (code == KeyEvent.VK_DOWN) {
				game.playSE(0);
				game.ui.commandNum++;
				if (game.ui.commandNum > 2) {
					game.ui.commandNum = 0;
				}
			}
			if (code == KeyEvent.VK_ENTER) {
				// back to game
				if (game.ui.commandNum == 0) {
					game.ui.option = false;
				}
				// back to main menu
				else if (game.ui.commandNum == 1) {
					game.gameState = game.titleState;
					game.player.setPlayerState(game.titleState);
					game.clearPlayers();
					game.ui.titleScreenState = 0;
					game.ui.commandNum = 0;
					new Pkt02Disconnect(game.player.getUsername()).sendData(game.socketClient);
				}
				// exit game
				else if (game.ui.commandNum == 2) {
					System.exit(0);
				}
			}
			// press escape to go back to game
			if (code == KeyEvent.VK_ESCAPE) {
				game.playSE(0);
				game.ui.option = false;
			}
		} else if (game.gameState == game.waitState || game.gameState == game.playState) {
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
				if (game.socketServer != null)
					new Pkt14StartGame(game.player.getUsername()).sendData(game.socketClient);

			}
			if (code == KeyEvent.VK_ESCAPE) {
				game.playSE(0);
				game.ui.option = true;
				game.ui.commandNum = 0;
				;
			}

			if (code == KeyEvent.VK_Q) {
				drop = false;
			}
			if (code == KeyEvent.VK_M) {
				map = !map;
				game.playSE(5);
			}

			if (code == KeyEvent.VK_1) {
				game.player.setPlayerWeapIndex(0);
			}
			if (code == KeyEvent.VK_2) {
				game.player.setPlayerWeapIndex(1);
			}
			if (code == KeyEvent.VK_3) {
				game.player.setPlayerWeapIndex(2);
			}
			if (code == KeyEvent.VK_4) {
				game.player.setPlayerWeapIndex(3);
			}

		}
		// when in end game screen
		if (game.gameState == game.endState) {
			if (code == KeyEvent.VK_UP) {
				game.playSE(0);
				game.ui.commandNum--;
				if (game.ui.commandNum < 0) {
					game.ui.commandNum = 1;
				}
			}
			if (code == KeyEvent.VK_DOWN) {
				game.playSE(0);
				game.ui.commandNum++;
				if (game.ui.commandNum > 1) {
					game.ui.commandNum = 0;
				}
			}
			if (code == KeyEvent.VK_ENTER) {
				// back to lobby
				if (game.ui.commandNum == 0) {
					new Pkt17BackToLobby(game.player.getUsername()).sendData(game.socketClient);
					// back to main menu
				} else if (game.ui.commandNum == 1) {
					game.gameState = game.titleState;
					game.ui.titleScreenState = 0;
					game.ui.commandNum = 0;
					new Pkt02Disconnect(game.player.getUsername()).sendData(game.socketClient);
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
			if(game.gameState == game.titleState) return;
			game.socketClient.setLatency(System.currentTimeMillis());
			new Pkt08ServerPing().sendData(game.socketClient);
		}

	}
	
	// method to backspace a character when keying nickname
	public String removeLastChar(String str) {
		if (str != null && str.length() > 0) {
			str = str.substring(0, str.length() - 1);
		}
		System.out.println(str);
		return str;
	}

	// method to limit nickname and ip address length
	public String maxLength(String str, int max) {
		if (str.length() > max) {
			str = str.substring(0, max);
		}
		return str;
	}

}
