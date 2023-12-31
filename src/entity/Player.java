package entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import item.SuperWeapon;
import main.Game;
import main.KeyHandler;
import main.MouseHandler;
import main.RenderInterface;
import net.Pkt03Move;
import net.Pkt04MouseMove;
import net.Pkt10PickupWeapon;
import net.Pkt11CrateOpen;
import net.Pkt12DropWeapon;
import net.Pkt20GasDamage;

public class Player extends Entity implements RenderInterface { // inherits Entity class

	private Game game;
	private KeyHandler keys;
	private MouseHandler mouse;

	private int playerState;
	private final int playerOffset;
	private final int screenX;
	private final int screenY;
	private double mouseX;
	private double mouseY;

	protected String username;
	private boolean local;
	private boolean freeze;

	private double imageAngleRad;
	private BufferedImage playerHand;

	protected SuperWeapon[] playerWeap;
	protected int playerWeapIndex;
	private double health;

	public Player(Game game, KeyHandler keys, MouseHandler mouse, String username, boolean local) {
		this.game = game;
		this.keys = keys;
		this.mouse = mouse;

		this.setPlayerState(Game.waitState);
		this.playerOffset = 12;
		this.screenX = game.screen.getScreenWidth() / 2 - Game.playerSize / 2;
		this.screenY = game.screen.getScreenHeight() / 2 - Game.playerSize / 2;
		this.mouseX = 0;
		this.mouseY = 0;

		this.username = username;
		this.local = local;
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
		this.setPlayerState(Game.waitState);
		this.playerWeap = new SuperWeapon[4];
		this.health = 100;
	}

