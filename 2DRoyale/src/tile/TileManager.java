package tile;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import main.Game;

public class TileManager {

	private Game game;
	public Tile[] tile;
	public int mapTileNum[][];

	public TileManager(Game game) {

		this.game = game;
		tile = new Tile[10];			//Currently we just store 10 types of tiles

		mapTileNum = new int[game.maxWorldCol][game.maxWorldRow];	//create 2d array of the dimension of the world (units: tileSize)
		
		getTileImage();					//populate tile array
		loadMap("res/maps/picture.png");	//load map
		
	}

	public void getTileImage() {
		
		//load png image [\res\tiles] and store into tile arr
		try {

			tile[0] = new Tile();
			tile[0].image = ImageIO.read(getClass().getResourceAsStream("/tiles/grass.png"));

			tile[1] = new Tile();
			tile[1].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wall.png"));
			tile[1].collision = true;

			tile[2] = new Tile();
			tile[2].image = ImageIO.read(getClass().getResourceAsStream("/tiles/water.png"));
			tile[2].collision = true;
			
			tile[3] = new Tile();
			tile[3].image = ImageIO.read(getClass().getResourceAsStream("/tiles/earth.png"));
			
			tile[4] = new Tile();
			tile[4].image = ImageIO.read(getClass().getResourceAsStream("/tiles/tree.png"));
			tile[4].collision = true;
			
			tile[5] = new Tile();
			tile[5].image = ImageIO.read(getClass().getResourceAsStream("/tiles/sand.png"));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void loadMap(String filePath) {
		try {		
			File file = new File(filePath);
			BufferedImage img = ImageIO.read(file);
			
			for (int y = 0; y < game.maxWorldCol; y++) {
		         for (int x = 0; x < game.maxWorldRow; x++) {
		            //Retrieving contents of a pixel
		            int pixel = img.getRGB(x,y);
		            //Creating a Color object from pixel value
		            Color color = new Color(pixel, true);
		            //Retrieving the R G B values
		            int red = color.getRed();
		            int green = color.getGreen();
		            int blue = color.getBlue();
		            if(red == 34 && green == 177 && blue == 76) {	//grass
		            	mapTileNum[x][y] = 0;
		            }
		            if(red == 0 && green == 0 && blue == 0) {		//wall
		            	mapTileNum[x][y] = 1;
		            }
		            if(red == 0 && green == 162 && blue == 232) {	//woter
		            	mapTileNum[x][y] = 2;
		            }
		            if(red == 185 && green == 122 && blue == 87) {	//earth
		            	mapTileNum[x][y] = 3;
		            }
		            if(red == 181 && green == 230 && blue == 29) {	//tree
		            	mapTileNum[x][y] = 4;
		            }
		            if(red == 239 && green == 228 && blue == 176) {	//sand
		            	mapTileNum[x][y] = 5;
		            }
		         }
		      }
			
//			for (int y = 0; y < game.maxWorldCol; y++) {
//		         for (int x = 0; x < game.maxWorldRow; x++) {
//		        	 System.out.print(mapTileNum[x][y]);
//		         }
//		         System.out.println();
//		    }
		} catch (Exception e) {
			System.out.println(e);
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
