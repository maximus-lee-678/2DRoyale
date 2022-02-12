package structure;

import java.awt.Rectangle;

public class Obstruction {
	public Rectangle boundingBox;
	public int imageID;
	public boolean mirrored;
	
	public Obstruction(int obstructionTileSizeLower, int obstructionTileSizeUpper){
		this.boundingBox = new Rectangle(0, 0, obstructionTileSizeLower, obstructionTileSizeUpper);
	}

}
