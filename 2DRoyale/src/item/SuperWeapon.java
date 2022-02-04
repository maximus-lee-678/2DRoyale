package item;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import entity.Entity;
import main.Game;

public class SuperWeapon extends Entity implements shootInterface{

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
	
	public synchronized List<Projectile> getBullets() {
		return bullets;
	}

	public void render(Graphics2D g2) {
		for (int i = 0; i < getBullets().size(); i++) {
			int x = (int) getBullets().get(i).worldX - game.player.worldX + game.player.screenX;
			int y = (int) getBullets().get(i).worldY - game.player.worldY + game.player.screenY;

			g2.drawImage(bulletImg, x, y, bulletSize, bulletSize, null); // Draw player
		}
	}

	public void update() {
		for (int i = 0; i < getBullets().size(); i++) {
			Projectile p = getBullets().get(i);
			if(p.hasCollided() || p.checkTime())
				getBullets().remove(i);
			else
				p.update();
		}
	}
	
	public void updateMPProjectiles(double projAngle, int worldX, int worldY) {
		Projectile bullet = new Projectile(this, projAngle, worldX, worldY);
		getBullets().add(bullet);
	}

	public void shoot() {};
}
