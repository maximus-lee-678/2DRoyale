package tile;

public class MapTiles {
	public Tile tile;
	public boolean isFlipped = false;
	public boolean isGassed = false;

	public MapTiles(Tile tile, boolean isFlipped) {
		this.tile = tile;
		this.isFlipped = isFlipped;
	}

	public void setIsGassed(boolean isGassed) {
		this.isGassed = isGassed;
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
}
