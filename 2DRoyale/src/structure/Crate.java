package structure;

import java.awt.Rectangle;

public class Crate {

	public int crateId;
	public Rectangle collisionBoundingBox;
	public Rectangle interactBoundingBox;
	public int imageID;

	public Crate(int crateId, int crateTileSize, int interactRadius, int crateTileNum) {
		this.crateId = crateId;
		this.collisionBoundingBox = new Rectangle(0, 0, crateTileSize, crateTileSize);
		this.interactBoundingBox = new Rectangle(0, 0, crateTileSize + 2 * interactRadius, crateTileSize + 2 * interactRadius);
		this.imageID = crateTileNum;
	}
}
