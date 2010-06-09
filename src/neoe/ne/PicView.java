package neoe.ne;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;

public class PicView {

	public class Panel extends JComponent implements MouseMotionListener,
			MouseListener, MouseWheelListener, KeyListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = -74255011004476996L;
		private BufferedImage img;
		private int vy;
		private int vx;
		private int my;
		private int mx;
		double rate = 1.0;
		private int pw;
		private int ph;
		private int vy1;
		private int vx1;
		private boolean small = true;

		@Override
		protected void paintComponent(Graphics g) {
			int w = getWidth();
			int h = getHeight();
			int sw = w / 4;
			int sh = sw * ph / pw;
			g.drawImage(img, 0, 0, w, h, (int) (vx * rate), (int) (vy * rate),
					(int) ((w + vx) * rate), (int) ((h + vy) * rate), null);
			if (small) {
				g.drawImage(img, w - sw, h - sh, w, h, 0, 0, pw, ph, null);
				g.setColor(Color.WHITE);
				g.drawRect(w - sw, h - sh, sw, sh);
				g.setColor(Color.RED);
				g.drawRect((int) (w - sw + vx * rate * sw / pw),//
						(int) (h - sh + vy * rate * sh / ph),//
						(int) (sw * w * rate / pw),//
						(int) (sh * h * rate / ph));
			}
		}

		public Panel(File fn) throws IOException {
			long t1 = System.currentTimeMillis();
			img = ImageIO.read(fn);
			System.out.println("read in " + (System.currentTimeMillis() - t1));
			setPreferredSize(new Dimension(pw = img.getWidth(), ph = img
					.getHeight()));

			addMouseListener(this);
			addMouseMotionListener(this);
			addMouseWheelListener(this);
			addKeyListener(this);
			setFocusable(true);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int x = e.getX(), y = e.getY();
			int w = getWidth();
			int h = getHeight();
			int sw = w / 4;
			int sh = sw * ph / pw;
			if (x > w - sw && y > h - sh && small) {
				vx = (int) ((x - w + sw) * pw / rate / sw - w / 2);
				vy = (int) ((y - h + sh) * ph / rate / sh - h / 2);
				repaint();
			} else {
				int dx = e.getX() - mx;
				int dy = e.getY() - my;
				vx = (int) (vx1 - dx);
				vy = (int) (vy1 - dy);
				repaint();
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {

		}

		@Override
		public void mouseClicked(MouseEvent e) {
			int x = e.getX(), y = e.getY();
			if (e.getClickCount() == 2) {
				vx = (int) ((vx + x) * rate - x);
				vy = (int) ((vy + y) * rate - y);
				rate = 1;
				repaint();
			} else {

			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {

		}

		@Override
		public void mouseExited(MouseEvent e) {

		}

		@Override
		public void mousePressed(MouseEvent e) {
			int x = e.getX(), y = e.getY();
			int w = getWidth();
			int h = getHeight();
			int sw = w / 4;
			int sh = sw * ph / pw;
			{
				mx = e.getX();
				my = e.getY();
				vx1 = vx;
				vy1 = vy;
			}
			if (x > w - sw && y > h - sh && small) {
				vx = (int) ((x - w + sw) * pw / rate / sw - w / 2);
				vy = (int) ((y - h + sh) * ph / rate / sh - h / 2);
				repaint();
			} else {

			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int amount = e.getWheelRotation() * e.getScrollAmount();
			int x = e.getX(), y = e.getY();
			if (amount < 0) {
				rate = rate / 1.1;
				vx = (int) ((vx + x) * 1.1 - x);
				vy = (int) ((vy + y) * 1.1 - y);
			} else {
				rate = rate * 1.1;
				vx = (int) ((vx + x) / 1.1 - x);
				vy = (int) ((vy + y) / 1.1 - y);
			}
			// rate=rate1;
			repaint();
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_F1) {
				small = !small;
				repaint();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {

		}

		@Override
		public void keyTyped(KeyEvent e) {

		}

	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		new PicView().show(new File(args[0]));

	}

	public void show(File fn) throws IOException {
		JFrame f = new JFrame("PicView " + fn.getName());
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		Panel p;
		f.add(p = new Panel(fn));
		U.setFrameSize(f, p.pw, p.ph);
		f.setTransferHandler(U.th);
		f.setVisible(true);
	}
}
