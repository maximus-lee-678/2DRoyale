package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import entity.PlayerMP;
import net.GameClient;
import net.GameServer;
import net.Packet;
import tile.TileManager;

public class Game extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	// Screen
	public JFrame window;

	private final int originalTileSize = 16;
	private final int scale = 3;
	public final int tileSize = originalTileSize * scale;
	public final int playerSize = tileSize / 2;

	public final int maxScreenCol = 16;
	public final int maxScreenRow = 12;
	public final int screenWidth = tileSize * maxScreenCol;
	public final int screenHeight = tileSize * maxScreenRow;

	private int FPS = 60;
	private boolean running = false;

	// World
	public final int maxWorldCol = 50;
	public final int maxWorldRow = 50;
	public final int worldWidth = tileSize * maxWorldCol;
	public final int worldHeight = tileSize * maxWorldRow;

	public WindowHandler windowHandler;
	private TileManager tileM = new TileManager(this);
	public KeyHandler keys = new KeyHandler();
	public MouseHandler mouse = new MouseHandler();
	private List<PlayerMP> playerList = new ArrayList<PlayerMP>();

	public PlayerMP player = new PlayerMP(this, keys, mouse, null, null, -1);

	// Server
	public GameClient socketClient;
	private GameServer socketServer;

	public Game() {

		this.setPreferredSize(new Dimension(screenWidth, screenHeight));
		this.setBackground(Color.BLACK);

		this.setDoubleBuffered(true);

		this.setFocusable(true);
		this.addKeyListener(keys);
		this.addMouseMotionListener(mouse);

		window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.setTitle("IDK");

		window.add(this, BorderLayout.CENTER);
		window.pack();

		window.setLocationRelativeTo(null);
		window.setVisible(true);

	}

	public void startGameThread() {

		if (JOptionPane.showConfirmDialog(this, "Do you want to ok run the server") == 0) {
			socketServer = new GameServer(this);
			socketServer.start();
		}

		socketClient = new GameClient(this, "localhost");
		socketClient.start();

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
				delta--;
			}
			repaint();
			frames++;

			if (timer >= 1000000000) {
				window.setTitle("Name: " + player.getUsername() + " FPS: " + frames);
				timer = 0;
				frames = 0;
			}

		}

	}

	public void init() {

		windowHandler = new WindowHandler(this);
		player.setUsername(JOptionPane.showInputDialog(this, "Please enter a username"));
		this.getPlayers().add(player);
		if (socketServer != null) {
			socketServer.addConnection(player);
		}
		Packet loginPacket = new Packet(1, player.getUsername());
		socketClient.sendData(loginPacket.getPacket());
	}

	public synchronized List<PlayerMP> getPlayers() {
		return playerList;
	}

	public void update() {
		for (PlayerMP p : getPlayers())
			p.update();

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		tileM.render(g2);
		for (PlayerMP p : getPlayers())
			p.render(g2);

		g2.dispose();
	}

	public static void main(String[] args) {
		new Game().startGameThread();
	}

}
