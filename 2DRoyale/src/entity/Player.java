package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Game;
import main.KeyHandler;
import main.MouseHandler;
import net.Packet;

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
		
		this.solidArea = new Rectangle(10,10,12,12);		

		setDefaultValues();
		getPlayerImage();
	}

	private void setDefaultValues() {
		worldX = game.tileSize * 23 + ((int) (Math.random() * (25 + 25 + 1)) - 25)*4;
		worldY = game.tileSize * 21 + ((int) (Math.random() * (25 + 25 + 1)) - 25)*4;

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
				int xa = 0;
				int ya = 0;
				
				if (keys.up == true) ya -= 1;
				if (keys.down == true) ya += 1;
				if (keys.left == true) xa -= 1;
				if (keys.right == true) xa += 1;
				
				move(xa, ya);
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

	private void move(int xa, int ya) {
		if(xa != 0 && ya != 0) {
			move(xa, 0);
			move(0, ya);
			return;
		}
		if(!hasCollided(xa, ya)) {			
			worldX += xa * speed;
			worldY += ya * speed;
			Packet movePacket = new Packet(3, this.username, this.worldX, this.worldY);
			game.socketClient.sendData(movePacket.getPacket());
		}
	}

	private boolean hasCollided(int xa, int ya) {
		int entityLeftWorldX = worldX + solidArea.x;
		int entityRightWorldX = worldX + solidArea.x + solidArea.width;
		int entityTopWorldY = worldY + solidArea.y;
		int entityBottomWorldY = worldY + solidArea.y + solidArea.height;
		
		int entityLeftCol = entityLeftWorldX/game.tileSize;
		int entityRightCol = entityRightWorldX/game.tileSize;
		int entityTopRow = entityTopWorldY/game.tileSize;
		int entityBottomRow = entityBottomWorldY/game.tileSize;
		
		int tileNum1, tileNum2;
		
		if (ya < 0) { //UP
			entityTopRow = (entityTopWorldY - speed)/game.tileSize;
			tileNum1 = game.tileM.mapTileNum[entityLeftCol][entityTopRow];
			tileNum2 = game.tileM.mapTileNum[entityRightCol][entityTopRow];
			if (game.tileM.tile[tileNum1].collision|| game.tileM.tile[tileNum2].collision)
				return true;
		}
		if (ya > 0) { //DOWN
			entityBottomRow = (entityBottomWorldY + speed)/game.tileSize;
			tileNum1 = game.tileM.mapTileNum[entityLeftCol][entityBottomRow];
			tileNum2 = game.tileM.mapTileNum[entityRightCol][entityBottomRow];
			if (game.tileM.tile[tileNum1].collision|| game.tileM.tile[tileNum2].collision)
				return true;
		}
		if (xa < 0) { //LEFT
			entityLeftCol = (entityLeftWorldX - speed)/game.tileSize;
			tileNum1 = game.tileM.mapTileNum[entityLeftCol][entityTopRow];
			tileNum2 = game.tileM.mapTileNum[entityLeftCol][entityBottomRow];
			if (game.tileM.tile[tileNum1].collision|| game.tileM.tile[tileNum2].collision)
				return true;
		}
		if (xa > 0) { //RIGHT
			entityRightCol = (entityRightWorldX + speed)/game.tileSize;
			tileNum1 = game.tileM.mapTileNum[entityRightCol][entityTopRow];
			tileNum2 = game.tileM.mapTileNum[entityRightCol][entityBottomRow];
			if (game.tileM.tile[tileNum1].collision|| game.tileM.tile[tileNum2].collision)
				return true;
		}
		
		return false;
	}

	public void updateXY(int worldX, int worldY) {
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
