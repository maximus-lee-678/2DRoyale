package structure;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import main.Game;
import tile.Tile;

public class StructuresManager {

	private Game game;

	// Building Variables
	private static final int buildingTileSize = 16;
	private static final int buildingBlueprintCount = 7;

	// Crate Variables
	private static final int crateTileSize = 32;
	private static final int interactRadius = 16;
	private List<Integer> crateTileNum;

	// Obstruction Variables
	private static final int obstructionTileSizeLower = 48;
	private static final int obstructionTileSizeUpper = 96;

	// Universal Variables
	private static final int offset = 48; // used to prevent solids from spawning too near each other

	private Tile[] buildingTile;
	private Tile[] solid;
	private Building[] buildings;
	private List<Crate> crates;
	private Obstruction[] obstructions;

	public StructuresManager(Game game, int numberOfBuildings, int numberOfObstructions, int numberOfCrates) {
		this.game = game;
		this.buildingTile = new Tile[10];
		this.solid = new Tile[10];
		this.buildings = new Building[numberOfBuildings];
		this.crates = new ArrayList<Crate>();
		this.obstructions = new Obstruction[numberOfObstructions];
		this.crateTileNum = new ArrayList<Integer>();

		getTileImage(); // populate tile array
		getSolidImage();

		// generate buildings, then crates, then environmental obstructions
		loadBuildings(numberOfBuildings);
		loadCrates(numberOfCrates);
		loadObstructions(numberOfObstructions);
	}

