package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import javax.imageio.ImageIO;

import entity.PlayerMP;
import item.SuperWeapon;
import structure.Building;
import structure.Crate;

public class Screen {

	private Game game;

	public final int maxScreenCol = 24;
	public final int maxScreenRow = 18;
	public final int screenWidth;
	public final int screenHeight;

	public Screen(Game game) {
		this.game = game;
		this.screenWidth = game.tileSize * maxScreenCol;
		this.screenHeight = game.tileSize * maxScreenRow;
	}

	public void render(Graphics2D g2) {

		if (game.gameState == game.playState) {
			renderWorld(g2);
			renderBuildings(g2);
			renderCrates(g2);
			renderItems(g2);
			for (PlayerMP p : game.getPlayers())
				p.renderBullets(g2);
			for (PlayerMP p : game.getPlayers())
				p.render(g2);
			renderMinimap(g2);
		}

		game.ui.draw(g2);

	}

	private void renderMinimap(Graphics2D g2) {
		int minimapRadius = 18;
		int minimapTileSize = 4;
		int minimapRenderAt = 48;

		int minimapBackSize = (minimapRadius * 2 + 1) * minimapTileSize + (minimapTileSize * 2);
		int minimapBackRenderAt = minimapRenderAt - minimapTileSize;

		// Draw minimap back
		g2.drawImage(game.structM.tile[0].image, minimapBackRenderAt, minimapBackRenderAt, minimapBackSize,
				minimapBackSize, null);

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

		// Draw tiles
		for (int y = yLowerBound; y < yUpperBound + 1; y++) {
			minimapX = 0;
			for (int x = xLowerBound; x < xUpperBound + 1; x++) {
				int tileNum = game.tileM.mapTileNum[x][y][0];

				if (game.tileM.mapTileNum[x][y][1] == 1)
					g2.drawImage(game.tileM.tile[tileNum].image,
							minimapRenderAt + minimapX + (xOffset * minimapTileSize),
							minimapRenderAt + minimapY + (yOffset * minimapTileSize), minimapTileSize, minimapTileSize,
							null);
				else
					g2.drawImage(game.tileM.tile[tileNum].image,
							minimapRenderAt + minimapX + minimapTileSize + (xOffset * minimapTileSize),
							minimapRenderAt + minimapY + (yOffset * minimapTileSize), -minimapTileSize, minimapTileSize,
							null);
				minimapX += minimapTileSize;
			}
			minimapY += minimapTileSize;
		}

//		Building[] building = game.structM.building;
//		int[][] buildingOccupiesTile = new int[game.maxWorldRow][game.maxWorldCol];
//
//		for (int y = 0; y < game.maxWorldCol; y++) {
//			for (int x = 0; x < game.maxWorldRow; x++) {
//				buildingOccupiesTile[x][y] = 0;
//			}
//		}
//
//		for (int i = 0; i < game.numberOfBuildings; i++) {
//			int buildingTopLeftX;
//			int buildingTopLeftY;
//			int buildingBottomRightX;
//			int buildingBottomRightY;
//
//			buildingTopLeftX = building[i].boundingBox.x / game.tileSize;
//			buildingTopLeftY = building[i].boundingBox.y / game.tileSize;
//			buildingBottomRightX = (building[i].boundingBox.x + building[i].boundingBox.width) / game.tileSize;
//			buildingBottomRightY = (building[i].boundingBox.y + building[i].boundingBox.height) / game.tileSize;
//			
//			for (int j = buildingTopLeftX ; j <= buildingBottomRightX ; j++) {
//				
//			}
//		}

		// Draw player sprite (terrible quality)
		g2.drawImage(game.player.sprite, minimapRenderAt + (minimapRadius * minimapTileSize),
				minimapRenderAt + (minimapRadius * minimapTileSize), minimapTileSize, minimapTileSize, null);
	}

	private void renderWorld(Graphics2D g2) {
		int worldCol = 0;
		int worldRow = 0;

		while (worldCol < game.maxWorldCol && worldRow < game.maxWorldRow) {

			int tileNum = game.tileM.mapTileNum[worldCol][worldRow][0];
			int worldX = worldCol * game.tileSize;
			int worldY = worldRow * game.tileSize;
			int gameX = worldX - game.player.worldX + game.player.screenX;
			int gameY = worldY - game.player.worldY + game.player.screenY;

			if (worldX + game.tileSize > game.player.worldX - game.player.screenX && worldX - game.tileSize < game.player.worldX + game.player.screenX && worldY + game.tileSize > game.player.worldY - game.player.screenY && worldY - game.tileSize < game.player.worldY + game.player.screenY) {
				if (game.tileM.mapTileNum[worldCol][worldRow][1] == 1)
					g2.drawImage(game.tileM.tile[tileNum].image, gameX, gameY, game.tileSize, game.tileSize, null);
				else
					g2.drawImage(game.tileM.tile[tileNum].image, gameX + game.tileSize, gameY, -game.tileSize, game.tileSize, null);
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
		Building[] building = game.structM.building;
		int buildingTileSize = game.structM.buildingTileSize;

		for (int i = 0; i < game.numberOfBuildings; i++) {
			worldCol = 0;
			worldRow = 0;
			while (worldCol < building[i].boundingBox.width / buildingTileSize
					&& worldRow < building[i].boundingBox.height / buildingTileSize) {

				int[] rowNum = building[i].buildingTileNum.get(worldRow);
				int tileNum = rowNum[worldCol];
				int worldX = building[i].boundingBox.x + (worldCol * buildingTileSize);
				int worldY = building[i].boundingBox.y + (worldRow * buildingTileSize);
				int gameX = worldX - game.player.worldX + game.player.screenX;
				int gameY = worldY - game.player.worldY + game.player.screenY;

				if (tileNum != 0) {
					if (worldX + game.tileSize > game.player.worldX - game.player.screenX && worldX - game.tileSize < game.player.worldX + game.player.screenX && worldY + game.tileSize > game.player.worldY - game.player.screenY && worldY - game.tileSize < game.player.worldY + game.player.screenY) {
						g2.drawImage(game.structM.tile[tileNum].image, gameX, gameY, buildingTileSize, buildingTileSize, null);
					}
				}

				worldCol++;

				if (worldCol == building[i].boundingBox.width / buildingTileSize) {
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

			if (worldX + game.tileSize > game.player.worldX - game.player.screenX && worldX - game.tileSize < game.player.worldX + game.player.screenX && worldY + game.tileSize > game.player.worldY - game.player.screenY && worldY - game.tileSize < game.player.worldY + game.player.screenY) {
				g2.drawImage(game.structM.obstruction[crate.crateTileNum].image, gameX, gameY, crateTileSize, crateTileSize, null);
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
						
			if (worldX + game.tileSize > game.player.worldX - game.player.screenX && worldX - game.tileSize < game.player.worldX + game.player.screenX && worldY + game.tileSize > game.player.worldY - game.player.screenY && worldY - game.tileSize < game.player.worldY + game.player.screenY) {
				Color c = new Color(255, 255, 50);
				g2.setColor(c);
				g2.setStroke(new BasicStroke(2));
				g2.drawOval(gameX + weap.entityArea.x, gameY + weap.entityArea.y, 18, 18);
				g2.drawImage(weap.entityImg, gameX, gameY, weap.imgIconWidth, weap.imgIconHeight, null);				
			}
		}
		
	}

}