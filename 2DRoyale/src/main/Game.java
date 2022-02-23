package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import entity.PlayerMP;
import item.ItemManager;
import net.GameClient;
import net.GameServer;
import structure.StructuresManager;
import tile.TileManager;

public class Game extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	// Screen
	private JFrame window;

	public static final int tileSize = 48;
	public static final int playerSize = 24;
	private static final int FPS = 60;
	
	private boolean running = false;
	private BufferedImage cursor;

	// World
	private long randSeed;
	private Random rand;

	public WindowHandler windowHandler;
	public TileManager tileM;
	public ItemManager itemM;
	public StructuresManager structM;
	public Screen screen;	
	public UI ui;	
	public SoundHandler soundHandler;
	public PlayerMP player;
	private List<PlayerMP> playerList;

	public KeyHandler keys;
	public MouseHandler mouse;

	// Server
	public GameClient socketClient;
	public GameServer socketServer;

	// Game State
	private boolean loading;
	private int gameState;
	public static final int titleState = 0;
	public static final int waitState = 1;
	public static final int playState = 2;
	public static final int endState = 3;

	public Game() {

		this.keys = new KeyHandler(this);
		this.mouse = new MouseHandler(this);
		this.screen = new Screen(this);
		this.soundHandler = new SoundHandler();
		this.player = new PlayerMP(this, keys, mouse, null, null, -1);
		this.windowHandler = new WindowHandler(this);
		this.ui = new UI(this);

		this.randSeed = System.currentTimeMillis();
		this.rand = new Random(randSeed);
		this.playerList = new ArrayList<PlayerMP>();
		this.loading = false;
		this.gameState = titleState;	

		this.setPreferredSize(new Dimension(screen.getScreenWidth(), screen.getScreenHeight()));
		this.setBackground(Color.BLACK);
		this.setDoubleBuffered(true);
		this.setFocusable(true);
		this.addKeyListener(keys);
		this.addMouseMotionListener(mouse);
		this.addMouseWheelListener(mouse);
		this.addMouseListener(mouse);

		this.window = new JFrame();
		this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.window.setResizable(false);
		this.window.setTitle("2D Royale");
		this.window.add(this, BorderLayout.CENTER);
		this.window.pack();
		this.window.setLocationRelativeTo(null);
		this.window.setVisible(true);	
		this.window.addWindowListener(windowHandler);			
		
		try {
			cursor = ImageIO.read(getClass().getResourceAsStream("/cursor/crosshair.png"));
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Cursor c = toolkit.createCustomCursor(cursor, getLocation(), "img");
			this.setCursor(c);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void startGameThread() {
		// Start game thread
		running = true;
		new Thread(this).start();

	}

	// GAME LOOP
	public void run() {

		double drawInterval = 1000000000 / FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		int timer = 0;
		int frames = 0;

		while (running) {

			currentTime = System.nanoTime();
			delta += (currentTime - lastTime) / drawInterval;
			timer += currentTime - lastTime;
			lastTime = currentTime;

			if (delta >= 1) {
				update();
				delta--;

				repaint();
				frames++;
			}

			if (timer >= 1000000000) {
				ui.setClient_fps(frames);
				timer = 0;
				frames = 0;
			}
		}

	}

	public void loadDefaults() {
		int numberOfBuildings = 0, numberOfObstructions = 0, numberOfCrates = 0;
		// Load world defaults based on game state
		this.loading = true;
		this.tileM = new TileManager(this);
		if (gameState == playState) {
			numberOfBuildings = 50;
			numberOfObstructions = 100;
			numberOfCrates = 100;
		} else if (gameState == waitState) {
			numberOfBuildings = 10;
			numberOfObstructions = 15;
			numberOfCrates = 0;
		}
		this.itemM = new ItemManager(this);
		this.structM = new StructuresManager(this, numberOfBuildings, numberOfObstructions, numberOfCrates);
		this.loading = false;
	}

	public synchronized List<PlayerMP> getPlayers() {
		return playerList;
	}

	public void clearPlayers() {
		this.playerList = new ArrayList<PlayerMP>();
	}

	// UPDATE ALL ASSETS
	public void update() {
		if (!loading) {
			if (gameState != titleState) {
				for (PlayerMP p : getPlayers())
					p.update();

				if (socketServer != null) {
					socketServer.update();
				}
				ui.update();
			}
		}
	}

	// RENDER ALL ASSETS
	public void paintComponent(Graphics g) {
		// Increase performance settings
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		if (!loading)
			screen.render(g2);
		g2.dispose();

	}

	public static void main(String[] args) {
		// Activate OpenGL
		System.setProperty("sun.java2d.opengl", "True");
		new Game().startGameThread();
	}

	public int getGameState() {
		return gameState;
	}

	public void setGameState(int gameState) {
		this.gameState = gameState;
	}

	public long getRandSeed() {
		return randSeed;
	}

	public void setRandSeed(long randSeed) {
		this.randSeed = randSeed;
		this.rand = new Random(randSeed);
	}
	
	public Random getRand() {
		return rand;
	}

	public void setRand(Random rand) {
		this.rand = rand;
	}	
	
}
