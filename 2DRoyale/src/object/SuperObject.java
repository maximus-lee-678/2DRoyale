package object;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import main.Game;

public class SuperObject {

	public BufferedImage image;
	public String name;
	public int quantity;
	public int worldX, worldY;
	
	public void draw(Graphics2D g2, Game gp) {
		g2.drawImage(image, worldX, worldY, 25, 25, null);
		Color c = new Color(255,50,50);
		g2.setColor(c);
		g2.fillRect(worldX*2, worldY, quantity*2, worldY/2);
	}

}
