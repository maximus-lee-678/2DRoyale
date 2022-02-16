package item;

import java.util.ArrayList;
import java.util.List;

import main.Game;
import structure.Crate;

public class ItemManager {

	public SuperWeapon[] weaponsArr;
	public Game game;
	public List<SuperWeapon> worldWeapons;

	public ItemManager(Game game) {
		this.game = game;
		this.weaponsArr = new SuperWeapon[4];
		this.worldWeapons = new ArrayList<SuperWeapon>();

		loadWeapons();
	}

	// Init all weapons to weaponsArr
	private void loadWeapons() {
		weaponsArr[0] = new Rifle(game, null);
		weaponsArr[1] = new SMG(game, null);
		weaponsArr[2] = new Shotgun(game, null);
		weaponsArr[3] = new Sniper(game, null);
	}

	// Create new weapon to be placed in the world (at the crates position)
	public void spawnWeap(Crate crate, int weapType, int weapId) {
		try {
			SuperWeapon newWeap = (SuperWeapon) weaponsArr[weapType].clone();
			newWeap.id = weapId;
			newWeap.worldX = crate.collisionBoundingBox.x - newWeap.imgIconWidth / 2 + crate.collisionBoundingBox.width / 2;
			newWeap.worldY = crate.collisionBoundingBox.y - newWeap.imgIconHeight / 2 + crate.collisionBoundingBox.height / 2;
			worldWeapons.add(newWeap);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	// Drop weapon to be placed in the world (at the player position)
	public void dropWeap(int weapType, int weapId, int worldX, int worldY) {
		try {
			SuperWeapon newWeap = (SuperWeapon) weaponsArr[weapType].clone();
			newWeap.id = weapId;
			newWeap.worldX = worldX;
			newWeap.worldY = worldY;
			worldWeapons.add(newWeap);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
	}

	// When weapon is picked up, remove from the world
	public void deleteWorldWeapon(int weapId) {
		for (SuperWeapon w : worldWeapons) {
			if (w.id == weapId) {
				worldWeapons.remove(w);
				return;
			}
		}
	}

	// Check if weapon within range of player
	public SuperWeapon withinWeaponsRange(int entityLeftWorldX, int entityRightWorldX, int entityTopWorldY, int entityBottomWorldY) {
		for (int i = 0; i < worldWeapons.size(); i++) {
			SuperWeapon weap = worldWeapons.get(i);

			int weapX = weap.worldX + weap.entityArea.x;
			int weapY = weap.worldY + weap.entityArea.y;
			int weapWidth = weap.entityArea.width;
			int weapHeight = weap.entityArea.height;

			if (entityLeftWorldX < weapX + weapWidth && entityRightWorldX > weapX && entityTopWorldY < weapY + weapHeight && entityBottomWorldY > weapY)
				return weap;
		}
		return null;
	}

}