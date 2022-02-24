package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import entity.PlayerMP;
import item.SuperWeapon;

public class UI implements RenderInterface{

	private Game game;
	private Graphics2D g2;
	private Font maruMonica;
	private ArrayList<String> message = new ArrayList<>();
	private ArrayList<Integer> messageCounter = new ArrayList<>();

	private static final int textSpacing = 48;
	private static final int selectSpacing = 32;
	private static final int imgSize = 48;

	private String name;
	private String ipAddress;

	private int titleScreenState;
	private boolean optionScreenState;
	private boolean diagnosticState;

	private BufferedImage healthImage, killCounterImage, remainingPlayersImage;
	private int countdown;
	private int kills;
	private boolean win;
	private int waitingPlayerCount;
	private int playingPlayerCount;
	private int client_fps;
	private long ping;

	public UI(Game game) {
		this.game = game;
		this.titleScreenState = 0;
		this.name = "";
		this.ipAddress = "";
		this.kills = 0;

		try {
			// Get the font
			InputStream is = getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf");
			this.maruMonica = Font.createFont(Font.TRUETYPE_FONT, is);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}

		try {
			// Get the UI images
			this.healthImage = ImageIO.read(getClass().getResourceAsStream("/UI/HP.png"));
			this.killCounterImage = ImageIO.read(getClass().getResourceAsStream("/UI/killcounter.png"));
			this.remainingPlayersImage = ImageIO.read(getClass().getResourceAsStream("/UI/remainingplayers.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void update() {
		if (game.getGameState() == Game.endState)
			return;
		playingPlayerCount = waitingPlayerCount = 0;
		for (PlayerMP p : game.getPlayers()) {
			if (p.getPlayerState() == Game.playState)
				playingPlayerCount++;
			if (p.getPlayerState() == Game.waitState)
				waitingPlayerCount++;
		}
	}

	public void render(Graphics2D g2) {

		this.g2 = g2;
		g2.setFont(maruMonica);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setColor(Color.white);

		// Title State
		if (game.getGameState() == Game.titleState)
			drawTitleScreen();
		// Play State
		int playerCount;
		if (game.getGameState() == Game.waitState || game.getGameState() == Game.playState) {
			if (playingPlayerCount > 1)
				playerCount = playingPlayerCount;
			else
				playerCount = waitingPlayerCount;

			// Draw all UI
			drawHP();
			drawInventory();
			drawKillsStat();
			drawPlayerCount(playerCount);
			drawMessage();
			drawCountdown();
			if (diagnosticState)
				drawDiagnostics();
			if (optionScreenState)
				drawOption();
			// Start game message
			if (game.getGameState() == Game.waitState)
				drawWaitMessage();

		}
		// End state
		if (game.getGameState() == Game.endState)
			drawEndGame(win);

	}

	// Title Name
	public void drawTitleScreen() {
		// Main Screen when user opens the game
		int x, y;
		if (titleScreenState == 0) {

			g2.setColor(new Color(0, 0, 0));
			g2.fillRect(0, 0, game.screen.getScreenWidth(), game.screen.getScreenHeight());

			// How to navigate
			g2.setFont(g2.getFont().deriveFont(24F));
			String text = "[Up/Down Arrows] to navigate, [Enter] to enter.";
			x = 16;
			y = 32;
			g2.setColor(Color.white);
			g2.drawString(text, x, y);

			g2.setFont(g2.getFont().deriveFont(Font.BOLD, 96F));
			text = "2D Royale";
			x = getXforCenteredText(text);
			y = textSpacing * 3;

			// Shadow
			g2.setColor(Color.gray);
			g2.drawString(text, x + 5, y + 5);

			// Main Color
			g2.setColor(Color.white);
			g2.drawString(text, x, y);

			// Menu
			g2.setFont(g2.getFont().deriveFont(Font.BOLD, 48F));

			text = "START";
			x = getXforCenteredText(text);
			y += textSpacing * 4;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 0)
				g2.drawString(">", x - selectSpacing, y);

			text = "HOW TO PLAY";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 1)
				g2.drawString(">", x - selectSpacing, y);

			text = "PLAYERS CONTROL";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 2)
				g2.drawString(">", x - selectSpacing, y);

			text = "QUIT";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 3)
				g2.drawString(">", x - selectSpacing, y);

			// When user click on "HOW TO PLAY"
		} else if (titleScreenState == 1) {
			g2.setColor(Color.white);
			g2.setFont(g2.getFont().deriveFont(42F));

			String text = "OBJECTIVES";
			x = getXforCenteredText(text);
			y = textSpacing * 2;
			g2.drawString(text, x, y);

			g2.setFont(g2.getFont().deriveFont(32F));

			text = "You are spawned randomly in the world.";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "Open crates to obtain weapons.";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "Gas closes on the centre of the map. Best to stay out of it.";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "Kill everyone. Be the last man standing.";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			g2.setFont(g2.getFont().deriveFont(42F));

			text = "WEAPONS";
			x = getXforCenteredText(text);
			y += textSpacing * 2;
			g2.drawString(text, x, y);

			g2.setFont(g2.getFont().deriveFont(32F));

			text = "Shotgun: Fast firing weapon with extreme close quarters lethality.";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "Rifle: Well rounded automatic weapon.";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "SMG: Close quarters spray and pray weapon.";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "Sniper: Low fire rate, slow projectile speed, but massive damage.";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "Back to Main Menu";
			x = getXforCenteredText(text);
			y += textSpacing * 2;
			g2.drawString(text, x, y);
			g2.drawString(">", x - selectSpacing, y);

			// When user click on "PLAYERS CONTROL"
		} else if (titleScreenState == 2) {
			g2.setColor(Color.white);
			g2.setFont(g2.getFont().deriveFont(42F));

			String text = "CONTROLS";
			x = getXforCenteredText(text);
			y = textSpacing * 2;
			g2.drawString(text, x, y);

			g2.setFont(g2.getFont().deriveFont(32F));

			text = "[F] HOST: Start Game";
			x = getXforCenteredText(text);
			y += textSpacing * 2;
			g2.drawString(text, x, y);

			text = "[W][A][S][D] Character Movement";
			x = getXforCenteredText(text);
			y += textSpacing * 2;
			g2.drawString(text, x, y);

			text = "[E] Open Crates, Pick Up Weapons";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "[Q] Drop Held Weapon";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "[M] Toggle Minimap/Full-Sized Map";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "[Scroll] Change Weapons";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "[Mouse1] Aim and Shoot";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "[F3] Toggle Diagnostic Information";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			text = "Back to Main Menu";
			x = getXforCenteredText(text);
			y += textSpacing * 2;
			g2.drawString(text, x, y);
			g2.drawString(">", x - selectSpacing, y);

			// When user click on "START"
		} else if (titleScreenState == 3) {
			g2.setColor(Color.white);
			g2.setFont(g2.getFont().deriveFont(42F));

			// Ask the user if he/she wants to host the server
			String text = "Do you want to run the server?";
			x = getXforCenteredText(text);
			y = textSpacing * 3;
			g2.drawString(text, x, y);

			text = "YES";
			x = getXforCenteredText(text);
			y += textSpacing * 4;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 0)
				g2.drawString(">", x - selectSpacing, y);

