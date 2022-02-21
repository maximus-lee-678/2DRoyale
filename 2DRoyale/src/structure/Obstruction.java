package structure;

import java.awt.Rectangle;

public class Obstruction {
	private Rectangle boundingBox;
	private int imageID;
	private boolean mirrored;

	public Obstruction(int obstructionTileSizeLower, int obstructionTileSizeUpper) {
		this.boundingBox = new Rectangle(0, 0, obstructionTileSizeLower, obstructionTileSizeUpper);
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}

	public int getImageID() {
		return imageID;
	}

	public void setImageID(int imageID) {
		this.imageID = imageID;
	}

	public boolean isMirrored() {
		return mirrored;
	}

	public void setMirrored(boolean mirrored) {
		this.mirrored = mirrored;
	}
	
	

}
