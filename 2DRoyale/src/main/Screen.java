package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.imageio.ImageIO;

import entity.PlayerMP;
import item.SuperWeapon;
import structure.Building;
import structure.Crate;
import structure.Obstruction;
import structure.StructuresManager;

public class Screen implements RenderInterface {

	private Game game;

	private final int maxScreenCol;
	private final int maxScreenRow;
	private final int screenWidth;
	private final int screenHeight;

	// Variables for mini and megamap
	private final int minimapRadius = 18;
	private final int minimapTileSize = 4;
	private final int minimapRenderAtX = 70;
	private final int minimapRenderAtY = 70;

	private final int megamapLength = 640; // must be multiple of 128 (map size)
	private final int megamapHeight = 640; // for accurate gas drawing
	private final int megamapBackBorder = 2;
	private final int megaPlayerSize = 8;

	private BufferedImage minimapBack, minimapVoid, megamapLobby, megamapGame, buildingMinimap;

	public Screen(Game game) {
		this.game = game;
		this.maxScreenCol = 26;
		this.maxScreenRow = 15;
		this.screenWidth = Game.tileSize * maxScreenCol;
		this.screenHeight = Game.tileSize * maxScreenRow;
		try {
			this.minimapBack = ImageIO.read(getClass().getResourceAsStream("/UI/minimap_back.png"));
			this.minimapVoid = ImageIO.read(getClass().getResourceAsStream("/world_textures/tiles/water.png"));
			this.megamapLobby = ImageIO.read(getClass().getResourceAsStream("/maps/lobbyMega.png"));
			this.megamapGame = ImageIO.read(getClass().getResourceAsStream("/maps/olympusMega.png"));
			this.buildingMinimap = ImageIO.read(getClass().getResourceAsStream("/world_textures/buildings/wall.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Handles screen rendering. Different aspects of the game are rendered in a specific order to properly display information.
	 */
	public void render(Graphics2D g2) {
		if (game.getGameState() == Game.waitState || game.getGameState() == Game.playState) {
			// 1st layer
			renderWorld(g2);

			// 2nd layer
			renderBuildings(g2);
			renderCrates(g2);
			renderObstructions(g2);

			// 3rd layer
			renderItems(g2);

			// 4th layer
			for (PlayerMP p : game.getPlayers())
				if (p.getPlayerState() == game.getGameState())
					p.renderBullets(g2);

			// 5th layer
			for (PlayerMP p : game.getPlayers())
				if (p.getPlayerState() == game.getGameState())
					p.render(g2);

			// 6th layer
			renderGas(g2);

			// 7th layer
			if (game.keys.isMap())
				renderMegamap(g2);
			else
				renderMinimap(g2);
		}

		// 8th layer
		game.ui.draw(g2);
	}

	/**
	 * Renders tiles on the ground. 1st layer to render.
	 */
	private void renderWorld(Graphics2D g2) {

		for (int y = 0; y < game.tileM.getMaxWorldRow(); y++) {
			for (int x = 0; x < game.tileM.getMaxWorldCol(); x++) {
				int worldX = x * Game.tileSize;
				int worldY = y * Game.tileSize;
				int gameX = worldX - game.player.getWorldX() + game.player.getScreenX();
				int gameY = worldY - game.player.getWorldY() + game.player.getScreenY();

				// Only render tiles player can see
				if (worldX + Game.tileSize > game.player.getWorldX() - game.player.getScreenX() && worldX - Game.tileSize < game.player.getWorldX() + game.player.getScreenX()
						&& worldY + Game.tileSize > game.player.getWorldY() - game.player.getScreenY()
						&& worldY - Game.tileSize < game.player.getWorldY() + game.player.getScreenY()) {
					// Render tiles based on flip orientation
					if (!game.tileM.getMapTileData()[x][y].isFlipped())
						g2.drawImage(game.tileM.getMapTileData()[x][y].getTile().getImage(), gameX, gameY, Game.tileSize, Game.tileSize, null);
					else
						g2.drawImage(game.tileM.getMapTileData()[x][y].getTile().getImage(), gameX + Game.tileSize, gameY, -Game.tileSize, Game.tileSize, null);
				}

			}
		}

	}

	/**
	 * Renders buildings. 2nd layer to render.
	 */
	private void renderBuildings(Graphics2D g2) {
		Building[] buildings = game.structM.getBuildings();
		int buildingTileSize = StructuresManager.getBuildingTileSize();

		for (int i = 0; i < buildings.length; i++) {

			for (int y = 0; y < buildings[i].getBoundingBox().height / buildingTileSize; y++) {
				for (int x = 0; x < buildings[i].getBoundingBox().width / buildingTileSize; x++) {
					int[] rowNum = buildings[i].getBuildingTileNum().get(y);
					int tileNum = rowNum[x];
					int worldX = buildings[i].getBoundingBox().x + (x * buildingTileSize);
					int worldY = buildings[i].getBoundingBox().y + (y * buildingTileSize);
					int gameX = worldX - game.player.getWorldX() + game.player.getScreenX();
					int gameY = worldY - game.player.getWorldY() + game.player.getScreenY();

					// Only render bounding boxes player can see
					if (worldX + Game.tileSize > game.player.getWorldX() - game.player.getScreenX()
							&& worldX - Game.tileSize < game.player.getWorldX() + game.player.getScreenX()
							&& worldY + Game.tileSize > game.player.getWorldY() - game.player.getScreenY()
							&& worldY - Game.tileSize < game.player.getWorldY() + game.player.getScreenY()) {
						if (tileNum != 0) // Don't draw empty spaces
							g2.drawImage(game.structM.getBuildingTile()[tileNum].getImage(), gameX, gameY, buildingTileSize, buildingTileSize, null);
					}

				}
			}
		}
	}

	/**
	 * Renders crates. 2nd layer to render.
	 */
	private void renderCrates(Graphics2D g2) {
		List<Crate> crates = game.structM.getCrates();
		int crateTileSize = StructuresManager.getCrateTileSize();

		for (int i = 0; i < crates.size(); i++) {
			Crate crate = crates.get(i);
			int worldX = crate.getCollisionBoundingBox().x;
			int worldY = crate.getCollisionBoundingBox().y;
			int gameX = worldX - game.player.getWorldX() + game.player.getScreenX();
			int gameY = worldY - game.player.getWorldY() + game.player.getScreenY();

			// Only render crates player can see
			if (worldX + Game.tileSize > game.player.getWorldX() - game.player.getScreenX() && worldX - Game.tileSize < game.player.getWorldX() + game.player.getScreenX()
					&& worldY + Game.tileSize > game.player.getWorldY() - game.player.getScreenY()
					&& worldY - Game.tileSize < game.player.getWorldY() + game.player.getScreenY()) {
				g2.drawImage(game.structM.getSolid()[crate.getImageID()].getImage(), gameX, gameY, crateTileSize, crateTileSize, null);
			}
		}
	}

	/**
	 * Renders obstructions. 2nd layer to render.
	 */
	private void renderObstructions(Graphics2D g2) {
		Obstruction[] obstructions = game.structM.getObstructions();

		for (int i = 0; i < obstructions.length; i++) {
			int worldX = obstructions[i].getBoundingBox().x;
			int worldY = obstructions[i].getBoundingBox().y;
			int gameX = worldX - game.player.getWorldX() + game.player.getScreenX();
			int gameY = worldY - game.player.getWorldY() + game.player.getScreenY();

			// Only render crates player can see
			if (worldX + obstructions[i].getBoundingBox().width > game.player.getWorldX() - game.player.getScreenX()
					&& worldX - obstructions[i].getBoundingBox().width < game.player.getWorldX() + game.player.getScreenX()
					&& worldY + obstructions[i].getBoundingBox().height > game.player.getWorldY() - game.player.getScreenY()
					&& worldY - obstructions[i].getBoundingBox().height < game.player.getWorldY() + game.player.getScreenY()) {
				// Render obstructions based on flip orientation
				if (!obstructions[i].isMirrored())
					g2.drawImage(game.structM.getSolid()[obstructions[i].getImageID()].getImage(), gameX, gameY, obstructions[i].getBoundingBox().width,
							obstructions[i].getBoundingBox().height, null);
				else
					g2.drawImage(game.structM.getSolid()[obstructions[i].getImageID()].getImage(), gameX + obstructions[i].getBoundingBox().width, gameY,
							-obstructions[i].getBoundingBox().width, obstructions[i].getBoundingBox().height, null);

			}
		}
	}

	/**
	 * Renders items. 3rd layer to render.
	 */
	private void renderItems(Graphics2D g2) {
		List<SuperWeapon> worldWeapons = game.itemM.getWorldWeapons();
		for (int i = 0; i < worldWeapons.size(); i++) {

			SuperWeapon weap = worldWeapons.get(i);

			int worldX = weap.getWorldX();
			int worldY = weap.getWorldY();
			int gameX = worldX - game.player.getWorldX() + game.player.getScreenX();
			int gameY = worldY - game.player.getWorldY() + game.player.getScreenY();

			// Only render weapons player can see
			if (worldX + Game.tileSize > game.player.getWorldX() - game.player.getScreenX() && worldX - Game.tileSize < game.player.getWorldX() + game.player.getScreenX()
					&& worldY + Game.tileSize > game.player.getWorldY() - game.player.getScreenY()
					&& worldY - Game.tileSize < game.player.getWorldY() + game.player.getScreenY()) {
				Color c = new Color(255, 255, 50);
				g2.setColor(c);
				g2.setStroke(new BasicStroke(2));
				g2.drawOval(gameX + weap.getEntityArea().x, gameY + weap.getEntityArea().y, 18, 18);
				g2.drawImage(weap.getEntityImg(), gameX, gameY, weap.getImgIconWidth(), weap.getImgIconHeight(), null);
			}
		}

	}

	/**
	 * Renders items. 6th layer to render.
	 */
	private void renderGas(Graphics2D g2) {

		// Render gas tiles
		for (int y = 0; y < game.tileM.getMaxWorldRow(); y++) {
			for (int x = 0; x < game.tileM.getMaxWorldCol(); x++) {
				int worldX = x * Game.tileSize;
				int worldY = y * Game.tileSize;
				int gameX = worldX - game.player.getWorldX() + game.player.getScreenX();
				int gameY = worldY - game.player.getWorldY() + game.player.getScreenY();

				// Only render tiles player can see
				if (worldX + Game.tileSize > game.player.getWorldX() - game.player.getScreenX() && worldX - Game.tileSize < game.player.getWorldX() + game.player.getScreenX()
						&& worldY + Game.tileSize > game.player.getWorldY() - game.player.getScreenY()
						&& worldY - Game.tileSize < game.player.getWorldY() + game.player.getScreenY()) {
					if (game.tileM.getMapTileData()[x][y].isGassed())
						g2.drawImage(game.tileM.getGasTile().getImage(), gameX, gameY, Game.tileSize, Game.tileSize, null);
				}

			}
		}

	}

	/**
	 * Draws fullscreen map. 7th layer to render.
	 */
	private void renderMegamap(Graphics2D g2) {
		int megamapTileSizeX = megamapLength / game.tileM.getMaxWorldRow();
		int megamapTileSizeY = megamapLength / game.tileM.getMaxWorldCol();
		int megamapRenderAtX = game.player.getScreenX() - megamapLength / 2;
		int megamapRenderAtY = game.player.getScreenY() - megamapHeight / 2;
		int fullMapLength = game.tileM.getMaxWorldCol() * Game.tileSize * megamapLength;
		int fullMapHeight = game.tileM.getMaxWorldRow() * Game.tileSize * megamapHeight;

		int playerMapX = (int) Math.round((double) game.player.getWorldX() / fullMapLength);
		int playerMapY = (int) Math.round((double) game.player.getWorldY() / fullMapHeight);

		// Draw megamap back
		Color back = new Color(11, 227, 178);
		g2.setColor(back);
		g2.fillRect(megamapRenderAtX - megamapBackBorder, megamapRenderAtY - megamapBackBorder, megamapLength + 2 * megamapBackBorder, megamapHeight + 2 * megamapBackBorder);

		// Draw Map
		if (game.getGameState() == Game.waitState)
			g2.drawImage(megamapLobby, megamapRenderAtX, megamapRenderAtY, megamapLength, megamapHeight, null);
		else
			g2.drawImage(megamapGame, megamapRenderAtX, megamapRenderAtY, megamapLength, megamapHeight, null);

		// Draw Gas
		TexturePaint farts = new TexturePaint(game.tileM.getGasTile().getImage(), new Rectangle(0, 0, megamapTileSizeX, megamapTileSizeY));

		int gasCounter = game.tileM.getGasCounter();

		// Prevent gas from drawing over itself
		if (gasCounter > game.tileM.getMaxWorldCol() / 2 || gasCounter > game.tileM.getMaxWorldRow() / 2)
			gasCounter = game.tileM.getMaxWorldCol() / 2;

		// Creeping From Left
		g2.setPaint(farts);
		g2.fillRect(megamapRenderAtX, megamapRenderAtY, megamapTileSizeX * gasCounter, megamapHeight);
		// Creeping From Right
		g2.setPaint(farts);
		g2.fillRect(megamapRenderAtX - (megamapTileSizeX * gasCounter) + megamapLength, megamapRenderAtY, megamapTileSizeX * gasCounter, megamapHeight);
		// Creeping From Top
		g2.setPaint(farts);
		g2.fillRect(megamapRenderAtX + (megamapTileSizeX * gasCounter), megamapRenderAtY, megamapLength - (megamapTileSizeX * gasCounter * 2), megamapTileSizeY * gasCounter);
		// Creeping From Bottom
		g2.setPaint(farts);
		g2.fillRect(megamapRenderAtX + (megamapTileSizeX * gasCounter), megamapRenderAtY - (megamapTileSizeX * gasCounter) + megamapLength,
				megamapHeight - (megamapTileSizeX * gasCounter * 2), megamapTileSizeY * gasCounter);

		// Draw player sprite
		g2.drawImage(game.player.getSprite(), megamapRenderAtX + playerMapX - (megaPlayerSize / 2), megamapRenderAtY + playerMapY - (megaPlayerSize / 2), megaPlayerSize,
				megaPlayerSize, null);
	}

	/**
	 * Draws minimap. 7th layer to render.
	 */
	private void renderMinimap(Graphics2D g2) {
		int minimapBorderSize = (int) Math.round(minimapTileSize * 5.5);
		int minimapBackSize = (minimapRadius * 2 + 1) * minimapTileSize + (minimapBorderSize * 2);
		int minimapBackRenderAtX = minimapRenderAtX - minimapBorderSize;
		int minimapBackRenderAtY = minimapRenderAtY - minimapBorderSize;

		// Draw minimap back
		g2.drawImage(minimapBack, minimapBackRenderAtX, minimapBackRenderAtY, minimapBackSize, minimapBackSize, null);

		int playerTileX = game.player.getWorldX() / Game.tileSize;
		int playerTileY = game.player.getWorldY() / Game.tileSize;

		// For making minimap render with player in center
		int xLowerBound = playerTileX - minimapRadius;
		int xUpperBound = playerTileX + minimapRadius;
		int yLowerBound = playerTileY - minimapRadius;
		int yUpperBound = playerTileY + minimapRadius;

		int xOffset = 0;
		int yOffset = 0;

		if (xLowerBound < 0) {
			xOffset = -xLowerBound;
			xLowerBound = 0;
		}
		if (xUpperBound < 0) {
			xOffset = -xLowerBound;
			xUpperBound = 0;
		}
		if (yLowerBound < 0) {
			yOffset = -yLowerBound;
			yLowerBound = 0;
		}
		if (yUpperBound < 0) {
			yOffset = -yLowerBound;
			yUpperBound = 0;
		}

		if (xLowerBound > game.tileM.getMaxWorldCol() - 1)
			xLowerBound = game.tileM.getMaxWorldCol() - 1;
		if (xUpperBound > game.tileM.getMaxWorldCol() - 1)
			xUpperBound = game.tileM.getMaxWorldCol() - 1;
		if (yLowerBound > game.tileM.getMaxWorldRow() - 1)
			yLowerBound = game.tileM.getMaxWorldCol() - 1;
		if (yUpperBound > game.tileM.getMaxWorldRow() - 1)
			yUpperBound = game.tileM.getMaxWorldCol() - 1;

		int minimapX = 0;
		int minimapY = 0;

		// Draw void fill
		for (int y = 0; y < minimapRadius * 2 + 1; y++) {
			minimapX = 0;
			for (int x = 0; x < minimapRadius * 2 + 1; x++) {
				g2.drawImage(minimapVoid, minimapRenderAtX + minimapX, minimapRenderAtY + minimapY, minimapTileSize, minimapTileSize, null);

				if (game.getGameState() == Game.playState) // only draw gas on border of minimap during combat phase
					g2.drawImage(game.tileM.getGasTile().getImage(), minimapRenderAtX + minimapX, minimapRenderAtY + minimapY, minimapTileSize, minimapTileSize, null);

				minimapX += minimapTileSize;
			}
			minimapY += minimapTileSize;
		}

		minimapX = 0;
		minimapY = 0;

		// Draw tiles
		for (int y = yLowerBound; y < yUpperBound + 1; y++) {
			minimapX = 0;
			for (int x = xLowerBound; x < xUpperBound + 1; x++) {

				g2.drawImage(game.tileM.getMapTileData()[x][y].getTile().getImage(), minimapRenderAtX + minimapX + (xOffset * minimapTileSize),
						minimapRenderAtY + minimapY + (yOffset * minimapTileSize), minimapTileSize, minimapTileSize, null);

				minimapX += minimapTileSize;
			}
			minimapY += minimapTileSize;
		}

		minimapX = 0;
		minimapY = 0;

		// Draw buildings
		for (int y = yLowerBound; y < yUpperBound + 1; y++) {
			minimapX = 0;
			for (int x = xLowerBound; x < xUpperBound + 1; x++) {
				// Checks mapTileData array populated during generation to see if
				// building intersects that tile
				if (game.tileM.getMapTileData()[x][y].hasBuilding())
					g2.drawImage(buildingMinimap, minimapRenderAtX + minimapX + (xOffset * minimapTileSize), minimapRenderAtY + minimapY + (yOffset * minimapTileSize),
							minimapTileSize, minimapTileSize, null);

				minimapX += minimapTileSize;
			}
			minimapY += minimapTileSize;
		}

		minimapX = 0;
		minimapY = 0;

		// Draw gas
		for (int y = yLowerBound; y < yUpperBound + 1; y++) {
			minimapX = 0;
			for (int x = xLowerBound; x < xUpperBound + 1; x++) {

				if (game.tileM.getMapTileData()[x][y].isGassed())
					g2.drawImage(game.tileM.getGasTile().getImage(), minimapRenderAtX + minimapX + (xOffset * minimapTileSize),
							minimapRenderAtY + minimapY + (yOffset * minimapTileSize), minimapTileSize, minimapTileSize, null);

				minimapX += minimapTileSize;
			}
			minimapY += minimapTileSize;
		}

		// Draw player sprite
		g2.drawImage(game.player.getSprite(), minimapRenderAtX + (minimapRadius * minimapTileSize), minimapRenderAtY + (minimapRadius * minimapTileSize), minimapTileSize,
				minimapTileSize, null);
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public int getMaxScreenCol() {
		return maxScreenCol;
	}

	public int getMaxScreenRow() {
		return maxScreenRow;
	}

}