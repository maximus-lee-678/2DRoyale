package item;

import java.io.IOException;

import javax.imageio.ImageIO;

import main.Game;
import net.Pkt06Shoot;

public class Shotgun extends SuperWeapon {
	public Shotgun(Game game) {
		super(game);

		this.name = "Shotgun";
		this.imgOffset = -3;
		this.speed = 10;
		this.range = 5 * game.tileSize;
		this.bulletSpread = 10; //in degrees
		this.bulletSize = 8;
		this.fireRate = 20;

		try {
			this.sprite = ImageIO.read(getClass().getResourceAsStream("/player/shottyhand.png"));
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
			
			for(int i = -2; i < 3; i++) {
				double spreadRad = Math.toRadians(bulletSpread*i);				
				Pkt06Shoot shootPacket = new Pkt06Shoot(game.player.getUsername(), this.name, angle+spreadRad, worldX, worldY);
				shootPacket.sendData(game.socketClient);
			}
			
			fireRateTick = 0;
		}	

	}

}
