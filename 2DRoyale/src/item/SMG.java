package item;

import java.awt.Graphics2D;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import main.Game;

public class SMG extends SuperWeapon {
	public SMG(Game game) {
		super(game);

		this.name = "SMG";
		this.imgOffset = -5;
		this.speed = 20;
		this.range = 10 * game.tileSize;
		this.bulletSpread = 10; //in degrees
		this.bulletSize = 8;
		this.fireRate = 2;

		try {
			this.sprite = ImageIO.read(getClass().getResourceAsStream("/player/smghand.png"));
			this.bulletImg = ImageIO.read(getClass().getResourceAsStream("/projectile/bullet1.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shoot() {

		fireRateTick++;
		if(fireRateTick == fireRate) {
			int worldX = game.player.worldX + game.playerSize / 2 - bulletSize / 2;
			int worldY = game.player.worldY + game.playerSize / 2 - bulletSize / 2;
			double angle = Math.atan2(game.player.mouseX - game.player.screenX, game.player.mouseY - game.player.screenY);
			
			Random rand = new Random();
			int spreadRad = rand.nextInt(bulletSpread*2+1)-bulletSpread;
			angle += Math.toRadians(spreadRad);
			
			Projectile bullet = new Projectile(this, angle, worldX, worldY);
			getBullets().add(bullet);
			
			fireRateTick = 0;
		}	

	}
	
	private synchronized List<Projectile> getBullets() {
		return bullets;
	}

	@Override
	public void update() {

		for (int i = 0; i < getBullets().size(); i++) {
			getBullets().get(i).update();
			if (getBullets().get(i).checkTime())
				getBullets().remove(i);
		}
	}

	public void render(Graphics2D g2) {

		for (int i = 0; i < getBullets().size(); i++) {
			int x = (int) getBullets().get(i).worldX - game.player.worldX + game.player.screenX;
			int y = (int) getBullets().get(i).worldY - game.player.worldY + game.player.screenY;

			g2.drawImage(bulletImg, x, y, bulletSize, bulletSize, null); // Draw player
		}

	}
}
