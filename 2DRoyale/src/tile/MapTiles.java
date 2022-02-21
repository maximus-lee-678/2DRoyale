package tile;

public class MapTiles {
	private Tile tile;
	private boolean isFlipped;
	private boolean isGassed;
	private boolean hasBuilding;

		public MapTiles(Tile tile, boolean isFlipped) {
		this.tile = tile;
		this.isFlipped = isFlipped;
	}

	public void setIsGassed(boolean isGassed) {
		this.isGassed = isGassed;
	}
	
	public void setHasBuilding(boolean hasBuilding) {
		this.hasBuilding = hasBuilding;
	}
	
	public Tile getTile() {
		return tile;
	}

	public boolean isFlipped() {
		return isFlipped;
	}

	public boolean isGassed() {
		return isGassed;
	}
	
	public boolean hasBuilding() {
		return hasBuilding;
	}
}
