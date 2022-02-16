package item;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import entity.Player;
import main.Game;
import net.Pkt06Shoot;

public class Rifle extends SuperWeapon {

	public Rifle(Game game, Player player) {
		super(game, player);

		this.name = "Rifle";
		this.typeId = 0;
		this.damage = 18;
		this.range = 20 * game.tileSize;
		this.speed = 5;
		this.fireRate = 15;
		this.bulletSpread = 5; // in degrees
		this.bulletSize = 12;

		this.imgOffset = -3;
		try {
			this.sprite = ImageIO.read(getClass().getResourceAsStream("/player/riflehand.png"));
			this.entityImg = ImageIO.read(getClass().getResourceAsStream("/weap/rifle.png"));
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
				game.playSE(2);
			}
		}

	}

}
