package entity;

import java.net.InetAddress;

import main.KeyHandler;
import main.MouseHandler;
import main.Game;

public class PlayerMP extends Player{		//inherits Player class
	
	public InetAddress ipAddress;
	public int port;
	
	//Create using this constructor when player is the user (we have keyHandler here)
	public PlayerMP(Game game, KeyHandler keys, MouseHandler mouse, String username, InetAddress ipAddress, int port) {
		//FYI: Player constructor is Player(Game screen, KeyHandler keys, String username, boolean isLocal)
		super(game, keys, mouse, username, true);
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	//Create using this constructor when player not the user (we do not have keyHandler here, because we do not want to update other player's movement with user inputs)
	public PlayerMP(Game game, String username, InetAddress ipAddress, int port) {
		super(game, null, null, username, false);
		this.ipAddress = ipAddress;
		this.port = port;
	}
	
	public String getUsername() {
		return username;
	}
	
	@Override
	public void update() {
		super.update();
	}

}
