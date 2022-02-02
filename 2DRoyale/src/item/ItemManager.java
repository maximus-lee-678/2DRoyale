package item;

public class ItemManager {

	public SuperWeapon[] weaponsArr;

	public ItemManager() {
		weaponsArr = new SuperWeapon[10];

		loadWeapons();
	}

	private void loadWeapons() {
//		weaponsArr[0] = new Rifle();
	}

}