package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import net.Pkt12DropWeapon;
import net.Pkt20GasDamage;
import structure.Crate;

public class Player extends Entity { // inherits Entity class

	private Game game;
	private KeyHandler keys;
	private MouseHandler mouse;
	public int playerState;

	public final int playerOffset = 12;
	public final int screenX;
	public final int screenY;
	public double mouseX;
	public double mouseY;

	protected String username;
	public boolean isLocal;
	public boolean freeze;

	private double imageAngleRad = 0;
	private BufferedImage playerHand;

	public SuperWeapon[] playerWeap;
	public int playerWeapIndex;
	public double health;

	public Player(Game game, KeyHandler keys, MouseHandler mouse, String username, boolean isLocal) {
		this.game = game;
		this.keys = keys;
		this.mouse = mouse;
		this.username = username;
		this.isLocal = isLocal;
		this.playerState = game.waitState;

		this.screenX = game.screen.screenWidth / 2 - game.playerSize / 2;
		this.screenY = game.screen.screenHeight / 2 - game.playerSize / 2;

		this.entityArea = new Rectangle(6, 6, 12, 12);

		this.playerWeapIndex = 0;
		this.speed = 4;
		this.mouseX = 0;
		this.mouseY = 0;

		this.freeze = false;

		setPlayerDefault();
		getPlayerImage();
	}

	public void setPlayerDefault() {
		this.playerState = game.waitState;
		this.playerWeap = new SuperWeapon[4];
		this.health = 100;
	}

