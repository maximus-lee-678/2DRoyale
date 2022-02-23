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

public abstract class SuperWeapon extends Entity implements Cloneable, RenderInterface {

	protected Game game;
	protected Player player;

	protected String name;
	protected int id;
	protected int typeId;
	protected double damage;
	protected int range;
	protected int speed;
	protected int fireRate;
	protected int bulletSpread;
	protected int bulletSize;
	protected int bulletIdCount;
	protected int fireRateTick;

	protected BufferedImage entityImg;
	protected int imgIconWidth;
	protected int imgIconHeight;
	protected int imgOffset;

	protected List<Projectile> bullets;
	protected BufferedImage bulletImg;

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
			int x = (int) getBullets().get(i).getWorldX() - game.player.getWorldX() + game.player.getScreenX();
			int y = (int) getBullets().get(i).getWorldY() - game.player.getWorldY() + game.player.getScreenY();

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
			if (bulletId == p.getId()) {
				getBullets().remove(i--);
				break;
			}				
		}
	}

	public void checkPlayerHit(GameServer socketServer) {
		for (PlayerMP p : socketServer.getPlayers()) {

			if (player.getUsername().equals(p.getUsername()) || player.getPlayerState() != p.getPlayerState())
				continue;
			for (int i = 0; i < getBullets().size(); i++) {
				Projectile proj = getBullets().get(i);

				if (p.getWorldX() < proj.getWorldX() + bulletSize && p.getWorldX() + Game.playerSize > proj.getWorldX() && p.getWorldY() < proj.getWorldY() + bulletSize
						&& p.getWorldY() + Game.playerSize > proj.getWorldY()) {
					getBullets().remove(i--);
					Pkt09ServerBulletHit serverHitPacket = new Pkt09ServerBulletHit(player.getUsername(), p.getUsername(), this.id, proj.getId());
					serverHitPacket.sendData(socketServer);

					p.updatePlayerHP(-this.damage);
					if (p.getHealth() == 0) {
						if (p.getWeapons()[p.getPlayerWeapIndex()] != null) {
							SuperWeapon dropWeap = p.getWeapons()[p.getPlayerWeapIndex()];
							Pkt12DropWeapon dropPacket = new Pkt12DropWeapon(p.getUsername(), p.getPlayerWeapIndex(), dropWeap.typeId, dropWeap.id,
									p.getWorldX() - dropWeap.imgIconWidth / 2 + Game.playerSize / 2, p.getWorldY() - dropWeap.imgIconHeight / 2 + Game.playerSize / 2);
							dropPacket.sendData(game.socketClient);
						}
						p.setPlayerState(Game.endState);
						Pkt16Death deathPacket = new Pkt16Death(player.getUsername(), p.getUsername(), socketServer.getPlayerRemaining());
						socketServer.decrementPlayerRemaining();
						deathPacket.sendData(socketServer);
					}
				}
			}
		}
	}
	
	

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getTypeId() {
		return typeId;
	}

	public int getImgIconWidth() {
		return imgIconWidth;
	}

	public int getImgIconHeight() {
		return imgIconHeight;
	}

	public int getImgOffset() {
		return imgOffset;
	}

	public double getDamage() {
		return damage;
	}

	public BufferedImage getEntityImg() {
		return entityImg;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		SuperWeapon cloned = (SuperWeapon) super.clone();
		cloned.bullets = new ArrayList<Projectile>();
		return cloned;
	}
}
