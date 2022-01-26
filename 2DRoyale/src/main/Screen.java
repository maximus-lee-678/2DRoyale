package main;

import java.awt.Graphics2D;

import entity.PlayerMP;

public class Screen {
	
	private Game game;
	

	public final int maxScreenCol = 32;
	public final int maxScreenRow = 24;
	public final int screenWidth;
	public final int screenHeight;
	
	public Screen(Game game) {
		this.game = game;
		this.screenWidth = game.tileSize * maxScreenCol;
		this.screenHeight = game.tileSize * maxScreenRow;
	}
	
	public void render(Graphics2D g2) {
		
		if (game.gameState == game.titleState) {
			game.ui.draw(g2);			
		}
		
		// Others
		else {
			renderWorld(g2);
			for (PlayerMP p : game.getPlayers())
				p.render(g2);

			g2.dispose();
		}
		
	}
	
	private void renderWorld(Graphics2D g2) {
		int worldCol = 0;
		int worldRow = 0;

		while (worldCol < game.maxWorldCol && worldRow < game.maxWorldRow) {
			
			int tileNum = game.tileM.mapTileNum[worldCol][worldRow];	
			int worldX = worldCol * game.tileSize;
			int worldY = worldRow * game.tileSize;
			int gameX = worldX - game.player.worldX + game.player.screenX;
			int gameY = worldY - game.player.worldY + game.player.screenY;
			
			if(worldX + game.tileSize > game.player.worldX - game.player.screenX &&
					worldX - game.tileSize < game.player.worldX + game.player.screenX &&
					worldY + game.tileSize > game.player.worldY - game.player.screenY &&
					worldY - game.tileSize < game.player.worldY + game.player.screenY) {
				g2.drawImage(game.tileM.tile[tileNum].image, gameX, gameY, game.tileSize, game.tileSize, null);
			}		
			
			worldCol++;

			if (worldCol == game.maxWorldCol) {
				worldCol = 0;
				worldRow++;
			}
		}
	}
}