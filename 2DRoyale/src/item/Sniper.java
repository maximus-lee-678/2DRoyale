package item;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import entity.Player;
import main.Game;
import net.Pkt06Shoot;

public class Sniper extends SuperWeapon{

	public Sniper(Player player, Game game) {
		super(player, game);

		this.name = "Sniper";
		this.typeId = 3;
		this.imgOffset = -3;
		this.speed = 10;
		this.range = 30 * game.tileSize;
		this.bulletSpread = 0; // in degrees
		this.bulletSize = 16;
		this.fireRate = 30;
		this.damage = 50;

		try {
			this.sprite = ImageIO.read(getClass().getResourceAsStream("/player/sniperhand.png"));
			this.entityImg = ImageIO.read(getClass().getResourceAsStream("/weap/sniper.png"));
			this.bulletImg = ImageIO.read(getClass().getResourceAsStream("/projectile/bullet1.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		this.imgIconWidth = 64;
		double scale = (double) imgIconWidth / entityImg.getWidth();
		this.imgIconHeight = (int) (entityImg.getHeight() * scale);

		this.entityArea = new Rectangle();
		entityArea.height = 18;
		entityArea.width = 18;
		entityArea.x = imgIconWidth/2 - entityArea.width/2;
		entityArea.y = imgIconHeight/2 - entityArea.height/2;
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
			
			Pkt06Shoot shootPacket = new Pkt06Shoot(game.player.getUsername(), this.id, angle, worldX, worldY);
			shootPacket.sendData(game.socketClient);
			
			fireRateTick = 0;
		}	

	}


}
