package structure;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import main.Game;
import tile.Tile;

public class StructuresManager {

	private Game game;

	// Building Variables
	public int buildingTileSize = 16;
	public int buildingBlueprintCount = 7;
	public int[][] buildingOccupiesTile;

	// Crate Variables
	public int crateTileSize = 32;
	public int interactRadius = 16;
	public List<Integer> crateTileNum;

	// Universal Variables
	public int offset = 48;

	public Tile[] tile;
	public Tile[] obstruction;
	public Building[] building;
	public List<Crate> crates;

	public StructuresManager(Game game) {

		this.game = game;
		tile = new Tile[10];
		obstruction = new Tile[10];
		building = new Building[game.numberOfBuildings];
		crates = new ArrayList<Crate>();// new Crate[game.numberOfCrates];
		buildingOccupiesTile = new int[game.maxWorldCol][game.maxWorldRow];
		crateTileNum = new ArrayList<Integer>();

		getTileImage(); // populate tile array
		getObstructionImage();

		// generate buildings, then crates, then environmental obstructions(wip)
		loadBuildings(game.numberOfBuildings);
		loadCrates(game.numberOfCrates);
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

	private void getObstructionImage() {
		try {

			obstruction[0] = new Tile();
			obstruction[0].image = ImageIO.read(getClass().getResourceAsStream("/crate/crate.png"));
			obstruction[0].collisionPlayer = true;
			obstruction[0].collisionProjectile = true;
			crateTileNum.add(0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void loadBuildings(int numberOfBuildings) {
		int placedBuildings = 0;
		int failedBuildingAttempts = 0; // debug variable

		mainLoop:
		while (placedBuildings < numberOfBuildings) {
			Building tryBuilding = new Building(
					"/blueprint/building" + game.rand.nextInt(buildingBlueprintCount) + ".txt", buildingTileSize);

			int randomX = offset + game.rand
					.nextInt((game.tileSize * game.maxWorldCol - tryBuilding.boundingBox.width - (offset * 2)));
			int randomY = offset + game.rand
					.nextInt((game.tileSize * game.maxWorldRow - tryBuilding.boundingBox.height - (offset * 2)));
			Rectangle separationHitbox = new Rectangle(randomX - offset, randomY - offset,
					tryBuilding.boundingBox.width + offset * 2, tryBuilding.boundingBox.height + offset * 2);

			int topLeftTileX = separationHitbox.x / game.tileSize;
			int topLeftTileY = separationHitbox.y / game.tileSize;

			int topRightTileX = (separationHitbox.x + separationHitbox.width) / game.tileSize;
			int bottomLeftTileY = (separationHitbox.y + separationHitbox.height) / game.tileSize;

			for (int x = topLeftTileX; x <= topRightTileX; x++) { // prevent buildings from spawning on obstructions
				for (int y = topLeftTileY; y <= bottomLeftTileY; y++) {
					if (game.tileM.tile[game.tileM.mapTileNum[x][y][0]].collisionPlayer) {
						failedBuildingAttempts++;
						continue mainLoop;
					}
				}
			}

			for (int i = 1; i <= placedBuildings; i++) { // prevent buildings from spawning on top of each other
				if (separationHitbox.x < building[i - 1].boundingBox.x + building[i - 1].boundingBox.width
						&& separationHitbox.x + separationHitbox.width > building[i - 1].boundingBox.x
						&& separationHitbox.y < building[i - 1].boundingBox.y + building[i - 1].boundingBox.height
						&& separationHitbox.y + separationHitbox.height > building[i - 1].boundingBox.y) {
					failedBuildingAttempts++;
					continue mainLoop;
				}
			}

			tryBuilding.boundingBox.x = randomX;
			tryBuilding.boundingBox.y = randomY;

			building[placedBuildings] = tryBuilding;
			placedBuildings++;
			
			System.out.println("Generated " + placedBuildings + "/" + numberOfBuildings + " buildings...");
		}

		System.out.println("Building collisions: " + failedBuildingAttempts);
		
		// Fill buildings in tile array, for minimap rendering
		for (int y = 0; y < game.maxWorldCol; y++) {
			for (int x = 0; x < game.maxWorldRow; x++) {
				buildingOccupiesTile[x][y] = 0;
			}
		}

		for (int i = 0; i < game.numberOfBuildings; i++) {
			int buildingTopLeftX;
			int buildingTopLeftY;
			int buildingBottomRightX;
			int buildingBottomRightY;

			buildingTopLeftX = building[i].boundingBox.x / game.tileSize;
			buildingTopLeftY = building[i].boundingBox.y / game.tileSize;
			buildingBottomRightX = (building[i].boundingBox.x + building[i].boundingBox.width) / game.tileSize;
			buildingBottomRightY = (building[i].boundingBox.y + building[i].boundingBox.height) / game.tileSize;

			for (int y = buildingTopLeftY; y <= buildingBottomRightY; y++) {
				for (int x = buildingTopLeftX; x < buildingBottomRightX; x++) {
					buildingOccupiesTile[x][y] = 1;
				}
			}
		}
	}
	public void loadCrates(int numberOfCrates) {

		int placedCrates = 0;
		int failedCrateAttempts = 0; // debug variable

		mainLoop: while (placedCrates < numberOfCrates) {
			Crate tryCrate = new Crate(placedCrates, crateTileSize, interactRadius, crateTileNum.get(0));

			int randomX = offset + game.rand.nextInt((game.tileSize * game.maxWorldCol - crateTileSize - (offset * 2)));
			int randomY = offset + game.rand.nextInt((game.tileSize * game.maxWorldRow - crateTileSize - (offset * 2)));
			Rectangle separationHitbox = new Rectangle(randomX - offset, randomY - offset, crateTileSize + offset * 2,
					crateTileSize + offset * 2);

			int topLeftTileX = separationHitbox.x / game.tileSize; // int will floor the value
			int topLeftTileY = separationHitbox.y / game.tileSize;

			int topRightTileX = (separationHitbox.x + separationHitbox.width) / game.tileSize;
			int bottomLeftTileY = (separationHitbox.y + separationHitbox.height) / game.tileSize;

			for (int x = topLeftTileX; x <= topRightTileX; x++) { // prevent crates from spawning on obstructions
				for (int y = topLeftTileY; y <= bottomLeftTileY; y++) {
					if (game.tileM.tile[game.tileM.mapTileNum[x][y][0]].collisionPlayer) {
						failedCrateAttempts++;
						continue mainLoop;
					}
				}
			}

			for (int i = 0; i < building.length; i++) { // prevent crates from spawning on top of buildings
				if (separationHitbox.x < building[i].boundingBox.x + building[i].boundingBox.width
						&& separationHitbox.x + separationHitbox.width > building[i].boundingBox.x
						&& separationHitbox.y < building[i].boundingBox.y + building[i].boundingBox.height
						&& separationHitbox.y + separationHitbox.height > building[i].boundingBox.y) {
					failedCrateAttempts++;
					continue mainLoop;
				}
			}

			for (int i = 1; i <= placedCrates; i++) { // prevent crates from spawning on top of each other
				if (separationHitbox.x < crates.get(i - 1).collisionBoundingBox.x
						+ crates.get(i - 1).collisionBoundingBox.width
						&& separationHitbox.x + separationHitbox.width > crates.get(i - 1).collisionBoundingBox.x
						&& separationHitbox.y < crates.get(i - 1).collisionBoundingBox.y
								+ crates.get(i - 1).collisionBoundingBox.height
						&& separationHitbox.y + separationHitbox.height > crates.get(i - 1).collisionBoundingBox.y) {
					failedCrateAttempts++;
					continue mainLoop;
				}
			}

			tryCrate.collisionBoundingBox.x = randomX;
			tryCrate.collisionBoundingBox.y = randomY;
			tryCrate.interactBoundingBox.x = randomX - interactRadius;
			tryCrate.interactBoundingBox.y = randomY - interactRadius;

			crates.add(tryCrate);
			placedCrates++;
			
			System.out.println("Generated " + placedCrates + "/" + numberOfCrates + " crates...");
		}

		System.out.println("Crate collisions: " + failedCrateAttempts);
	}


	public Crate deleteCrate(int delCrateId) {
		for (int i = 0; i < crates.size(); i++) {
			Crate crate = crates.get(i);
			if (crate.crateId == delCrateId) {
				crates.remove(i);
				return crate;
			}
		}
		return null;
	}

	public boolean hasCollidedBuilding(int xa, int ya, int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY,
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
				
				if (entityLeftCol < 0)
					entityLeftCol = 0;
				if (entityRightCol < 0)
					entityRightCol = 0;
				if (entityTopRow < 0)
					entityTopRow = 0;
				if (entityBottomRow < 0)
					entityBottomRow = 0;

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

	public boolean hasCollidedCrate(int xa, int ya, int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY,
			int entityBottomWorldY, String type) {

		for (int crateIndex = 0; crateIndex < crates.size(); crateIndex++) {
			Crate crate = crates.get(crateIndex);

			int structX = crate.collisionBoundingBox.x;
			int structY = crate.collisionBoundingBox.y;
			int structWidth = crate.collisionBoundingBox.width;
			int structHeight = crate.collisionBoundingBox.height;

			if (entityLeftWorldX < structX + structWidth && entityRightWorldX > structX
					&& entityTopWorldY < structY + structHeight && entityBottomWorldY > structY) {
				if (type == "Entity") {
					if (obstruction[crate.crateTileNum].collisionPlayer
							|| obstruction[crate.crateTileNum].collisionPlayer)
						return true;
				} else if (type == "Projectile") {
					if (obstruction[crate.crateTileNum].collisionProjectile
							|| obstruction[crate.crateTileNum].collisionProjectile)
						return true;
				}
			}

		}

		return false;

	}

	public int withinCrateRange(int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY,
			int entityBottomWorldY) {

		for (int crateIndex = 0; crateIndex < crates.size(); crateIndex++) {
			Crate crate = crates.get(crateIndex);

			int structX = crate.interactBoundingBox.x;
			int structY = crate.interactBoundingBox.y;
			int structWidth = crate.interactBoundingBox.width;
			int structHeight = crate.interactBoundingBox.height;

			if (entityLeftWorldX < structX + structWidth && entityRightWorldX > structX
					&& entityTopWorldY < structY + structHeight && entityBottomWorldY > structY) {
				return crate.crateId;
			}
		}

		return -1;

	}
}
