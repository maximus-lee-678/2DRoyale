package tile;

public class MapTiles {
	private Tile tile;
	private boolean isFlipped;
	private boolean isGassed;

	public MapTiles(Tile tile, boolean isFlipped) {
		this.tile = tile;
		this.isFlipped = isFlipped;
		this.isGassed = false;
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
