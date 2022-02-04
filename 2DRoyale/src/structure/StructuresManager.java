package structure;

import java.awt.Graphics2D;
import java.io.IOException;

import javax.imageio.ImageIO;

import main.Game;
import tile.Tile;

public class StructuresManager {

//	private Game game;
	public int buildingTileSize = 16;
	public Tile[] tile;
	public Building[] building;

	public StructuresManager(Game game) {

//		this.game = game;
		tile = new Tile[10];
		building = new Building[2];

		getTileImage(); // populate tile array
		loadBuildings();

	}

	private void loadBuildings() {

		building[0] = new Building("/blueprint/building1.txt", 900, 900, buildingTileSize);
		building[1] = new Building("/blueprint/building2.txt", 1500, 1500, buildingTileSize);

	}

	private void getTileImage() {

		try {
			
			tile[0] = new Tile();
			tile[0].image = ImageIO.read(getClass().getResourceAsStream("/tiles/missing.png"));

			tile[1] = new Tile();
			tile[1].image = ImageIO.read(getClass().getResourceAsStream("/tiles/marble.png"));

			tile[2] = new Tile();
			tile[2].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wall.png"));
			tile[2].collisionPlayer = true;
			tile[2].collisionProjectile = true;
			
			tile[3] = new Tile();
			tile[3].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wallHL.png"));
			tile[3].collisionPlayer = true;
			tile[3].collisionProjectile = true;
			
			tile[4] = new Tile();
			tile[4].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wallHC.png"));
			tile[4].collisionPlayer = true;
			tile[4].collisionProjectile = true;
			
			tile[5] = new Tile();
			tile[5].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wallHR.png"));
			tile[5].collisionPlayer = true;
			tile[5].collisionProjectile = true;
			
			tile[6] = new Tile();
			tile[6].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wallVT.png"));
			tile[6].collisionPlayer = true;
			tile[6].collisionProjectile = true;
			
			tile[7] = new Tile();
			tile[7].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wallVC.png"));
			tile[7].collisionPlayer = true;
			tile[7].collisionProjectile = true;
			
			tile[8] = new Tile();
			tile[8].image = ImageIO.read(getClass().getResourceAsStream("/tiles/wallVB.png"));
			tile[8].collisionPlayer = true;
			tile[8].collisionProjectile = true;
			
			tile[9] = new Tile();
			tile[9].image = ImageIO.read(getClass().getResourceAsStream("/tiles/earth1.png"));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void render(Graphics2D g2) {

	}

	public boolean hasCollided(int xa, int ya, int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY,
			int entityBottomWorldY, String type) {

		int buildingIndex;
		for (buildingIndex = 0; buildingIndex < building.length; buildingIndex++) {
			int structX = building[buildingIndex].boundingBox.x;
			int structY = building[buildingIndex].boundingBox.y;
			int structWidth = building[buildingIndex].boundingBox.width;
			int structHeight = building[buildingIndex].boundingBox.height;

			if (entityLeftWorldX < structX + structWidth && entityRightWorldX > structX
					&& entityTopWorldY < structY + structHeight && entityBottomWorldY > structY) {
				int checkLimitX = building[buildingIndex].boundingBox.width / buildingTileSize - 1;
				int checkLimitY = building[buildingIndex].boundingBox.height / buildingTileSize - 1;

				// get coords of player relative to top left of bounding box
				int entityLeftCol = (entityLeftWorldX - building[buildingIndex].boundingBox.x) / buildingTileSize;
				int entityRightCol = (entityRightWorldX - building[buildingIndex].boundingBox.x) / buildingTileSize;
				int entityTopRow = (entityTopWorldY - building[buildingIndex].boundingBox.y) / buildingTileSize;
				int entityBottomRow = (entityBottomWorldY - building[buildingIndex].boundingBox.y) / buildingTileSize;

				// if the player's adjacent tile is not within bounding box, e.g. going down and
				// facing tile 12,
				// will be out of bound, so set it to face tile 11. Will not interfere with
				// collision, if
				// the player reached tile 11, they can path through that tile to begin with.
				if (entityLeftCol > checkLimitX)
					entityLeftCol = entityRightCol;
				if (entityRightCol > checkLimitX)
					entityRightCol = entityLeftCol;
				if (entityTopRow > checkLimitY)
					entityTopRow = entityBottomRow;
				if (entityBottomRow > checkLimitY)
					entityBottomRow = entityTopRow;

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
				if (type == "Entity") {
					if (tile[tileNum1].collisionPlayer || tile[tileNum2].collisionPlayer)
						return true;
				} else if (type == "Projectile") {
					if (tile[tileNum1].collisionProjectile || tile[tileNum2].collisionProjectile)
						return true;
				}
				
			}

		}

		return false;

	}
}