package control;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class Display extends JPanel {
	private static final long serialVersionUID = -5020551299965642376L;
	Image image;
	public Graphics2D graphics;
	
	public Display(int w, int h) {
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		graphics = (Graphics2D)image.getGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, w, h);
		graphics.setColor(Color.BLACK);
		setPreferredSize(new Dimension(w, h));
	}
	
	public void paintComponent(Graphics g) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(image, 0, 0, this);
	}
}
