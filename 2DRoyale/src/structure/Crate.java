package structure;

import java.awt.Rectangle;

public class Crate {

	private int crateId;
	private Rectangle collisionBoundingBox;
	private Rectangle interactBoundingBox;
	private int imageID;

	public Crate(int crateId, int crateTileSize, int interactRadius, int crateTileNum) {
		this.crateId = crateId;
		this.collisionBoundingBox = new Rectangle(0, 0, crateTileSize, crateTileSize);
		this.interactBoundingBox = new Rectangle(0, 0, crateTileSize + 2 * interactRadius, crateTileSize + 2 * interactRadius);
		this.imageID = crateTileNum;
	}
	
	public void setCollisionBoxXY(int x, int y) {
		this.collisionBoundingBox.x = x;
		this.collisionBoundingBox.y = y;
	}
	
	public void setInteractBoxXY(int x, int y) {
		this.interactBoundingBox.x = x;
		this.interactBoundingBox.y = y;
	}

	public int getCrateId() {
		return crateId;
	}

	public Rectangle getCollisionBoundingBox() {
		return collisionBoundingBox;
	}

	public Rectangle getInteractBoundingBox() {
		return interactBoundingBox;
	}

	public int getImageID() {
		return imageID;
	}
	
	
}
