package item;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import entity.Entity;
import main.Game;

public class SuperWeapon extends Entity {

	public Game game;

	public String name;
	public double damage;
	public int range;
	public int speed;
	public int fireRate;
	public int bulletSpread;
	public int bulletSize;

	public int fireRateTick;
	public int imgOffset;

	public List<Projectile> bullets = new ArrayList<Projectile>();
	public BufferedImage bulletImg;

	public SuperWeapon(Game game) {
		this.game = game;
		this.fireRateTick = 0;
	}

	public void shoot() {

	}

	public void render(Graphics2D g2) {

	}

	public void update() {

	}
}
