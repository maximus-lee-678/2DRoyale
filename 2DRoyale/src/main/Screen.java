package main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

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
			
		}

		game.ui.draw(g2);

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
			while (worldCol < building[i].boundingBox.width / buildingTileSize && worldRow < building[i].boundingBox.height / buildingTileSize) {

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