package item;

import java.io.IOException;

import javax.imageio.ImageIO;

public class ItemManager {
	
	public Weapon[] weaponsArr;

	public ItemManager(){
		weaponsArr = new Weapon[10];
	}
	
	private void loadWeapons() {
		
		try {
			weaponsArr[0] = new Weapon("Assault");
			weaponsArr[0].equipImg = ImageIO.read(getClass().getResourceAsStream("/player/hand.png"));
			weaponsArr[0].itemImg = ImageIO.read(getClass().getResourceAsStream("/player/hand.png"));			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}