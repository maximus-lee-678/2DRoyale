package tile;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import main.Game;

public class TileManager {

	Game game;
	Tile[] tile;
	int mapTileNum[][];

	public TileManager(Game game) {

		this.game = game;
		tile = new Tile[10];			//Currently we just store 10 types of tiles

		mapTileNum = new int[game.maxWorldCol][game.maxWorldRow];	//create 2d array of the dimension of the world (units: tileSize)
		
		getTileImage();					//populate tile array
		loadMap("/maps/world01.txt");	//load map
		
	}

	public void getTileImage() {
		
		//load png image [\res\tiles] and store into tile arr
		try {

			tile[0] = new Tile();
			tile[0].image = ImageIO.read(getClass().getResourceAsStream("/tiles/grass.png"));

			tile[1] = new Tile();
			tile[1].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wall.png"));

			tile[2] = new Tile();
			tile[2].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water.png"));
			
			tile[3] = new Tile();
			tile[3].image = ImageIO.read(getClass().getResourceAsStream("/tiles/earth.png"));
			
			tile[4] = new Tile();
			tile[4].image = ImageIO.read(getClass().getResourceAsStream("/tiles/tree.png"));
			
			tile[5] = new Tile();
			tile[5].image = ImageIO.read(getClass().getResourceAsStream("/tiles/sand.png"));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void loadMap(String filePath) {
		
		//Currently it reads map txt file [\res\maps\world01.txt]
		//General idea is to convert the values in the txt file to the corresponding tile id
		try {
			
			InputStream is = getClass().getResourceAsStream(filePath);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			int col = 0;
			int row = 0;
			
			while(col < game.maxWorldCol && row < game.maxWorldRow) {
				
				String line = br.readLine();
				while(col < game.maxWorldCol) {
					String numbers[] = line.split(" ");
					int num = Integer.parseInt(numbers[col]);
					
					mapTileNum[col][row] = num;
					col++;
				}
				if(col == game.maxWorldCol) {
					col = 0;
					row++;
				}	
				
			}
			br.close();
			
		} catch (Exception e) {
		}

	}

	public void render(Graphics2D g2) {
		/**
			g2.drawImage(tile[0].image, 0, 0, game.tileSize, game.tileSize, null);
			g2.drawImage(tile[1].image, 48, 0, game.tileSize, game.tileSize, null);
			g2.drawImage(tile[2].image, 96, 0, game.tileSize, game.tileSize, null);
		 */
		int worldCol = 0;
		int worldRow = 0;

		while (worldCol < game.maxWorldCol && worldRow < game.maxWorldRow) {
			
			int tileNum = mapTileNum[worldCol][worldRow];	
			int worldX = worldCol * game.tileSize;
			int worldY = worldRow * game.tileSize;
			int gameX = worldX - game.player.worldX + game.player.screenX;
			int gameY = worldY - game.player.worldY + game.player.screenY;
			
			if(worldX + game.tileSize > game.player.worldX - game.player.screenX &&
					worldX - game.tileSize < game.player.worldX + game.player.screenX &&
					worldY + game.tileSize > game.player.worldY - game.player.screenY &&
					worldY - game.tileSize < game.player.worldY + game.player.screenY) {
				g2.drawImage(tile[tileNum].image, gameX, gameY, game.tileSize, game.tileSize, null);
			}		
			
			worldCol++;

			if (worldCol == game.maxWorldCol) {
				worldCol = 0;
				worldRow++;
			}
		}

	}
}
