package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import item.Rifle;
import item.SMG;
import item.Shotgun;
import item.SuperWeapon;
import main.Game;
import main.KeyHandler;
import main.MouseHandler;
import net.Pkt03Move;
import net.Pkt04MouseMove;

public class Player extends Entity implements Cloneable{ // inherits Entity class

	private Game game;
	private KeyHandler keys;
	private MouseHandler mouse;

	public final int screenX;
	public final int screenY;
	public double mouseX;
	public double mouseY;

	protected String username;
	public boolean isLocal;

	private double imageAngleRad = 0;
	private BufferedImage playerHand;
	
	public ArrayList<SuperWeapon> playerWeap;
	public int playerWeapIndex = -1;
	public int health;

	public Player(Game game, KeyHandler keys, MouseHandler mouse, String username, boolean isLocal) {
		this.game = game;
		this.keys = keys;
		this.mouse = mouse;
		this.username = username;
		this.isLocal = isLocal;

		this.screenX = game.screen.screenWidth / 2 - game.playerSize / 2;
		this.screenY = game.screen.screenHeight / 2 - game.playerSize / 2;

		this.solidArea = new Rectangle(6, 6, 12, 12);
		
		this.playerWeap = new ArrayList<SuperWeapon>();

		
		this.health = 100;
		this.speed = 4;

		setDefaultValues();
		getPlayerImage();
	}

	private void setDefaultValues() {
		worldX = game.tileSize * 23 + ((int) (Math.random() * (25 + 25 + 1)) - 25) * 4;
		worldY = game.tileSize * 21 + ((int) (Math.random() * (25 + 25 + 1)) - 25) * 4;

		
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

	public void addWeapon() {
		playerWeap.add(new Rifle(this, game));
		playerWeap.add(new SMG(this, game));
		playerWeap.add(new Shotgun(this, game));
	}
	
	public void setUsername(String username) {
		this.username = username;
		
	}

	public String getUsername() {
		return username;
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

				for (int i = 0; i < speed; i++)
					move(xa, ya);
				
				withinRange(xa, ya);
				
				Pkt03Move movePacket = new Pkt03Move(this.username, this.worldX, this.worldY);
				movePacket.sendData(game.socketClient);
			}
		}

		if (mouse != null) {
			if (mouseX != mouse.x || mouseY != mouse.y) {
				this.mouseX = mouse.x;
				this.mouseY = mouse.y;

				updateMouseDirection(mouse.x, mouse.y);
				Pkt04MouseMove mouseMovePacket = new Pkt04MouseMove(this.username, this.mouseX, this.mouseY);
				mouseMovePacket.sendData(game.socketClient);
			}
			if(mouse.mousePressed)
				if(playerWeapIndex >= 0)
					getWeapons().get(playerWeapIndex).shoot();
		}
		
		for(SuperWeapon weap: getWeapons())
			weap.update();
	}

	public void playerMouseScroll(int direction) {
		if(direction < 0 && playerWeapIndex >= 0) 
			playerWeapIndex--;
		else if (direction > 0 && playerWeapIndex < playerWeap.size() - 1)
			playerWeapIndex++;
	}
	
	private void move(int xa, int ya) {
		if (xa != 0 && ya != 0) {
			move(xa, 0);
			move(0, ya);
			return;
		}
		
		if (!hasCollided(xa, ya)) {			
			worldX += xa;
			worldY += ya;
		}
		
	}

	private boolean hasCollided(int xa, int ya) {
		int entityLeftWorldX = worldX + solidArea.x + xa;
		int entityRightWorldX = worldX + solidArea.x + solidArea.width + xa;
		int entityTopWorldY = worldY + solidArea.y + ya;
		int entityBottomWorldY = worldY + solidArea.y + solidArea.height + ya;

		if (game.tileM.hasCollidedWorld(xa, ya, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Entity"))
			return true;
		
		if (game.structM.hasCollidedBuilding(xa, ya, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Entity"))
			return true;	
		
		if (game.structM.hasCollidedCrate(xa, ya, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Entity"))
			return true;

		return false;
	}
	
	private void withinRange(int xa, int ya) {
		int entityLeftWorldX = worldX + solidArea.x + xa;
		int entityRightWorldX = worldX + solidArea.x + solidArea.width + xa;
		int entityTopWorldY = worldY + solidArea.y + ya;
		int entityBottomWorldY = worldY + solidArea.y + solidArea.height + ya;
		
		if (game.structM.withinCrateRange(xa, ya, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY))
			System.out.println("Near Crate!");
	}

	public void updatePlayerXY(int worldX, int worldY) {
		this.worldX = worldX;
		this.worldY = worldY;
	}

	public void updateMouseDirection(double x, double y) {
		double dx = x - screenX;
		double dy = y - screenY;
		this.imageAngleRad = Math.atan2(dy, dx);
	}
	
	public synchronized List<SuperWeapon> getWeapons() {
		return playerWeap;
	}

	public void render(Graphics2D g2) {
		
		BufferedImage holding;		
		int handOffset;		

		if(playerWeapIndex < 0) {
			holding = playerHand;
			handOffset = -4;
		} else {
			holding = getWeapons().get(playerWeapIndex).sprite; // This will be replaced by the img of the weapon the player is holding
			handOffset = getWeapons().get(playerWeapIndex).imgOffset;
		}		
		
		int x, y;
		int handX, handY;		

		if (!isLocal) {
			x = worldX - game.player.worldX + game.player.screenX;
			y = worldY - game.player.worldY + game.player.screenY;
			handX = worldX - game.player.worldX + game.screen.screenWidth / 2 - holding.getWidth() / 2;
			handY = worldY - game.player.worldY + game.screen.screenHeight / 2 - holding.getHeight() / 2;
		} else {
			x = screenX;
			y = screenY;
			handX = game.screen.screenWidth / 2 - holding.getWidth() / 2;
			handY = game.screen.screenHeight / 2 - holding.getHeight() / 2;
		}

		AffineTransform t = new AffineTransform();
		t.setToTranslation(handX, handY);
		t.rotate(imageAngleRad, holding.getWidth() / 2, holding.getHeight() / 2);
		t.translate(game.playerSize / 2 + holding.getWidth() / 2 + handOffset, 0);

		g2.drawImage(holding, t, null); // Draw hand (weapons)
		g2.drawImage(sprite, x, y, game.playerSize, game.playerSize, null); // Draw player

	}
	
	public void renderBullets(Graphics2D g2) {
		for(SuperWeapon weap: getWeapons())
			weap.render(g2);
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException{
		Player cloned = (Player) super.clone();
		cloned.playerWeap = new ArrayList<SuperWeapon>();
		return cloned;
	}

}
