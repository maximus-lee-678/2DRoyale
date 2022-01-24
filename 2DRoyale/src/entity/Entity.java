package entity;

import java.awt.image.BufferedImage;

public class Entity {

	/*	All entity inherits this class. Entity can include:
	 * 	1) Player (implemented)
	 *  2) Weapons on the floor (idea)
	 *  3) Bullets (idea)
	 */	
	public int worldX, worldY;
	public int speed;
	
	public BufferedImage sprite;	//image (png) currently: [\res\player] and [\res\tiles]
	
}
