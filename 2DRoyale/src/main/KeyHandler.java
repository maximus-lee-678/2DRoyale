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

	private Game game;
	private boolean up, down, left, right;
	private boolean interact, drop, map, ping;
	private boolean isHost;

	private int userSelect;
	private static final String pattern = "^[a-zA-Z0-9]*$";// regex for username
	private static final String ipPattern = "^[0-9\\.]*$"; // regex for ip address

	public KeyHandler(Game game) {
		this.game = game;
		this.interact = false;
		this.drop = false;
		this.map = false;
		this.ping = false;
	}

	public void keyTyped(KeyEvent e) {
	};

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		// when in menu page
		if (game.getGameState() == Game.titleState) {
			if (game.ui.getTitleScreenState() == 0) {
				if (code == KeyEvent.VK_UP) {
					game.soundHandler.playSound(0);
					userSelect--;
					if (userSelect < 0) { userSelect = 3; }
				}
				if (code == KeyEvent.VK_DOWN) {
					game.soundHandler.playSound(0);
					userSelect++;
					if (userSelect > 3) { userSelect = 0; }
				}
				if (code == KeyEvent.VK_ENTER) {
					// when user click "start"
					if (userSelect == 0) { game.ui.setTitleScreenState(3); }
					//// when user click "how to play"
					if (userSelect == 1) { game.ui.setTitleScreenState(1); }
					// when user click "players control"
					if (userSelect == 2) { game.ui.setTitleScreenState(2); }
					// when user click "quit"
					if (userSelect == 3) { System.exit(0); }
				}
			}
			// when in "how to play" page
			else if (game.ui.getTitleScreenState() == 1) {
				if (code == KeyEvent.VK_ENTER) { game.ui.setTitleScreenState(0); }
			}
			// when in "players control page"
			else if (game.ui.getTitleScreenState() == 2) {
				if (code == KeyEvent.VK_ENTER) { game.ui.setTitleScreenState(0); }
			}
			// when in "do you want to host the server" page
			else if (game.ui.getTitleScreenState() == 3) {
				if (code == KeyEvent.VK_UP) {
					game.soundHandler.playSound(0);
					userSelect--;
					if (userSelect < 0) { userSelect = 2; }
				}
				if (code == KeyEvent.VK_DOWN) {
					game.soundHandler.playSound(0);
					userSelect++;
					if (userSelect > 2) { userSelect = 0; }
				}
				if (code == KeyEvent.VK_ENTER) {
					// user will create a server using his/her ip address
					if (userSelect == 0) {
						game.ui.setTitleScreenState(4);
						isHost = true;
					} else if (userSelect == 1) {
						game.ui.setTitleScreenState(5);
						userSelect = 0;
					} else if (userSelect == 2) {
						game.ui.setTitleScreenState(0);
						userSelect = 0;
					}
				}
			}
			// when in "Enter your nickname" page
			else if (game.ui.getTitleScreenState() == 4) {
				if (code == KeyEvent.VK_UP) {
					game.soundHandler.playSound(0);
					userSelect--;
					if (userSelect < 0) { userSelect = 1; }
				}
				if (code == KeyEvent.VK_DOWN) {
					game.soundHandler.playSound(0);
					userSelect++;
					if (userSelect > 1) { userSelect = 0; }
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
				if (userSelect == 0) {
					if (game.ui.name != "" && code == KeyEvent.VK_ENTER) {
						game.player.setUsername(game.ui.name.trim());
						Pkt01Login loginPacket = new Pkt01Login(game.player.getUsername(), game.player.getWorldX(), game.player.getWorldY(), game.player.getPlayerWeapIndex(), Game.waitState);
						if (isHost) {
							game.socketServer = new GameServer(game, game.getRandSeed());
							game.socketServer.start();
							game.socketClient = new GameClient(game, "localhost");
							game.socketClient.start();

							game.getPlayers().add(game.player);
							PlayerMP clonePlayer = null;
							try {
								clonePlayer = (PlayerMP) game.player.clone();
							} catch (CloneNotSupportedException e1) {
								e1.printStackTrace();
							}
							game.socketServer.addConnection(clonePlayer, loginPacket);
							game.setGameState(Game.waitState);
							game.player.setPlayerState(Game.waitState);
							game.loadDefaults();
							loginPacket.sendData(game.socketClient);
							game.player.generatePlayerXY();
						} else {
							game.socketClient = new GameClient(game, game.ui.ipAddress);
							game.socketClient.start();

							loginPacket.sendData(game.socketClient);
						}

					}
				} else if (userSelect == 1) {
					if (code == KeyEvent.VK_ENTER) {
						if (isHost) {
							game.ui.setTitleScreenState(3);
							userSelect = 0;
						} else {
							game.ui.setTitleScreenState(5);
							userSelect = 0;
						}
					}
				}
			}
			// when in "Type the server ip:" page
			else if (game.ui.getTitleScreenState() == 5) {
				if (code == KeyEvent.VK_UP) {
					game.soundHandler.playSound(0);
					userSelect--;
					if (userSelect < 0) { userSelect = 2; }
				}
				if (code == KeyEvent.VK_DOWN) {
					game.soundHandler.playSound(0);
					userSelect++;
					if (userSelect > 2) { userSelect = 0; }
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
				if (userSelect == 0) {
					if (code == KeyEvent.VK_ENTER) {
						game.ui.ipAddress = game.ui.ipAddress.trim();
						// if user leave it empty, the user will enter his/her own ip address
						if (game.ui.ipAddress.isEmpty() == true) { game.ui.ipAddress = "localhost"; }
						// go to "Enter your nickname" page
						game.ui.setTitleScreenState(4);
						isHost = false;

					}
					// if user wants to copy from keyboard
				} else if (userSelect == 1) {
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
				} else if (userSelect == 2) {
					if (code == KeyEvent.VK_ENTER) {
						game.ui.setTitleScreenState(3);
						userSelect = 0;
					}
				}
			}
		}
		// when in option screen
		if ((game.getGameState() == Game.waitState || game.getGameState() == Game.playState) && game.ui.isOptionScreenState() == true) {
			if (code == KeyEvent.VK_UP) {
				game.soundHandler.playSound(0);
				userSelect--;
				if (userSelect < 0) { userSelect = 2; }
			}
			if (code == KeyEvent.VK_DOWN) {
				game.soundHandler.playSound(0);
				userSelect++;
				if (userSelect > 2) { userSelect = 0; }
			}
			if (code == KeyEvent.VK_ENTER) {
				// back to game
				if (userSelect == 0) {
					game.ui.setOptionScreenState(false);
				}
				// back to main menu
				else if (userSelect == 1) {
					game.setGameState(Game.titleState);
					game.player.setPlayerState(Game.titleState);
					game.clearPlayers();
					game.ui.setTitleScreenState(0);
					userSelect = 0;
					new Pkt02Disconnect(game.player.getUsername()).sendData(game.socketClient);
				}
				// exit game
				else if (userSelect == 2) { System.exit(0); }
			}
			// press escape to go back to game
			if (code == KeyEvent.VK_ESCAPE) {
				game.soundHandler.playSound(0);
				game.ui.setOptionScreenState(false);
			}
		} else if (game.getGameState() == Game.waitState || game.getGameState() == Game.playState) {
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
				game.soundHandler.playSound(0);
				game.ui.setOptionScreenState(true);
				userSelect = 0;
				;
			}

			if (code == KeyEvent.VK_Q) { drop = false; }
			if (code == KeyEvent.VK_M) {
				map = !map;
				game.soundHandler.playSound(5);
			}

			if (code == KeyEvent.VK_1) { game.player.setPlayerWeapIndex(0); }
			if (code == KeyEvent.VK_2) { game.player.setPlayerWeapIndex(1); }
			if (code == KeyEvent.VK_3) { game.player.setPlayerWeapIndex(2); }
			if (code == KeyEvent.VK_4) { game.player.setPlayerWeapIndex(3); }

		}
		// when in end game screen
		if (game.getGameState() == Game.endState) {
			if (code == KeyEvent.VK_UP) {
				game.soundHandler.playSound(0);
				userSelect--;
				if (userSelect < 0) { userSelect = 1; }
			}
			if (code == KeyEvent.VK_DOWN) {
				game.soundHandler.playSound(0);
				userSelect++;
				if (userSelect > 1) { userSelect = 0; }
			}
			if (code == KeyEvent.VK_ENTER) {
				// back to lobby
				if (userSelect == 0) {
					new Pkt17BackToLobby(game.player.getUsername()).sendData(game.socketClient);
					// back to main menu
				} else if (userSelect == 1) {
					game.setGameState(Game.titleState);
					game.ui.setTitleScreenState(0);
					userSelect = 0;
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
			if (game.getGameState() == Game.titleState)
				return;
			game.socketClient.setLatency(System.currentTimeMillis());
			new Pkt08ServerPing().sendData(game.socketClient);
		}

	}

	// method to backspace a character when keying nickname
	public String removeLastChar(String str) {
		if (str != null && str.length() > 0) { str = str.substring(0, str.length() - 1); }
		System.out.println(str);
		return str;
	}

	// method to limit nickname and ip address length
	public String maxLength(String str, int max) {
		if (str.length() > max) { str = str.substring(0, max); }
		return str;
	}

	public boolean isUp() {
		return up;
	}

	public boolean isDown() {
		return down;
	}

	public boolean isLeft() {
		return left;
	}

	public boolean isRight() {
		return right;
	}

	public boolean isInteract() {
		return interact;
	}

	public boolean isDrop() {
		return drop;
	}

	public boolean isPing() {
		return ping;
	}

	public boolean isMap() {
		return map;
	}

	public void setInteract(boolean interact) {
		this.interact = interact;
	}

	public void setDrop(boolean drop) {
		this.drop = drop;
	}

	public int getUserSelect() {
		return userSelect;
	}

	public void setUserSelect(int userSelect) {
		this.userSelect = userSelect;
	}

}
