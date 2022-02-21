package entity;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

public class Entity {

	protected int worldX, worldY;
	protected int speed;

	protected BufferedImage sprite;

	protected Rectangle entityArea;

	public int getWorldX() {
		return worldX;
	}

	public void setWorldX(int worldX) {
		this.worldX = worldX;
	}

	public int getWorldY() {
		return worldY;
	}

	public void setWorldY(int worldY) {
		this.worldY = worldY;
	}
	
	public BufferedImage getSprite() {
		return sprite;
	}

	public Rectangle getEntityArea() {
		return entityArea;
	}

	
	

}
