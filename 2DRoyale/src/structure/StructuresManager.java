package structure;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import main.Game;
import tile.Tile;

public class StructuresManager {

	private Game game;

	// Building Variables
	public int buildingTileSize = 16;
	public int buildingBlueprintCount = 7;
	public boolean[][] buildingOccupiesTile;

	// Crate Variables
	public int crateTileSize = 32;
	public int interactRadius = 16;
	public List<Integer> crateTileNum;

	// Obstruction Variables
	public int obstructionTileSizeLower = 48;
	public int obstructionTileSizeUpper = 96;

	// Universal Variables
	public int offset = 48; // used to prevent solids from spawning too near each other

	public Tile[] tile;
	public Tile[] solid;
	public Building[] buildings;
	public List<Crate> crates;
	public Obstruction[] obstructions;

	public StructuresManager(Game game) {

		this.game = game;
		tile = new Tile[10];
		solid = new Tile[10];
		buildings = new Building[game.numberOfBuildings];
		crates = new ArrayList<Crate>();
		buildingOccupiesTile = new boolean[game.maxWorldCol][game.maxWorldRow]; // initialised to false
		obstructions = new Obstruction[game.numberOfObstructions];
		crateTileNum = new ArrayList<Integer>();

		getTileImage(); // populate tile array
		getSolidImage();

		// generate buildings, then crates, then environmental obstructions(wip)
		loadBuildings(game.numberOfBuildings);
		loadCrates(game.numberOfCrates);
		loadObstructions(game.numberOfObstructions);
	}

	/**
	 * Loads building textures into memory.
	 */
	private void getTileImage() {

		tile[0] = new Tile("misc", "missing.png");
		tile[1] = new Tile("buildings", "marble.png");

		tile[2] = new Tile("buildings", "wall.png", true, true);
		tile[3] = new Tile("buildings", "wallHL.png", true, true);
		tile[4] = new Tile("buildings", "wallHC.png", true, true);
		tile[5] = new Tile("buildings", "wallHR.png", true, true);
		tile[6] = new Tile("buildings", "wallVT.png", true, true);
		tile[7] = new Tile("buildings", "wallVC.png", true, true);
		tile[8] = new Tile("buildings", "wallVB.png", true, true);

	}

	/**
	 * Loads obstructive textures into memory.
	 */
	private void getSolidImage() {

		solid[0] = new Tile("solids", "crate.png", true, true);
		crateTileNum.add(0);

		solid[1] = new Tile("solids", "tree.png", true, true);
		solid[2] = new Tile("solids", "rock.png", true, true);

		solid[3] = new Tile("solids", "wintertree.png", true, true);
		solid[4] = new Tile("solids", "winterrock.png", true, true);

		solid[5] = new Tile("solids", "deadtree.png", true, true);
		solid[6] = new Tile("solids", "ashrock.png", true, true);

	}

