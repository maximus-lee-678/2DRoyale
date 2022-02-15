package tile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import main.Game;

public class TileManager {

	private Game game;
	public Tile[] tile;
	public Tile gasTile;
	public MapTiles[][] mapTileNum;
	public int maxWorldCol, maxWorldRow;
	public int gasCounter;

	public TileManager(Game game) {

		this.game = game;
		tile = new Tile[20]; // Currently we just store 20 types of tiles
		gasCounter = 0;
		getTileImage(); // populate tile array

		if (game.gameState == game.waitState)
			loadMap("/maps/lobby.png"); // load map
		if (game.gameState == game.playState)
			loadMap("/maps/olympus.png"); // load map

	}

	/**
	 * Loads tile textures into memory.
	 */
	private void getTileImage() {
		tile[0] = new Tile("tiles", "grass1.png", "Forest");
		tile[1] = new Tile("tiles", "grass2.png", "Forest");
		tile[2] = new Tile("tiles", "grass3.png", "Forest");
		tile[3] = new Tile("tiles", "grasstall.png", "Forest");
		tile[4] = new Tile("tiles", "earth1.png", "Forest");
		tile[5] = new Tile("tiles", "earth2.png", "Forest");

		tile[6] = new Tile("tiles", "coldstone.png", "Snow");
		tile[7] = new Tile("tiles", "ice.png", "Snow");
		tile[8] = new Tile("tiles", "slush.png", "Snow");
		tile[9] = new Tile("tiles", "snow.png", "Snow");

		tile[10] = new Tile("tiles", "ash.png", "Wasteland");
		tile[11] = new Tile("tiles", "volcanic.png", "Wasteland");

		tile[12] = new Tile("tiles", "sand.png", "undefined");
		tile[13] = new Tile("tiles", "water.png", "undefined", true, false);
		tile[14] = new Tile("tiles", "magma.png", "undefined", true, false);
		tile[15] = new Tile("misc", "missing.png", "undefined");

		gasTile = new Tile("misc", "gas.png");
	}

	/**
	 * Called by loadMap() to map RGB values read from the map schematic to actual tiles in tile[].
	 */
	public int mapRGBValues(int red, int green, int blue) {

		double roll = game.rand.nextDouble();

		if (red == 34 && green == 177 && blue == 76) { // grass
			if (roll < (double) 1 / 2)
				return 0;
			else if (roll < (double) 2 / 3)
				return 1;
			else if (roll < (double) 5 / 6)
				return 2;
			else
				return 3;
		} else if (red == 185 && green == 122 && blue == 87) { // earth
			if (roll < 0.50)
				return 4;
			else
				return 5;
		} else if (red == 63 && green == 72 && blue == 204) { // coldstone
			return 6;
		} else if (red == 140 && green == 255 && blue == 251) { // ice
			return 7;
		} else if (red == 88 && green == 88 && blue == 88) { // slush
			return 8;
		} else if (red == 255 && green == 255 && blue == 255) { // snow
			return 9;
		} else if (red == 195 && green == 195 && blue == 195) { // ash
			return 10;
		} else if (red == 0 && green == 0 && blue == 0) { // volcanic
			return 11;
		} else if (red == 239 && green == 228 && blue == 176) { // sand
			return 12;
		} else if (red == 0 && green == 162 && blue == 232) { // water
			return 13;
		} else if (red == 255 && green == 127 && blue == 39) { // magma
			return 14;
		} else { // missing texture
			return 15;
		}
	}