			text = "NO";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 1)
				g2.drawString(">", x - selectSpacing, y);

			text = "Back";
			x = getXforCenteredText(text);
			y += textSpacing * 3;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 2)
				g2.drawString(">", x - selectSpacing, y);

			// Enter nickname screen
		} else if (titleScreenState == 4) {
			g2.setColor(Color.white);
			g2.setFont(g2.getFont().deriveFont(42F));

			String text = "Enter your nickname:";
			x = getXforCenteredText(text);
			y = textSpacing * 3;
			g2.drawString(text, x, y);

			g2.drawRect(textSpacing * 6, textSpacing * 5, game.screen.getScreenWidth() - textSpacing * 12, textSpacing * 2);
			x = getXforCenteredText(name);
			y += textSpacing * 3;
			g2.drawString(name, x, y);

			text = "Let's go";
			x = getXforCenteredText(text);
			y += textSpacing * 3;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 0)
				g2.drawString(">", x - selectSpacing, y);

			text = "Back";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 1)
				g2.drawString(">", x - selectSpacing, y);

			// Only shown when user does not wants to host the server, he/she will have to
			// provide the server's ip to join
		} else if (titleScreenState == 5) {
			g2.setColor(Color.white);
			g2.setFont(g2.getFont().deriveFont(42F));

			String text = "Enter server IP:";
			x = getXforCenteredText(text);
			y = textSpacing * 3;
			g2.drawString(text, x, y);

			g2.setFont(g2.getFont().deriveFont(24F));

			text = "(empty for localhost)";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);

			g2.setFont(g2.getFont().deriveFont(42F));

			g2.drawRect(textSpacing * 6, textSpacing * 5, game.screen.getScreenWidth() - textSpacing * 12, textSpacing * 2);
			x = getXforCenteredText(ipAddress);
			y += textSpacing * 2;
			g2.drawString(ipAddress, x, y);

			text = "Ok";
			x = getXforCenteredText(text);
			y += textSpacing * 3;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 0)
				g2.drawString(">", x - selectSpacing, y);

			text = "Copy From Keyboard";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 1)
				g2.drawString(">", x - selectSpacing, y);

			text = "Back";
			x = getXforCenteredText(text);
			y += textSpacing;
			g2.drawString(text, x, y);
			if (game.keys.getUserSelect() == 2)
				g2.drawString(">", x - selectSpacing, y);
		}

	}

	// function to align text in middle
	public int getXforCenteredText(String text) {
		int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
		int x = game.screen.getScreenWidth() / 2 - length / 2;
		return x;
	}

	// draw HP UI
	public void drawHP() {
		// display health logo
		g2.drawImage(healthImage, imgSize, imgSize * 12, 25, 25, null);
		// display health bar
		Color c = new Color(255, 0, 30);
		g2.setColor(c);
		g2.fillRect(imgSize * 2, imgSize * 12, (int) (game.player.getHealth() * 2), imgSize / 2);
	}

	// draw Inventory UI
	public void drawInventory() {

		// frame
		int frameX = imgSize;
		int frameY = imgSize * 5;
		int frameWidth = imgSize * 2;
		int frameHeight = imgSize * 5;
		drawSubWindow(frameX, frameY, frameWidth, frameHeight);

		// slot
		final int slotXstart = frameX + 24;
		final int slotYstart = frameY + 20;

		// draw player's items in inventory
		for (int i = 0; i < game.player.getWeapons().length; i++) {
			SuperWeapon weap = game.player.getWeapons()[i];
			if (weap != null)
				g2.drawImage(weap.getEntityImg(), slotXstart, slotYstart + imgSize * i + imgSize / 2 - weap.getImgIconHeight() / 2, 50, 20, null);
		}
		// cursor
		int cursorX = slotXstart;
		int cursorY = slotYstart + imgSize * (game.player.getPlayerWeapIndex());
		int cursorWidth = imgSize;
		int cursorHeight = imgSize;

		// draw cursor
		g2.setColor(Color.white);
		g2.setStroke(new BasicStroke(3));
		g2.drawRoundRect(cursorX, cursorY, cursorWidth, cursorHeight, 10, 10);
	}

	// draw sub translucent window
	public void drawSubWindow(int x, int y, int width, int height) {
		Color c = new Color(0, 0, 0, 100);
		g2.setColor(c);
		g2.fillRoundRect(x, y, width, height, 10, 10);
	}

	// draw kill count
	public void drawKillsStat() {
		int x = game.screen.getScreenWidth() - textSpacing * 4;
		int y = textSpacing;
		// make box
		drawSubWindow(x, y, imgSize * 3, imgSize);
		// draw knife
		g2.drawImage(killCounterImage, x + 8, y + 12, 25, 25, null);
		// draw string
		String text = ": " + this.kills;
		g2.setColor(Color.white);
		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18F));
		g2.drawString(text, x + 30, y + 30);

	}

	// draw player count
	public void drawPlayerCount(int playerCount) {

		int x = game.screen.getScreenWidth() - textSpacing * 4 + 85;
		int y = textSpacing + 14;
		// draw player count image
		g2.drawImage(remainingPlayersImage, x, y, 20, 20, null);
		// draw string
		String text = ": " + playerCount;
		g2.setColor(Color.white);
		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18F));
		g2.drawString(text, x + 22, y + 16);

	}

	// countdown timer interface
	public void drawCountdown() {
		if (game.getGameState() == Game.playState && countdown > 0) {

			String text = "Game Starting in " + countdown;
			int x = getXforCenteredText(text);
			int y = textSpacing * 3;
			g2.setFont(g2.getFont().deriveFont(Font.BOLD, 26F));
			g2.setColor(Color.black);
			g2.drawString(text, x + 2, y + 2);
			g2.setColor(Color.white);
			g2.drawString(text, x, y);

		}
	}

	// Show FPS, seed and ping
	private void drawDiagnostics() {
		String text = " FPS: " + client_fps + " SEED: " + game.getRandSeed() + " PING: " + ping + "ms";

		int length = (int) g2.getFontMetrics().getStringBounds(text, g2).getWidth();

		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18F));
		g2.setColor(Color.black);
		g2.drawString(text, game.screen.getScreenWidth() - length - 10 + 2, game.screen.getScreenHeight() - 10 + 2);
		g2.setColor(Color.white);
		g2.drawString(text, game.screen.getScreenWidth() - length - 10, game.screen.getScreenHeight() - 10);
	}

	// display option screen when esc is pressed
	public void drawOption() {
		int x = textSpacing * 6;
		int y = textSpacing * 2;
		// make box
		drawSubWindow(x, y, game.screen.getScreenWidth() - x * 2, game.screen.getScreenHeight() - y * 5);
		String text = "Back To Game";
		x = getXforCenteredText(text);
		y += textSpacing + 30;
		g2.setColor(Color.white);
		g2.drawString(text, x, y);
		if (game.keys.getUserSelect() == 0)
			g2.drawString(">", x - selectSpacing, y);

		text = "Back to Main Menu";
		x = getXforCenteredText(text);
		y += textSpacing;
		g2.drawString(text, x, y);
		if (game.keys.getUserSelect() == 1)
			g2.drawString(">", x - selectSpacing, y);

		text = "Quit Game";
		x = getXforCenteredText(text);
		y += textSpacing;
		g2.drawString(text, x, y);
		if (game.keys.getUserSelect() == 2)
			g2.drawString(">", x - selectSpacing, y);
	}

	// host message to start game
	public void drawWaitMessage() {
		String text;
		if (game.socketServer != null)
			text = "You are the host! Press F to start game!";
		else
			text = "Waiting for host to start game!";

		int x = getXforCenteredText(text);
		int y = textSpacing;
		g2.setColor(Color.black);
		g2.drawString(text, x + 2, y + 2);
		g2.setColor(Color.white);
		g2.drawString(text, x, y);
	}

	// for scrolling killing feed
	public void addMessage(String text) {
		message.add(text);
		messageCounter.add(0);
	}

	// also for scrolling kill feed
	public void drawMessage() {
		int messageX = game.screen.getScreenWidth() - textSpacing * 4;
		int messageY = textSpacing * 4;
		g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18F));

		for (int i = 0; i < message.size(); i++) {
			if (message.get(i) != null) {

				g2.setColor(Color.black);
				g2.drawString(message.get(i), messageX + 2, messageY + 2);
				g2.setColor(Color.white);
				g2.drawString(message.get(i), messageX, messageY);

				int counter = messageCounter.get(i) + 1; // messageCounter++
				messageCounter.set(i, counter); // set the counter to the array
				messageY += 50;

				if (messageCounter.get(i) > 180) {
					message.remove(i);
					messageCounter.remove(i);
				}
			}
		}
	}

	// end game screen
	public void drawEndGame(boolean win) {
		g2.setColor(Color.white);
		g2.setFont(g2.getFont().deriveFont(42F));
		String text;
		int x;
		int y;

		// if player wins
		if (win) {
			text = "You Win!";
			x = getXforCenteredText(text);
			y = textSpacing * 3;
			g2.drawString(text, x, y);
			// if player loses
		} else {
			text = "You Died!";
			x = getXforCenteredText(text);
			y = textSpacing * 3;
			g2.drawString(text, x, y);
		}

		// get kills from player
		text = "Number of Kills: " + this.kills;
		x = getXforCenteredText(text);
		y += textSpacing * 3;
		g2.drawString(text, x, y);

		// get position of player from the length of array
		text = "Position: #" + (this.playingPlayerCount);
		x = getXforCenteredText(text);
		y += textSpacing;
		g2.drawString(text, x, y);

		text = "Back to Lobby";
		x = getXforCenteredText(text);
		y += textSpacing * 3;
		g2.drawString(text, x, y);
		if (game.keys.getUserSelect() == 0)
			g2.drawString(">", x - selectSpacing, y);

		text = "Back to Main Menu";
		x = getXforCenteredText(text);
		y += textSpacing;
		g2.drawString(text, x, y);
		if (game.keys.getUserSelect() == 1)
			g2.drawString(">", x - selectSpacing, y);
	}

	public void nameRemoveLastChar() {
		if (name != null && name.length() > 0)
			name = name.substring(0, name.length() - 1);
	}

	public void nameAddChar(char input) {
		name += input;
		if (name.length() > 15)
			name = name.substring(0, 15);
	}
	
	public void ipRemoveLastChar() {
		if (ipAddress != null && ipAddress.length() > 0)
			ipAddress = ipAddress.substring(0, ipAddress.length() - 1);
	}

	public void ipAddChar(char input) {
		ipAddress += input;
		if (ipAddress.length() > 15)
			ipAddress = ipAddress.substring(0, 15);
	}

	public int getTitleScreenState() {
		return titleScreenState;
	}

	public void setTitleScreenState(int titleScreenState) {
		this.titleScreenState = titleScreenState;
	}

	public void setCountdown(int countdown) {
		this.countdown = countdown;
	}

	public boolean isOptionScreenState() {
		return optionScreenState;
	}

	public void setOptionScreenState(boolean optionScreenState) {
		this.optionScreenState = optionScreenState;
	}

	public void setDiagnosticState(boolean diagnosticState) {
		this.diagnosticState = diagnosticState;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public void incrementKills() {
		this.kills++;
	}

	public void setWin(boolean win) {
		this.win = win;
	}

	public void setClient_fps(int client_fps) {
		this.client_fps = client_fps;
	}

	public void setPing(long ping) {
		this.ping = ping;
	}
	
	public String getName() {
		name.trim();
		return name;
	}
	
	public String getIpAddress() {
		ipAddress.trim();
		return ipAddress;
	}
	
	public void setIpAddress(String ip) {
		if (ip.length() > 15)
			ip = ip.substring(0, 15);
		this.ipAddress = ip;		
	}

	public void setPlayingPlayerCount(int playingPlayerCount) {
		this.playingPlayerCount = playingPlayerCount;
	}

}
