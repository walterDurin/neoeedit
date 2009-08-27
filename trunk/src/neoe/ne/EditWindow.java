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

import neoe.ne.PlainPage.PageInfo;

public class EditWindow extends JComponent implements MouseMotionListener,
		MouseListener, MouseWheelListener, KeyListener {

	private static final long serialVersionUID = -1667283144475200365L;
	private static final String REV = "$Rev$";
	private static final String WINDOW_NAME = "neoeedit r"
			+ REV.substring(6, REV.length() - 2);
	private boolean debugFPS = false;

	public EditWindow() {
		pages = new Vector<PageInfo>();
		frame = new JFrame(WINDOW_NAME);
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
				PageInfo pi = getCurrentPage();
				pi.page.xpaint(g, this.getSize());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		if (debugFPS) {
			System.out.println("p " + (System.currentTimeMillis() - t1));
		}
	}

	private PageInfo getCurrentPage() {
		if (pageNo >= pages.size()) {
			pageNo = pages.size() - 1;
		}
		return pages.get(pageNo);
	}

	List<PageInfo> pages;
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

	public PageInfo openFile(String fn) throws Exception {
		File f = new File(fn);
		PageInfo pi = null;
		if (f.exists() && f.isFile()) {
			long size = f.length();
			U.log(String.format("open %s(%s)", new Object[] { fn, size }));
			pages.add(pi = new PageInfo(fn, size, this));
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

	public PageInfo newFileInNewWindow() throws Exception {
		EditWindow ed = new EditWindow();
		PageInfo pi = ed.newEmptyFile(getWorkPath());
		ed.show(true);
		return pi;
	}

	String getWorkPath() {
		return (pages.size() == 0) ? null : pages.get(pageNo).workPath;
	}

	public PageInfo newEmptyFile(String workPath) throws Exception {
		PageInfo pi = new PageInfo(null, 0, this);
		pi.workPath = workPath;
		pages.add(pi);
		changePage(pages.size() - 1);
		return pi;
	}

	public void changeTitle() {
		String fn = pages.get(pageNo).fn;
		if (fn != null) {
			frame.setTitle(new File(fn).getName() + " "
					+ new File(fn).getParent() + " - " + WINDOW_NAME);
		} else {
			frame.setTitle(WINDOW_NAME);
		}
	}

}