	/**
	 * Loads building textures into memory.
	 */
	public void loadMap(String filePath) {

		try {
			BufferedImage img = ImageIO.read(getClass().getResourceAsStream(filePath));

			maxWorldCol = img.getWidth();
			maxWorldRow = img.getHeight();

			mapTileNum = new MapTiles[maxWorldCol][maxWorldRow];

			for (int y = 0; y < maxWorldRow; y++) {
				for (int x = 0; x < maxWorldCol; x++) {
					// Retrieving contents of a pixel
					int pixel = img.getRGB(x, y);
					// Creating a Color object from pixel value
					Color color = new Color(pixel, true);
					// Retrieving the RGB values
					int red = color.getRed();
					int green = color.getGreen();
					int blue = color.getBlue();

					mapTileNum[x][y] = new MapTiles(tile[mapRGBValues(red, green, blue)], game.rand.nextBoolean());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates gas status of tiles in mapTileNum.
	 */
	public void closeGas() {
		// Close x axis
		for (int y = 0; y < maxWorldRow; y++) {
			mapTileNum[gasCounter][y].setIsGassed(true);
			mapTileNum[maxWorldCol - gasCounter - 1][y].setIsGassed(true);
		}
		
		// Close y axis
		for (int x = 0; x < maxWorldCol; x++) {
			mapTileNum[x][gasCounter].setIsGassed(true);
			mapTileNum[x][maxWorldRow - gasCounter - 1].setIsGassed(true);
		}
		
		gasCounter++;
		if (gasCounter > maxWorldCol - 1 || gasCounter > maxWorldRow - 1)
			gasCounter--;
	}

	/**
	 * Checks for collision with floor tiles.
	 */
	public boolean hasCollidedWorld(int xa, int ya, int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY, int entityBottomWorldY, String type) {

		int entityLeftCol = entityLeftWorldX / game.tileSize;
		int entityRightCol = entityRightWorldX / game.tileSize;
		int entityTopRow = entityTopWorldY / game.tileSize;
		int entityBottomRow = entityBottomWorldY / game.tileSize;

		int checkLimitX = game.maxWorldCol - 1;
		int checkLimitY = game.maxWorldRow - 1;
		// Out of bounds prevention
		if (entityLeftCol > checkLimitX || entityRightCol > checkLimitX || entityTopRow > checkLimitY || entityBottomRow > checkLimitY)
			return true;
		if (entityLeftWorldX < 0 || entityRightWorldX < 0 || entityTopWorldY < 0 || entityBottomWorldY < 0)
			return true;

		Tile tileNum1 = null, tileNum2 = null;

		if (ya < 0) { // UP
			tileNum1 = game.tileM.mapTileNum[entityLeftCol][entityTopRow].tile;
			tileNum2 = game.tileM.mapTileNum[entityRightCol][entityTopRow].tile;
		}
		if (ya > 0) { // DOWN
			tileNum1 = game.tileM.mapTileNum[entityLeftCol][entityBottomRow].tile;
			tileNum2 = game.tileM.mapTileNum[entityRightCol][entityBottomRow].tile;
		}
		if (xa < 0) { // LEFT
			tileNum1 = game.tileM.mapTileNum[entityLeftCol][entityTopRow].tile;
			tileNum2 = game.tileM.mapTileNum[entityLeftCol][entityBottomRow].tile;
		}
		if (xa > 0) { // RIGHT
			tileNum1 = game.tileM.mapTileNum[entityRightCol][entityTopRow].tile;
			tileNum2 = game.tileM.mapTileNum[entityRightCol][entityBottomRow].tile;
		}

			if (type == "Entity") {
				if (tileNum1.collisionPlayer || tileNum2.collisionPlayer)
					return true;
			} else if (type == "Projectile") {
				if (tileNum1.collisionProjectile || tileNum2.collisionProjectile)
					return true;
			}		

		return false;
	}

	/**
	 * Checks whether the player is currently in a gassed tile.
	 */
	public boolean withinGas(int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY, int entityBottomWorldY) {

		int topLeftTileX = entityLeftWorldX / game.tileSize;
		int topLeftTileY = entityTopWorldY / game.tileSize;
		int topRightTileX = entityRightWorldX / game.tileSize;
		int topRightTileY = entityTopWorldY / game.tileSize;
		int bottomLeftTileX = entityLeftWorldX / game.tileSize;
		int bottomLeftTileY = entityBottomWorldY / game.tileSize;
		int bottomRightTileX = entityRightWorldX / game.tileSize;
		int bottomRightTileY = entityBottomWorldY / game.tileSize;

		if (mapTileNum[topLeftTileX][topLeftTileY].isGassed() || mapTileNum[topRightTileX][topRightTileY].isGassed() || mapTileNum[bottomLeftTileX][bottomLeftTileY].isGassed()
				|| mapTileNum[bottomRightTileX][bottomRightTileY].isGassed()) {
			return true;
		}
		return false;
	}

}