	/**
	 * Loads building textures into memory.
	 */
	private void getTileImage() {

		buildingTile[0] = new Tile("misc", "missing.png");
		buildingTile[1] = new Tile("buildings", "marble.png");

		buildingTile[2] = new Tile("buildings", "wall.png", true, true);
		buildingTile[3] = new Tile("buildings", "wallHL.png", true, true);
		buildingTile[4] = new Tile("buildings", "wallHC.png", true, true);
		buildingTile[5] = new Tile("buildings", "wallHR.png", true, true);
		buildingTile[6] = new Tile("buildings", "wallVT.png", true, true);
		buildingTile[7] = new Tile("buildings", "wallVC.png", true, true);
		buildingTile[8] = new Tile("buildings", "wallVB.png", true, true);

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
	private void loadBuildings(int numberOfBuildings) {
		int placedBuildings = 0;
		int failedBuildingAttempts = 0; // debug variable

		mainLoop: while (placedBuildings < numberOfBuildings) {
			// Create building with random blueprint
			Building tryBuilding = new Building("/blueprint/building" + game.getRand().nextInt(buildingBlueprintCount) + ".txt", buildingTileSize);

			int randomX = offset + game.getRand().nextInt((Game.tileSize * game.tileM.getMaxWorldCol() - tryBuilding.getBoundingBox().width - (offset * 2)));
			int randomY = offset + game.getRand().nextInt((Game.tileSize * game.tileM.getMaxWorldRow() - tryBuilding.getBoundingBox().height - (offset * 2)));
			Rectangle separationHitbox = new Rectangle(randomX - offset, randomY - offset, tryBuilding.getBoundingBox().width + offset * 2,
					tryBuilding.getBoundingBox().height + offset * 2);

			int topLeftTileX = separationHitbox.x / Game.tileSize;
			int topLeftTileY = separationHitbox.y / Game.tileSize;

			int topRightTileX = (separationHitbox.x + separationHitbox.width) / Game.tileSize;
			int bottomLeftTileY = (separationHitbox.y + separationHitbox.height) / Game.tileSize;

			// Spawn buildings on solid tiles
			for (int x = topLeftTileX; x <= topRightTileX; x++) {
				for (int y = topLeftTileY; y <= bottomLeftTileY; y++) {
					if (game.tileM.getMapTileData()[x][y].getTile().isCollisionPlayer()) {
						failedBuildingAttempts++;
						continue mainLoop;
					}
				}
			}

			// Prevent buildings from spawning on top of each other
			for (int i = 1; i <= placedBuildings; i++) {
				if (separationHitbox.x < buildings[i - 1].getBoundingBox().x + buildings[i - 1].getBoundingBox().width
						&& separationHitbox.x + separationHitbox.width > buildings[i - 1].getBoundingBox().x
						&& separationHitbox.y < buildings[i - 1].getBoundingBox().y + buildings[i - 1].getBoundingBox().height
						&& separationHitbox.y + separationHitbox.height > buildings[i - 1].getBoundingBox().y) {
					failedBuildingAttempts++;
					continue mainLoop;
				}
			}

			// Assign x and y coordinates now that checks have been passed
			tryBuilding.getBoundingBox().x = randomX;
			tryBuilding.getBoundingBox().y = randomY;

			// Place building into array
			buildings[placedBuildings] = tryBuilding;
			placedBuildings++;

			System.out.println("Generated " + placedBuildings + "/" + numberOfBuildings + " buildings...");
		}

		System.out.println("Building collisions: " + failedBuildingAttempts);

		// Sets hasBuilding in mapTileData, for minimap rendering
		for (int i = 0; i < numberOfBuildings; i++) {
			int buildingTopLeftX;
			int buildingTopLeftY;
			int buildingBottomRightX;
			int buildingBottomRightY;

			buildingTopLeftX = buildings[i].getBoundingBox().x / Game.tileSize;
			buildingTopLeftY = buildings[i].getBoundingBox().y / Game.tileSize;
			buildingBottomRightX = (buildings[i].getBoundingBox().x + buildings[i].getBoundingBox().width) / Game.tileSize;
			buildingBottomRightY = (buildings[i].getBoundingBox().y + buildings[i].getBoundingBox().height) / Game.tileSize;

			for (int y = buildingTopLeftY; y <= buildingBottomRightY; y++) {
				for (int x = buildingTopLeftX; x <= buildingBottomRightX; x++) {
					game.tileM.getMapTileData()[x][y].setHasBuilding(true);
				}
			}
		}
	}

	/**
	 * Generates crates.
	 */
	private void loadCrates(int numberOfCrates) {

		int placedCrates = 0;
		int failedCrateAttempts = 0; // debug variable

		mainLoop: while (placedCrates < numberOfCrates) {
			Crate tryCrate = new Crate(placedCrates, crateTileSize, interactRadius, crateTileNum.get(0));

			int randomX = offset + game.getRand().nextInt((Game.tileSize * game.tileM.getMaxWorldCol() - crateTileSize - (offset * 2)));
			int randomY = offset + game.getRand().nextInt((Game.tileSize * game.tileM.getMaxWorldRow() - crateTileSize - (offset * 2)));
			Rectangle separationHitbox = new Rectangle(randomX - offset, randomY - offset, crateTileSize + offset * 2, crateTileSize + offset * 2);

			int topLeftTileX = separationHitbox.x / Game.tileSize; // int will floor the value
			int topLeftTileY = separationHitbox.y / Game.tileSize;

			int topRightTileX = (separationHitbox.x + separationHitbox.width) / Game.tileSize;
			int bottomLeftTileY = (separationHitbox.y + separationHitbox.height) / Game.tileSize;

			// Spawn buildings on solid tiles
			for (int x = topLeftTileX; x <= topRightTileX; x++) {
				for (int y = topLeftTileY; y <= bottomLeftTileY; y++) {
					if (game.tileM.getMapTileData()[x][y].getTile().isCollisionPlayer()) {
						failedCrateAttempts++;
						continue mainLoop;
					}
				}
			}

			// Prevent crates from spawning on buildings
			for (int i = 0; i < buildings.length; i++) {
				if (separationHitbox.x < buildings[i].getBoundingBox().x + buildings[i].getBoundingBox().width
						&& separationHitbox.x + separationHitbox.width > buildings[i].getBoundingBox().x
						&& separationHitbox.y < buildings[i].getBoundingBox().y + buildings[i].getBoundingBox().height
						&& separationHitbox.y + separationHitbox.height > buildings[i].getBoundingBox().y) {
					failedCrateAttempts++;
					continue mainLoop;
				}
			}

			// Prevent crates from spawning on each other
			for (int i = 1; i <= placedCrates; i++) {
				if (separationHitbox.x < crates.get(i - 1).getCollisionBoundingBox().x + crates.get(i - 1).getCollisionBoundingBox().width
						&& separationHitbox.x + separationHitbox.width > crates.get(i - 1).getCollisionBoundingBox().x
						&& separationHitbox.y < crates.get(i - 1).getCollisionBoundingBox().y + crates.get(i - 1).getCollisionBoundingBox().height
						&& separationHitbox.y + separationHitbox.height > crates.get(i - 1).getCollisionBoundingBox().y) {
					failedCrateAttempts++;
					continue mainLoop;
				}
			}

			// Assign x and y coordinates now that checks have been passed
			tryCrate.setCollisionBoxXY(randomX, randomY);
			tryCrate.setInteractBoxXY(randomX - interactRadius, randomY - interactRadius);

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
	private void loadObstructions(int numberOfObstructions) {

		int placedObstructions = 0;
		int failedObstructionsAttempts = 0; // debug variable

		mainLoop: while (placedObstructions < numberOfObstructions) {
			// Get a random size for the object, then create an object with it
			int randomSize = game.getRand().nextInt(obstructionTileSizeLower, obstructionTileSizeUpper + 1);
			Obstruction tryObstruction = new Obstruction(randomSize, randomSize);

			int randomX = offset + game.getRand().nextInt((Game.tileSize * game.tileM.getMaxWorldCol() - randomSize - (offset * 2)));
			int randomY = offset + game.getRand().nextInt((Game.tileSize * game.tileM.getMaxWorldRow() - randomSize - (offset * 2)));
			Rectangle separationHitbox = new Rectangle(randomX - offset, randomY - offset, randomSize + offset * 2, randomSize + offset * 2);

			int topLeftTileX = separationHitbox.x / Game.tileSize; // int will floor the value
			int topLeftTileY = separationHitbox.y / Game.tileSize;

			int topRightTileX = (separationHitbox.x + separationHitbox.width) / Game.tileSize;
			int bottomLeftTileY = (separationHitbox.y + separationHitbox.height) / Game.tileSize;

			// Spawn buildings on solid tiles
			for (int x = topLeftTileX; x <= topRightTileX; x++) {
				for (int y = topLeftTileY; y <= bottomLeftTileY; y++) {
					if (game.tileM.getMapTileData()[x][y].getTile().isCollisionPlayer()) {
						failedObstructionsAttempts++;
						continue mainLoop;
					}
				}
			}

			// Prevent obstructions from spawning on buildings
			for (int i = 0; i < buildings.length; i++) {
				if (separationHitbox.x < buildings[i].getBoundingBox().x + buildings[i].getBoundingBox().width
						&& separationHitbox.x + separationHitbox.width > buildings[i].getBoundingBox().x
						&& separationHitbox.y < buildings[i].getBoundingBox().y + buildings[i].getBoundingBox().height
						&& separationHitbox.y + separationHitbox.height > buildings[i].getBoundingBox().y) {
					failedObstructionsAttempts++;
					continue mainLoop;
				}
			}

			// Prevent obstructions from spawning on crates
			for (int i = 0; i < crates.size(); i++) {
				if (separationHitbox.x < crates.get(i).getCollisionBoundingBox().x + crates.get(i).getCollisionBoundingBox().width
						&& separationHitbox.x + separationHitbox.width > crates.get(i).getCollisionBoundingBox().x
						&& separationHitbox.y < crates.get(i).getCollisionBoundingBox().y + crates.get(i).getCollisionBoundingBox().height
						&& separationHitbox.y + separationHitbox.height > crates.get(i).getCollisionBoundingBox().y) {
					failedObstructionsAttempts++;
					continue mainLoop;
				}
			}

			// Prevent obstructions from spawning on each other
			for (int i = 1; i <= placedObstructions; i++) {
				if (separationHitbox.x < obstructions[i - 1].getBoundingBox().x + obstructions[i - 1].getBoundingBox().width
						&& separationHitbox.x + separationHitbox.width > obstructions[i - 1].getBoundingBox().x
						&& separationHitbox.y < obstructions[i - 1].getBoundingBox().y + obstructions[i - 1].getBoundingBox().height
						&& separationHitbox.y + separationHitbox.height > obstructions[i - 1].getBoundingBox().y) {
					failedObstructionsAttempts++;
					continue mainLoop;
				}
			}

			// Assign x and y coordinates now that checks have been passed
			tryObstruction.getBoundingBox().x = randomX;
			tryObstruction.getBoundingBox().y = randomY;

			// Chooses tile image based on 'biome'
			int biomeTileX = 0, biomeTileY = 0;

			// Choose which corner of obstruction to decide biome
			switch (game.getRand().nextInt(3 + 1)) {
			case 0: // top left tile
				biomeTileX = randomX / Game.tileSize;
				biomeTileY = randomY / Game.tileSize;
				break;
			case 1: // top right tile
				biomeTileX = (randomX / Game.tileSize) + 1;
				biomeTileY = randomY / Game.tileSize;
				break;
			case 2: // bottom left tile
				biomeTileX = randomX / Game.tileSize;
				biomeTileY = (randomY / Game.tileSize) + 1;
				break;
			case 3: // bottom right tile
				biomeTileX = (randomX / Game.tileSize) + 1;
				biomeTileY = (randomY / Game.tileSize) + 1;
				break;
			}

			// Picks a corresponding biome tile
			switch (game.tileM.getMapTileData()[biomeTileX][biomeTileY].getTile().getBiome()) {
			case "Forest":
				tryObstruction.setImageID(game.getRand().nextInt(1, 2 + 1));
				break;
			case "Snow":
				tryObstruction.setImageID(game.getRand().nextInt(3, 4 + 1));
				break;
			case "Wasteland":
				tryObstruction.setImageID(game.getRand().nextInt(5, 6 + 1));
				break;
			default:
				tryObstruction.setImageID(1);
				break;
			}

			// Determines mirrored status
			tryObstruction.setMirrored(game.getRand().nextBoolean());

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
			if (crate.getCrateId() == delCrateId) {
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
			if (entityLeftWorldX < buildings[buildingIndex].getBoundingBox().x + buildings[buildingIndex].getBoundingBox().width
					&& entityRightWorldX > buildings[buildingIndex].getBoundingBox().x
					&& entityTopWorldY < buildings[buildingIndex].getBoundingBox().y + buildings[buildingIndex].getBoundingBox().height
					&& entityBottomWorldY > buildings[buildingIndex].getBoundingBox().y) {

				int checkLimitX = buildings[buildingIndex].getBoundingBox().width / buildingTileSize - 1;
				int checkLimitY = buildings[buildingIndex].getBoundingBox().height / buildingTileSize - 1;

				// Get coords of player relative to top left of bounding box
				int entityLeftCol = (entityLeftWorldX - buildings[buildingIndex].getBoundingBox().x) / buildingTileSize;
				int entityRightCol = (entityRightWorldX - buildings[buildingIndex].getBoundingBox().x) / buildingTileSize;
				int entityTopRow = (entityTopWorldY - buildings[buildingIndex].getBoundingBox().y) / buildingTileSize;
				int entityBottomRow = (entityBottomWorldY - buildings[buildingIndex].getBoundingBox().y) / buildingTileSize;

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
					rowNum = buildings[buildingIndex].getBuildingTileNum().get(entityTopRow);
					tileNum1 = rowNum[entityLeftCol];
					tileNum2 = rowNum[entityRightCol];
				}
				if (ya > 0) { // DOWN
					rowNum = buildings[buildingIndex].getBuildingTileNum().get(entityBottomRow);
					tileNum1 = rowNum[entityLeftCol];
					tileNum2 = rowNum[entityRightCol];
				}
				if (xa < 0) { // LEFT
					rowNum = buildings[buildingIndex].getBuildingTileNum().get(entityTopRow);
					tileNum1 = rowNum[entityLeftCol];
					rowNum = buildings[buildingIndex].getBuildingTileNum().get(entityBottomRow);
					tileNum2 = rowNum[entityLeftCol];
				}
				if (xa > 0) { // RIGHT
					rowNum = buildings[buildingIndex].getBuildingTileNum().get(entityTopRow);
					tileNum1 = rowNum[entityRightCol];
					rowNum = buildings[buildingIndex].getBuildingTileNum().get(entityBottomRow);
					tileNum2 = rowNum[entityRightCol];
				}
				if (type == "Entity") {
					if (buildingTile[tileNum1].isCollisionPlayer() || buildingTile[tileNum2].isCollisionPlayer())
						return true;
				} else if (type == "Projectile") {
					if (buildingTile[tileNum1].isCollisionProjectile() || buildingTile[tileNum2].isCollisionProjectile())
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

			int structX = crate.getCollisionBoundingBox().x;
			int structY = crate.getCollisionBoundingBox().y;
			int structWidth = crate.getCollisionBoundingBox().width;
			int structHeight = crate.getCollisionBoundingBox().height;

			if (entityLeftWorldX < structX + structWidth && entityRightWorldX > structX && entityTopWorldY < structY + structHeight && entityBottomWorldY > structY) {
				if (type == "Entity") {
					if (solid[crate.getImageID()].isCollisionPlayer() || solid[crate.getImageID()].isCollisionPlayer())
						return true;
				} else if (type == "Projectile") {
					if (solid[crate.getImageID()].isCollisionProjectile() || solid[crate.getImageID()].isCollisionProjectile())
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
			int obstructionX = obstructions[obstructionIndex].getBoundingBox().x;
			int obstructionY = obstructions[obstructionIndex].getBoundingBox().y;
			int obstructionWidth = obstructions[obstructionIndex].getBoundingBox().width;
			int obstructionHeight = obstructions[obstructionIndex].getBoundingBox().height;

			if (entityLeftWorldX < obstructionX + obstructionWidth && entityRightWorldX > obstructionX && entityTopWorldY < obstructionY + obstructionHeight
					&& entityBottomWorldY > obstructionY) {
				if (type == "Entity") {
					if (solid[obstructions[obstructionIndex].getImageID()].isCollisionPlayer() || solid[obstructions[obstructionIndex].getImageID()].isCollisionPlayer())
						return true;
				} else if (type == "Projectile") {
					if (solid[obstructions[obstructionIndex].getImageID()].isCollisionProjectile() || solid[obstructions[obstructionIndex].getImageID()].isCollisionProjectile())
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

			int structX = crate.getInteractBoundingBox().x;
			int structY = crate.getInteractBoundingBox().y;
			int structWidth = crate.getInteractBoundingBox().width;
			int structHeight = crate.getInteractBoundingBox().height;

			if (entityLeftWorldX < structX + structWidth && entityRightWorldX > structX && entityTopWorldY < structY + structHeight && entityBottomWorldY > structY) {
				return crate.getCrateId();
			}
		}

		return -1;

	}
	
	public static int getBuildingTileSize() {
		return buildingTileSize;
	}

	public static int getCrateTileSize() {
		return crateTileSize;
	}

	public Tile[] getBuildingTile() {
		return buildingTile;
	}
	
	public Tile[] getSolid() {
		return solid;
	}

	public Building[] getBuildings() {
		return buildings;
	}

	public List<Crate> getCrates() {
		return crates;
	}

	public Obstruction[] getObstructions() {
		return obstructions;
	}
}
