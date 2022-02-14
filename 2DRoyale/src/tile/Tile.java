package tile;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class Tile {

	public BufferedImage image;
	public boolean collisionPlayer = false;
	public boolean collisionProjectile = false;
	public String biome;

	// no biomes, no collision
	public Tile(String folder, String fileName) {
		try {
			this.image = toCompatibleImage(ImageIO.read(getClass().getResourceAsStream(String.format("/world_textures/%s/%s", folder, fileName))));
		} catch (Exception e) {
			System.out.format("%s: %s/%s could not be loaded!", e, folder, fileName);
		}
	}

	// with biomes, no collision
	public Tile(String folder, String fileName, String biome) {
		try {
			this.image = toCompatibleImage(ImageIO.read(getClass().getResourceAsStream(String.format("/world_textures/%s/%s", folder, fileName))));
		} catch (Exception e) {
			System.out.format("%s: %s/%s could not be loaded!", e, folder, fileName);
		}

		this.biome = biome;
	}

	// no biomes, with collision
	public Tile(String folder, String fileName, boolean collisionPlayer, boolean collisionProjectile) {
		try {
			this.image = toCompatibleImage(ImageIO.read(getClass().getResourceAsStream(String.format("/world_textures/%s/%s", folder, fileName))));
		} catch (Exception e) {
			System.out.format("%s: %s/%s could not be loaded!", e, folder, fileName);
		}

		this.collisionPlayer = collisionPlayer;
		this.collisionProjectile = collisionProjectile;
	}

	// with biomes, with collision
	public Tile(String folder, String fileName, String biome, boolean collisionPlayer, boolean collisionProjectile) {
		try {
			this.image = toCompatibleImage(ImageIO.read(getClass().getResourceAsStream(String.format("/world_textures/%s/%s", folder, fileName))));
		} catch (Exception e) {
			System.out.format("%s: %s/%s could not be loaded!", e, folder, fileName);
		}

		this.biome = biome;
		this.collisionPlayer = collisionPlayer;
		this.collisionProjectile = collisionProjectile;
	}

	private BufferedImage toCompatibleImage(BufferedImage image) {
		// obtain the current system graphical settings
		GraphicsConfiguration gfxConfig = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

		/*
		 * if image is already compatible and optimized for current system settings,
		 * simply return it
		 */
		if (image.getColorModel().equals(gfxConfig.getColorModel()))
			return image;

		// image is not optimized, so create a new image that is
		BufferedImage newImage = gfxConfig.createCompatibleImage(image.getWidth(), image.getHeight(), image.getTransparency());

		// get the graphics context of the new image to draw the old image on
		Graphics2D g2d = newImage.createGraphics();

		// actually draw the image and dispose of context no longer needed
		g2d.drawImage(image, 0, 0, null);
		g2d.dispose();
		
		// return the new optimized image
		return newImage;
	}
}
