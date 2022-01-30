package item;

public class Projectile {

	public double worldX;
	public double worldY;
	private double travelDistance;
	
	private SuperWeapon weap;
	private double xVel;
	private double yVel;
	
	public Projectile(SuperWeapon weap, double angle, int worldX, int worldY) {
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
}