package neoe.ne;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Editor extends JComponent implements MouseMotionListener,
		MouseListener, MouseWheelListener, KeyListener {

	public Editor() {
		pages = new Vector<PageInfo>();
		frame = new JFrame("neoeedit");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(new Dimension(800, 600));
		frame.getContentPane().add(this);
		setFocusable(true);
		addMouseMotionListener(this);
		addMouseListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		setOpaque(false);
		setCursor(new Cursor(Cursor.TEXT_CURSOR));
		setFocusTraversalKeysEnabled(false);
	}

	public void paint(Graphics g) {
		try {
			if (pages.size() > 0) {
				if (pageNo >= pages.size()) {
					pageNo = pages.size() - 1;
				}
				PageInfo pi = pages.get(pageNo);
				if (pi.page == null) {
					if (pi.size < 10000000) {
						pi.page = new PlainPage(this, pi);
					} else {
						pi.page = new LargePage(this, pi);
					}
				}

				pi.page.xpaint(g, this.getSize());

			}
		} catch (Throwable e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(frame, "" + e);
		}

	}

	List<PageInfo> pages;
	private int pageNo;
	JFrame frame;

	public void openFile(String fn) {
		File f = new File(fn);
		if (f.exists() && f.isFile()) {
			if (pages.contains(fn)) {
				// already open
			} else {
				long size = f.length();
				Log.debug(String.format("open %s(%s)",
						new Object[] { fn, size }));
				pages.add(new PageInfo(fn, size));
			}
			changePage(find(pages, fn));
		} else {
			Log.debug("cannot open " + fn);
		}

	}

	private int find(List<PageInfo> l, String fn) {
		for (int i = 0; i < l.size(); i++) {
			PageInfo pi = l.get(i);			
			if (pi.fn!=null && pi.fn.equals(fn)) {
				return i;
			}
		}
		return -1;
	}

	private void changePage(int p) {
		this.pageNo = p;
		changeTitle();
	}

	public void show(boolean b) {
		frame.setVisible(b);
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent env) {
		if (pageNo < pages.size()) {
			pages.get(pageNo).page.mouseDragged(env);
		}
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent env) {

		if (pageNo < pages.size()) {
			pages.get(pageNo).page.mousePressed(env);
		}

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent env) {
		int amount = env.getWheelRotation() * env.getScrollAmount();
		if (pageNo < pages.size()) {
			pages.get(pageNo).page.scroll(amount);
		}

	}

	@Override
	public void keyPressed(KeyEvent env) {
		if (pageNo < pages.size()) {
			pages.get(pageNo).page.keyPressed(env);
		}

	}

	@Override
	public void keyReleased(KeyEvent env) {
		if (pageNo < pages.size()) {
			pages.get(pageNo).page.keyReleased(env);
		}

	}

	@Override
	public void keyTyped(KeyEvent env) {
		if (pageNo < pages.size()) {
			pages.get(pageNo).page.keyTyped(env);
		}

	}

	public void newFile() {
		pages.add(new PageInfo(null, 0));
		changePage(pages.size()-1);
	}

	public void changeTitle() {
		String fn = pages.get(pageNo).fn;
		if (fn != null) {
			frame.setTitle(new File(fn).getName()+" "+new File(fn).getParent() + " - neoeedit");
		}
	}

}
