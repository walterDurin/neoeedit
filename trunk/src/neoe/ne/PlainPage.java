package neoe.ne;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class PlainPage implements Page {

	private static final long VANISHTIME = 3000;
	private Editor edit;
	private PageInfo info;
	private Font font;
	private int lineHeight;
	private int lineGap;
	private Color color;
	private String encoding;
	private List<StringBuffer> lines;
	private Color bkColor;
	private int cy;
	private int cx;
	private int showLineCnt;
	private int my;
	private int mx;
	private int selectstopy;
	private int selectstopx;
	private int selectstarty;
	private int selectstartx;
	private boolean mshift;
	public int sy;
	public int sx;
	private String lineSep = "\n";
	private int mcount;
	private History history;
	private int gutterWidth = 40;
	private int toolbarHeight = 40;
	private long msgtime;
	private String msg;
	private Dimension size;
	private String text2find;
	private FindReplaceWindow findWindow;
	private boolean ignoreCase = true;

	public PlainPage(Editor editor, PageInfo pi) throws Exception {
		this.edit = editor;
		this.info = pi;
		sx = 0;
		sy = 0;
		cx = 0;
		cy = 0;
		this.font = new Font("Monospaced", Font.PLAIN, 12);
		this.lineHeight = 10;
		this.lineGap = 5;
		this.color = Color.BLACK;
		this.bkColor = new Color(0xe0e0f0);
		this.encoding = "utf8";
		this.lines = readFile(pi.fn);
		history = new History(this);
		this.findWindow = new FindReplaceWindow(editor.frame, this);
	}

	private List<StringBuffer> readFile(String fn) {

		List<StringBuffer> lines = new ArrayList<StringBuffer>();
		if (fn == null) {
			lines.add(new StringBuffer("edit here..."));
			return lines;
		}
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fn), encoding));

			String line;
			while ((line = in.readLine()) != null) {
				lines.add(new StringBuffer(line));
			}
			in.close();

		} catch (Throwable e) {
			lines = new ArrayList<StringBuffer>();
			lines.add(new StringBuffer(e.toString()));
		}
		return lines;
	}

	public void xpaint(Graphics g, Dimension size) {
		this.size = size;

		Graphics2D g2 = (Graphics2D) g;
		g2.setFont(font);

		showLineCnt = (size.height - toolbarHeight) / (lineHeight + lineGap);
		int charCntInLine = (size.width - gutterWidth) / (lineHeight) * 2;

		// change sx if needed
		cx = Math.min(getline(cy).length(), cx);
		if (cx < sx) {
			sx = cx;
		}
		if (strWidth(g2, subs(getline(cy), sx, cx)) > size.width - lineHeight
				* 3) {
			sx = Math.max(0, cx - 1);
		}

		// apply mouse click position
		if (mx >= gutterWidth && my >= toolbarHeight) {
			mx -= gutterWidth;
			my -= toolbarHeight;
			cy = sy + my / (lineHeight + lineGap);
			if (cy >= getLinesize()) {
				cy = getLinesize() - 1;
			}
			RoSb sb = getline(cy);
			int i = sx;
			for (; i < sb.length(); i++) {
				String s1 = subs(sb, sx, i);
				int w = strWidth(g2, s1);
				if (mx < w) {
					i -= 1;
					break;
				}
			}
			cx = i;
			if (cx > sb.length()) {
				cx = sb.length();
			}
			mx = -1;
			my = -1;
			sb = getline(cy);
			if (mcount == 2) {
				int x1 = cx;
				int x2 = cx;
				while (x1 > 0
						&& Character.isJavaIdentifierPart(sb.charAt(x1 - 1))) {
					x1 -= 1;
				}
				while (x2 < sb.length() - 1
						&& Character.isJavaIdentifierPart(sb.charAt(x2 + 1))) {
					x2 += 1;
				}
				selectstartx = x1;
				selectstarty = cy;
				selectstopx = x2 + 1;
				selectstopy = cy;
			} else if (mcount == 3) {
				selectstartx = 0;
				selectstarty = cy;
				selectstopx = sb.length();
				selectstopy = cy;
			} else {
				if (mshift) {
					selectstopx = cx;
					selectstopy = cy;
				} else {
					cancelSelect();
				}
			}
		}
		// draw toolbar
		drawToolbar(g2);
		// draw gutter
		g2.translate(0, toolbarHeight);
		drawGutter(g2);
		// draw text
		g2.translate(gutterWidth, 0);
		g2.setColor(bkColor);
		g2.fillRect(0, 0, size.width, size.height);
		g2.setColor(color);

		// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		// RenderingHints.VALUE_ANTIALIAS_ON);
		int y = sy;
		int py = lineHeight;
		for (int i = 0; i < showLineCnt; i++) {

			if (y >= getLinesize()) {
				break;
			}
			RoSb sb = getline(y);
			if (sx < sb.length()) {
				int chari2 = Math.min(charCntInLine + sx, sb.length());
				String s = subs(sb, sx, chari2);
				g2.setColor(color);
				drawString(g2, s, 0, py);
				int w = strWidth(g2, s);
				g2.setColor(Color.red);
				g2.drawLine(w, py - lineHeight + font.getSize(), w + 3, py
						- lineHeight + font.getSize());
			} else {
				int w = 0;
				g2.drawLine(w, py - lineHeight + font.getSize(), w + 3, py
						- lineHeight + font.getSize());
			}
			y += 1;
			py += lineHeight + lineGap;
		}
		// draw cursor
		if (cy >= sy && cy <= sy + showLineCnt) {
			g2.setXORMode(new Color(0x30f0f0));
			String s = subs(getline(cy), sx, cx);
			int w = strWidth(g2, s);
			g2.fillRect(w, (cy - sy) * (lineHeight + lineGap), 2, lineHeight);
		}
		if (true) {// select mode
			Rectangle r = getSelectRect();
			int x1 = r.x;
			int y1 = r.y;
			int x2 = r.width;
			int y2 = r.height;

			if (y1 == y2 && x1 < x2) {
				g2.setColor(Color.BLUE);
				g2.setXORMode(new Color(0xf0f030));
				drawSelect(g2, y1, x1, x2);
			} else if (y1 < y2) {
				g2.setColor(Color.BLUE);
				g2.setXORMode(new Color(0xf0f030));
				drawSelect(g2, y1, x1, Integer.MAX_VALUE);
				for (int i = y1 + 1; i < y2; i++) {
					drawSelect(g2, i, 0, Integer.MAX_VALUE);
				}
				drawSelect(g2, y2, 0, x2);
			}
		}
	}

	private void drawString(Graphics2D g2, String s, int x, int y) {
		if (s.indexOf("\t") < 0) {
			g2.drawString(s, x, y);
		} else {
			int w = 0;
			int p1 = 0;
			while (true) {
				int p2 = s.indexOf("\t", p1);
				if (p2 < 0) {
					g2.drawString(s.substring(p1), x + w, y);
					w += g2.getFontMetrics().stringWidth(s.substring(p1));
					break;
				} else {
					g2.drawString(s.substring(p1, p2), x + w, y);
					w += g2.getFontMetrics().stringWidth(s.substring(p1, p2));
					g2.drawImage(U.TabImg, x + w, y - lineHeight, null);
					w += U.TABWIDTH;
					p1 = p2 + 1;
				}
			}
		}
	}

	private void drawGutter(Graphics2D g2) {
		g2.setColor(new Color(0x115511));
		for (int i = 0; i < showLineCnt; i++) {
			if (sy + i + 1 > lines.size()) {
				break;
			}
			g2.drawString("" + (sy + i + 1), 0, lineHeight
					+ (lineHeight + lineGap) * i);
		}
	}

	private void drawToolbar(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.drawString("<F1>:Help," + lines.size() + "," + edit.pages.size()
				+ "," + info.size + "," + encoding + "," + (cx + 1) + ","
				+ history.size() + "," + info.fn, 0, lineHeight);
		if (msg != null) {
			if (System.currentTimeMillis() - msgtime > VANISHTIME) {
				msg = null;
			} else {
				int w = g2.getFontMetrics().stringWidth(msg);
				g2.setColor(new Color(0xee6666));
				g2
						.fillRect(size.width - w, 0, size.width, lineHeight
								+ lineGap);
				g2.setColor(Color.YELLOW);
				g2.drawString(msg, size.width - w, lineHeight);
				repaintAfter(VANISHTIME);
			}
		}
	}

	private void repaintAfter(final long t) {
		new Thread() {
			public void run() {
				try {
					Thread.sleep(t);
					edit.repaint();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

	private String subs(RoSb sb, int a, int b) {
		return subs(sb.toString(), a, b);
	}

	private String subs(String sb, int a, int b) {
		if (a >= b) {
			return "";
		}
		if (a >= sb.length()) {
			return "";
		}
		if (a < 0 || b < 0) {
			return "";
		}
		if (b > sb.length()) {
			b = sb.length();
		}
		return sb.substring(a, b);
	}

	private void drawSelect(Graphics2D g2, int y1, int x1, int x2) {
		int scry = y1 - sy;
		if (scry < showLineCnt) {
			String s = getline(y1).toString();
			if (sx > s.length()) {
				return;
			}
			s = subs(s, sx, s.length());
			x1 -= sx;
			x2 -= sx;
			if (x1 < 0) {
				x1 = 0;
			}
			if (x2 < 0) {
				x2 = 0;
			}
			if (x2 > s.length()) {
				x2 = s.length();
			}
			if (x1 > s.length()) {
				x1 = s.length();
			}
			int w1 = strWidth(g2, s.substring(0, x1));
			int w2 = strWidth(g2, s.substring(0, x2));
			g2.fillRect(w1, scry * (lineHeight + lineGap), (w2 - w1),
					lineHeight + lineGap);
		}
	}

	private int strWidth(Graphics2D g2, String s) {
		if (s.indexOf("\t") < 0) {
			return g2.getFontMetrics().stringWidth(s);
		} else {
			int w = 0;
			int p1 = 0;
			while (true) {
				int p2 = s.indexOf("\t", p1);
				if (p2 < 0) {
					w += g2.getFontMetrics().stringWidth(s.substring(p1));
					break;
				} else {
					w += g2.getFontMetrics().stringWidth(s.substring(p1, p2));
					w += U.TABWIDTH;
					p1 = p2 + 1;
				}
			}
			return w;
		}
	}

	@Override
	public void scroll(int amount) {
		sy += amount;
		if (sy >= getLinesize()) {
			sy = getLinesize() - 1;
		}
		if (sy < 0) {
			sy = 0;
		}
		edit.repaint();
	}

	@Override
	public void keyReleased(KeyEvent env) {

	}

	@Override
	public void keyPressed(KeyEvent env) {

		int kc = env.getKeyCode();
		if (kc == KeyEvent.VK_F1) {
			help();
		} else if (kc == KeyEvent.VK_F2) {
			saveAs();
		} else if (kc == KeyEvent.VK_F3) {
			findNext();
		} else if (kc == KeyEvent.VK_F5) {
			reloadWithEncoding();
		}
		boolean cmoved = false;
		if (env.isAltDown()) {
			if (kc == KeyEvent.VK_LEFT) {
				String s = getline(cy).toString();
				if (s.length() > 0
						&& (s.charAt(0) == '\t' || s.charAt(0) == ' ')) {
					getline(cy).sb().deleteCharAt(0);
				}
				cx -= 1;
				if (cx < 0) {					
					cx = 0;					
				}
				focusCursor();
				cmoved = true;
			} else if (kc == KeyEvent.VK_RIGHT) {
				getline(cy).sb().insert(0, '\t');
				cx += 1;
				focusCursor();
				cmoved = true;
			}
		} else if (env.isControlDown()) {
			if (kc == KeyEvent.VK_C) {
				copySelected();
			} else if (kc == KeyEvent.VK_V) {
				pasteSelected();
			} else if (kc == KeyEvent.VK_X) {
				cutSelected();
			} else if (kc == KeyEvent.VK_A) {
				selectAll();
			} else if (kc == KeyEvent.VK_D) {
				if (isSelected()) {
					deleteSelection();
				} else {
					cx = 0;
					if (getLinesize() == 1) {
						deleteInLine(0, 0, getline(0).length());
					} else {
						u_removeLine(cy);
						if (cy >= getLinesize()) {
							cy = getLinesize() - 1;
						}
					}
				}
				focusCursor();
			} else if (kc == KeyEvent.VK_O) {
				openFile();
			} else if (kc == KeyEvent.VK_N) {
				newFile();
			} else if (kc == KeyEvent.VK_S) {
				saveFile();
			} else if (kc == KeyEvent.VK_L) {
				gotoLine();
			} else if (kc == KeyEvent.VK_Z) {
				undo();
			} else if (kc == KeyEvent.VK_F) {
				find();
			} else if (kc == KeyEvent.VK_TAB) {
				changePage();
			} else if (kc == KeyEvent.VK_Y) {
				redo();
			} else if (kc == KeyEvent.VK_W) {
				closePage();
			} else if (kc == KeyEvent.VK_E) {
				changeEncoding();
			} else if (kc == KeyEvent.VK_PAGE_UP) {
				cy = 0;
				cx = 0;
				focusCursor();
				cmoved = true;
			} else if (kc == KeyEvent.VK_PAGE_DOWN) {
				cy = getLinesize() - 1;
				cx = 0;
				focusCursor();
				cmoved = true;
			}
		} else {

			if (kc == KeyEvent.VK_LEFT) {
				cx -= 1;
				if (cx < 0) {
					if (cy > 0) {
						cy -= 1;
						cx = getline(cy).length();
					} else {
						cx = 0;
					}
				}
				focusCursor();
				cmoved = true;
			} else if (kc == KeyEvent.VK_RIGHT) {
				cx += 1;
				if (cx>getline(cy).length()&& cy<lines.size()-1){
					cy+=1;
					cx=0;
				}
				focusCursor();
				cmoved = true;
			} else if (kc == KeyEvent.VK_UP) {
				cy -= 1;
				if (cy < 0) {
					cy = 0;
				}
				focusCursor();
				cmoved = true;
			} else if (kc == KeyEvent.VK_DOWN) {
				cy += 1;
				if (cy >= getLinesize()) {
					cy = getLinesize() - 1;
				}
				focusCursor();
				cmoved = true;
			} else if (kc == KeyEvent.VK_HOME) {
				cx = 0;
				focusCursor();
				cmoved = true;
			} else if (kc == KeyEvent.VK_END) {
				cx = Integer.MAX_VALUE;
				focusCursor();
				cmoved = true;
			} else if (kc == KeyEvent.VK_PAGE_UP) {
				cy -= showLineCnt;
				if (cy < 0) {
					cy = 0;
				}
				focusCursor();
				cmoved = true;
			} else if (kc == KeyEvent.VK_PAGE_DOWN) {
				cy += showLineCnt;
				if (cy >= getLinesize()) {
					cy = getLinesize() - 1;
				}
				focusCursor();
				cmoved = true;
			} else if (kc == KeyEvent.VK_CONTROL || kc == KeyEvent.VK_SHIFT
					|| kc == KeyEvent.VK_ALT) {
				return;
			}

		}
		if (cmoved) {
			if (env.isShiftDown()) {
				selectstopx = cx;
				selectstopy = cy;
			} else {
				cancelSelect();

			}
		}
		edit.repaint();
	}

	private void changeEncoding() {
		String s = JOptionPane.showInputDialog(edit, "Encoding:", encoding);
		if (s == null) {
			return;
		}
		try {
			"a".getBytes(s);
		} catch (Exception e) {
			message("bad encoding:" + s);
			return;
		}
		encoding = s;
	}

	private void reloadWithEncoding() {
		if (info.fn == null) {
			message("use change encoding for new text.");
			changeEncoding();
			return;
		}
		String s = JOptionPane.showInputDialog(edit, "Reload with Encoding:",
				encoding);
		if (s == null) {
			return;
		}
		try {
			"a".getBytes(s);
		} catch (Exception e) {
			message("bad encoding:" + s);
			return;
		}
		encoding = s;
		lines = readFile(info.fn);
	}

	private void closePage() {
		if (history.size() != 0) {
			if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(edit,
					"Are you sure to close?", "Changes made",
					JOptionPane.YES_NO_OPTION)) {
				return;
			}
		}
		edit.pages.remove(edit.pageNo);
		if (edit.pageNo >= edit.pages.size()) {
			edit.pageNo = edit.pages.size() - 1;
		}
		if (edit.pages.size() == 0) {
			edit.frame.dispose();
			return;
		}
		edit.changePage(edit.pageNo);
	}

	private void saveAs() {
		JFileChooser chooser = new JFileChooser(info.fn);
		int returnVal = chooser.showSaveDialog(edit);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fn = chooser.getSelectedFile().getAbsolutePath();
			if (new File(fn).exists()) {
				if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
						edit, "file exists, are you sure to overwrite?",
						"save as...", JOptionPane.YES_NO_OPTION)) {
					message("not renamed");
					return;
				}
			}
			info.fn = fn;
			edit.changeTitle();
			message("file renamed");
			savePageToFile();
		}
	}

	private void redo() {

		HistoryInfo o = history.getRedo();
		if (o == null) {
			return;
		}
		// tem.out.println(o);
		if (o.type == History.INSERT) {
			doPaste(o.s, o.x1, o.y1, false);
		} else if (o.type == History.DELETE) {
			deleteRect(new Rectangle(o.x1, o.y1, o.x2, o.y2), false);
		} else if (o.type == History.REPLACEALL) {
			cx = o.x1;
			cy = o.y1;
			doReplaceAll(o.s, true, false, o.s2, false);// bug expected!
		} else {
			System.out.println("not supported " + o);
		}

	}

	private void changePage() {
		Object[] possibilities = edit.pages.toArray();
		PageInfo p = (PageInfo) JOptionPane.showInputDialog(edit,
				"Select Document:", "Select Document",
				JOptionPane.QUESTION_MESSAGE, null, possibilities, null);

		if (p != null) {
			int i = edit.pages.indexOf(p);
			if (i >= 0) {
				edit.changePage(i);
			}
		}
	}

	private void findNext() {
		if (text2find != null && text2find.length() > 0) {
			Point p = find(text2find, cx, cy);
			if (p == null) {
				message("string not found");
			} else {
				cx = p.x;
				cy = p.y;
				selectstartx = cx;
				selectstarty = cy;
				selectstopx = cx + text2find.length();
				selectstopy = cy;
				focusCursor();
			}
		}
	}

	private void find() {
		String t = getSelected();
		int p1 = t.indexOf("\n");
		if (p1 >= 0) {
			t = t.substring(0, p1);
		}
		if (t.length() == 0 && text2find != null) {
			t = text2find;
		}

		if (findWindow.jta1.getText().length() == 0) {
			findWindow.jta1.setText(t);
		}
		findWindow.show();
	}

	private Point find(String s, int x, int y) {
		if (ignoreCase) {
			s = s.toLowerCase();
		}
		// first half row
		int p1 = getline(y).toString(ignoreCase).indexOf(s, x + 1);
		if (p1 >= 0) {
			return new Point(p1, y);
		}
		// middle rows
		int fy = y;
		for (int i = 0; i < lines.size() - 1; i++) {
			fy += 1;
			if (fy >= lines.size()) {
				fy = 0;
			}
			p1 = getline(fy).toString(ignoreCase).indexOf(s);
			if (p1 >= 0) {
				return new Point(p1, fy);
			}
		}
		// last half row
		fy += 1;
		if (fy >= lines.size()) {
			fy = 0;
		}
		p1 = getline(fy).toString(ignoreCase).substring(0, x).indexOf(s);
		if (p1 >= 0) {
			return new Point(p1, fy);
		}
		return null;
	}

	private void gotoLine() {
		String s = JOptionPane.showInputDialog("Goto Line");
		int line = -1;
		try {
			line = Integer.parseInt(s);
		} catch (Exception e) {
			line = -1;
		}
		if (line > lines.size()) {
			line = -1;
		}
		if (line > 0) {
			line -= 1;
			sy = line;
			cy = line;
			cx = 0;
			focusCursor();
			edit.repaint();
		}
	}

	private void newFile() {
		edit.newFile();
	}

	private void saveFile() {
		if (info.fn == null) {
			JFileChooser chooser = new JFileChooser(info.defaultPath);
			int returnVal = chooser.showSaveDialog(edit);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fn = chooser.getSelectedFile().getAbsolutePath();
				if (new File(fn).exists()) {
					if (JOptionPane.YES_OPTION != JOptionPane
							.showConfirmDialog(edit,
									"Are you sure to overwrite?",
									"File exists", JOptionPane.YES_NO_OPTION)) {
						message("not saved");
						return;
					}
				}
				info.fn = fn;
				edit.changeTitle();
			} else {
				return;
			}
		}
		savePageToFile();

	}

	private void savePageToFile() {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(info.fn), encoding));
			for (StringBuffer sb : lines) {
				out.write(sb.toString());
				out.write("\n");
			}
			out.close();
			System.out.println("saved");
			message("saved");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void message(String s) {
		msg = s;
		msgtime = System.currentTimeMillis();

	}

	private void openFile() {
		JFileChooser chooser = new JFileChooser();
		// Note: source for ExampleFileFilter can be found in FileChooserDemo,
		// under the demo/jfc directory in the JDK.
		if (info.fn != null) {
			chooser.setSelectedFile(new File(info.fn));
		}
		int returnVal = chooser.showOpenDialog(edit);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fn = chooser.getSelectedFile().getAbsolutePath();
			System.out.println("You chose to open this file: "
					+ chooser.getSelectedFile().getAbsolutePath());
			edit.openFile(fn);
		}

	}

	private void help() {
		// Editor editor = new Editor();
		// editor.openFile(args[0]);
		// editor.show(true);
		// editor.repaint();

	}

	private void cancelSelect() {
		selectstartx = cx;
		selectstarty = cy;
		selectstopx = cx;
		selectstopy = cy;
	}

	private void selectAll() {
		selectstartx = 0;
		selectstarty = 0;
		selectstopy = getLinesize() - 1;
		selectstopx = getline(selectstopy).length();

	}

	private void focusCursor() {
		if (cy < sy) {
			sy = cy;
		}
		if (showLineCnt > 0) {
			if (sy + showLineCnt - 1 < cy) {
				sy = cy;
			}
		}

	}

	@Override
	public void keyTyped(KeyEvent env) {
		if (env.isControlDown() || env.isAltDown()) {
			// ignore
		} else {
			insert(env.getKeyChar());
		}

	}

	private void cutSelected() {
		copySelected();
		deleteSelection();
		cancelSelect();
	}

	private void pasteSelected() {
		if (isSelected()) {
			deleteSelection();
		}
		String s;
		try {
			s = Toolkit.getDefaultToolkit().getSystemClipboard().getData(
					DataFlavor.stringFlavor).toString();
		} catch (Exception e) {
			s = "";
		}
		doPaste(s, cx, cy, true);

	}

	private Rectangle doPaste(String s, int cx, int cy, boolean record) {
		Rectangle r = new Rectangle();
		r.x = cx;
		r.y = cy;
		int p1 = 0;
		int ocx = cx;
		int ocy = cy;
		while (true) {
			int p2 = s.indexOf("\n", p1);
			if (p2 < 0) {
				getline(cy).sb().insert(cx, U.f(s.substring(p1, s.length())));
				cx += s.length() - p1;
				break;
			}
			if (cx == 0) {
				lines.add(cy, new StringBuffer(U.f(s.substring(p1, p2))));
			} else {
				getline(cy).sb().insert(cx, U.f(s.substring(p1, p2)));
				lines.add(cy + 1, new StringBuffer(getline(cy).substring(
						cx + p2 - p1)));
				getline(cy).sb().setLength(cx + p2 - p1);
				cx = 0;
			}
			cy += 1;
			p1 = p2 + 1;
		}
		if (record) {
			history.add(new HistoryInfo(History.INSERT, ocy, ocx, cx, s, cy));
		}
		this.cx = cx;
		this.cy = cy;
		r.width = cx;
		r.height = cy;
		cancelSelect();
		focusCursor();
		return r;

	}

	private void copySelected() {
		String s = getSelected();
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				new StringSelection(s), null);
		message("copied " + s.length());
	}

	private String getTextInRect(Rectangle r) {
		int x1 = r.x;
		int y1 = r.y;
		int x2 = r.width;
		int y2 = r.height;

		StringBuffer sb = new StringBuffer();
		if (y1 == y2 && x1 < x2) {
			sb.append(getInLine(y1, x1, x2));
		} else if (y1 < y2) {
			sb.append(getInLine(y1, x1, Integer.MAX_VALUE));
			for (int i = y1 + 1; i < y2; i++) {
				sb.append(lineSep);
				sb.append(getline(i));
			}
			sb.append(lineSep);
			sb.append(getInLine(y2, 0, x2));

		}
		return sb.toString();
	}

	private String getSelected() {
		return getTextInRect(getSelectRect());
	}

	private void insert(char ch) {
		if (ch == KeyEvent.VK_ENTER) {
			if (isSelected()) {
				deleteSelection();
			}
			RoSb sb = getline(cy);
			String indent = getIndent(sb.toString());
			String s = sb.substring(cx, sb.length());
			lines.add(cy + 1, new StringBuffer(indent + s));
			deleteInLine(cy, cx, Integer.MAX_VALUE, false);
			int ocy = cy;
			int ocx = cx;
			cy += 1;
			cx = indent.length();
			history
					.add(new HistoryInfo(History.INSERT, ocy, ocx, cx, null, cy));
		} else if (ch == KeyEvent.VK_BACK_SPACE) {
			if (isSelected()) {
				deleteSelection();
			} else {

				if (cx > 0) {
					deleteInLine(cy, cx - 1, cx);
					cx -= 1;
				} else {
					if (cy > 0) {
						cx = getline(cy - 1).length();
						u_mergeLine(cy - 1);
						cy -= 1;
					}
				}
			}
		} else if (ch == KeyEvent.VK_DELETE) {
			if (isSelected()) {
				deleteSelection();
			} else {
				if (cx < getline(cy).length()) {
					deleteInLine(cy, cx, cx + 1);
				} else {
					if (cy < getLinesize() - 1) {
						u_mergeLine(cy);
					}
				}
			}
		} else {
			if (isSelected()) {
				deleteSelection();
			}
			RoSb sb = getline(cy);
			if (cx > sb.length()) {
				cx = sb.length();
			}
			u_insertInLine(cy, cx, ch);
			cx += 1;
		}
		focusCursor();
		cancelSelect();
		edit.repaint();
	}

	private String getIndent(String s) {
		int p = 0;
		while (p < s.length() && (s.charAt(p) == ' ' || s.charAt(p) == '\t')) {
			p += 1;
		}
		return s.substring(0, p);
	}

	private void u_insertInLine(int cy, int cx, char ch) {
		getline(cy).sb().insert(cx, ch);
		history.add(new HistoryInfo(History.INSERT, cy, cx, cx + 1, null, cy));
	}

	private void u_mergeLine(int i) {
		String x = getline(i + 1).toString();
		int ol = getline(i).length();
		getline(i).sb().append(x);
		lines.remove(i + 1);
		history.add(new HistoryInfo(History.DELETE, i, ol, 0, "\n", i + 1));
	}

	private void u_removeLine(int i) {
		StringBuffer sb = lines.remove(i);
		history.add(new HistoryInfo(History.DELETE, i, 0, sb.length(), sb
				.toString(), i));

	}

	private void deleteSelection() {
		deleteSelection(true);
	}

	private void deleteSelection(boolean record) {
		deleteRect(getSelectRect(), record);
	}

	private Rectangle getSelectRect() {
		int x1, x2, y1, y2;
		if (selectstopy < selectstarty) {
			y1 = selectstopy;
			y2 = selectstarty;
			x1 = selectstopx;
			x2 = selectstartx;
		} else {
			y2 = selectstopy;
			y1 = selectstarty;
			x2 = selectstopx;
			x1 = selectstartx;
			if (x1 > x2 && y1 == y2) {
				x1 = selectstopx;
				x2 = selectstartx;
			}
		}
		return new Rectangle(x1, y1, x2, y2);
	}

	private void deleteRect(Rectangle r, boolean record) {
		int x1 = r.x;
		int y1 = r.y;
		int x2 = r.width;
		int y2 = r.height;
		if (y1 == y2 && x1 < x2) {
			deleteInLine(y1, x1, x2, record);
		} else if (y1 < y2) {
			int delcnt = y2 - y1;
			if (record && delcnt > 200 && delcnt > getLinesize() - delcnt) {
				System.out.println("reverse delete mode");
				List<StringBuffer> l2 = new Vector<StringBuffer>();
				for (int i = 0; i < y1; i++) {
					l2.add(getline(i).sb());
				}
				if (x1 > 0) {
					l2.add(new StringBuffer(getline(y1).substring(0, x1)));
				}
				if (x2 < getline(y2).length() - 1) {
					l2.add(new StringBuffer(getline(y2).substring(x2)));
				}
				for (int i = y2 + 1; i < getLinesize(); i++) {
					l2.add(getline(i).sb());
				}
				lines = l2;
				if (getLinesize() == 0) {
					lines.add(new StringBuffer());
				}
				U.gc();
				history.clear();
			} else {// normal mode
				if (record) {
					history.add(new HistoryInfo(History.DELETE, y1, x1, x2,
							getSelected(), y2));
				}
				deleteInLine(y1, x1, Integer.MAX_VALUE, false);
				deleteInLine(y2, 0, x2, false);
				for (int i = y1 + 1; i < y2; i++) {
					lines.remove(y1 + 1);
				}
				lines.get(y1).append(getline(y1 + 1).toString());
				lines.remove(y1 + 1);
			}
		}
		cx = x1;
		cy = y1;
		focusCursor();
	}

	private void deleteInLine(int y, int x1, int x2) {
		deleteInLine(y, x1, x2, true);
	}

	private int getLinesize() {
		return lines.size();
	}

	private void deleteInLine(int y, int x1, int x2, boolean record) {
		StringBuffer sb = lines.get(y);
		if (x2 > sb.length()) {
			x2 = sb.length();
		}
		if (x1 > sb.length()) {
			x1 = sb.length();
		}
		String s = sb.substring(x1, x2);
		sb.delete(x1, x2);
		if (record) {
			history.add(new HistoryInfo(History.DELETE, y, x1, x2, s, y));
		}
	}

	private RoSb getline(int i) {
		return new RoSb(lines.get(i));
	}

	private String getInLine(int y, int x1, int x2) {
		RoSb sb = getline(y);
		if (x2 > sb.length()) {
			x2 = sb.length();
		}
		if (x1 > sb.length()) {
			x1 = sb.length();
		}
		return sb.substring(x1, x2);
	}

	private boolean isSelected() {
		Rectangle r = getSelectRect();
		int x1 = r.x;
		int y1 = r.y;
		int x2 = r.width;
		int y2 = r.height;
		if (y1 == y2 && x1 < x2) {
			return true;
		} else if (y1 < y2) {
			return true;
		}
		return false;
	}

	@Override
	public void mousePressed(MouseEvent env) {
		mx = env.getX();
		my = env.getY();
		mshift = env.isShiftDown();
		mcount = env.getClickCount();
		edit.repaint();

	}

	@Override
	public void mouseDragged(MouseEvent env) {
		mx = env.getX();
		my = env.getY();
		mshift = true;
		edit.repaint();
	}

	public void undo() {
		HistoryInfo o = history.get();
		System.out.println(o);
		if (o == null) {
			return;
		}
		// tem.out.println(o);
		if (o.type == History.INSERT) {
			Rectangle r = new Rectangle(o.x1, o.y1, o.x2, o.y2);
			o.s = getTextInRect(r);
			deleteRect(r, false);
		} else if (o.type == History.DELETE) {
			Rectangle r = doPaste(o.s, o.x1, o.y1, false);
			o.x2 = r.width;
			o.y2 = r.height;
		} else if (o.type == History.REPLACEALL) {
			doReplaceAll(o.s2, true, false, o.s, false);// bug expected!
		} else {
			System.out.println("not supported " + o);
		}

	}

	public void doFind(String text, boolean ignoreCase, boolean selected2) {
		text2find = text;
		this.ignoreCase = ignoreCase;
		findNext();
		edit.repaint();
	}

	public void doReplace(String text, boolean ignoreCase, boolean selected2,
			String text2, boolean record) {
		_doReplace(text, ignoreCase, selected2, text2, false, record);

	}

	private void _doReplace(String text, boolean ignoreCase, boolean selected2,
			String text2, boolean all, boolean record) {
		text2find = text;
		if (text2find != null && text2find.length() > 0) {
			Point p = replace(text2find, cx, cy, text2, all, ignoreCase);
			if (p == null) {
				message("string not found");
			} else {
				cx = p.x;
				cy = p.y;
				selectstartx = cx;
				selectstarty = cy;
				selectstopx = cx + text2.length();
				selectstopy = cy;
				focusCursor();
				if (record) {
					if (!all) {
						history.add(new HistoryInfo(History.DELETE, cy, cx, cx
								+ text.length(), text, cy));
						history.add(new HistoryInfo(History.INSERT, cy, cx, cx
								+ text2.length(), text2, cy));
					} else {
						history.add(new HistoryInfo(History.REPLACEALL, cy, cx,
								cx, text, cy, text2));
					}
				}
			}
		}
		edit.repaint();
	}

	private Point replace(String s, int x, int y, String s2, boolean all,
			boolean ignoreCase) {
		if (ignoreCase) {
			s = s.toLowerCase();
		}
		// first half row
		boolean found = false;
		int p1 = x + 1;
		while (true) {
			p1 = getline(y).toString(ignoreCase).indexOf(s, p1);
			if (p1 >= 0) {
				found = true;
				getline(y).sb().replace(p1, p1 + s.length(), s2);
				if (!all) {
					return new Point(p1, y);
				}
				p1 = p1 + 1;
			} else {
				break;
			}
		}
		// middle rows
		int fy = y;
		for (int i = 0; i < lines.size() - 1; i++) {
			fy += 1;
			if (fy >= lines.size()) {
				fy = 0;
			}
			p1 = 0;
			while (true) {
				p1 = getline(fy).toString(ignoreCase).indexOf(s, p1);
				if (p1 >= 0) {
					found = true;
					getline(fy).sb().replace(p1, p1 + s.length(), s2);
					if (!all) {
						return new Point(p1, fy);
					}
					p1 = p1 + 1;
				} else {
					break;
				}
			}
		}
		// last half row
		fy += 1;
		if (fy >= lines.size()) {
			fy = 0;
		}
		p1 = 0;
		while (true) {
			p1 = getline(fy).toString(ignoreCase).substring(0, x)
					.indexOf(s, p1);
			if (p1 >= 0) {
				found = true;
				getline(fy).sb().replace(p1, p1 + s.length(), s2);
				if (!all) {
					return new Point(p1, fy);
				}
				p1 = p1 + 1;
			} else {
				break;
			}
		}
		if (found) {
			return new Point(x, y);
		} else {
			return null;
		}
	}

	public void doReplaceAll(String text, boolean ignoreCase,
			boolean selected2, String text2, boolean record) {
		_doReplace(text, ignoreCase, selected2, text2, true, record);

	}
}
