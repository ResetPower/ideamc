package org.imcl.widget;
 
import java.awt.Graphics;
import java.awt.Image;
 
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class SwingImagePanel extends JPanel {
	ImageIcon icon;
	Image img;
	public SwingImagePanel(ImageIcon imageIcon) {
		img = imageIcon.getImage();
	}
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(img, 0, 0,this.getWidth(), this.getHeight(), this);
	}
}