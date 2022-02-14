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

public class Screen {

	private Game game;

	public final int maxScreenCol = 24;
	public final int maxScreenRow = 18;
	public final int screenWidth;
	public final int screenHeight;
	private BufferedImage minimapBack, megamapLobby, megamapGame, buildingMinimap;

	public Screen(Game game) {
		this.game = game;
		this.screenWidth = game.tileSize * maxScreenCol;
		this.screenHeight = game.tileSize * maxScreenRow;
		try {
			minimapBack = ImageIO.read(getClass().getResourceAsStream("/UI/minimap_back.png"));
			megamapLobby = ImageIO.read(getClass().getResourceAsStream("/maps/lobbyMega.png"));
			megamapGame = ImageIO.read(getClass().getResourceAsStream("/maps/olympusMega.png"));
			buildingMinimap = ImageIO.read(getClass().getResourceAsStream("/world_textures/buildings/wall.png"));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void render(Graphics2D g2) {

		if (game.gameState == game.waitState || game.gameState == game.playState) {
			renderWorld(g2);
			renderBuildings(g2);
			renderCrates(g2);
			renderObstructions(g2);
			renderItems(g2);
			for (PlayerMP p : game.getPlayers())
				if (p.playerState == game.gameState)
					p.renderBullets(g2);

			for (PlayerMP p : game.getPlayers())
				if (p.playerState == game.gameState)
					p.render(g2);

			renderGas(g2);

			if (game.keys.map)
				renderMegamap(g2);
			else
				renderMinimap(g2);
		}

		game.ui.draw(g2);

	}

	private void renderMegamap(Graphics2D g2) {
		int megamapLength = 640; // must be multiple of 128 (map size)
		int megamapHeight = 640; // for accurate gas drawing
		int megamapBackBorder = 2;
		int megamapTileSizeX = megamapLength / game.maxWorldRow;
		int megamapTileSizeY = megamapLength / game.maxWorldCol;
		int megamapRenderAtX = game.player.screenX - megamapLength / 2;
		int megamapRenderAtY = game.player.screenY - megamapHeight / 2;
		int playerSize = 8;

		int playerMapX = (int) Math.round((double) game.player.worldX / game.worldWidth * megamapLength);
		int playerMapY = (int) Math.round((double) game.player.worldY / game.worldHeight * megamapHeight);

		// Draw megamap back
		Color back = new Color(11, 227, 178);
		g2.setColor(back);
		g2.fillRect(megamapRenderAtX - megamapBackBorder, megamapRenderAtY - megamapBackBorder, megamapLength + 2 * megamapBackBorder, megamapHeight + 2 * megamapBackBorder);

		// Draw Map
		if (game.gameState == game.waitState)
			g2.drawImage(megamapLobby, megamapRenderAtX, megamapRenderAtY, megamapLength, megamapHeight, null);
		else
			g2.drawImage(megamapGame, megamapRenderAtX, megamapRenderAtY, megamapLength, megamapHeight, null);

		// Draw Gas
		TexturePaint farts = new TexturePaint(game.tileM.gasTile.image, new Rectangle(0, 0, megamapTileSizeX, megamapTileSizeY));

		int gasCounter = game.tileM.gasCounter;

		if (gasCounter > game.maxWorldCol / 2 || gasCounter > game.maxWorldRow / 2)
			gasCounter = game.maxWorldCol / 2;

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
		g2.drawImage(game.player.sprite, megamapRenderAtX + playerMapX - (playerSize / 2), megamapRenderAtY + playerMapY - (playerSize / 2), playerSize, playerSize, null);
	}

	// NOTE: not in final release due to performance issues
	// Current still here for picture taking purposes
	// Remove before final release
	private void renderMegamapObsolete(Graphics2D g2) {

		int megamapLength = 640; // must be multiple of 128 (map size)
		int megamapHeight = 640;
		int megamapRenderAtX = game.player.screenX - megamapLength / 2;
		int megamapRenderAtY = game.player.screenY - megamapHeight / 2;
		int megamapTileSizeX = megamapLength / game.maxWorldCol;
		int megamapTileSizeY = megamapHeight / game.maxWorldRow;
		int playerSize = 8;

		int playerMapX = (int) Math.round((double) game.player.worldX / game.worldWidth * megamapLength);
		int playerMapY = (int) Math.round((double) game.player.worldY / game.worldHeight * megamapHeight);

		int megamapX = 0;
		int megamapY = 0;

		// Render blocks
		for (int y = 0; y < game.maxWorldRow; y++) {
			megamapX = 0;
			for (int x = 0; x < game.maxWorldCol; x++) {

				if (game.tileM.mapTileNum[x][y].isFlipped)
					g2.drawImage(game.tileM.mapTileNum[x][y].tile.image, megamapRenderAtX + megamapX, megamapRenderAtY + megamapY, megamapTileSizeX, megamapTileSizeY, null);
				else
					g2.drawImage(game.tileM.mapTileNum[x][y].tile.image, megamapRenderAtX + megamapX + megamapTileSizeX, megamapRenderAtY + megamapY, -megamapTileSizeX, megamapTileSizeY,
							null);
				megamapX += megamapTileSizeX;
			}
			megamapY += megamapTileSizeY;
		}

		megamapX = 0;
		megamapY = 0;
		// Render buildings
		for (int y = 0; y < game.maxWorldRow; y++) {
			megamapX = 0;
			for (int x = 0; x < game.maxWorldCol; x++) {
				if (game.structM.buildingOccupiesTile[x][y] == 1)
					g2.drawImage(game.tileM.tile[6].image, megamapRenderAtX + megamapX, megamapRenderAtY + megamapY, megamapTileSizeX, megamapTileSizeY, null);
				megamapX += megamapTileSizeX;
			}
			megamapY += megamapTileSizeY;
		}

		megamapX = 0;
		megamapY = 0;
		// Draw gas
		for (int y = 0; y < game.maxWorldRow; y++) {
			megamapX = 0;
			for (int x = 0; x < game.maxWorldCol; x++) {

				if (game.tileM.mapTileNum[x][y].isGassed)
					g2.drawImage(game.tileM.gasTile.image, megamapRenderAtX + megamapX, megamapRenderAtY + megamapY, megamapTileSizeX, megamapTileSizeY, null);

				megamapX += megamapTileSizeX;
			}
			megamapY += megamapTileSizeY;
		}

		// Draw player sprite (decent quality)
		g2.drawImage(game.player.sprite, megamapRenderAtX + playerMapX - (playerSize / 2), megamapRenderAtY + playerMapY - (playerSize / 2), playerSize, playerSize, null);

	}

	private void renderMinimap(Graphics2D g2) {
		int minimapRadius = 18;
		int minimapTileSize = 4;
		int minimapRenderAtX = 70;
		int minimapRenderAtY = 70;
		int minimapBorderSize = (int) (minimapRadius * 1.25);
		int minimapBackSize = (minimapRadius * 2 + 1) * minimapTileSize + (minimapBorderSize * 2);
		int minimapBackRenderAtX = minimapRenderAtX - minimapBorderSize;
		int minimapBackRenderAtY = minimapRenderAtY - minimapBorderSize;

		// Draw minimap back
		g2.drawImage(minimapBack, minimapBackRenderAtX, minimapBackRenderAtY, minimapBackSize, minimapBackSize, null);

		int playerTileX = game.player.worldX / game.tileSize;
		int playerTileY = game.player.worldY / game.tileSize;

		// For making minimap stop at border
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

		if (xLowerBound > game.maxWorldCol - 1)
			xLowerBound = game.maxWorldCol - 1;
		if (xUpperBound > game.maxWorldCol - 1)
			xUpperBound = game.maxWorldCol - 1;
		if (yLowerBound > game.maxWorldRow - 1)
			yLowerBound = game.maxWorldCol - 1;
		if (yUpperBound > game.maxWorldRow - 1)
			yUpperBound = game.maxWorldCol - 1;

		int minimapX = 0;
		int minimapY = 0;

		// Draw void fill

		TexturePaint outOfBounds = new TexturePaint(game.tileM.tile[13].image, new Rectangle(0, 0, minimapTileSize, minimapTileSize));
		g2.setPaint(outOfBounds);
		g2.fillRect(minimapRenderAtX + minimapX, minimapRenderAtY + minimapY, minimapTileSize * (minimapRadius * 2 + 1), minimapTileSize * (minimapRadius * 2 + 1));
		
		if(game.gameState == game.playState) {
			TexturePaint farts = new TexturePaint(game.tileM.gasTile.image, new Rectangle(0, 0, minimapTileSize, minimapTileSize));
			g2.setPaint(farts);
			g2.fillRect(minimapRenderAtX + minimapX, minimapRenderAtY + minimapY, minimapTileSize * (minimapRadius * 2 + 1), minimapTileSize * (minimapRadius * 2 + 1));
		}

//		for (int y = 0; y < minimapRadius * 2 + 1; y++) {
//			minimapX = 0;
//			for (int x = 0; x < minimapRadius * 2 + 1; x++) {
//				g2.drawImage(game.tileM.tile[13].image, minimapRenderAtX + minimapX, minimapRenderAtY + minimapY, minimapTileSize, minimapTileSize, null);
//
//				minimapX += minimapTileSize;
//			}
//			minimapY += minimapTileSize;
//		}

		minimapX = 0;
		minimapY = 0;

		// Draw tiles
		for (int y = yLowerBound; y < yUpperBound + 1; y++) {
			minimapX = 0;
			for (int x = xLowerBound; x < xUpperBound + 1; x++) {

				g2.drawImage(game.tileM.mapTileNum[x][y].tile.image, minimapRenderAtX + minimapX + (xOffset * minimapTileSize), minimapRenderAtY + minimapY + (yOffset * minimapTileSize),
						minimapTileSize, minimapTileSize, null);

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
				if (game.structM.buildingOccupiesTile[x][y] == 1)
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

				if (game.tileM.mapTileNum[x][y].isGassed())
					g2.drawImage(game.tileM.gasTile.image, minimapRenderAtX + minimapX + (xOffset * minimapTileSize), minimapRenderAtY + minimapY + (yOffset * minimapTileSize),
							minimapTileSize, minimapTileSize, null);

				minimapX += minimapTileSize;
			}
			minimapY += minimapTileSize;
		}

		// Draw player sprite (terrible quality)
		g2.drawImage(game.player.sprite, minimapRenderAtX + (minimapRadius * minimapTileSize), minimapRenderAtY + (minimapRadius * minimapTileSize), minimapTileSize,
				minimapTileSize, null);
	}

	private void renderWorld(Graphics2D g2) {
		int worldCol = 0;
		int worldRow = 0;

		while (worldCol < game.maxWorldCol && worldRow < game.maxWorldRow) {

			int worldX = worldCol * game.tileSize;
			int worldY = worldRow * game.tileSize;
			int gameX = worldX - game.player.worldX + game.player.screenX;
			int gameY = worldY - game.player.worldY + game.player.screenY;

			if (worldX + game.tileSize > game.player.worldX - game.player.screenX && worldX - game.tileSize < game.player.worldX + game.player.screenX
					&& worldY + game.tileSize > game.player.worldY - game.player.screenY && worldY - game.tileSize < game.player.worldY + game.player.screenY) {
				if (game.tileM.mapTileNum[worldCol][worldRow].isGassed())
					g2.drawImage(game.tileM.mapTileNum[worldCol][worldRow].tile.image, gameX, gameY, game.tileSize, game.tileSize, null);
				else
					g2.drawImage(game.tileM.mapTileNum[worldCol][worldRow].tile.image, gameX + game.tileSize, gameY, -game.tileSize, game.tileSize, null);
			}

			worldCol++;

			if (worldCol == game.maxWorldCol) {
				worldCol = 0;
				worldRow++;
			}
		}
	}

	private void renderGas(Graphics2D g2) {
		int worldCol = 0;
		int worldRow = 0;

		while (worldCol < game.maxWorldCol && worldRow < game.maxWorldRow) {

			int worldX = worldCol * game.tileSize;
			int worldY = worldRow * game.tileSize;
			int gameX = worldX - game.player.worldX + game.player.screenX;
			int gameY = worldY - game.player.worldY + game.player.screenY;

			if (worldX + game.tileSize > game.player.worldX - game.player.screenX && worldX - game.tileSize < game.player.worldX + game.player.screenX
					&& worldY + game.tileSize > game.player.worldY - game.player.screenY && worldY - game.tileSize < game.player.worldY + game.player.screenY) {
				if (game.tileM.mapTileNum[worldCol][worldRow].isGassed())
					g2.drawImage(game.tileM.gasTile.image, gameX, gameY, game.tileSize, game.tileSize, null);
			}

			worldCol++;

			if (worldCol == game.maxWorldCol) {
				worldCol = 0;
				worldRow++;
			}
		}
	}

	private void renderBuildings(Graphics2D g2) {
		int worldCol, worldRow;
		Building[] buildings = game.structM.buildings;
		int buildingTileSize = game.structM.buildingTileSize;

		for (int i = 0; i < game.numberOfBuildings; i++) {
			worldCol = 0;
			worldRow = 0;
			while (worldCol < buildings[i].boundingBox.width / buildingTileSize && worldRow < buildings[i].boundingBox.height / buildingTileSize) {

				int[] rowNum = buildings[i].buildingTileNum.get(worldRow);
				int tileNum = rowNum[worldCol];
				int worldX = buildings[i].boundingBox.x + (worldCol * buildingTileSize);
				int worldY = buildings[i].boundingBox.y + (worldRow * buildingTileSize);
				int gameX = worldX - game.player.worldX + game.player.screenX;
				int gameY = worldY - game.player.worldY + game.player.screenY;

				if (tileNum != 0) {
					if (worldX + game.tileSize > game.player.worldX - game.player.screenX && worldX - game.tileSize < game.player.worldX + game.player.screenX
							&& worldY + game.tileSize > game.player.worldY - game.player.screenY && worldY - game.tileSize < game.player.worldY + game.player.screenY) {
						g2.drawImage(game.structM.tile[tileNum].image, gameX, gameY, buildingTileSize, buildingTileSize, null);
					}
				}

				worldCol++;

				if (worldCol == buildings[i].boundingBox.width / buildingTileSize) {
					worldCol = 0;
					worldRow++;
				}
			}
		}
	}

	private void renderCrates(Graphics2D g2) {
		List<Crate> crates = game.structM.crates;
		int crateTileSize = game.structM.crateTileSize;

		for (int i = 0; i < crates.size(); i++) {
			Crate crate = crates.get(i);
			int worldX = crate.collisionBoundingBox.x;
			int worldY = crate.collisionBoundingBox.y;
			int gameX = worldX - game.player.worldX + game.player.screenX;
			int gameY = worldY - game.player.worldY + game.player.screenY;

			if (worldX + game.tileSize > game.player.worldX - game.player.screenX && worldX - game.tileSize < game.player.worldX + game.player.screenX
					&& worldY + game.tileSize > game.player.worldY - game.player.screenY && worldY - game.tileSize < game.player.worldY + game.player.screenY) {
				g2.drawImage(game.structM.solid[crate.imageID].image, gameX, gameY, crateTileSize, crateTileSize, null);
			}
		}
	}

	private void renderObstructions(Graphics2D g2) {
		Obstruction[] obstructions = game.structM.obstructions;

		for (int i = 0; i < obstructions.length; i++) {
			int worldX = obstructions[i].boundingBox.x;
			int worldY = obstructions[i].boundingBox.y;
			int gameX = worldX - game.player.worldX + game.player.screenX;
			int gameY = worldY - game.player.worldY + game.player.screenY;

			if (worldX + obstructions[i].boundingBox.width > game.player.worldX - game.player.screenX
					&& worldX - obstructions[i].boundingBox.width < game.player.worldX + game.player.screenX
					&& worldY + obstructions[i].boundingBox.height > game.player.worldY - game.player.screenY
					&& worldY - obstructions[i].boundingBox.height < game.player.worldY + game.player.screenY) {

				if (obstructions[i].mirrored)
					g2.drawImage(game.structM.solid[obstructions[i].imageID].image, gameX, gameY, obstructions[i].boundingBox.width, obstructions[i].boundingBox.height, null);
				else
					g2.drawImage(game.structM.solid[obstructions[i].imageID].image, gameX + obstructions[i].boundingBox.width, gameY, -obstructions[i].boundingBox.width,
							obstructions[i].boundingBox.height, null);

			}
		}
	}

	private void renderItems(Graphics2D g2) {
		List<SuperWeapon> worldWeapons = game.itemM.worldWeapons;
		for (int i = 0; i < worldWeapons.size(); i++) {

			SuperWeapon weap = worldWeapons.get(i);

			int worldX = weap.worldX;
			int worldY = weap.worldY;
			int gameX = worldX - game.player.worldX + game.player.screenX;
			int gameY = worldY - game.player.worldY + game.player.screenY;

			if (worldX + game.tileSize > game.player.worldX - game.player.screenX && worldX - game.tileSize < game.player.worldX + game.player.screenX
					&& worldY + game.tileSize > game.player.worldY - game.player.screenY && worldY - game.tileSize < game.player.worldY + game.player.screenY) {
				Color c = new Color(255, 255, 50);
				g2.setColor(c);
				g2.setStroke(new BasicStroke(2));
				g2.drawOval(gameX + weap.entityArea.x, gameY + weap.entityArea.y, 18, 18);
				g2.drawImage(weap.entityImg, gameX, gameY, weap.imgIconWidth, weap.imgIconHeight, null);
			}
		}

	}

}