	public void generatePlayerXY() {
		int failedPlayerAttempts = 0;

		mainLoop: while (true) {

			int randomX = playerOffset + (int) (Math.random() * (game.worldWidth - entityArea.width - (playerOffset * 2)));
			int randomY = playerOffset + (int) (Math.random() * (game.worldHeight - entityArea.height - (playerOffset * 2)));

			Rectangle separationHitbox = new Rectangle(randomX - playerOffset, randomY - playerOffset, entityArea.width + playerOffset * 2, entityArea.height + playerOffset * 2);

			// Spawn player on solid blocks
			int randomTopLeftTileX = separationHitbox.x / game.tileSize;
			int randomTopLeftTileY = separationHitbox.y / game.tileSize;
			int randomTopRightTileX = (separationHitbox.x + separationHitbox.width) / game.tileSize;
			int randomTopRightTileY = separationHitbox.y / game.tileSize;
			int randomBottomLeftTileX = randomX / game.tileSize;
			int randomBottomLeftTileY = (separationHitbox.y + separationHitbox.height) / game.tileSize;
			int randomBottomRightTileX = (separationHitbox.x + separationHitbox.width) / game.tileSize;
			int randomBottomRightTileY = (separationHitbox.y + separationHitbox.height) / game.tileSize;

			if (game.tileM.tile[game.tileM.mapTileNum[randomTopLeftTileX][randomTopLeftTileY][0]].collisionPlayer
					|| game.tileM.tile[game.tileM.mapTileNum[randomTopRightTileX][randomTopRightTileY][0]].collisionPlayer
					|| game.tileM.tile[game.tileM.mapTileNum[randomBottomLeftTileX][randomBottomLeftTileY][0]].collisionPlayer
					|| game.tileM.tile[game.tileM.mapTileNum[randomBottomRightTileX][randomBottomRightTileY][0]].collisionPlayer) {
				failedPlayerAttempts++;
				continue mainLoop;
			}

			// Prevent player from spawning in buildings
			for (int i = 0; i < game.structM.buildings.length; i++) {
				if (separationHitbox.x < game.structM.buildings[i].boundingBox.x + game.structM.buildings[i].boundingBox.width
						&& separationHitbox.x + separationHitbox.width > game.structM.buildings[i].boundingBox.x
						&& separationHitbox.y < game.structM.buildings[i].boundingBox.y + game.structM.buildings[i].boundingBox.height
						&& separationHitbox.y + separationHitbox.height > game.structM.buildings[i].boundingBox.y) {
					failedPlayerAttempts++;
					continue mainLoop;
				}
			}
			
			// Prevent player from spawning in crates
			for (int i = 0; i < game.structM.crates.size(); i++) {
				if (separationHitbox.x < game.structM.crates.get(i).collisionBoundingBox.x + game.structM.crates.get(i).collisionBoundingBox.width
						&& separationHitbox.x + separationHitbox.width > game.structM.crates.get(i).collisionBoundingBox.x
						&& separationHitbox.y < game.structM.crates.get(i).collisionBoundingBox.y + game.structM.crates.get(i).collisionBoundingBox.height
						&& separationHitbox.y + separationHitbox.height > game.structM.crates.get(i).collisionBoundingBox.y) {
					failedPlayerAttempts++;
					continue mainLoop;
				}
			}

			// Prevent player from spawning in obstructions
			for (int i = 0; i < game.structM.obstructions.length; i++) {
				if (separationHitbox.x < game.structM.obstructions[i].boundingBox.x + game.structM.obstructions[i].boundingBox.width
						&& separationHitbox.x + separationHitbox.width > game.structM.obstructions[i].boundingBox.x
						&& separationHitbox.y < game.structM.obstructions[i].boundingBox.y + game.structM.obstructions[i].boundingBox.height
						&& separationHitbox.y + separationHitbox.height > game.structM.obstructions[i].boundingBox.y) {
					failedPlayerAttempts++;
					continue mainLoop;
				}
			}

			worldX = randomX;
			worldY = randomY;
			break;
		}

		System.out.println("Player collisions: " + failedPlayerAttempts);
		Pkt03Move movePacket = new Pkt03Move(this.username, this.worldX, this.worldY);
		movePacket.sendData(game.socketClient);
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

	public void dropWeapon(int playerWeapIndex) {
		playerWeap[playerWeapIndex] = null;
	}

	public void setUsername(String username) {
		this.username = username;

	}

	public String getUsername() {
		return username;
	}

	public void update() {

		if (keys != null) {
			if ((keys.up == true || keys.down == true || keys.left == true || keys.right == true) && !freeze) {
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
			if (keys.drop == true) {
				if (playerWeap[playerWeapIndex] != null) {
					SuperWeapon dropWeap = playerWeap[playerWeapIndex];
					Pkt12DropWeapon dropPacket = new Pkt12DropWeapon(username, playerWeapIndex, dropWeap.typeId, dropWeap.id,
							this.worldX - dropWeap.imgIconWidth / 2 + game.playerSize / 2, this.worldY - dropWeap.imgIconHeight / 2 + game.playerSize / 2);
					dropPacket.sendData(game.socketClient);
				}
				keys.drop = false;
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
		
		if(game.tileM.withinGas(worldX, worldX + game.playerSize, worldY, worldY + game.playerSize)){
			Pkt20GasDamage gasDamage = new Pkt20GasDamage(this.getUsername());
			gasDamage.sendData(game.socketClient);
		}

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

		if (game.structM.hasCollidedObstruction(xa, ya, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Entity"))
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
			if (playerWeap[playerWeapIndex] != null) {
				SuperWeapon dropWeap = playerWeap[playerWeapIndex];
				Pkt12DropWeapon dropPacket = new Pkt12DropWeapon(username, playerWeapIndex, dropWeap.typeId, dropWeap.id, weapon.worldX, weapon.worldY);
				dropPacket.sendData(game.socketClient);
			}
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
		if (this.health < 0)
			this.health = 0;
	}

	public double getPlayerHP() {
		return this.health;
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
		
		g2.setColor(new Color(255,0,30));
		g2.fillRect(x - game.tileSize/4, y - 15, (int) ((this.health))/2, 10);
		g2.setColor(Color.white);
		g2.drawString(this.getUsername(), x, y + 40);
	}

	public void renderBullets(Graphics2D g2) {
		for (SuperWeapon weap : getWeapons())
			if (weap != null)
				weap.render(g2);
	}

}