	/**
	 * Generates buildings.
	 */
	public void loadBuildings(int numberOfBuildings) {
		int placedBuildings = 0;
		int failedBuildingAttempts = 0; // debug variable

		mainLoop: while (placedBuildings < numberOfBuildings) {
			// Create building with random blueprint
			Building tryBuilding = new Building("/blueprint/building" + game.rand.nextInt(buildingBlueprintCount) + ".txt", buildingTileSize);

			int randomX = offset + game.rand.nextInt((game.tileSize * game.maxWorldCol - tryBuilding.boundingBox.width - (offset * 2)));
			int randomY = offset + game.rand.nextInt((game.tileSize * game.maxWorldRow - tryBuilding.boundingBox.height - (offset * 2)));
			Rectangle separationHitbox = new Rectangle(randomX - offset, randomY - offset, tryBuilding.boundingBox.width + offset * 2,
					tryBuilding.boundingBox.height + offset * 2);

			int topLeftTileX = separationHitbox.x / game.tileSize;
			int topLeftTileY = separationHitbox.y / game.tileSize;

			int topRightTileX = (separationHitbox.x + separationHitbox.width) / game.tileSize;
			int bottomLeftTileY = (separationHitbox.y + separationHitbox.height) / game.tileSize;

			// Spawn buildings on solid tiles
			for (int x = topLeftTileX; x <= topRightTileX; x++) {
				for (int y = topLeftTileY; y <= bottomLeftTileY; y++) {
					if (game.tileM.mapTileNum[x][y].tile.collisionPlayer) {
						failedBuildingAttempts++;
						continue mainLoop;
					}
				}
			}

			// Prevent buildings from spawning on top of each other
			for (int i = 1; i <= placedBuildings; i++) {
				if (separationHitbox.x < buildings[i - 1].boundingBox.x + buildings[i - 1].boundingBox.width
						&& separationHitbox.x + separationHitbox.width > buildings[i - 1].boundingBox.x
						&& separationHitbox.y < buildings[i - 1].boundingBox.y + buildings[i - 1].boundingBox.height
						&& separationHitbox.y + separationHitbox.height > buildings[i - 1].boundingBox.y) {
					failedBuildingAttempts++;
					continue mainLoop;
				}
			}

			// Assign x and y coordinates now that checks have been passed
			tryBuilding.boundingBox.x = randomX;
			tryBuilding.boundingBox.y = randomY;

			// Place building into array
			buildings[placedBuildings] = tryBuilding;
			placedBuildings++;

			System.out.println("Generated " + placedBuildings + "/" + numberOfBuildings + " buildings...");
		}

		System.out.println("Building collisions: " + failedBuildingAttempts);

		// Fill buildings in tile array, for minimap rendering
		for (int i = 0; i < game.numberOfBuildings; i++) {
			int buildingTopLeftX;
			int buildingTopLeftY;
			int buildingBottomRightX;
			int buildingBottomRightY;

			buildingTopLeftX = buildings[i].boundingBox.x / game.tileSize;
			buildingTopLeftY = buildings[i].boundingBox.y / game.tileSize;
			buildingBottomRightX = (buildings[i].boundingBox.x + buildings[i].boundingBox.width) / game.tileSize;
			buildingBottomRightY = (buildings[i].boundingBox.y + buildings[i].boundingBox.height) / game.tileSize;

			for (int y = buildingTopLeftY; y <= buildingBottomRightY; y++) {
				for (int x = buildingTopLeftX; x < buildingBottomRightX; x++) {
					buildingOccupiesTile[x][y] = true;
				}
			}
		}
	}

	/**
	 * Generates crates.
	 */
	public void loadCrates(int numberOfCrates) {

		int placedCrates = 0;
		int failedCrateAttempts = 0; // debug variable

		mainLoop: while (placedCrates < numberOfCrates) {
			Crate tryCrate = new Crate(placedCrates, crateTileSize, interactRadius, crateTileNum.get(0));

			int randomX = offset + game.rand.nextInt((game.tileSize * game.maxWorldCol - crateTileSize - (offset * 2)));
			int randomY = offset + game.rand.nextInt((game.tileSize * game.maxWorldRow - crateTileSize - (offset * 2)));
			Rectangle separationHitbox = new Rectangle(randomX - offset, randomY - offset, crateTileSize + offset * 2, crateTileSize + offset * 2);

			int topLeftTileX = separationHitbox.x / game.tileSize; // int will floor the value
			int topLeftTileY = separationHitbox.y / game.tileSize;

			int topRightTileX = (separationHitbox.x + separationHitbox.width) / game.tileSize;
			int bottomLeftTileY = (separationHitbox.y + separationHitbox.height) / game.tileSize;

			// Spawn buildings on solid tiles
			for (int x = topLeftTileX; x <= topRightTileX; x++) {
				for (int y = topLeftTileY; y <= bottomLeftTileY; y++) {
					if (game.tileM.mapTileNum[x][y].tile.collisionPlayer) {
						failedCrateAttempts++;
						continue mainLoop;
					}
				}
			}

			// Prevent crates from spawning on buildings
			for (int i = 0; i < buildings.length; i++) {
				if (separationHitbox.x < buildings[i].boundingBox.x + buildings[i].boundingBox.width
						&& separationHitbox.x + separationHitbox.width > buildings[i].boundingBox.x
						&& separationHitbox.y < buildings[i].boundingBox.y + buildings[i].boundingBox.height
						&& separationHitbox.y + separationHitbox.height > buildings[i].boundingBox.y) {
					failedCrateAttempts++;
					continue mainLoop;
				}
			}

			// Prevent crates from spawning on each other
			for (int i = 1; i <= placedCrates; i++) {
				if (separationHitbox.x < crates.get(i - 1).collisionBoundingBox.x + crates.get(i - 1).collisionBoundingBox.width
						&& separationHitbox.x + separationHitbox.width > crates.get(i - 1).collisionBoundingBox.x
						&& separationHitbox.y < crates.get(i - 1).collisionBoundingBox.y + crates.get(i - 1).collisionBoundingBox.height
						&& separationHitbox.y + separationHitbox.height > crates.get(i - 1).collisionBoundingBox.y) {
					failedCrateAttempts++;
					continue mainLoop;
				}
			}

			// Assign x and y coordinates now that checks have been passed
			tryCrate.collisionBoundingBox.x = randomX;
			tryCrate.collisionBoundingBox.y = randomY;
			tryCrate.interactBoundingBox.x = randomX - interactRadius;
			tryCrate.interactBoundingBox.y = randomY - interactRadius;

			// Add crate to array
			crates.add(tryCrate);
			placedCrates++;

			System.out.println("Generated " + placedCrates + "/" + numberOfCrates + " crates...");
		}

		System.out.println("Crate collisions: " + failedCrateAttempts);
	}

