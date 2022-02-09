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
import net.Pkt10PickupWeapon;
import net.Pkt11CrateOpen;
import structure.Crate;

public class Player extends Entity { // inherits Entity class

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

	public SuperWeapon[] playerWeap;
	public int playerWeapIndex = 0;
	public double health;

	public Player(Game game, KeyHandler keys, MouseHandler mouse, String username, boolean isLocal) {
		this.game = game;
		this.keys = keys;
		this.mouse = mouse;
		this.username = username;
		this.isLocal = isLocal;

		this.screenX = game.screen.screenWidth / 2 - game.playerSize / 2;
		this.screenY = game.screen.screenHeight / 2 - game.playerSize / 2;

		this.entityArea = new Rectangle(6, 6, 12, 12);

		this.playerWeap = new SuperWeapon[4];

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

	public void addWeapon(int playerWeapIndex, int weapType, int weapId) {

		try {

			SuperWeapon newWeap;
			newWeap = (SuperWeapon) game.itemM.weaponsArr[weapType].clone();
			newWeap.id = weapId;
			newWeap.player = this;

			playerWeap[playerWeapIndex] = newWeap;

		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}

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

				if (keys.up == true)
					ya -= 1;
				if (keys.down == true)
					ya += 1;
				if (keys.left == true)
					xa -= 1;
				if (keys.right == true)
					xa += 1;

				for (int i = 0; i < speed; i++)
					move(xa, ya);

				Pkt03Move movePacket = new Pkt03Move(this.username, this.worldX, this.worldY);
				movePacket.sendData(game.socketClient);
			}
			if (keys.interact == true) {
				withinRange();
				keys.interact = false;
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
			if (mouse.mousePressed)
				if (playerWeapIndex >= 0)
					if (getWeapons()[playerWeapIndex] != null)
						getWeapons()[playerWeapIndex].shoot();
		}

		for (SuperWeapon weap : getWeapons())
			if (weap != null)
				weap.update();

	}

	public void playerMouseScroll(int direction) {
		if (direction < 0 && playerWeapIndex > 0)
			playerWeapIndex--;
		else if (direction > 0 && playerWeapIndex < playerWeap.length - 1)
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
		int entityLeftWorldX = worldX + entityArea.x + xa;
		int entityRightWorldX = worldX + entityArea.x + entityArea.width + xa;
		int entityTopWorldY = worldY + entityArea.y + ya;
		int entityBottomWorldY = worldY + entityArea.y + entityArea.height + ya;

		if (game.tileM.hasCollidedWorld(xa, ya, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Entity"))
			return true;

		if (game.structM.hasCollidedBuilding(xa, ya, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Entity"))
			return true;

		if (game.structM.hasCollidedCrate(xa, ya, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Entity"))
			return true;

		return false;
	}

	private void withinRange() {
		int entityLeftWorldX = worldX + entityArea.x;
		int entityRightWorldX = worldX + entityArea.x + entityArea.width;
		int entityTopWorldY = worldY + entityArea.y;
		int entityBottomWorldY = worldY + entityArea.y + entityArea.height;

		SuperWeapon weapon = game.itemM.withinWeaponsRange(entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY);
		
		if (weapon != null) {
			Pkt10PickupWeapon pickUpPacket = new Pkt10PickupWeapon(username, playerWeapIndex, weapon.typeId, weapon.id);
			pickUpPacket.sendData(game.socketClient);
			return;
		}
		
		int crateIndex = game.structM.withinCrateRange(entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY);
		if (crateIndex != -1) {
			Pkt11CrateOpen crateOpenPacket = new Pkt11CrateOpen(username, crateIndex);
			crateOpenPacket.sendData(game.socketClient);
			return;
		}
	}
	
	public void updatePlayerHP(double health) {
		this.health += health;
		if(this.health < 0)
			this.health = 0;
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

	public synchronized SuperWeapon[] getWeapons() {
		return playerWeap;
	}

	public void render(Graphics2D g2) {

		BufferedImage holding;
		int handOffset;

		SuperWeapon sp = getWeapons()[playerWeapIndex];
		if (sp != null) {
			holding = sp.sprite; // This will be replaced by the img of the weapon the player is holding
			handOffset = sp.imgOffset;
		} else {
			holding = playerHand;
			handOffset = -4;
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
		for (SuperWeapon weap : getWeapons())
			if (weap != null)
				weap.render(g2);
	}

}
