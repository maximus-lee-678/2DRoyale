package item;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Weapon {

	private String name;	
	private double damage;
	private double range;
	private int fireRate;
	
	public BufferedImage equipImg;
	public BufferedImage itemImg;
	
	private List<Projectile> bullets = new ArrayList<Projectile>();
	
	public Weapon(String name) {
		this.name = name;
	}
	
	public void shoot() {
		
	}
}