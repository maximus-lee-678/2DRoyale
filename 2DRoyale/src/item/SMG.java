package item;

import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import entity.Player;
import main.Game;
import net.Pkt06Shoot;

public class SMG extends SuperWeapon  {
	public SMG(Player player, Game game) {
		super(player, game);

		this.name = "SMG";
		this.imgOffset = -5;
		this.speed = 20;
		this.range = 10 * game.tileSize;
		this.bulletSpread = 10; //in degrees
		this.bulletSize = 8;
		this.fireRate = 2;
		this.damage = 5;

		try {
			this.sprite = ImageIO.read(getClass().getResourceAsStream("/player/smghand.png"));
			this.imgIcon = ImageIO.read(getClass().getResourceAsStream("/weap/SMG.png"));
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
