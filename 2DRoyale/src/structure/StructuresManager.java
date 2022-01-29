package structure;

import java.awt.Graphics2D;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Game;
import tile.Tile;

public class StructuresManager {

	private Game game;
	public int buildingTileSize = 16;
	public Tile[] tile;
	public Building[] building;

	public StructuresManager(Game game) {

		this.game = game;
		tile = new Tile[10];
		building = new Building[2];

		getTileImage(); // populate tile array
		loadBuildings();

	}

	private void loadBuildings() {
		
		building[0] = new Building("/blueprint/building1.txt", 800, 800, buildingTileSize);
		building[1] = new Building("/blueprint/building2.txt", 1500, 1500, buildingTileSize);

	}

	private void getTileImage() {

		try {

			tile[0] = new Tile();
			tile[0].image = ImageIO.read(getClass().getResourceAsStream("/tiles/marble.png"));

			tile[1] = new Tile();
			tile[1].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wall.png"));
			tile[1].collision = true;

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void render(Graphics2D g2) {
		

		
	}

	public boolean hasCollided(int xa, int ya, int entityTopWorldY, int entityBottomWorldY, int entityLeftWorldX,
			int entityRightWorldX, int buildingIndex) {

		int checkLimitX = building[buildingIndex].boundingBox.width / buildingTileSize - 1;
		int checkLimitY = building[buildingIndex].boundingBox.height / buildingTileSize - 1;
		
		// get coords of player relative to top left of bounding box
		int entityLeftCol = (entityLeftWorldX - building[buildingIndex].boundingBox.x) / buildingTileSize;
		int entityRightCol = (entityRightWorldX - building[buildingIndex].boundingBox.x) / buildingTileSize;
		int entityTopRow = (entityTopWorldY - building[buildingIndex].boundingBox.y) / buildingTileSize;
		int entityBottomRow = (entityBottomWorldY - building[buildingIndex].boundingBox.y) / buildingTileSize;

		// if the player's adjacent tile is not within bounding box, e.g. going down and facing tile 12,
		// will be out of bound, so set it to face tile 11. Will not interfere with collision, if
		// the player reached tile 11, they can path through that tile to begin with.
		if (entityLeftCol > checkLimitX) entityLeftCol = entityRightCol;
		if (entityRightCol > checkLimitX) entityRightCol = entityLeftCol;
		if (entityTopRow > checkLimitY) entityTopRow = entityBottomRow;
		if (entityBottomRow > checkLimitY) entityBottomRow = entityTopRow;

		int tileNum1 = 0, tileNum2 = 0;
		int[] rowNum;

		if (ya < 0) { // UP
			rowNum = building[buildingIndex].buildingTileNum.get(entityTopRow);
			tileNum1 = rowNum[entityLeftCol];
			tileNum2 = rowNum[entityRightCol];			
		}
		if (ya > 0) { // DOWN
			rowNum = building[buildingIndex].buildingTileNum.get(entityBottomRow);
			tileNum1 = rowNum[entityLeftCol];
			tileNum2 = rowNum[entityRightCol];
		}
		if (xa < 0) { // LEFT
			rowNum = building[buildingIndex].buildingTileNum.get(entityTopRow);
			tileNum1 = rowNum[entityLeftCol];
			rowNum = building[buildingIndex].buildingTileNum.get(entityBottomRow);
			tileNum2 = rowNum[entityLeftCol];
		}
		if (xa > 0) { // RIGHT
			rowNum = building[buildingIndex].buildingTileNum.get(entityTopRow);
			tileNum1 = rowNum[entityRightCol];
			rowNum = building[buildingIndex].buildingTileNum.get(entityBottomRow);
			tileNum2 = rowNum[entityRightCol];
		}
		if (tile[tileNum1].collision || tile[tileNum2].collision) return true;

		return false;
	}
}