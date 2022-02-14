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
	public final int playerOffset;
	public final int screenX;
	public final int screenY;
	public double mouseX;
	public double mouseY;

	protected String username;
	public boolean isLocal;
	public boolean freeze;

	private double imageAngleRad = 0;
	private BufferedImage playerHand;

	protected SuperWeapon[] playerWeap;
	public int playerWeapIndex;
	public double health;

	public Player(Game game, KeyHandler keys, MouseHandler mouse, String username, boolean isLocal) {
		this.game = game;
		this.keys = keys;
		this.mouse = mouse;

		this.playerState = game.waitState;
		this.playerOffset = 12;
		this.screenX = game.screen.screenWidth / 2 - game.playerSize / 2;
		this.screenY = game.screen.screenHeight / 2 - game.playerSize / 2;
		this.mouseX = 0;
		this.mouseY = 0;

		this.username = username;
		this.isLocal = isLocal;
		this.freeze = false;

		this.playerWeapIndex = 0;

		this.speed = 5;
		this.entityArea = new Rectangle(6, 6, 12, 12);

		setPlayerDefault();
		getPlayerImage();
	}

	// Init images
	private void getPlayerImage() {
		try {
			this.sprite = ImageIO.read(getClass().getResourceAsStream("/player/player.png"));
			this.playerHand = ImageIO.read(getClass().getResourceAsStream("/player/hand.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Refresh player to a new state
	public void setPlayerDefault() {
		this.playerState = game.waitState;
		this.playerWeap = new SuperWeapon[4];
		this.health = 100;
	}

	// Generate player (also prevents player from spawning in structures/solid tile)
	public void generatePlayerXY() {
		int failedPlayerAttempts = 0;

		mainLoop: while (true) {

			int randomX = playerOffset + (int) (Math.random() * (game.worldWidth - entityArea.width - (playerOffset * 2)));
			int randomY = playerOffset + (int) (Math.random() * (game.worldHeight - entityArea.height - (playerOffset * 2)));

			Rectangle separationHitbox = new Rectangle(randomX - playerOffset, randomY - playerOffset, entityArea.width + playerOffset * 2, entityArea.height + playerOffset * 2);

			// Prevent player from spawning in unpassable tiles (eg. walls, water)
			int randomTopLeftTileX = separationHitbox.x / game.tileSize;
			int randomTopLeftTileY = separationHitbox.y / game.tileSize;
			int randomTopRightTileX = (separationHitbox.x + separationHitbox.width) / game.tileSize;
			int randomTopRightTileY = separationHitbox.y / game.tileSize;
			int randomBottomLeftTileX = randomX / game.tileSize;
			int randomBottomLeftTileY = (separationHitbox.y + separationHitbox.height) / game.tileSize;
			int randomBottomRightTileX = (separationHitbox.x + separationHitbox.width) / game.tileSize;
			int randomBottomRightTileY = (separationHitbox.y + separationHitbox.height) / game.tileSize;

			if (game.tileM.mapTileNum[randomTopLeftTileX][randomTopLeftTileY].tile.collisionPlayer
					|| game.tileM.mapTileNum[randomTopRightTileX][randomTopRightTileY].tile.collisionPlayer
					|| game.tileM.mapTileNum[randomBottomLeftTileX][randomBottomLeftTileY].tile.collisionPlayer
					|| game.tileM.mapTileNum[randomBottomRightTileX][randomBottomRightTileY].tile.collisionPlayer) {
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

			// Commit random values
			worldX = randomX;
			worldY = randomY;
			break;
		}

		System.out.println("Player collisions: " + failedPlayerAttempts);
		// Tell server of the new player position
		new Pkt03Move(this.username, this.worldX, this.worldY).sendData(game.socketClient);
	}

	// Add a weapon to user inventory
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

	// Remove weapon from user inventory
	public void dropWeapon(int playerWeapIndex) {
		playerWeap[playerWeapIndex] = null;
	}

	public void update() {

		// Check keyboard inputs
		if (keys != null) {
			if ((keys.up == true || keys.down == true || keys.left == true || keys.right == true) && !freeze) {
				int xa = 0;
				int ya = 0;

				// Read keyboard inputs
				if (keys.up == true)
					ya -= 1;
				if (keys.down == true)
					ya += 1;
				if (keys.left == true)
					xa -= 1;
				if (keys.right == true)
					xa += 1;

				if (!(xa == 0 && ya == 0)) {
					// Move player
					for (int i = 0; i < speed; i++)
						move(xa, ya);

					// Update new position to server
					new Pkt03Move(this.username, this.worldX, this.worldY).sendData(game.socketClient);
				}
			}

			if (keys.interact == true) {
				// Handle "F" key input to check if there's any within range events
				withinRange();
				keys.interact = false;
			}
			if (keys.drop == true) {
				// Handle "Q" key input to drop player weapon
				if (playerWeap[playerWeapIndex] != null) {
					SuperWeapon dropWeap = playerWeap[playerWeapIndex];
					// Update weapon drop to server
					new Pkt12DropWeapon(username, playerWeapIndex, dropWeap.typeId, dropWeap.id, this.worldX - dropWeap.imgIconWidth / 2 + game.playerSize / 2,
							this.worldY - dropWeap.imgIconHeight / 2 + game.playerSize / 2).sendData(game.socketClient);
					game.playSE(9);
				}
				keys.drop = false;
			}

		}
		// Check mouse inputs
		if (mouse != null) {
			// Check mouse movement
			if (mouseX != mouse.x || mouseY != mouse.y) {
				this.mouseX = mouse.x;
				this.mouseY = mouse.y;

				updateMouseDirection(mouse.x, mouse.y);
				// Update mouse direction to server
				new Pkt04MouseMove(this.username, this.mouseX, this.mouseY).sendData(game.socketClient);
			}
			// Check mouse clicks
			if (mouse.mousePressed)
				// Handle shoot event
				if (playerWeapIndex >= 0)
					if (getWeapons()[playerWeapIndex] != null)
						getWeapons()[playerWeapIndex].shoot();
		}

		// Update bullets in each weapon
		for (SuperWeapon weap : getWeapons())
			if (weap != null)
				weap.update();

		// Check if user is in gas
		if (game.tileM.withinGas(worldX, worldX + game.playerSize, worldY, worldY + game.playerSize))
			new Pkt20GasDamage(this.getUsername()).sendData(game.socketClient);

	}

	// Move event
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

	// Handle within range events
	private void withinRange() {
		int entityLeftWorldX = worldX + entityArea.x;
		int entityRightWorldX = worldX + entityArea.x + entityArea.width;
		int entityTopWorldY = worldY + entityArea.y;
		int entityBottomWorldY = worldY + entityArea.y + entityArea.height;

		// Check if weapons are in range to pick up
		SuperWeapon weapon = game.itemM.withinWeaponsRange(entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY);
		if (weapon != null) {
			// If player is holding a weapon, swap with the one the player is picking up
			if (playerWeap[playerWeapIndex] != null) {
				SuperWeapon dropWeap = playerWeap[playerWeapIndex];
				new Pkt12DropWeapon(username, playerWeapIndex, dropWeap.typeId, dropWeap.id, weapon.worldX, weapon.worldY).sendData(game.socketClient);
				game.playSE(9);
			}
			new Pkt10PickupWeapon(username, playerWeapIndex, weapon.typeId, weapon.id).sendData(game.socketClient);
			game.playSE(8);
			return;
		}

		// Check if crates are in range to open
		int crateIndex = game.structM.withinCrateRange(entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY);
		if (crateIndex != -1) {
			new Pkt11CrateOpen(username, crateIndex).sendData(game.socketClient);
			game.playSE(7);
			return;
		}
	}

	public void updateMouseDirection(double x, double y) {
		double dx = x - screenX;
		double dy = y - screenY;
		this.imageAngleRad = Math.atan2(dy, dx);
	}

	public synchronized SuperWeapon[] getWeapons() {
		return playerWeap;
	}

	////////// RENDER FUNCTIONS //////////
	public void render(Graphics2D g2) {

		BufferedImage holding;
		int handOffset;

		SuperWeapon sp = getWeapons()[playerWeapIndex];
		// If player holding weap, render weap. If player holding nothing, render his
		// hand
		if (sp != null) {
			holding = sp.sprite;
			handOffset = sp.imgOffset;
		} else {
			holding = playerHand;
			handOffset = -4;
		}

		int x, y;
		int handX, handY;

		// If player is himself, render him in the middle of the screen, else render
		// other players relative to the main player
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

		// Rotate weapon/hand to mouse
		AffineTransform t = new AffineTransform();
		t.setToTranslation(handX, handY);
		t.rotate(imageAngleRad, holding.getWidth() / 2, holding.getHeight() / 2);
		t.translate(game.playerSize / 2 + holding.getWidth() / 2 + handOffset, 0);

		g2.drawImage(holding, t, null); // Draw hand (weapons)
		g2.drawImage(sprite, x, y, game.playerSize, game.playerSize, null); // Draw player

		g2.setColor(new Color(255, 0, 30));
		g2.fillRect(x - game.tileSize / 4, y - 15, (int) ((this.health)) / 2, 10);
		g2.setColor(Color.white);
		g2.drawString(this.getUsername(), x, y + 40);
	}

	public void renderBullets(Graphics2D g2) {
		for (SuperWeapon weap : getWeapons())
			if (weap != null)
				weap.render(g2);
	}

	////////// SERVER AND CLIENT FUNCTIONS //////////
	public void playerMouseScroll(int direction) {
		if (direction < 0 && playerWeapIndex > 0)
			playerWeapIndex--;
		else if (direction > 0 && playerWeapIndex < playerWeap.length - 1)
			playerWeapIndex++;
	}

	public void updatePlayerHP(double health) {
		this.health += health;
		if (this.health < 0)
			this.health = 0;
	}

	public void updatePlayerXY(int worldX, int worldY) {
		this.worldX = worldX;
		this.worldY = worldY;
	}

	////////// GETTERS AND SETTERS //////////
	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public double getPlayerHP() {
		return this.health;
	}

}
