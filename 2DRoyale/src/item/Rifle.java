package item;

import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import main.Game;
import net.Pkt06Shoot;

public class Rifle extends SuperWeapon{

	public Rifle(Game game) {
		super(game);

		this.name = "Rifle";
		this.imgOffset = -3;
		this.speed = 2;
		this.range = 20 * game.tileSize;
		this.bulletSpread = 5; // in degrees
		this.bulletSize = 12;
		this.fireRate = 10;

		try {
			this.sprite = ImageIO.read(getClass().getResourceAsStream("/player/riflehand.png"));
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
			
			Pkt06Shoot shootPacket = new Pkt06Shoot(game.player.getUsername(), this.name, angle, worldX, worldY);
			shootPacket.sendData(game.socketClient);
			
			fireRateTick = 0;
		}	

	}


}
