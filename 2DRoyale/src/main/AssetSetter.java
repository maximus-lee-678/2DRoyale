package main;

import object.OBJ_Health;

public class AssetSetter {
	
	Game gp;

	public AssetSetter(Game gp) {
		this.gp = gp;
	}
	
	public void setObject() {
		
		gp.obj[0] = new OBJ_Health();
		gp.obj[0].worldX = gp.tileSize;
		gp.obj[0].worldY = gp.tileSize;
		gp.obj[0].quantity = 100;
		
	}

}
