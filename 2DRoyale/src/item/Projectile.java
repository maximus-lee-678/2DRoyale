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

	public void update() {
		// Move projectile forward
		worldX += xVel;
		worldY += yVel;
		// Update distance travel
		double aSqr = Math.abs(xVel) * Math.abs(xVel);
		double bSqr = Math.abs(yVel) * Math.abs(yVel);
		travelDistance += Math.sqrt(aSqr + bSqr);
	}

	public boolean checkDistance() {
		// Check if projectile reached maximum range
		if (travelDistance > weap.range)
			return true;
		return false;
	}

	// Check projectile collision
	public boolean hasCollided() {

		int entityLeftWorldX = (int) (worldX + xVel);
		int entityRightWorldX = (int) (worldX + weap.bulletSize + xVel);
		int entityTopWorldY = (int) (worldY + yVel);
		int entityBottomWorldY = (int) (worldY + weap.bulletSize + yVel);

		double adjustedxVel, adjustedyVel;

		if (xVel > 0)
			adjustedxVel = Math.ceil(xVel);
		else
			adjustedxVel = Math.floor(xVel);

		if (yVel > 0)
			adjustedyVel = Math.ceil(yVel);
		else
			adjustedyVel = Math.floor(yVel);

		System.out.println(xVel + "," + adjustedxVel);
		System.out.println(yVel + "," + adjustedyVel);

		if (weap.game.tileM.hasCollidedWorld((int) adjustedxVel, 0, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;
		if (weap.game.tileM.hasCollidedWorld(0, (int) adjustedyVel, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;

		if (weap.game.structM.hasCollidedBuilding((int) adjustedxVel, 0, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;
		if (weap.game.structM.hasCollidedBuilding(0, (int) adjustedyVel, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;

		if (weap.game.structM.hasCollidedCrate((int) adjustedxVel, 0, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;
		if (weap.game.structM.hasCollidedCrate(0, (int) adjustedyVel, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;

		if (weap.game.structM.hasCollidedObstruction((int) adjustedxVel, 0, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;
		if (weap.game.structM.hasCollidedObstruction(0, (int) adjustedyVel, entityLeftWorldX, entityRightWorldX, entityTopWorldY, entityBottomWorldY, "Projectile"))
			return true;

		return false;
	}
}