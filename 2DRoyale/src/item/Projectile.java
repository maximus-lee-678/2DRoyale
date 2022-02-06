package item;

public class Projectile {

	public int id;
	public double worldX;
	public double worldY;
	private double travelDistance;
	
	private SuperWeapon weap;
	private double xVel;
	private double yVel;
	
	public Projectile(int id, SuperWeapon weap, double angle, int worldX, int worldY) {
		this.id = id;
		this.weap = weap;
		this.xVel = weap.speed * Math.sin(angle);
		this.yVel = weap.speed * Math.cos(angle);
		this.worldX = worldX;
		this.worldY = worldY;
	}
	
	public void update(){
		worldX += xVel;
		worldY += yVel;
		
		travelDistance += Math.abs(xVel) +  Math.abs(yVel);		
	}

	public boolean checkTime() {
		if(travelDistance > weap.range)
			return true;
		return false;
	}
	
	public boolean hasCollided() {
		
		int entityLeftWorldX = (int) (worldX + xVel);
		int entityRightWorldX = (int) (worldX + weap.bulletSize + xVel);
		int entityTopWorldY = (int) (worldY + yVel);
		int entityBottomWorldY = (int) (worldY + weap.bulletSize + yVel);

		if (weap.game.tileM.hasCollided((int)xVel, 0, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;
		if (weap.game.tileM.hasCollided(0, (int)yVel, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;
		
		if (weap.game.structM.hasCollidedBuilding((int)xVel, 0, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;		
		if (weap.game.structM.hasCollidedBuilding(0, (int)yVel, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;

		return false;
	}
	
}