	// Generate player (also prevents player from spawning in structures/solid tile)
	public void generatePlayerXY() {
		int failedPlayerAttempts = 0;

		mainLoop: while (true) {

			int randomX = playerOffset + (int) (Math.random() * ((Game.tileSize * game.tileM.getMaxWorldCol()) - entityArea.width - (playerOffset * 2)));
			int randomY = playerOffset + (int) (Math.random() * ((Game.tileSize * game.tileM.getMaxWorldRow()) - entityArea.height - (playerOffset * 2)));

			Rectangle separationHitbox = new Rectangle(randomX - playerOffset, randomY - playerOffset, entityArea.width + playerOffset * 2, entityArea.height + playerOffset * 2);

			// Prevent player from spawning in unpassable tiles (eg. walls, water)
			int randomTopLeftTileX = separationHitbox.x / Game.tileSize;
			int randomTopLeftTileY = separationHitbox.y / Game.tileSize;
			int randomTopRightTileX = (separationHitbox.x + separationHitbox.width) / Game.tileSize;
			int randomTopRightTileY = separationHitbox.y / Game.tileSize;
			int randomBottomLeftTileX = randomX / Game.tileSize;
			int randomBottomLeftTileY = (separationHitbox.y + separationHitbox.height) / Game.tileSize;
			int randomBottomRightTileX = (separationHitbox.x + separationHitbox.width) / Game.tileSize;
			int randomBottomRightTileY = (separationHitbox.y + separationHitbox.height) / Game.tileSize;

			if (game.tileM.getMapTileData()[randomTopLeftTileX][randomTopLeftTileY].getTile().isCollisionPlayer()
					|| game.tileM.getMapTileData()[randomTopRightTileX][randomTopRightTileY].getTile().isCollisionPlayer()
					|| game.tileM.getMapTileData()[randomBottomLeftTileX][randomBottomLeftTileY].getTile().isCollisionPlayer()
					|| game.tileM.getMapTileData()[randomBottomRightTileX][randomBottomRightTileY].getTile().isCollisionPlayer()) {
				failedPlayerAttempts++;
				continue mainLoop;
			}

			// Prevent player from spawning in buildings
			for (int i = 0; i < game.structM.getBuildings().length; i++) {
				if (separationHitbox.x < game.structM.getBuildings()[i].getBoundingBox().x + game.structM.getBuildings()[i].getBoundingBox().width
						&& separationHitbox.x + separationHitbox.width > game.structM.getBuildings()[i].getBoundingBox().x
						&& separationHitbox.y < game.structM.getBuildings()[i].getBoundingBox().y + game.structM.getBuildings()[i].getBoundingBox().height
						&& separationHitbox.y + separationHitbox.height > game.structM.getBuildings()[i].getBoundingBox().y) {
					failedPlayerAttempts++;
					continue mainLoop;
				}
			}

			// Prevent player from spawning in crates
			for (int i = 0; i < game.structM.getCrates().size(); i++) {
				if (separationHitbox.x < game.structM.getCrates().get(i).getCollisionBoundingBox().x + game.structM.getCrates().get(i).getCollisionBoundingBox().width
						&& separationHitbox.x + separationHitbox.width > game.structM.getCrates().get(i).getCollisionBoundingBox().x
						&& separationHitbox.y < game.structM.getCrates().get(i).getCollisionBoundingBox().y + game.structM.getCrates().get(i).getCollisionBoundingBox().height
						&& separationHitbox.y + separationHitbox.height > game.structM.getCrates().get(i).getCollisionBoundingBox().y) {
					failedPlayerAttempts++;
					continue mainLoop;
				}
			}

			// Prevent player from spawning in obstructions
			for (int i = 0; i < game.structM.getObstructions().length; i++) {
				if (separationHitbox.x < game.structM.getObstructions()[i].getBoundingBox().x + game.structM.getObstructions()[i].getBoundingBox().width
						&& separationHitbox.x + separationHitbox.width > game.structM.getObstructions()[i].getBoundingBox().x
						&& separationHitbox.y < game.structM.getObstructions()[i].getBoundingBox().y + game.structM.getObstructions()[i].getBoundingBox().height
						&& separationHitbox.y + separationHitbox.height > game.structM.getObstructions()[i].getBoundingBox().y) {
					failedPlayerAttempts++;
					continue mainLoop;
				}
			}

			// Commit random values, need to shift as worldX and Y refer to player model coords, we want to set the hitbox coords
			worldX = randomX - entityArea.x;
			worldY = randomY - entityArea.y;
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
			newWeap = (SuperWeapon) game.itemM.getWeaponsArr()[weapType].clone();
			newWeap.setId(weapId);
			newWeap.setPlayer(this);

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
			if ((keys.isUp() == true || keys.isDown() == true || keys.isLeft() == true || keys.isRight() == true) && !freeze) {
				int xa = 0;
				int ya = 0;

				// Read keyboard inputs
				if (keys.isUp() == true)
					ya -= 1;
				if (keys.isDown() == true)
					ya += 1;
				if (keys.isLeft() == true)
					xa -= 1;
				if (keys.isRight() == true)
					xa += 1;

				if (!(xa == 0 && ya == 0)) {
					// Move player
					for (int i = 0; i < speed; i++)
						move(xa, ya);

					// Update new position to server
					new Pkt03Move(this.username, this.worldX, this.worldY).sendData(game.socketClient);
				}
			}

			if (keys.isInteract() == true) {
				// Handle "F" key input to check if there's any within range events
				withinRange();
				keys.setInteract(false);
			}
			if (keys.isDrop() == true) {
				// Handle "Q" key input to drop player weapon
				if (playerWeap[playerWeapIndex] != null) {
					SuperWeapon dropWeap = playerWeap[playerWeapIndex];
					// Update weapon drop to server
					new Pkt12DropWeapon(username, playerWeapIndex, dropWeap.getTypeId(), dropWeap.getId(), this.worldX - dropWeap.getImgIconWidth() / 2 + Game.playerSize / 2,
							this.worldY - dropWeap.getImgIconHeight() / 2 + Game.playerSize / 2).sendData(game.socketClient);
					game.soundHandler.playSound(9);
				}
				keys.setDrop(false);
			}

		}
		// Check mouse inputs
		if (mouse != null) {
			// Check mouse movement
			if (mouseX != mouse.getX() || mouseY != mouse.getY()) {
				this.mouseX = mouse.getX();
				this.mouseY = mouse.getY();

				updateMouseDirection(mouse.getX(), mouse.getY());
				// Update mouse direction to server
				new Pkt04MouseMove(this.username, this.mouseX, this.mouseY).sendData(game.socketClient);
			}
			// Check mouse clicks
			if (mouse.isMousePressed())
				// Handle shoot event
				if (playerWeapIndex >= 0)
					if (getWeapons()[playerWeapIndex] != null)
						getWeapons()[playerWeapIndex].shoot();
		}

		// Update bullets in each weapon
		for (SuperWeapon weap : getWeapons())
			if (weap != null)
				weap.update();

		
		if (game.getGameState() == Game.playState) {
			// Check if user is in gas
			if (game.tileM.withinGas(worldX, worldX + Game.playerSize, worldY, worldY + Game.playerSize))
				new Pkt20GasDamage(this.getUsername()).sendData(game.socketClient);
		}
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
				new Pkt12DropWeapon(username, playerWeapIndex, dropWeap.getTypeId(), dropWeap.getId(), weapon.worldX, weapon.worldY).sendData(game.socketClient);
				game.soundHandler.playSound(9);
			}
			new Pkt10PickupWeapon(username, playerWeapIndex, weapon.getTypeId(), weapon.getId()).sendData(game.socketClient);
			game.soundHandler.playSound(8);
			return;
		}

		// Check if crates are in range to open
		int crateIndex = game.structM.withinCrateRange(entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY);
		if (crateIndex != -1) {
			new Pkt11CrateOpen(username, crateIndex).sendData(game.socketClient);
			game.soundHandler.playSound(7);
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
			handOffset = sp.getImgOffset();
		} else {
			holding = playerHand;
			handOffset = -4;
		}

		int x, y;
		int handX, handY;

		// If player is himself, render him in the middle of the screen, else render
		// other players relative to the main player
		if (!local) {
			x = worldX - game.player.worldX + game.player.getScreenX();
			y = worldY - game.player.worldY + game.player.getScreenY();
			handX = worldX - game.player.worldX + game.screen.getScreenWidth() / 2 - holding.getWidth() / 2;
			handY = worldY - game.player.worldY + game.screen.getScreenHeight() / 2 - holding.getHeight() / 2;
		} else {
			x = screenX;
			y = screenY;
			handX = game.screen.getScreenWidth() / 2 - holding.getWidth() / 2;
			handY = game.screen.getScreenHeight() / 2 - holding.getHeight() / 2;
		}

		// Rotate weapon/hand to mouse
		AffineTransform t = new AffineTransform();
		t.setToTranslation(handX, handY);
		t.rotate(imageAngleRad, holding.getWidth() / 2, holding.getHeight() / 2);
		t.translate(Game.playerSize / 2 + holding.getWidth() / 2 + handOffset, 0);

		g2.drawImage(holding, t, null); // Draw hand (weapons)
		g2.drawImage(sprite, x, y, Game.playerSize, Game.playerSize, null); // Draw player

		g2.setColor(new Color(255, 0, 30));
		g2.fillRect(x - Game.tileSize / 4, y - 15, (int) ((this.health)) / 2, 10);
		g2.setColor(Color.white);
		int length = (int) g2.getFontMetrics().getStringBounds(this.getUsername(), g2).getWidth();
		g2.drawString(this.getUsername(), x - length /2 + Game.playerSize / 2 , y + 40);
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

	public int getPlayerState() {
		return playerState;
	}

	public void setPlayerState(int playerState) {
		this.playerState = playerState;
	}

	public int getScreenX() {
		return screenX;
	}

	public int getScreenY() {
		return screenY;
	}

	public double getMouseX() {
		return mouseX;
	}

	public void setMouseX(double mouseX) {
		this.mouseX = mouseX;
	}

	public double getMouseY() {
		return mouseY;
	}

	public void setMouseY(double mouseY) {
		this.mouseY = mouseY;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public int getPlayerWeapIndex() {
		return playerWeapIndex;
	}

	public void setPlayerWeapIndex(int playerWeapIndex) {
		this.playerWeapIndex = playerWeapIndex;
	}

	public double getHealth() {
		return health;
	}

	public void setHealth(double health) {
		this.health = health;
	}

	public boolean isFreeze() {
		return freeze;
	}

	public void setFreeze(boolean freeze) {
		this.freeze = freeze;
	}
	
	
	
}
