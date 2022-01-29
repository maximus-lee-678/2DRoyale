package main;

import java.awt.Graphics2D;

import entity.PlayerMP;
import structure.Building;

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

		// Title screen
		if (game.gameState == game.titleState) {
			game.ui.draw(g2);
		}

		// Others
		else {
			renderWorld(g2);
			renderBuildings(g2);
			for (PlayerMP p : game.getPlayers())
				p.render(g2);
		}

	}

	private void renderBuildings(Graphics2D g2) {
		int worldCol, worldRow;
		Building[] building = game.structM.building;
		int buildingTileSize = game.structM.buildingTileSize;

		for (int i = 0; i < 2; i++) {
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

				if (worldX + game.tileSize > game.player.worldX - game.player.screenX
						&& worldX - game.tileSize < game.player.worldX + game.player.screenX
						&& worldY + game.tileSize > game.player.worldY - game.player.screenY
						&& worldY - game.tileSize < game.player.worldY + game.player.screenY) {
					g2.drawImage(game.structM.tile[tileNum].image, gameX, gameY, buildingTileSize, buildingTileSize, null);
				}
				worldCol++;

				if (worldCol == building[i].boundingBox.width / buildingTileSize) {
					worldCol = 0;
					worldRow++;
				}
			}
		}
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

			if (worldX + game.tileSize > game.player.worldX - game.player.screenX
					&& worldX - game.tileSize < game.player.worldX + game.player.screenX
					&& worldY + game.tileSize > game.player.worldY - game.player.screenY
					&& worldY - game.tileSize < game.player.worldY + game.player.screenY) {
				if(game.tileM.mapTileNum[worldCol][worldRow][1] == 1)
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
}