	/**
	 * Generates obstructions.
	 */
	public void loadObstructions(int numberOfObstructions) {

		int placedObstructions = 0;
		int failedObstructionsAttempts = 0; // debug variable

		mainLoop: while (placedObstructions < numberOfObstructions) {
			// Get a random size for the object, then create an object with it
			int randomSize = game.rand.nextInt(obstructionTileSizeLower, obstructionTileSizeUpper + 1);
			Obstruction tryObstruction = new Obstruction(randomSize, randomSize);

			int randomX = offset + game.rand.nextInt((game.tileSize * game.maxWorldCol - randomSize - (offset * 2)));
			int randomY = offset + game.rand.nextInt((game.tileSize * game.maxWorldRow - randomSize - (offset * 2)));
			Rectangle separationHitbox = new Rectangle(randomX - offset, randomY - offset, randomSize + offset * 2, randomSize + offset * 2);

			int topLeftTileX = separationHitbox.x / game.tileSize; // int will floor the value
			int topLeftTileY = separationHitbox.y / game.tileSize;

			int topRightTileX = (separationHitbox.x + separationHitbox.width) / game.tileSize;
			int bottomLeftTileY = (separationHitbox.y + separationHitbox.height) / game.tileSize;

			// Spawn buildings on solid tiles
			for (int x = topLeftTileX; x <= topRightTileX; x++) {
				for (int y = topLeftTileY; y <= bottomLeftTileY; y++) {
					if (game.tileM.mapTileNum[x][y].tile.collisionPlayer) {
						failedObstructionsAttempts++;
						continue mainLoop;
					}
				}
			}

			// Prevent obstructions from spawning on buildings
			for (int i = 0; i < buildings.length; i++) {
				if (separationHitbox.x < buildings[i].boundingBox.x + buildings[i].boundingBox.width
						&& separationHitbox.x + separationHitbox.width > buildings[i].boundingBox.x
						&& separationHitbox.y < buildings[i].boundingBox.y + buildings[i].boundingBox.height
						&& separationHitbox.y + separationHitbox.height > buildings[i].boundingBox.y) {
					failedObstructionsAttempts++;
					continue mainLoop;
				}
			}

			// Prevent obstructions from spawning on crates
			for (int i = 0; i < crates.size(); i++) {
				if (separationHitbox.x < crates.get(i).collisionBoundingBox.x + crates.get(i).collisionBoundingBox.width
						&& separationHitbox.x + separationHitbox.width > crates.get(i).collisionBoundingBox.x
						&& separationHitbox.y < crates.get(i).collisionBoundingBox.y + crates.get(i).collisionBoundingBox.height
						&& separationHitbox.y + separationHitbox.height > crates.get(i).collisionBoundingBox.y) {
					failedObstructionsAttempts++;
					continue mainLoop;
				}
			}

			// Prevent obstructions from spawning on each other
			for (int i = 1; i <= placedObstructions; i++) {
				if (separationHitbox.x < obstructions[i - 1].boundingBox.x + obstructions[i - 1].boundingBox.width
						&& separationHitbox.x + separationHitbox.width > obstructions[i - 1].boundingBox.x
						&& separationHitbox.y < obstructions[i - 1].boundingBox.y + obstructions[i - 1].boundingBox.height
						&& separationHitbox.y + separationHitbox.height > obstructions[i - 1].boundingBox.y) {
					failedObstructionsAttempts++;
					continue mainLoop;
				}
			}

			// Assign x and y coordinates now that checks have been passed
			tryObstruction.boundingBox.x = randomX;
			tryObstruction.boundingBox.y = randomY;

			// Chooses tile image based on 'biome'
			int biomeTileX = 0, biomeTileY = 0;

			// Choose which corner of obstruction to decide biome
			switch (game.rand.nextInt(3 + 1)) {
			case 0: // top left tile
				biomeTileX = randomX / game.tileSize;
				biomeTileY = randomY / game.tileSize;
				break;
			case 1: // top right tile
				biomeTileX = (randomX / game.tileSize) + 1;
				biomeTileY = randomY / game.tileSize;
				break;
			case 2: // bottom left tile
				biomeTileX = randomX / game.tileSize;
				biomeTileY = (randomY / game.tileSize) + 1;
				break;
			case 3: // bottom right tile
				biomeTileX = (randomX / game.tileSize) + 1;
				biomeTileY = (randomY / game.tileSize) + 1;
				break;
			}

			// Picks a corresponding biome tile
			switch (game.tileM.mapTileNum[biomeTileX][biomeTileY].tile.biome) {
			case "Forest":
				tryObstruction.imageID = game.rand.nextInt(1, 2 + 1);
				break;
			case "Snow":
				tryObstruction.imageID = game.rand.nextInt(3, 4 + 1);
				break;
			case "Wasteland":
				tryObstruction.imageID = game.rand.nextInt(5, 6 + 1);
				break;
			default:
				tryObstruction.imageID = 1;
				break;
			}

			// Determines mirrored status
			tryObstruction.mirrored = game.rand.nextBoolean();

			// Place obstruction into array
			obstructions[placedObstructions] = tryObstruction;
			placedObstructions++;

			System.out.println("Generated " + placedObstructions + "/" + numberOfObstructions + " obstructions...");
		}

		System.out.println("Obstruction collisions: " + failedObstructionsAttempts);
	}

