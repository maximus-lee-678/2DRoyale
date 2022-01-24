package entity;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.KeyHandler;
import main.MouseHandler;
import net.Packet;
import main.Game;

public class Player extends Entity { // inherits Entity class

	private Game game;
	private KeyHandler keys;
	private MouseHandler mouse;

	public final int screenX;
	public final int screenY;
	private double mouseX;
	private double mouseY;

	protected String username;
	public boolean isLocal;

	private double imageAngleRad = 0;
	private BufferedImage playerHand;

	public Player(Game game, KeyHandler keys, MouseHandler mouse, String username, boolean isLocal) {
		this.game = game;
		this.keys = keys;
		this.mouse = mouse;
		this.username = username;
		this.isLocal = isLocal;

		this.screenX = game.screenWidth / 2 - game.playerSize / 2;
		this.screenY = game.screenHeight / 2 - game.playerSize / 2;

		setDefaultValues();
		getPlayerImage();
	}

	private void setDefaultValues() {
		worldX = game.tileSize * 23 + (int) (Math.random() * (100 + 100 + 1)) - 100;
		worldY = game.tileSize * 21 + (int) (Math.random() * (100 + 100 + 1)) - 100;
		speed = 4;
		mouseX = 0;
		mouseY = 0;
	}

	private void getPlayerImage() {
		try {
			this.sprite = ImageIO.read(getClass().getResourceAsStream("/player/player.png"));			
			this.playerHand = ImageIO.read(getClass().getResourceAsStream("/player/hand.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void update() {

		if (keys != null) {
			if (keys.up == true || keys.down == true || keys.left == true || keys.right == true) {
				if (keys.up == true) worldY -= speed;
				if (keys.down == true) worldY += speed;
				if (keys.left == true) worldX -= speed;
				if (keys.right == true) worldX += speed;
				Packet movePacket = new Packet(3, this.username, this.worldX, this.worldY);
				game.socketClient.sendData(movePacket.getPacket());
			}
		}

		if (mouse != null) {
			if(mouseX != mouse.x || mouseY != mouse.y) {
				this.mouseX = mouse.x;
				this.mouseY = mouse.y;
				
				updateDirection(mouse.x, mouse.y);
				Packet mousePacket = new Packet(4, this.username, this.mouseX, this.mouseY);
				game.socketClient.sendData(mousePacket.getPacket());
			}
			
		}

	}

	public void move(int worldX, int worldY) {
		this.worldX = worldX;
		this.worldY = worldY;
	}
	
	public void updateDirection(double x, double y) {
		double dx = x - screenX;
		double dy = y - screenY;
		this.imageAngleRad = Math.atan2(dy, dx);
	}

	public void render(Graphics2D g2) {
		
		BufferedImage holding = playerHand;	//This will be replaced by the img of the weapon the player is holding
		
		int x, y;
		int handX, handY;
		int handOffset = -5; //How close you want the image to be to the player. can play around this value. Should retrieve this value inside the weapon object in the future
				
		if (!isLocal) {
			x = worldX - game.player.worldX + game.player.screenX;
			y = worldY - game.player.worldY + game.player.screenY;
			handX = worldX - game.player.worldX + game.screenWidth / 2 - holding.getWidth()/2;
			handY = worldY - game.player.worldY + game.screenHeight / 2 - holding.getHeight()/2;
		} else {
			x = screenX;
			y = screenY;
			handX = game.screenWidth / 2 - holding.getWidth()/2;
			handY = game.screenHeight / 2 - holding.getHeight()/2;
			
		}		
		
		AffineTransform t = new AffineTransform();
		t.setToTranslation(handX, handY);
		t.rotate(imageAngleRad, holding.getWidth()/2, holding.getHeight()/2);        
        t.translate(game.playerSize/2 + holding.getWidth()/2 + handOffset, 0);
        
		g2.drawImage(holding, t, null); //Draw hand (weapons)
		g2.drawImage(sprite, x, y, game.playerSize, game.playerSize, null);	//Draw player

	}

}
