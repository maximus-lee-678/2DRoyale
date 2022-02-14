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
	public int maxWorldCol, maxWorldRow;
	public int mapTileNum[][][];
	public int gasCounter;

	public TileManager(Game game) {

		this.game = game;
		tile = new Tile[20]; // Currently we just store 20 types of tiles
		gasCounter = 0;
		getTileImage(); // populate tile array

		System.out.println(game.gameState);
		if (game.gameState == game.waitState)
			loadMap("/maps/lobby.png"); // load map
		if (game.gameState == game.playState)
			loadMap("/maps/olympus.png"); // load map

	}

	private BufferedImage toCompatibleImage(BufferedImage image) {
		// obtain the current system graphical settings
		GraphicsConfiguration gfxConfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

		/*
		 * if image is already compatible and optimized for current system settings,
		 * simply return it
		 */
		if (image.getColorModel().equals(gfxConfig.getColorModel()))
			return image;

		// image is not optimized, so create a new image that is
		BufferedImage newImage = gfxConfig.createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());

		// get the graphics context of the new image to draw the old image on
		Graphics2D g2d = newImage.createGraphics();

		// actually draw the image and dispose of context no longer needed
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();

		// return the new optimized image
		return newImage;
	}

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

	public void loadMap(String filePath) {

		try {
			BufferedImage img = ImageIO.read(getClass().getResourceAsStream(filePath));

			maxWorldCol = img.getWidth();
			maxWorldRow = img.getHeight();
			mapTileNum = new int[maxWorldCol][maxWorldRow][3]; // create 2d array of the dimension of the world
			// [0] = tileID, [1] = isFlipped, [2] = isGassed

			for (int y = 0; y < maxWorldRow; y++) {
				for (int x = 0; x < maxWorldCol; x++) {
					// Retrieving contents of a pixel
					int pixel = img.getRGB(x, y);
					// Creating a Color object from pixel value
					Color color = new Color(pixel, true);
					// Retrieving the R G B values
					int red = color.getRed();
					int green = color.getGreen();
					int blue = color.getBlue();

					double roll = game.rand.nextDouble();

					mapTileNum[x][y][1] = game.rand.nextInt(2);
					if (red == 34 && green == 177 && blue == 76) { // grass
						if (roll < (double) 1 / 2)
							mapTileNum[x][y][0] = 0;
						else if (roll < (double) 2 / 3)
							mapTileNum[x][y][0] = 1;
						else if (roll < (double) 5 / 6)
							mapTileNum[x][y][0] = 2;
						else
							mapTileNum[x][y][0] = 3;
					} else if (red == 185 && green == 122 && blue == 87) { // earth
						if (roll < 0.50)
							mapTileNum[x][y][0] = 4;
						else
							mapTileNum[x][y][0] = 5;
					} else if (red == 63 && green == 72 && blue == 204) { // coldstone
						mapTileNum[x][y][0] = 6;
					} else if (red == 140 && green == 255 && blue == 251) { // ice
						mapTileNum[x][y][0] = 7;
					} else if (red == 88 && green == 88 && blue == 88) { // slush
						mapTileNum[x][y][0] = 8;
					} else if (red == 255 && green == 255 && blue == 255) { // snow
						mapTileNum[x][y][0] = 9;
					} else if (red == 195 && green == 195 && blue == 195) { // ash
						mapTileNum[x][y][0] = 10;
					} else if (red == 0 && green == 0 && blue == 0) { // volcanic
						mapTileNum[x][y][0] = 11;
					} else if (red == 239 && green == 228 && blue == 176) { // sand
						mapTileNum[x][y][0] = 12;
					} else if (red == 0 && green == 162 && blue == 232) { // water
						mapTileNum[x][y][0] = 13;
					} else if (red == 255 && green == 127 && blue == 39) { // magma
						mapTileNum[x][y][0] = 14;
					} else { // missing texture
						mapTileNum[x][y][0] = 15;
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeGas() {
		for (int y = 0; y < maxWorldRow; y++) {
			mapTileNum[gasCounter][y][2] = 1;
			mapTileNum[maxWorldCol - gasCounter - 1][y][2] = 1;
		}
		for (int x = 0; x < maxWorldCol; x++) {
			mapTileNum[x][gasCounter][2] = 1;
			mapTileNum[x][maxWorldRow - gasCounter - 1][2] = 1;
		}
		gasCounter++;
		if (gasCounter > maxWorldCol - 1 || gasCounter > maxWorldRow - 1)
			gasCounter--;
	}

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

		int tileNum1 = 0, tileNum2 = 0;

		if (ya < 0) { // UP
			entityTopRow = entityTopWorldY / game.tileSize;
			tileNum1 = game.tileM.mapTileNum[entityLeftCol][entityTopRow][0];
			tileNum2 = game.tileM.mapTileNum[entityRightCol][entityTopRow][0];
		}
		if (ya > 0) { // DOWN
			entityBottomRow = entityBottomWorldY / game.tileSize;
			tileNum1 = game.tileM.mapTileNum[entityLeftCol][entityBottomRow][0];
			tileNum2 = game.tileM.mapTileNum[entityRightCol][entityBottomRow][0];
		}
		if (xa < 0) { // LEFT
			entityLeftCol = entityLeftWorldX / game.tileSize;
			tileNum1 = game.tileM.mapTileNum[entityLeftCol][entityTopRow][0];
			tileNum2 = game.tileM.mapTileNum[entityLeftCol][entityBottomRow][0];
		}
		if (xa > 0) { // RIGHT
			entityRightCol = entityRightWorldX / game.tileSize;
			tileNum1 = game.tileM.mapTileNum[entityRightCol][entityTopRow][0];
			tileNum2 = game.tileM.mapTileNum[entityRightCol][entityBottomRow][0];
		}
		if (type == "Entity") {
			if (game.tileM.tile[tileNum1].collisionPlayer || game.tileM.tile[tileNum2].collisionPlayer)
				return true;
		} else if (type == "Projectile") {
			if (game.tileM.tile[tileNum1].collisionProjectile || game.tileM.tile[tileNum2].collisionProjectile)
				return true;
		}

		return false;
	}

	public boolean withinGas(int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY, int entityBottomWorldY) {

		int topLeftTileX = entityLeftWorldX / game.tileSize;
		int topLeftTileY = entityTopWorldY / game.tileSize;
		int topRightTileX = entityRightWorldX / game.tileSize;
		int topRightTileY = entityTopWorldY / game.tileSize;
		int bottomLeftTileX = entityLeftWorldX / game.tileSize;
		int bottomLeftTileY = entityBottomWorldY / game.tileSize;
		int bottomRightTileX = entityRightWorldX / game.tileSize;
		int bottomRightTileY = entityBottomWorldY / game.tileSize;

		if (mapTileNum[topLeftTileX][topLeftTileY][2] == 1 || mapTileNum[topRightTileX][topRightTileY][2] == 1 || mapTileNum[bottomLeftTileX][bottomLeftTileY][2] == 1
				|| mapTileNum[bottomRightTileX][bottomRightTileY][2] == 1) {
			return true;
		}
		return false;
	}

}
