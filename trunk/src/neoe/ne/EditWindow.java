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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class EditWindow extends JComponent implements MouseMotionListener,
		MouseListener, MouseWheelListener, KeyListener {

	private static final long serialVersionUID = -1667283144475200365L;

	private boolean debugFPS = false;

	public EditWindow() {
		pages = new ArrayList<PlainPage>();
		frame = new JFrame(PlainPage.WINDOW_NAME);
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
		long t1 = System.currentTimeMillis();
		try {
			if (pages.size() > 0) {
				PlainPage pi = getCurrentPage();
				pi.xpaint(g, this.getSize());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (debugFPS) {
			System.out.println("p " + (System.currentTimeMillis() - t1));
		}
	}

	private PlainPage getCurrentPage() {
		if (pageNo >= pages.size()) {
			pageNo = pages.size() - 1;
		}
		return pages.get(pageNo);
	}

	List<PlainPage> pages;
	int pageNo;
	JFrame frame;

	public void openFileInNewWindow(String fn) throws Exception {
		File f = new File(fn);
		if (f.exists() && f.isFile()) {
			long size = f.length();
			U.log(String.format("open %s(%s)", new Object[] { fn, size }));
			EditWindow ed = new EditWindow();
			ed.openFile(fn);
			ed.show(true);
		} else {
			U.log("cannot open " + fn);
		}
	}

	public PlainPage openFile(String fn) throws Exception {
		File f = new File(fn);
		PlainPage pi = null;
		if (f.exists() && f.isFile()) {
			long size = f.length();
			U.log(String.format("open %s(%s)", new Object[] { fn, size }));
			pages.add(pi = new PlainPage(this, fn));
			changePage(pages.size() - 1);
			return pi;
		} else {
			U.log("cannot open " + fn);
			return null;
		}
	}

	public void changePage(int p) {
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
			pages.get(pageNo).mouseDragged(env);
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
			pages.get(pageNo).mousePressed(env);
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
			pages.get(pageNo).scroll(amount);
		}

	}

	@Override
	public void keyPressed(KeyEvent env) {
		if (pageNo < pages.size()) {
			pages.get(pageNo).keyPressed(env);
		}

	}

	@Override
	public void keyReleased(KeyEvent env) {
		if (pageNo < pages.size()) {
			pages.get(pageNo).keyReleased(env);
		}

	}

	@Override
	public void keyTyped(KeyEvent env) {
		if (pageNo < pages.size()) {
			pages.get(pageNo).keyTyped(env);
		}

	}

	public PlainPage newFileInNewWindow() throws Exception {
		EditWindow ed = new EditWindow();
		PlainPage pi = ed.newEmptyFile(getWorkPath());
		ed.show(true);
		return pi;
	}

	String getWorkPath() {
		return (pages.size() == 0) ? null : pages.get(pageNo).workPath;
	}

	public PlainPage newEmptyFile(String workPath) throws Exception {
		PlainPage pi = new PlainPage(this, null);
		pi.workPath = workPath;
		pages.add(pi);
		changePage(pages.size() - 1);
		return pi;
	}

	public void changeTitle() {
		String fn = pages.get(pageNo).fn;
		if (fn != null) {
			frame.setTitle(new File(fn).getName() + " "
					+ new File(fn).getParent() + " - " + PlainPage.WINDOW_NAME);
		} else {
			frame.setTitle(PlainPage.WINDOW_NAME);
		}
	}

}
