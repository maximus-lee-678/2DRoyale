package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import net.Pkt08ServerTick;
import object.SuperObject;
import structure.StructuresManager;
import tile.TileManager;

public class Game extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	// Screen
	public JFrame window;

	private final int originalTileSize = 16;
	private final int scale = 3;
	public final int tileSize = originalTileSize * scale;
	public final int playerSize = tileSize / 2;

	private int FPS = 60;
	public boolean running = false;
	private BufferedImage cursor;
	
	//Objects
	public AssetSetter aSetter = new AssetSetter(this);
	public SuperObject obj[] = new SuperObject[10];

	// World
	public long randSeed = System.currentTimeMillis();
	public Random rand = new Random(randSeed);
	public final int maxWorldCol = 50;
	public final int maxWorldRow = 50;
	public final int worldWidth = tileSize * maxWorldCol;
	public final int worldHeight = tileSize * maxWorldRow;
	public final int numberOfBuildings = 10;

	public WindowHandler windowHandler;
	public TileManager tileM;
	public ItemManager itemM;
	public StructuresManager structM;
	public Screen screen = new Screen(this);
	public KeyHandler keys = new KeyHandler(this);
	public MouseHandler mouse = new MouseHandler(this);
	private List<PlayerMP> playerList = new ArrayList<PlayerMP>();
	public UI ui = new UI(this);

	public PlayerMP player = new PlayerMP(this, keys, mouse, null, null, -1);

	// Server
	public GameClient socketClient;
	public GameServer socketServer;

	// Game State
	public int gameState;
	public final int titleState = 0;
	public final int playState = 1;
	public final int endState = 2;
	
	private int cycles = 0;

	public Game() {

		this.setPreferredSize(new Dimension(screen.screenWidth, screen.screenHeight));
		this.setBackground(Color.BLACK);

		this.setDoubleBuffered(true);

		this.setFocusable(true);
		this.addKeyListener(keys);
		this.addMouseMotionListener(mouse);
		this.addMouseWheelListener(mouse);
		this.addMouseListener(mouse);

		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.setTitle("IDK");

		window.add(this, BorderLayout.CENTER);
		window.pack();

		window.setLocationRelativeTo(null);
		window.setVisible(true);
		gameState = titleState;

	}

	public void startGameThread() {
		running = true;
		new Thread(this).start();

	}

	public void run() {

		double drawInterval = 1000000000 / FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;
		int timer = 0;
		int frames = 0;

		init();

		while (running) {

			currentTime = System.nanoTime();
			delta += (currentTime - lastTime) / drawInterval;
			timer += currentTime - lastTime;
			lastTime = currentTime;

			if (delta >= 1) {
				update();
				repaint();
				frames++;
				delta--;
			}

			if (timer >= 1000000000) {
				window.setTitle("Name: " + player.getUsername() + " FPS: " + frames);
				timer = 0;
				frames = 0;
			}

		}

	}
	
	public void loadDefaults() {
		windowHandler = new WindowHandler(this);
		tileM = new TileManager(this);
		itemM = new ItemManager();
		structM = new StructuresManager(this);
	}

	public void init() {		

		try {
			cursor = ImageIO.read(getClass().getResourceAsStream("/cursor/crosshair.png"));
			Toolkit toolkit = Toolkit.getDefaultToolkit();
			Cursor c = toolkit.createCustomCursor(cursor, getLocation(), "img");
			this.setCursor(c);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public synchronized List<PlayerMP> getPlayers() {
		return playerList;
	}

	public void update() {
		if (gameState == titleState) {
			// do nothing
		}
		if (gameState == playState) {
			for (PlayerMP p : getPlayers())
				p.update();
			if(socketServer != null) {
				Pkt08ServerTick serverTickPacket = new Pkt08ServerTick();
				serverTickPacket.sendData(socketClient);
			}
			
		}

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		
		//Screen
		screen.render(g2);
		
		//Object
		if (gameState == playState) {
			setupGame();
			ui.draw(g2);
			for(int i = 0; i < obj.length; i++) {
				if(obj[i] != null) {
					obj[i].draw(g2,  this);
				}
			}
		}
		
		
		g2.dispose();

	}
	
	public void setupGame() {
		aSetter.setObject();
		
	}

	public static void main(String[] args) {
		
		new Game().startGameThread();
		
	}

}
