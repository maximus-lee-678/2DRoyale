package item;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import entity.Entity;
import entity.Player;
import entity.PlayerMP;
import main.Game;
import main.RenderInterface;
import net.GameServer;
import net.Pkt09ServerBulletHit;
import net.Pkt12DropWeapon;
import net.Pkt16Death;

public abstract class SuperWeapon extends Entity implements ShootInterface, Cloneable, RenderInterface {

	public Game game;
	public Player player;

	public String name;
	public int id;
	public int typeId;
	public double damage;
	public int range;
	public int speed;
	public int fireRate;
	public int bulletSpread;
	public int bulletSize;
	public int bulletIdCount;
	public int fireRateTick;

	public BufferedImage entityImg;
	public int imgIconWidth;
	public int imgIconHeight;
	public int imgOffset;

	public List<Projectile> bullets;
	public BufferedImage bulletImg;

	public SuperWeapon(Game game, Player player) {
		this.game = game;
		this.player = player;
		this.fireRateTick = 0;
		this.bulletIdCount = 0;
		this.bullets = new ArrayList<Projectile>();
	}

	public synchronized List<Projectile> getBullets() {
		return bullets;
	}

	// Render projectiles (bullets)
	public void render(Graphics2D g2) {
		for (int i = 0; i < getBullets().size(); i++) {
			int x = (int) getBullets().get(i).worldX - game.player.worldX + game.player.screenX;
			int y = (int) getBullets().get(i).worldY - game.player.worldY + game.player.screenY;

			g2.drawImage(bulletImg, x, y, bulletSize, bulletSize, null); // Draw player
		}
	}

	// Update projectiles (bullets)
	public void update() {
		for (int i = 0; i < getBullets().size(); i++) {
			Projectile p = getBullets().get(i);
			// Check if projectile collided or expired
			if (p.hasCollided() || p.checkDistance())
				getBullets().remove(i--);
			else
				p.update();
		}
	}

	// Handle shoot event
	public abstract void shoot();

	////////// SERVER AND CLIENT FUNCTIONS //////////
	public void updateMPProjectiles(double projAngle, int worldX, int worldY) {
		Projectile bullet = new Projectile(bulletIdCount++, this, projAngle, worldX, worldY);
		getBullets().add(bullet);
	}

	public void serverHit(int bulletId) {
		for (int i = 0; i < getBullets().size(); i++) {
			Projectile p = getBullets().get(i);
			if (bulletId == p.id)
				getBullets().remove(i--);
		}
	}

	public void checkPlayerHit(GameServer socketServer) {
		for (PlayerMP p : socketServer.connectedPlayers) {

			if (player.getUsername().equals(p.getUsername()) || player.playerState != p.playerState)
				continue;
			for (int i = 0; i < getBullets().size(); i++) {
				Projectile proj = getBullets().get(i);

				if (p.worldX < proj.worldX + bulletSize && p.worldX + game.playerSize > proj.worldX && p.worldY < proj.worldY + bulletSize
						&& p.worldY + game.playerSize > proj.worldY) {
					getBullets().remove(i--);
					Pkt09ServerBulletHit serverHitPacket = new Pkt09ServerBulletHit(player.getUsername(), p.getUsername(), this.id, proj.id);
					serverHitPacket.sendData(socketServer);

					p.updatePlayerHP(-this.damage);
					if (p.health == 0) {
						if (p.getWeapons()[p.playerWeapIndex] != null) {
							SuperWeapon dropWeap = p.getWeapons()[p.playerWeapIndex];
							Pkt12DropWeapon dropPacket = new Pkt12DropWeapon(p.getUsername(), p.playerWeapIndex, dropWeap.typeId, dropWeap.id,
									p.worldX - dropWeap.imgIconWidth / 2 + game.playerSize / 2, p.worldY - dropWeap.imgIconHeight / 2 + game.playerSize / 2);
							dropPacket.sendData(game.socketClient);
						}
						p.playerState = socketServer.endState;
						Pkt16Death deathPacket = new Pkt16Death(player.getUsername(), p.getUsername(), socketServer.playerRemaining--);
						deathPacket.sendData(socketServer);
					}
				}
			}
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		SuperWeapon cloned = (SuperWeapon) super.clone();
		cloned.bullets = new ArrayList<Projectile>();
		return cloned;
	}
}
