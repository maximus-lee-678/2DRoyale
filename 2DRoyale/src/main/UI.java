package main;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.InputStream;



public class UI{

	Game gp;
	Graphics2D g2;
	Font maruMonica;
	public boolean messageOn = false;
	public String message = "";
	int messageCoutner = 0;
	public boolean gameFinished = false;
	public String currentDialogue = "";
	public int commandNum = 0;
	public int titleScreenState = 0;
	
	public UI(Game gp) {
		this.gp = gp;
		
		try {
			InputStream is = getClass().getResourceAsStream("/font/x12y16pxMaruMonica.ttf");
			maruMonica = Font.createFont(Font.TRUETYPE_FONT, is);
		} catch (FontFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void draw(Graphics2D g2) {
		
		this.g2 = g2;
		g2.setFont(maruMonica);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g2.setColor(Color.white);
		
		// Title State
		if(gp.gameState == gp.titleState) {
			drawTitleScreen();
		}
	}
	public void drawTitleScreen() {
		
		if(titleScreenState == 0) {
			g2.setColor(new Color(0,0,0));
			g2.fillRect(0, 0, gp.screen.screenWidth, gp.screen.screenHeight);
			// Title Name
			g2.setFont(g2.getFont().deriveFont(Font.BOLD,96F));
			String text = "2D Royale";
			int x = getXforCenteredText(text);
			int y = gp.tileSize*3;
			
			// Shadow
			g2.setColor(Color.gray);
			g2.drawString(text, x+5, y+5);
			
			// Main Color
			g2.setColor(Color.white);
			g2.drawString(text, x, y);
			
			
			// Menu
			g2.setFont(g2.getFont().deriveFont(Font.BOLD,48F));
			
			text = "START";
			x = getXforCenteredText(text);
			y += gp.tileSize * 4;
			g2.drawString(text,  x,  y);
			if (commandNum == 0) {
				g2.drawString(">", x-gp.tileSize, y);
			}
			
			text = "HOW TO PLAY";
			x = getXforCenteredText(text);
			y += gp.tileSize;
			g2.drawString(text,  x,  y);
			if (commandNum == 1) {
				g2.drawString(">", x-gp.tileSize, y);
			}
			
			text = "PLAYERS CONTROL";
			x = getXforCenteredText(text);
			y += gp.tileSize;
			g2.drawString(text,  x,  y);
			if (commandNum == 2) {
				g2.drawString(">", x-gp.tileSize, y);
			}
			
			text = "QUIT";
			x = getXforCenteredText(text);
			y += gp.tileSize;
			g2.drawString(text,  x,  y);
			if (commandNum == 3) {
				g2.drawString(">", x-gp.tileSize, y);
			}
		}
		else if (titleScreenState == 1) {
			g2.setColor(Color.white);
			g2.setFont(g2.getFont().deriveFont(42F));
			
			String text = "Objectives";
			int x = getXforCenteredText(text);
			int y = gp.tileSize*3;
			g2.drawString(text, x, y);
			
			text = "- Kill everyone and be the last man standing";
			x = getXforCenteredText(text);
			y += gp.tileSize*2;
			g2.drawString(text,  x,  y);
			
			text = "- Search around for weapons and stay in the restricted area";
			x = getXforCenteredText(text);
			y += gp.tileSize;
			g2.drawString(text,  x,  y);
			
			text = "Good luck";
			x = getXforCenteredText(text);
			y += gp.tileSize;
			g2.drawString(text,  x,  y);
			
			text = "Back to Main Menu";
			x = getXforCenteredText(text);
			y += gp.tileSize*2;
			g2.drawString(text,  x,  y);
			g2.drawString(">", x-gp.tileSize, y);
		}
		else if (titleScreenState == 2) {
			g2.setColor(Color.white);
			g2.setFont(g2.getFont().deriveFont(42F));
			
			String text = "Players Control";
			int x = getXforCenteredText(text);
			int y = gp.tileSize*3;
			g2.drawString(text, x, y);
			
			text = "- Use WASD to move your character around";
			x = getXforCenteredText(text);
			y += gp.tileSize*2;
			g2.drawString(text,  x,  y);
			
			text = "- Use E to pick and drop weapons";
			x = getXforCenteredText(text);
			y += gp.tileSize;
			g2.drawString(text,  x,  y);
			
			text = "- Left-click to shoot";
			x = getXforCenteredText(text);
			y += gp.tileSize;
			g2.drawString(text,  x,  y);
			
			text = "Back to Main Menu";
			x = getXforCenteredText(text);
			y += gp.tileSize*2;
			g2.drawString(text,  x,  y);
			g2.drawString(">", x-gp.tileSize, y);
		}
		else if (titleScreenState == 3) {
			g2.setColor(Color.white);
			g2.setFont(g2.getFont().deriveFont(42F));
			
			String text = "Do you want to run the server?";
			int x = getXforCenteredText(text);
			int y = gp.tileSize*3;
			g2.drawString(text, x, y);
			
			text = "YES";
			x = getXforCenteredText(text);
			y += gp.tileSize * 4;
			g2.drawString(text,  x,  y);
			if (commandNum == 0) {
				g2.drawString(">", x-gp.tileSize, y);
			}
			
			text = "NO";
			x = getXforCenteredText(text);
			y += gp.tileSize;
			g2.drawString(text,  x,  y);
			if (commandNum == 1) {
				g2.drawString(">", x-gp.tileSize, y);
			}
			
			text = "Back";
			x = getXforCenteredText(text);
			y += gp.tileSize*3;
			g2.drawString(text,  x,  y);
			if (commandNum == 2) {
				g2.drawString(">", x-gp.tileSize, y);
			}
		}
		else if (titleScreenState == 4) {
			g2.setColor(Color.white);
			g2.setFont(g2.getFont().deriveFont(42F));
			
			String text = "Enter your nickname:";
			int x = getXforCenteredText(text);
			int y = gp.tileSize*3;
			g2.drawString(text, x, y);
			
			text = "HERE";
			x = getXforCenteredText(text);
			y += gp.tileSize * 4;
			g2.drawString(text,  x,  y);
		}
		
		
		
	}
	public int getXforCenteredText(String text) {
		int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
		int x = gp.screen.screenWidth/2 - length/2;
		return x;
	}
}
