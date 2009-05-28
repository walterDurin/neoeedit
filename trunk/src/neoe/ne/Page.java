package neoe.ne;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface Page {

	void xpaint(Graphics g, Dimension size);

	void scroll(int amount);

	void keyReleased(KeyEvent env);

	void keyPressed(KeyEvent env);

	void keyTyped(KeyEvent env);

	void mousePressed(MouseEvent env);

	void mouseDragged(MouseEvent env);

}