	/**
	 * Deletes crates after they have been opened.
	 */
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

	/**
	 * Checks for collision with buildings.
	 */
	public boolean hasCollidedBuilding(int xa, int ya, int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY, int entityBottomWorldY, String type) {

		for (int buildingIndex = 0; buildingIndex < buildings.length; buildingIndex++) {
			if (entityLeftWorldX < buildings[buildingIndex].boundingBox.x + buildings[buildingIndex].boundingBox.width
					&& entityRightWorldX > buildings[buildingIndex].boundingBox.x
					&& entityTopWorldY < buildings[buildingIndex].boundingBox.y + buildings[buildingIndex].boundingBox.height
					&& entityBottomWorldY > buildings[buildingIndex].boundingBox.y) {

				int checkLimitX = buildings[buildingIndex].boundingBox.width / buildingTileSize - 1;
				int checkLimitY = buildings[buildingIndex].boundingBox.height / buildingTileSize - 1;

				// Get coords of player relative to top left of bounding box
				int entityLeftCol = (entityLeftWorldX - buildings[buildingIndex].boundingBox.x) / buildingTileSize;
				int entityRightCol = (entityRightWorldX - buildings[buildingIndex].boundingBox.x) / buildingTileSize;
				int entityTopRow = (entityTopWorldY - buildings[buildingIndex].boundingBox.y) / buildingTileSize;
				int entityBottomRow = (entityBottomWorldY - buildings[buildingIndex].boundingBox.y) / buildingTileSize;

				// If the player's adjacent tile is not within bounding box, e.g. going down and
				// facing tile 12, of bound, so set it to face
				// tile 11. Will not interfere with collision, if the player reached tile 11,
				// they can path through that tile to begin with.
				if (entityLeftCol > checkLimitX)
					entityLeftCol = entityRightCol;
				if (entityRightCol > checkLimitX)
					entityRightCol = entityLeftCol;
				if (entityTopRow > checkLimitY)
					entityTopRow = entityBottomRow;
				if (entityBottomRow > checkLimitY)
					entityBottomRow = entityTopRow;

				// Floors checks to 0 to prevent out of bounds
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
					rowNum = buildings[buildingIndex].buildingTileNum.get(entityTopRow);
					tileNum1 = rowNum[entityLeftCol];
					tileNum2 = rowNum[entityRightCol];
				}
				if (ya > 0) { // DOWN
					rowNum = buildings[buildingIndex].buildingTileNum.get(entityBottomRow);
					tileNum1 = rowNum[entityLeftCol];
					tileNum2 = rowNum[entityRightCol];
				}
				if (xa < 0) { // LEFT
					rowNum = buildings[buildingIndex].buildingTileNum.get(entityTopRow);
					tileNum1 = rowNum[entityLeftCol];
					rowNum = buildings[buildingIndex].buildingTileNum.get(entityBottomRow);
					tileNum2 = rowNum[entityLeftCol];
				}
				if (xa > 0) { // RIGHT
					rowNum = buildings[buildingIndex].buildingTileNum.get(entityTopRow);
					tileNum1 = rowNum[entityRightCol];
					rowNum = buildings[buildingIndex].buildingTileNum.get(entityBottomRow);
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

	/**
	 * Checks for collision with crates.
	 */
	public boolean hasCollidedCrate(int xa, int ya, int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY, int entityBottomWorldY, String type) {

		for (int crateIndex = 0; crateIndex < crates.size(); crateIndex++) {
			Crate crate = crates.get(crateIndex);
			if (crate == null)
				return false;

			int structX = crate.collisionBoundingBox.x;
			int structY = crate.collisionBoundingBox.y;
			int structWidth = crate.collisionBoundingBox.width;
			int structHeight = crate.collisionBoundingBox.height;

			if (entityLeftWorldX < structX + structWidth && entityRightWorldX > structX && entityTopWorldY < structY + structHeight && entityBottomWorldY > structY) {
				if (type == "Entity") {
					if (solid[crate.imageID].collisionPlayer || solid[crate.imageID].collisionPlayer)
						return true;
				} else if (type == "Projectile") {
					if (solid[crate.imageID].collisionProjectile || solid[crate.imageID].collisionProjectile)
						return true;
				}
			}

		}

		return false;

	}

	/**
	 * Checks for collision with obstructions.
	 */
	public boolean hasCollidedObstruction(int xa, int ya, int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY, int entityBottomWorldY, String type) {

		for (int obstructionIndex = 0; obstructionIndex < obstructions.length; obstructionIndex++) {
			int obstructionX = obstructions[obstructionIndex].boundingBox.x;
			int obstructionY = obstructions[obstructionIndex].boundingBox.y;
			int obstructionWidth = obstructions[obstructionIndex].boundingBox.width;
			int obstructionHeight = obstructions[obstructionIndex].boundingBox.height;

			if (entityLeftWorldX < obstructionX + obstructionWidth && entityRightWorldX > obstructionX && entityTopWorldY < obstructionY + obstructionHeight
					&& entityBottomWorldY > obstructionY) {
				if (type == "Entity") {
					if (solid[obstructions[obstructionIndex].imageID].collisionPlayer || solid[obstructions[obstructionIndex].imageID].collisionPlayer)
						return true;
				} else if (type == "Projectile") {
					if (solid[obstructions[obstructionIndex].imageID].collisionProjectile || solid[obstructions[obstructionIndex].imageID].collisionProjectile)
						return true;
				}
			}
		}

		return false;
	}

	/**
	 * Checks for overlapping of player collision boxes and crate interact bounding boxes.
	 */
	public int withinCrateRange(int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY, int entityBottomWorldY) {

		for (int crateIndex = 0; crateIndex < crates.size(); crateIndex++) {
			Crate crate = crates.get(crateIndex);

			int structX = crate.interactBoundingBox.x;
			int structY = crate.interactBoundingBox.y;
			int structWidth = crate.interactBoundingBox.width;
			int structHeight = crate.interactBoundingBox.height;

			if (entityLeftWorldX < structX + structWidth && entityRightWorldX > structX && entityTopWorldY < structY + structHeight && entityBottomWorldY > structY) {
				return crate.crateId;
			}
		}

		return -1;

	}

}
