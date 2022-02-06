package structure;

import java.awt.Rectangle;

public class Crate {
	public Rectangle collisionBoundingBox;
	public Rectangle interactBoundingBox;
	public int crateTileNum;
	
	public Crate(int crateTileSize, int interactRadius, int crateTileNum) {
		this.collisionBoundingBox = new Rectangle(0, 0, crateTileSize, crateTileSize);
		this.interactBoundingBox = new Rectangle(0, 0, crateTileSize + 2 * interactRadius, crateTileSize + 2 * interactRadius);
		this.crateTileNum = crateTileNum;
	}
}
