package item;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import entity.Player;
import main.Game;
import net.Pkt06Shoot;

public class SMG extends SuperWeapon {
	public SMG(Game game, Player player) {
		super(game, player);

		this.name = "SMG";
		this.typeId = 1;
		this.damage = 8;
		this.range = 10 * game.tileSize;
		this.speed = 20;
		this.fireRate = 7;
		this.bulletSpread = 10; // in degrees
		this.bulletSize = 8;

		this.imgOffset = -5;
		try {
			this.sprite = ImageIO.read(getClass().getResourceAsStream("/player/smghand.png"));
			this.entityImg = ImageIO.read(getClass().getResourceAsStream("/weap/SMG.png"));
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
		entityArea.x = imgIconWidth / 2 - entityArea.width / 2;
		entityArea.y = imgIconHeight / 2 - entityArea.height / 2;
	}

	@Override
	public void shoot() {

		fireRateTick++;
		if (fireRateTick == fireRate) {
			// Spawn bullet at the player's location
			int worldX = game.player.worldX + game.playerSize / 2 - bulletSize / 2;
			int worldY = game.player.worldY + game.playerSize / 2 - bulletSize / 2;
			double angle = Math.atan2(game.player.mouseX - game.player.screenX, game.player.mouseY - game.player.screenY);

			// Get random value for weapon spread
			Random rand = new Random();
			int spreadRad = rand.nextInt(bulletSpread * 2 + 1) - bulletSpread;
			angle += Math.toRadians(spreadRad);

			// Update server on this shoot event
			new Pkt06Shoot(game.player.getUsername(), this.id, angle, worldX, worldY).sendData(game.socketClient);

			fireRateTick = 0;
			if(game.gameState == game.playState) {
				game.playSE(1);
			}
		}

	}

}
