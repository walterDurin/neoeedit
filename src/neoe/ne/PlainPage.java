package neoe.ne;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import neoe.ne.U.RoSb;
import neoe.util.FileIterator;

public class PlainPage implements IPage {
	class Cursor {
	}

	class Edit {
		void appendMsgLine(PlainPage plainPage, String s) {
			plainPage.lines.add(new StringBuffer(s));
		}

		void deleteInLine(int y, int x1, int x2) {
			deleteInLine(y, x1, x2, true);
		}

		void deleteInLine(int y, int x1, int x2, boolean record) {
			StringBuffer sb = lines.get(y);
			if (x2 > sb.length()) {
				x2 = sb.length();
			}
			if (x1 > sb.length()) {
				x1 = sb.length();
			}
			String deleted = sb.substring(x1, x2);
			sb.delete(x1, x2);
			if (record) {
				history.addOne(new HistoryInfo(x1, y, deleted, x1, y, x2, y));
			}
		}

		void deleteRect(PlainPage plainPage, Rectangle r, boolean record) {
			int x1 = r.x;
			int y1 = r.y;
			int x2 = r.width;
			int y2 = r.height;
			if (y1 == y2 && x1 < x2) {
				ptEdit.deleteInLine(y1, x1, x2, record);
			} else if (y1 < y2) {
				int delcnt = y2 - y1;
				String deleted = null;
				if (record) {
					deleted = ptSelection.getTextInRect(r);
				}
				if (delcnt > 200
						&& delcnt > plainPage.ptEdit.getLinesize() - delcnt) {
					System.out.println("reverse delete mode");
					List<StringBuffer> l2 = new Vector<StringBuffer>();
					for (int i = 0; i < y1; i++) {
						l2.add(plainPage.ptEdit.getline(i).sb());
					}
					if (x1 > 0) {
						l2.add(new StringBuffer(plainPage.ptEdit.getline(y1)
								.substring(0, x1)));
					}
					if (x2 < plainPage.ptEdit.getline(y2).length() - 1) {
						l2.add(new StringBuffer(plainPage.ptEdit.getline(y2)
								.substring(x2)));
					}
					for (int i = y2 + 1; i < plainPage.ptEdit.getLinesize(); i++) {
						l2.add(plainPage.ptEdit.getline(i).sb());
					}
					plainPage.lines = l2;
					if (plainPage.ptEdit.getLinesize() == 0) {
						plainPage.lines.add(new StringBuffer());
					}
				} else {// normal mode

					ptEdit.deleteInLine(y1, x1, Integer.MAX_VALUE, false);
					ptEdit.deleteInLine(y2, 0, x2, false);
					for (int i = y1 + 1; i < y2; i++) {
						plainPage.lines.remove(y1 + 1);
					}
					plainPage.lines.get(y1).append(
							plainPage.ptEdit.getline(y1 + 1).toString());
					plainPage.lines.remove(y1 + 1);
				}
				if (record) {
					history.addOne(new HistoryInfo(x1, y1, deleted, x1, y1, x2,
							y2));
				}
			}
			plainPage.cx = x1;
			plainPage.cy = y1;
			plainPage.focusCursor();
		}

		void deleteSelection() {
			deleteSelection(true);
		}

		void deleteSelection(boolean record) {
			deleteRect(PlainPage.this, ptSelection.getSelectRect(), record);
		}

		Rectangle doPaste(String s, int cx, int cy, boolean record) {
			Rectangle r = new Rectangle();
			r.x = cx;
			r.y = cy;
			int p1 = 0;
			while (true) {
				int p2 = s.indexOf("\n", p1);
				if (p2 < 0) {
					ptEdit.getline(cy).sb().insert(cx,
							U.f(s.substring(p1, s.length())));
					cx += s.length() - p1;
					break;
				}
				if (cx == 0) {
					lines.add(cy, new StringBuffer(U.f(s.substring(p1, p2))));
				} else {
					ptEdit.getline(cy).sb()
							.insert(cx, U.f(s.substring(p1, p2)));
					lines.add(cy + 1, new StringBuffer(ptEdit.getline(cy)
							.substring(cx + p2 - p1)));
					ptEdit.getline(cy).sb().setLength(cx + p2 - p1);
					cx = 0;
				}
				cy += 1;
				p1 = p2 + 1;
			}
			if (record) {
				history.addOne(new HistoryInfo(r.x, r.y, "", cx, cy, -1, -1));
			}
			PlainPage.this.cx = cx;
			PlainPage.this.cy = cy;
			r.width = cx;
			r.height = cy;
			ptSelection.cancelSelect();
			focusCursor();
			return r;

		}

		RoSb getline(int i) {
			return new RoSb(lines.get(i));
		}

		int getLinesize() {
			return lines.size();
		}

		void insert(char ch) {
			if (ch == KeyEvent.VK_ENTER) {
				if (ptSelection.isSelected()) {
					ptEdit.deleteSelection();
				}
				RoSb sb = getline(cy);
				String indent = U.getIndent(sb.toString());
				String s = sb.substring(cx, sb.length());
				lines.add(cy + 1, new StringBuffer(indent + s));
				deleteInLine(cy, cx, Integer.MAX_VALUE, false);
				int ocy = cy;
				int ocx = cx;
				cy += 1;
				cx = indent.length();
				history.addOne(new HistoryInfo(ocx, ocy, "", cx, cy, -1, -1));
			} else if (ch == KeyEvent.VK_BACK_SPACE) {
				if (ptSelection.isSelected()) {
					ptEdit.deleteSelection();
				} else {
					if (cx > 0) {
						deleteInLine(cy, cx - 1, cx);
						cx -= 1;
					} else {
						if (cy > 0) {
							cx = ptEdit.getline(cy - 1).length();
							mergeLine(cy - 1);
							cy -= 1;
						}
					}
				}
			} else if (ch == KeyEvent.VK_DELETE) {
				if (ptSelection.isSelected()) {
					ptEdit.deleteSelection();
				} else {
					if (cx < ptEdit.getline(cy).length()) {
						deleteInLine(cy, cx, cx + 1);
					} else {
						if (cy < ptEdit.getLinesize() - 1) {
							mergeLine(cy);
						}
					}
				}
			} else if (ch == KeyEvent.VK_ESCAPE) {
				selectstopy = selectstarty;
				selectstopx = selectstartx;
			} else {
				if (ptSelection.isSelected()) {
					ptEdit.deleteSelection();
				}
				RoSb sb = ptEdit.getline(cy);
				if (cx > sb.length()) {
					cx = sb.length();
				}
				insertInLine(cy, cx, ch);
				cx += 1;
			}
			focusCursor();
			ptSelection.cancelSelect();
			edit.repaint();
		}

		private void insertInLine(int cy, int cx, char ch) {
			lines.get(cy).insert(cx, ch);
			history.addOne(new HistoryInfo(cx, cy, "", cx + 1, cy, -1, -1));
		}

		private void mergeLine(int y) {
			int ol = ptEdit.getline(y).length();
			lines.get(y).append(lines.get(y + 1));
			lines.remove(y + 1);
			history.addOne(new HistoryInfo(ol, y, "\n", ol, y, 0, y + 1));
		}

		void pasteSelected() {
			if (ptSelection.isSelected()) {
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

		public void readFile(String fn) {
			lines = ptFiles.readFile(fn);
			history.clear();
		}

		void reloadWithEncodingByUser() {
			if (info.fn == null) {
				message("file not saved.");
				return;
			}
			ptFiles.setEncodingByUser(PlainPage.this);
			readFile(info.fn);
		}

		private void removeLine(int y) {
			StringBuffer sb = lines.remove(y);
			history
					.addOne(new HistoryInfo(0, y, sb.toString(), 0, y, 0, y + 1));
		}

		private void removeTrailingSpace() {
			for (int i = 0; i < ptEdit.getLinesize(); i++) {
				RoSb sb = ptEdit.getline(i);
				int p = sb.length() - 1;
				while (p >= 0 && "\r\n\t ".indexOf(sb.charAt(p)) >= 0) {
					p--;
				}
				if (p < sb.length() - 1) {
					deleteInLine(i, p + 1, sb.length());
				}
			}
		}
	}

	class Files {
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

		private void closePage() {
			if (history.size() != 0) {
				if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
						edit, "Are you sure to close?", "Changes made",
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

		private void openFile() throws Exception {
			JFileChooser chooser = new JFileChooser();
			if (info.fn != null) {
				chooser.setSelectedFile(new File(info.fn));
			} else if (info.workPath != null) {
				chooser.setSelectedFile(new File(info.workPath));// Fixme:cannot
				// set
				// correctly
			}
			int returnVal = chooser.showOpenDialog(edit);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fn = chooser.getSelectedFile().getAbsolutePath();
				System.out.println("You chose to open this file: "
						+ chooser.getSelectedFile().getAbsolutePath());
				edit.openFileInNewWindow(fn);
			}

		}

		private List<StringBuffer> readFile(String fn) {
			if (fn == null) {
				List<StringBuffer> lines = new ArrayList<StringBuffer>();
				lines.add(new StringBuffer("edit here..."));
				return lines;
			}
			if (encoding == null) {
				encoding = U.guessEncodingForEditor(fn);
			}
			return U.readFileForEditor(fn, encoding);
		}

		private void saveAllFiles() throws Exception {
			int total = 0;
			for (PageInfo pi : edit.pages) {
				if (U.saveFile(pi)) {
					total++;
				}
			}
			System.out.println(total + " files saved");
			message(total + " files saved");
		}

		private void saveAs() throws Exception {
			JFileChooser chooser = new JFileChooser(info.fn);
			int returnVal = chooser.showSaveDialog(edit);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fn = chooser.getSelectedFile().getAbsolutePath();
				if (new File(fn).exists()) {
					if (JOptionPane.YES_OPTION != JOptionPane
							.showConfirmDialog(edit,
									"file exists, are you sure to overwrite?",
									"save as...", JOptionPane.YES_NO_OPTION)) {
						message("not renamed");
						return;
					}
				}
				info.fn = fn;
				edit.changeTitle();
				message("file renamed");
				U.savePageToFile(info);
			}
		}

		void setEncodingByUser(PlainPage plainPage) {
			String s = JOptionPane.showInputDialog(plainPage.edit,
					"Reload with Encoding:", plainPage.encoding);
			if (s == null) {
				return;
			}
			try {
				"a".getBytes(s);
			} catch (Exception e) {
				plainPage.message("bad encoding:" + s);
				return;
			}
			plainPage.encoding = s;
		}

	}

	class FindAndReplace {

		private boolean ignoreCase = true;
		private String text2find;

		void _doReplace(PlainPage plainPage, String text, boolean ignoreCase,
				boolean selected2, String text2, boolean all, boolean record,
				boolean inDir, String dir) {

			text2find = text;
			if (text2find != null && text2find.length() > 0) {
				Point p = replace(plainPage, text2find, plainPage.cx,
						plainPage.cy, text2, all, ignoreCase, record);
				if (p == null) {
					plainPage.message("string not found");
				} else {
					plainPage.cx = p.x;
					plainPage.cy = p.y;
					plainPage.selectstartx = plainPage.cx;
					plainPage.selectstarty = plainPage.cy;
					plainPage.selectstopx = plainPage.cx + text2.length();
					plainPage.selectstopy = plainPage.cy;
					plainPage.focusCursor();
					// if (record) {
					// if (!all) {
					// plainPage.history.add(new HistoryInfo(
					// History.DELETE, plainPage.cy, plainPage.cx,
					// plainPage.cx + text.length(), text,
					// plainPage.cy));
					// plainPage.history.add(new HistoryInfo(
					// History.INSERT, plainPage.cy, plainPage.cx,
					// plainPage.cx + text2.length(), text2,
					// plainPage.cy));
					// } else {
					// plainPage.history.add(new HistoryInfo(
					// History.REPLACEALL, plainPage.cy,
					// plainPage.cx, plainPage.cx, text,
					// plainPage.cy, text2));
					// }
					// }
				}
			}
			plainPage.edit.repaint();
		}

		public void doFind(String text, boolean ignoreCase, boolean selected2,
				boolean inDir, String dir) throws Exception {
			if (!inDir) {
				text2find = text;
				this.ignoreCase = ignoreCase;
				findNext();
				edit.repaint();
			} else {
				doFindInDir(text, ignoreCase, selected2, inDir, dir);
			}
		}

		private void doFindInDir(String text, boolean ignoreCase,
				boolean selected2, boolean inDir, String dir) throws Exception {
			Iterable<File> it = new FileIterator(dir);
			List<String> all = new ArrayList<String>();
			for (File f : it) {
				if (f.isDirectory()) {
					continue;
				}
				List<String> res = U.findInFile(f, text, ignoreCase);
				all.addAll(res);
			}
			PageInfo pi = edit.newEmptyFile(edit.getWorkPath());

			PlainPage p2 = (PlainPage) pi.page;
			p2.ptEdit.appendMsgLine(p2, String
					.format("find %s results in dir %s for '%s'", all.size(),
							dir, text));
			for (Object o : all) {
				p2.ptEdit.appendMsgLine(p2, o.toString());
			}
			edit.repaint();
		}

		public void doReplace(String text, boolean ignoreCase,
				boolean selected2, String text2, boolean record) {
			_doReplace(PlainPage.this, text, ignoreCase, selected2, text2,
					false, record, false, null);

		}

		public void doReplaceAll(String text, boolean ignoreCase,
				boolean selected2, String text2, boolean record, boolean inDir,
				String dir) throws Exception {
			if (inDir) {
				doReplaceInDir(text, ignoreCase, text2, inDir, dir);
			} else {
				ptFind._doReplace(PlainPage.this, text, ignoreCase, selected2,
						text2, true, record, inDir, dir);
			}
		}

		private void doReplaceInDir(String text, boolean ignoreCase2,
				String text2, boolean inDir, String dir) throws Exception {
			Iterable<File> it = new FileIterator(dir);
			List<String> all = new ArrayList<String>();
			for (File f : it) {
				if (f.isDirectory()) {
					continue;
				}
				try {
					List<String> res = U.findInFile(f, text, ignoreCase);
					if (res.size() > 0) {
						PageInfo pi = edit.openFile(f.getAbsolutePath());

						pi.initPage(edit);

						if (pi != null) {
							((PlainPage) pi.page).ptFind.doReplaceAll(text,
									ignoreCase2, false, text2, true, false,
									null);
						}
					}
					all.addAll(res);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			PageInfo pi = edit.newEmptyFile(edit.getWorkPath());
			PlainPage p2 = (PlainPage) pi.page;
			p2.ptEdit.appendMsgLine(p2, String.format(
					"replaced %s results in dir %s for '%s', not saved.", all
							.size(), dir, text));
			for (Object o : all) {
				p2.ptEdit.appendMsgLine(p2, o.toString());
			}
			edit.repaint();
		}

		private void find() {
			String t = ptSelection.getSelected();
			int p1 = t.indexOf("\n");
			if (p1 >= 0) {
				t = t.substring(0, p1);
			}
			if (t.length() == 0 && text2find != null) {
				t = text2find;
			}

			if (t.length() > 0) {
				findWindow.jta1.setText(t);
			}

			findWindow.show();
			findWindow.jta1.grabFocus();
		}

		private Point find(String s, int x, int y) {
			if (ignoreCase) {
				s = s.toLowerCase();
			}
			// first half row
			int p1 = ptEdit.getline(y).toString(ignoreCase).indexOf(s, x);
			if (p1 >= 0) {
				return new Point(p1, y);
			}
			// middle rows
			int fy = y;
			for (int i = 0; i < ptEdit.getLinesize() - 1; i++) {
				fy += 1;
				if (fy >= ptEdit.getLinesize()) {
					fy = 0;
				}
				p1 = ptEdit.getline(fy).toString(ignoreCase).indexOf(s);
				if (p1 >= 0) {
					return new Point(p1, fy);
				}
			}
			// last half row
			fy += 1;
			if (fy >= ptEdit.getLinesize()) {
				fy = 0;
			}
			p1 = ptEdit.getline(fy).toString(ignoreCase).substring(0, x)
					.indexOf(s);
			if (p1 >= 0) {
				return new Point(p1, fy);
			}
			return null;
		}

		private void findNext() {
			if (text2find != null && text2find.length() > 0) {
				Point p = find(text2find, cx + 1, cy);
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

		Point replace(PlainPage thePage, String s, int x, int y, String s2,
				boolean all, boolean ignoreCase, boolean record) {// TODO:record
			if (ignoreCase) {
				s = s.toLowerCase();
			}
			// first half row
			boolean found = false;
			int p1 = x;
			while (true) {
				p1 = thePage.ptEdit.getline(y).toString(ignoreCase).indexOf(s,
						p1);
				if (p1 >= 0) {
					found = true;
					thePage.ptEdit.getline(y).sb().replace(p1, p1 + s.length(),
							s2);
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
			for (int i = 0; i < thePage.ptEdit.getLinesize() - 1; i++) {
				fy += 1;
				if (fy >= thePage.ptEdit.getLinesize()) {
					fy = 0;
				}
				p1 = 0;
				while (true) {
					p1 = thePage.ptEdit.getline(fy).toString(ignoreCase)
							.indexOf(s, p1);
					if (p1 >= 0) {
						found = true;
						thePage.ptEdit.getline(fy).sb().replace(p1,
								p1 + s.length(), s2);
						if (!all) {
							return new Point(p1 + s2.length(), fy);
						}
						p1 = p1 + 1;
					} else {
						break;
					}
				}
			}
			// last half row
			fy += 1;
			if (fy >= thePage.ptEdit.getLinesize()) {
				fy = 0;
			}
			p1 = 0;
			while (true) {
				p1 = thePage.ptEdit.getline(fy).toString(ignoreCase).substring(
						0, x).indexOf(s, p1);
				if (p1 >= 0) {
					found = true;
					thePage.ptEdit.getline(fy).sb().replace(p1,
							p1 + s.length(), s2);
					if (!all) {
						return new Point(p1 + s2.length(), fy);
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

	}

	static class History {
		public static int MAXSIZE = 200;
		private List<HistoryInfo> atom;
		private LinkedList<List<HistoryInfo>> data;
		private int p;

		public History(PlainPage page) {
			data = new LinkedList<List<HistoryInfo>>();
			p = 0;
			atom = new ArrayList<HistoryInfo>();
		}

		private void add(List<HistoryInfo> o) {
			if (p < data.size() && p >= 0) {
				for (int i = 0; i < data.size() - p; i++) {
					data.removeLast();
				}
			}

			List<HistoryInfo> last = data.peekLast();
			// stem.out.println("last=" + last);
			if (!append(last, o)) {
//				System.out.println("add:" + o);
				data.add(o);
				if (data.size() > MAXSIZE) {
					data.removeFirst();
				} else {
					p += 1;
				}
			}

		}

		public void addOne(HistoryInfo historyInfo) {
			atom.add(historyInfo);
		}

		/**
		 * try to append this change to the last ones
		 */
		private boolean append(List<HistoryInfo> lasts, List<HistoryInfo> os) {
			if (lasts == null) {
				return false;
			}
			boolean ret = false;
			if (os.size() == 1) {
				HistoryInfo o = os.get(0);
				HistoryInfo last = lasts.get(lasts.size() - 1);
				if (o.deleted.length() == 0 && last.deleted.length() == 0
						&& o.y == last.y2 && o.x == last.x2) {
					// last.x2 = o.x2;
					// last.y2 = o.y2;
					lasts.addAll(os);
					ret = true;
				} else if (o.x == o.x2 && o.y == o.y2 && last.x == last.x2
						&& last.y == last.y2) {
					if (last.y == o.dely2 && last.x == o.delx2) {
						lasts.addAll(os);
						// last.deleted = o.deleted + last.deleted;
						// last.x = o.x;
						// last.y = o.y;
						ret = true;
					} else if (last.x == o.x && last.y == o.y) {
						lasts.addAll(os);
						// last.deleted = last.deleted + o.deleted;
						// last.delx2 = -1;// invalid, will be valid when undo a
						// delete
						// last.dely2 = -1;// invalid, will be valid when undo a
						// delete
						ret = true;
					}
				}
			}
//			if (ret == true) {
//				System.out.println("append:" + os);
//			}
			return ret;
		}

		public void beginAtom() {
			if (atom.size() > 0) {
				endAtom();
			}
		}

		public void clear() {
			data.clear();
			p = 0;
		}

		public void endAtom() {
			if (atom.size() > 0) {
				add(atom);
				atom = new ArrayList<HistoryInfo>();
			}
		}

		public List<HistoryInfo> get() {
			if (p <= 0) {
				return null;
			}
			p -= 1;
//			System.out.println("undo:" + data.get(p));
			return data.get(p);
		}

		public List<HistoryInfo> getRedo() {
			if (p < data.size()) {
				p += 1;
				return data.get(p - 1);
			} else {
				return null;
			}
		}

		public int size() {
			return p;
		}

	}

	static class HistoryInfo {
		public String deleted;
		public int delx2;
		public int dely2;
		/** set when undo a insert */
		public String inserted;
		public int x;
		public int x2;
		public int y;
		public int y2;

		public HistoryInfo(int x, int y, String deleted, int x2, int y2,
				int delx2, int dely2) {
			super();
			this.x = x;
			this.y = y;
			this.deleted = deleted == null ? "" : deleted;
			this.x2 = x2;
			this.y2 = y2;
			this.delx2 = delx2;
			this.dely2 = dely2;
		}

		@Override
		public String toString() {
			return "HistoryInfo [deleted=" + deleted + ", delx2=" + delx2
					+ ", dely2=" + dely2 + ", inserted=" + inserted + ", x="
					+ x + ", x2=" + x2 + ", y=" + y + ", y2=" + y2 + "]";
		}
	}

	public static class PageInfo {
		public String fn;

		public IPage page;

		public long size;
		public String workPath;

		public PageInfo(String fn, long size, EditWindow editor)
				throws Exception {
			this.fn = fn;
			this.size = size;
			if (fn != null) {
				this.workPath = new File(fn).getParent();
			}
			initPage(editor);
		}

		public void initPage(EditWindow editor) throws Exception {
			if (page == null) {
				if (size < 10000000) {
					page = new PlainPage(editor, this);
				} else {
					page = new LargePage(editor, this);
				}
			}

		}

		public String toString() {
			String s;
			if (fn == null) {
				s = "New Text...";
			} else {
				File f = new File(fn);
				s = f.getName() + " " + f.getParent();
			}
			return s;
		}

	}

	class RedoUndo {

		void redo() throws Exception {

			List<HistoryInfo> os = history.getRedo();
			if (os == null) {
				return;
			}
			// tem.out.println(o);
			for (HistoryInfo o : os) {
				if (o.deleted.length() == 0) {
					ptEdit.doPaste(o.inserted, o.x, o.y, false);
					o.inserted = null;
				} else {
					ptEdit.deleteRect(PlainPage.this, new Rectangle(o.x, o.y,
							o.x2, o.y2), false);
				}
			}
		}

		void undo() throws Exception {
			List<HistoryInfo> os = history.get();
			if (os == null) {
				return;
			}
			for (int i = os.size() - 1; i >= 0; i--) {
				HistoryInfo o = os.get(i);
				if (o.deleted.length() == 0) {
					Rectangle r = new Rectangle(o.x, o.y, o.x2, o.y2);
					o.inserted = ptSelection.getTextInRect(r);
					ptEdit.deleteRect(PlainPage.this, r, false);
				} else {
					Rectangle r = ptEdit.doPaste(o.deleted, o.x, o.y, false);
					o.x2 = r.width;
					o.y2 = r.height;
				}
			}
		}

	}

	class Selection {
		private void cancelSelect() {
			selectstartx = cx;
			selectstarty = cy;
			selectstopx = cx;
			selectstopy = cy;
		}

		private void copySelected() {
			String s = getSelected();
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
					new StringSelection(s), null);
			message("copied " + s.length());
		}

		private void cutSelected() {
			copySelected();
			ptEdit.deleteSelection();
			cancelSelect();
		}

		String getSelected() {
			return getTextInRect(getSelectRect());
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
					sb.append(ptEdit.getline(i));
				}
				sb.append(lineSep);
				sb.append(getInLine(y2, 0, x2));

			}
			return sb.toString();
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

		private void mouseSelection(RoSb sb) {
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

		private void selectAll() {
			selectstartx = 0;
			selectstarty = 0;
			selectstopy = ptEdit.getLinesize() - 1;
			selectstopx = ptEdit.getline(selectstopy).length();

		}

	}

	class UI {
		class CacheMode {
			private Map<Integer, Image> lineCache;

			U.PriorityList lineCacheKey;

			// private int notHit;

			public CacheMode() {
				lineCache = new HashMap<Integer, Image>();
				lineCacheKey = new U.PriorityList(lineCache);
			}

			private Image createLineImage(Graphics2D g3, String s, int x,
					int y, int lineno) {
				BufferedImage im = new BufferedImage(ui.size.width
						- gutterWidth, lineHeight + lineGap,
						BufferedImage.TYPE_INT_ARGB);

				Graphics2D g2 = (Graphics2D) im.getGraphics();
				g2.setFont(g3.getFont());
				y = lineHeight;
				ui.drawLine(g2, s, x, y);
				g2.dispose();
				return im;
			}

			public void drawStringLine(Graphics2D g2, String s, int x, int y,
					int lineno) {
				Image im = lineCache.get(lineno);
				if (im == null) {
					im = createLineImage(g2, s, x, y, lineno);
					lineCache.put(lineno, im);
					// notHit = 1;
				}
				lineCacheKey.touch(lineno);
				g2.drawImage(im, x, y - lineHeight, null);

			}
		}

		class Comment {
			private void findchar(char ch, int inc, int[] c1, char chx) {
				int cx1 = c1[0];
				int cy1 = c1[1];
				RoSb csb = ptEdit.getline(cy1);
				int lv = 1;
				while (true) {
					if (inc == -1) {
						cx1--;
						if (cx1 < 0) {
							cy1--;
							if (cy1 < 0) {
								c1[0] = -1;
								c1[1] = -1;
								return;
							} else {
								csb = ptEdit.getline(cy1);
								cx1 = csb.length() - 1;
								if (cx1 < 0) {
									continue;
								}
							}
						}
						char ch2 = csb.charAt(cx1);
						if (ch2 == chx) {
							lv++;
						} else if (ch2 == ch) {
							lv--;
							if (lv == 0) {
								c1[0] = cx1;
								c1[1] = cy1;
								return;
							}
						}
					} else {
						cx1++;
						if (cx1 >= csb.length()) {
							cy1++;
							if (cy1 >= ptEdit.getLinesize()) {
								c1[0] = -1;
								c1[1] = -1;
								return;
							} else {
								csb = ptEdit.getline(cy1);
								cx1 = 0;
								if (cx1 >= csb.length()) {
									continue;
								}
							}
						}
						char ch2 = csb.charAt(cx1);
						if (ch2 == chx) {
							lv++;
						} else if (ch2 == ch) {
							lv--;
							if (lv == 0) {
								c1[0] = cx1;
								c1[1] = cy1;
								return;
							}
						}
					}
				}
			}

			protected void guessComment() {
				String[] commentchars = { "#", "%", "'", "//", "!", ";", "--",
						"/*", "<!--" };
				int[] cnts = new int[commentchars.length];
				for (int i = 0; i < ptEdit.getLinesize(); i++) {
					RoSb sb = ptEdit.getline(i);
					for (int j = 0; j < cnts.length; j++) {
						if (sb.toString().trim().startsWith(commentchars[j])) {
							cnts[j]++;
						}
					}
				}
				int kind = 0;
				int max = 0;
				for (int j = 0; j < cnts.length; j++) {
					if (cnts[j] > 0) {
						kind++;
						max = Math.max(max, cnts[j]);
					}
				}
				if (kind == 1) {
					for (int j = 0; j < cnts.length; j++) {
						if (cnts[j] > 0) {
							comment = commentchars[j];
							message("comment found:" + comment);
							break;
						}
					}
				} else {
					int k2 = 0;
					int lv2 = Math.max(5, max / 10);
					for (int j = 0; j < cnts.length; j++) {
						if (cnts[j] > lv2) {
							k2++;
						}
					}
					if (k2 == 1) {
						for (int j = 0; j < cnts.length; j++) {
							if (cnts[j] > lv2) {
								comment = commentchars[j];
								message("comment found:" + comment);
								break;
							}
						}
					}
				}
				if (comment == null) {
					message("no comment found" + Arrays.toString(cnts));
				}
				edit.repaint();
			}

			private void markBox(Graphics2D g2, int x, int y) {
				if (y >= sy && y <= sy + showLineCnt && x >= sx) {
					RoSb sb = ptEdit.getline(y);
					int w1 = x > 0 ? U.strWidth(g2, sb.substring(sx, x)) : 0;
					String c = sb.substring(x, x + 1);
					int w2 = U.strWidth(g2, c);
					g2.setColor(Color.WHITE);
					g2.drawRect(w1 - 1, (y - sy) * (lineHeight + lineGap) - 4,
							w2, 16);
					g2.setColor(color);
					g2.drawRect(w1, (y - sy) * (lineHeight + lineGap) - 3, w2,
							16);

					g2.drawString(c, w1, lineHeight + (y - sy)
							* (lineHeight + lineGap));
				}
			}

			private void markGutLine(Graphics2D g2, int y1, int y2) {
				if (y1 > y2) {
					int t = y1;
					y1 = y2;
					y2 = t;
				}
				int o1 = y1, o2 = y2;
				y1 = Math.min(Math.max(y1, sy), sy + showLineCnt);
				y2 = Math.min(Math.max(y2, sy), sy + showLineCnt);

				int scy1 = 5 + (y1 - sy) * (lineHeight + lineGap);
				int scy2 = -8 + (y2 + 1 - sy) * (lineHeight + lineGap);

				g2.setColor(Color.WHITE);
				g2.drawLine(-6, scy1 - 1, -6, scy2 - 1);
				if (o1 == y1) {
					g2.setColor(Color.WHITE);
					g2.drawLine(-6, scy1 - 1, -1, scy1 - 1);
				}
				if (o2 == y2) {
					g2.setColor(Color.WHITE);
					g2.drawLine(-6, scy2 - 1, -1, scy2 - 1);
				}
				g2.setColor(Color.BLUE);
				g2.drawLine(-5, scy1, -5, scy2);
				if (o1 == y1) {
					g2.setColor(Color.BLUE);
					g2.drawLine(-5, scy1, 0, scy1);
				}
				if (o2 == y2) {
					g2.setColor(Color.BLUE);
					g2.drawLine(-5, scy2, 0, scy2);
				}
			}

			private void pairMark(Graphics2D g2, int cx2, int cy2, char ch,
					char ch2, int inc) {
				int[] c1 = new int[] { cx2, cy2 };
				findchar(ch, inc, c1, ch2);
				if (c1[0] >= 0) {// found
					markBox(g2, cx2, cy2);
					markBox(g2, c1[0], c1[1]);
					if (cy2 != c1[1]) {
						markGutLine(g2, cy2, c1[1]);
					}
				}
			}
		}

		private Color bkColor = new Color(0xe0e0f0);
		/** 3d char */
		Color c1 = new Color(200, 80, 50);

		/** 3d char */
		Color c2 = Color.WHITE;

		private CacheMode cacheMode = null;// disabled because slower.

		private Color color = Color.BLACK;
		String comment = null;
		Comment commentor = new Comment();
		private Color currentLineColor = new Color(0xF0F0F0);
		private Font font = new Font("Monospaced", Font.PLAIN, 12);
		private int gutterWidth = 40;
		private int lineGap = 5;
		private int lineHeight = 10;
		private Dimension size;

		/**
		 * quick find how much char can be shown in width
		 * 
		 * @param width
		 * @param g2
		 * @return
		 */
		private int computeShowIndex(String s, int width, Graphics2D g2) {
			if (s.length() == 0) {
				return 0;
			}
			if (U.strWidth(g2, s) <= width) {
				return s.length();
			}
			int i = s.length() / 2;
			while (true) {
				if (i == 0) {
					return 0;
				}
				int w = U.strWidth(g2, s.substring(0, i));
				if (w <= width) {
					return i + computeShowIndex(s.substring(i), width - w, g2);
				} else {
					i = i / 2;
				}
			}
		}

		private void drawGutter(Graphics2D g2) {
			g2.setColor(new Color(0x115511));
			for (int i = 0; i < showLineCnt; i++) {
				if (sy + i + 1 > ptEdit.getLinesize()) {
					break;
				}
				g2.drawString("" + (sy + i + 1), 0, lineHeight
						+ (lineHeight + lineGap) * i);
			}
		}

		private void drawLine(Graphics2D g2, String s, int x, int y) {
			int commentPos = comment == null ? -1 : s.indexOf(comment);
			if (commentPos >= 0) {
				String s1 = s.substring(0, commentPos);
				String s2 = s.substring(commentPos);
				int w1 = drawText(g2, s1, x, y, false);
				if (w1 < size.width - gutterWidth) {
					drawText(g2, s2, x + w1, y, true);
				}
			} else {
				drawText(g2, s, x, y, false);
			}
		}

		private void drawReturn(Graphics2D g2, int w, int py) {
			g2.setColor(Color.red);
			g2.drawLine(w, py - lineHeight + font.getSize(), w + 3, py
					- lineHeight + font.getSize());

		}

		private void drawSelect(Graphics2D g2, int y1, int x1, int x2) {
			int scry = y1 - sy;
			if (scry < showLineCnt) {
				String s = ptEdit.getline(y1).toString();
				if (sx > s.length()) {
					return;
				}
				s = U.subs(s, sx, s.length());
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
				int w1 = U.strWidth(g2, s.substring(0, x1));
				int w2 = U.strWidth(g2, s.substring(0, x2));
				g2.fillRect(w1, scry * (lineHeight + lineGap), (w2 - w1),
						lineHeight + lineGap);
			}
		}

		private int drawStringLine(Graphics2D g2, String s, int x, int y,
				int lineno) {
			if (cacheMode != null) {
				cacheMode.drawStringLine(g2, s, x, y, lineno);
			} else {
				drawLine(g2, s, x, y);
			}
			return 0;
		}

		private int drawText(Graphics2D g2, String s, int x, int y,
				boolean isComment) {
			int w = 0;
			if (isComment) {
				String[] ws = s.split("\t");
				int i = 0;
				for (String s1 : ws) {
					if (i++ != 0) {
						g2.drawImage(U.TabImg, x + w, y - lineHeight, null);
						w += U.TABWIDTH;
					}
					w += drawTwoColor(g2, s1, x + w, y, c1, c2);
					if (w > size.width - gutterWidth) {
						break;
					}
				}
			} else {
				List<String> s1x = U.split(s);
				for (String s1 : s1x) {
					if (s1.equals("\t")) {
						g2.drawImage(U.TabImg, x + w, y - lineHeight, null);
						w += U.TABWIDTH;
					} else {
						// int highlightid =
						getHighLightID(s1, g2);
						g2.drawString(s1, x + w, y);
						w += g2.getFontMetrics().stringWidth(s1);
					}
					if (w > size.width - gutterWidth) {
						break;
					}
				}
			}
			return w;

		}

		private void drawTextLines(Graphics2D g2, int charCntInLine) {
			int y = sy;
			int py = lineHeight;
			int notHit = 0;
			for (int i = 0; i < showLineCnt; i++) {
				if (y >= ptEdit.getLinesize()) {
					break;
				}
				RoSb sb = ptEdit.getline(y);
				if (sx < sb.length()) {
					int chari2 = Math.min(charCntInLine + sx, sb.length());
					String s = U.subs(sb, sx, chari2);
					g2.setColor(color);
					notHit += drawStringLine(g2, s, 0, py, y);
					int w = U.strWidth(g2, s);
					drawReturn(g2, w, py);
				} else {
					drawReturn(g2, 0, py);
				}
				y += 1;
				py += lineHeight + lineGap;
			}
			if (cacheMode != null) {
				System.out.println("not hit lines=" + notHit);
			}
		}

		private void drawToolbar(Graphics2D g2) {
			String s1 = "<F1>:Help, Line:" + ptEdit.getLinesize() + ", Doc:"
					+ edit.pages.size() + ", byte:" + info.size + ", "
					+ encoding + ", X:" + (cx + 1) + ", his:" + history.size()
					+ ", " + info.fn;
			g2.setColor(Color.WHITE);
			g2.drawString(s1, 2, lineHeight + 2);
			g2.setColor(Color.BLACK);
			g2.drawString(s1, 1, lineHeight + 1);
			if (msg != null) {
				if (System.currentTimeMillis() - msgtime > MSG_VANISH_TIME) {
					msg = null;
				} else {
					int w = g2.getFontMetrics().stringWidth(msg);
					g2.setColor(new Color(0xee6666));
					g2.fillRect(size.width - w, 0, size.width, lineHeight
							+ lineGap);
					g2.setColor(Color.YELLOW);
					g2.drawString(msg, size.width - w, lineHeight);
					U.repaintAfter(MSG_VANISH_TIME, edit);
				}
			}
		}

		private int drawTwoColor(Graphics2D g2, String s, int x, int y,
				Color c1, Color c2) {
			g2.setColor(c2);
			g2.drawString(s, x + 1, y + 1);
			g2.setColor(c1);
			g2.drawString(s, x, y);
			return g2.getFontMetrics().stringWidth(s);

		}

		private int getHighLightID(String s, Graphics2D g2) {
			if (Arrays.binarySearch(U.kws, s) >= 0
					|| Arrays.binarySearch(U.kws, s.toLowerCase()) >= 0) {
				g2.setColor(Color.BLUE);
			} else if (U.isAllDigital(s)) {
				g2.setColor(Color.RED);
			} else {
				g2.setColor(color);
			}
			return 0;
		}

		public void xpaint(Graphics g, Dimension size) {
			try {
				this.size = size;

				if (!isCommentChecked) {// find comment pattern
					isCommentChecked = true;
					new Thread() {
						public void run() {
							commentor.guessComment();
						}
					}.start();
				}

				Graphics2D g2 = (Graphics2D) g;
				g2.setFont(font);

				showLineCnt = (size.height - toolbarHeight)
						/ (lineHeight + lineGap);
				int charCntInLine = (size.width - gutterWidth) / (lineHeight)
						* 2;
				// change sx if needed
				cx = Math.min(ptEdit.getline(cy).length(), cx);
				if (cx < sx) {
					sx = Math.max(0, cx - charCntInLine / 2);
				} else {
					if (U.strWidth(g2, U.subs(ptEdit.getline(cy), sx, cx)) > size.width
							- lineHeight * 3) {
						sx = Math.max(0, cx - charCntInLine / 2);
						int xx = charCntInLine / 4;
						while (xx > 0
								&& U.strWidth(g2, U.subs(ptEdit.getline(cy),
										sx, cx)) > size.width - lineHeight * 3) {
							sx = Math.max(0, cx - xx - 1);
							xx /= 2; // quick guess
						}
					}
				}

				// apply mouse click position
				if (my > 0 && my < toolbarHeight) {
					if (info.fn != null) {
						Toolkit
								.getDefaultToolkit()
								.getSystemClipboard()
								.setContents(new StringSelection(info.fn), null);
						message("filename copied");
						my = 0;
					}
				} else if (my > 0 && mx >= gutterWidth && my >= toolbarHeight) {
					mx -= gutterWidth;
					my -= toolbarHeight;
					cy = sy + my / (lineHeight + lineGap);
					if (cy >= ptEdit.getLinesize()) {
						cy = ptEdit.getLinesize() - 1;
					}
					RoSb sb = ptEdit.getline(cy);
					cx = sx + computeShowIndex(sb.substring(sx), mx, g2);
					my = 0;
					ptSelection.mouseSelection(sb);
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

				{ // highlight current line
					int l1 = cy - sy;
					if (l1 >= 0 && l1 < showLineCnt) {
						g2.setColor(currentLineColor);
						g2.fillRect(0, l1 * (lineHeight + lineGap), size.width,
								lineHeight + lineGap - 1);
					}
				}

				g2.setColor(color);

				// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				// RenderingHints.VALUE_ANTIALIAS_ON);
				drawTextLines(g2, charCntInLine);

				if (true) {// select mode
					Rectangle r = ptSelection.getSelectRect();
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
				if (true) {// (){}[]<> pair marking
					if (cx - 1 < ptEdit.getline(cy).length() && cx - 1 >= 0) {
						char c = ptEdit.getline(cy).charAt(cx - 1);
						String pair = "(){}[]<>";
						int p1 = pair.indexOf(c);
						if (p1 >= 0) {
							if (p1 % 2 == 0) {
								commentor.pairMark(g2, cx - 1, cy, pair
										.charAt(p1 + 1), c, 1);
							} else {
								commentor.pairMark(g2, cx - 1, cy, pair
										.charAt(p1 - 1), c, -1);
							}
						}
					}
				}

				// draw cursor
				if (cy >= sy && cy <= sy + showLineCnt) {
					g2.setXORMode(new Color(0x30f0f0));
					String s = U.subs(ptEdit.getline(cy), sx, cx);
					int w = U.strWidth(g2, s);
					g2.fillRect(w, (cy - sy) * (lineHeight + lineGap), 2,
							lineHeight);
				}
			} catch (Throwable th) {
				th.printStackTrace();
				message("Bug:" + th);
			}
		}

	}

	static final int MAX_SHOW_CHARS_IN_LINE = 300;
	private static final long MSG_VANISH_TIME = 3000;
	Cursor cursor = new Cursor();;
	private int cx, cy, sy, sx;
	EditWindow edit;
	String encoding;
	private FindReplaceWindow findWindow;
	History history;
	public PageInfo info;
	private boolean isCommentChecked = false;

	List<StringBuffer> lines;
	private String lineSep = "\n";

	private int mcount;
	private String msg;
	private long msgtime;
	private boolean mshift;
	private int mx, my;
	Edit ptEdit = new Edit();

	Files ptFiles = new Files();

	FindAndReplace ptFind = new FindAndReplace();

	RedoUndo ptRedoUndo = new RedoUndo();

	Selection ptSelection = new Selection();

	private int selectstartx, selectstarty, selectstopx, selectstopy;

	private int showLineCnt;

	private int toolbarHeight = 40;

	UI ui = new UI();

	public PlainPage(EditWindow editor, PageInfo pi) throws Exception {
		this.edit = editor;
		this.info = pi;
		history = new History(this);
		ptEdit.readFile(pi.fn);
		this.findWindow = new FindReplaceWindow(editor.frame, this);
	}

	private void focusCursor() {
		if (cy < sy) {
			sy = Math.max(0, cy - showLineCnt / 2 + 1);
		}
		if (showLineCnt > 0) {
			if (sy + showLineCnt - 1 < cy) {
				sy = Math.max(0, cy - showLineCnt / 2 + 1);
			}
		}

	}

	private String getInLine(int y, int x1, int x2) {
		RoSb sb = ptEdit.getline(y);
		if (x2 > sb.length()) {
			x2 = sb.length();
		}
		if (x1 > sb.length()) {
			x1 = sb.length();
		}
		return sb.substring(x1, x2);
	}

	private void gotoLine() {
		String s = JOptionPane.showInputDialog("Goto Line");
		int line = -1;
		try {
			line = Integer.parseInt(s);
		} catch (Exception e) {
			line = -1;
		}
		if (line > ptEdit.getLinesize()) {
			line = -1;
		}
		if (line > 0) {
			line -= 1;
			sy = Math.max(0, line - showLineCnt / 2 + 1);
			cy = line;
			cx = 0;
			focusCursor();
			edit.repaint();
		}
	}

	private void help() {
		// Editor editor = new Editor();
		// editor.openFile(args[0]);
		// editor.show(true);
		// editor.repaint();
		String url = "http://code.google.com/p/neoeedit/";
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				new StringSelection(url), null);
		message("visit " + url + " for more info.(url copied)");
	}

	@Override
	public void keyPressed(KeyEvent env) {
		history.beginAtom();
		try {
			// System.out.println("press " + env.getKeyChar());

			int kc = env.getKeyCode();
			if (kc == KeyEvent.VK_F1) {
				help();
			} else if (kc == KeyEvent.VK_F2) {
				ptFiles.saveAs();
			} else if (kc == KeyEvent.VK_F3) {
				ptFind.findNext();
			} else if (kc == KeyEvent.VK_F5) {
				ptEdit.reloadWithEncodingByUser();
			}
			boolean cmoved = false;
			if (env.isAltDown()) {
				if (kc == KeyEvent.VK_LEFT) {
					String s = ptEdit.getline(cy).toString();
					if (s.length() > 0
							&& (s.charAt(0) == '\t' || s.charAt(0) == ' ')) {
						ptEdit.getline(cy).sb().deleteCharAt(0);
					}
					cx -= 1;
					if (cx < 0) {
						cx = 0;
					}
					focusCursor();
					cmoved = true;
				} else if (kc == KeyEvent.VK_RIGHT) {
					ptEdit.getline(cy).sb().insert(0, '\t');
					cx += 1;
					focusCursor();
					cmoved = true;
				}
			} else if (env.isControlDown()) {
				if (kc == KeyEvent.VK_C) {
					ptSelection.copySelected();
				} else if (kc == KeyEvent.VK_V) {
					ptEdit.pasteSelected();
				} else if (kc == KeyEvent.VK_X) {
					ptSelection.cutSelected();
				} else if (kc == KeyEvent.VK_A) {
					ptSelection.selectAll();
				} else if (kc == KeyEvent.VK_D) {
					if (ptSelection.isSelected()) {
						ptEdit.deleteSelection();
					} else {
						cx = 0;
						if (ptEdit.getLinesize() == 1) {
							ptEdit.deleteInLine(0, 0, ptEdit.getline(0)
									.length());
						} else {
							ptEdit.removeLine(cy);
							if (cy >= ptEdit.getLinesize()) {
								cy = ptEdit.getLinesize() - 1;
							}
						}
					}
					focusCursor();
				} else if (kc == KeyEvent.VK_O) {
					ptFiles.openFile();
				} else if (kc == KeyEvent.VK_N) {
					edit.newFileInNewWindow();
				} else if (kc == KeyEvent.VK_S && env.isShiftDown()) {
					ptFiles.saveAllFiles();

				} else if (kc == KeyEvent.VK_S) {
					if (U.saveFile(info)) {
						System.out.println("saved");
						message("saved");
					}
				} else if (kc == KeyEvent.VK_L) {
					gotoLine();
				} else if (kc == KeyEvent.VK_Z) {
					ptRedoUndo.undo();
				} else if (kc == KeyEvent.VK_F) {
					ptFind.find();
				} else if (kc == KeyEvent.VK_TAB) {
					ptFiles.changePage();
				} else if (kc == KeyEvent.VK_Y) {
					ptRedoUndo.redo();
				} else if (kc == KeyEvent.VK_W) {
					ptFiles.closePage();
				} else if (kc == KeyEvent.VK_E) {
					ptFiles.setEncodingByUser(this);
				} else if (kc == KeyEvent.VK_PAGE_UP) {
					cy = 0;
					cx = 0;
					focusCursor();
					cmoved = true;
				} else if (kc == KeyEvent.VK_PAGE_DOWN) {
					cy = ptEdit.getLinesize() - 1;
					cx = 0;
					focusCursor();
					cmoved = true;
				} else if (kc == KeyEvent.VK_R) {
					ptEdit.removeTrailingSpace();
				} else if (kc == KeyEvent.VK_LEFT) {
					RoSb line = ptEdit.getline(cy);
					cx = Math.max(0, cx - 1);
					char ch1 = line.charAt(cx);
					while (cx > 0 && U.isSkipChar(line.charAt(cx), ch1)) {
						cx--;
					}
				} else if (kc == KeyEvent.VK_RIGHT) {
					RoSb line = ptEdit.getline(cy);
					cx = Math.min(line.length(), cx + 1);
					if (cx < line.length()) {
						char ch1 = line.charAt(cx);
						while (cx < line.length()
								&& U.isSkipChar(line.charAt(cx), ch1)) {
							cx++;
						}
					}
				} else if (kc == KeyEvent.VK_UP) {
					sy = Math.max(0, sy - 1);
				} else if (kc == KeyEvent.VK_DOWN) {
					sy = Math.min(sy + 1, ptEdit.getLinesize() - 1);
				}
			} else {
				if (kc == KeyEvent.VK_LEFT) {
					cx -= 1;
					if (cx < 0) {
						if (cy > 0) {
							cy -= 1;
							cx = ptEdit.getline(cy).length();
						} else {
							cx = 0;
						}
					}
					focusCursor();
					cmoved = true;
				} else if (kc == KeyEvent.VK_RIGHT) {
					cx += 1;
					if (cx > ptEdit.getline(cy).length()
							&& cy < ptEdit.getLinesize() - 1) {
						cy += 1;
						cx = 0;
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
					if (cy >= ptEdit.getLinesize()) {
						cy = ptEdit.getLinesize() - 1;
					}
					focusCursor();
					cmoved = true;
				} else if (kc == KeyEvent.VK_HOME) {
					String line = ptEdit.getline(cy).toString();
					String lx = line.trim();
					int p1 = line.indexOf(lx);
					if (cx > p1 || cx == 0) {
						cx = p1;
					} else {
						cx = 0;
					}
					focusCursor();
					cmoved = true;
				} else if (kc == KeyEvent.VK_END) {
					String line = ptEdit.getline(cy).toString();
					String lx = line.trim();
					int p1 = line.lastIndexOf(lx) + lx.length();
					if (cx < p1 || cx >= line.length()) {
						cx = p1;
					} else {
						cx = Integer.MAX_VALUE;
					}
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
					if (cy >= ptEdit.getLinesize()) {
						cy = ptEdit.getLinesize() - 1;
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
					ptSelection.cancelSelect();
				}
			}
			edit.repaint();
		} catch (Exception e) {
			message("err:" + e);
			e.printStackTrace();
		}
		history.endAtom();
	}

	@Override
	public void keyReleased(KeyEvent env) {

	}

	@Override
	public void keyTyped(KeyEvent env) {
		history.beginAtom();
		char kc = env.getKeyChar();
		// System.out.println("type " + kc);
		if (kc == KeyEvent.VK_TAB && env.isShiftDown()) {
			for (int i = selectstarty; i <= selectstopy; i++) {
				if (ptEdit.getline(i).length() > 0) {
					char ch = ptEdit.getline(i).charAt(0);
					if (ch == ' ' || ch == '\t') {
						ptEdit.getline(i).sb().delete(0, 1);
					}
				}
			}
			focusCursor();
		} else if (kc == KeyEvent.VK_TAB && !env.isShiftDown()
				&& selectstarty < selectstopy) {

			if (selectstarty < selectstopy) {
				for (int i = selectstarty; i <= selectstopy; i++) {
					ptEdit.getline(i).sb().insert(0, "\t");
				}
				focusCursor();
			}
		} else if (env.isControlDown() || env.isAltDown()) {
			// ignore
		} else {
			ptEdit.insert(kc);
		}
		history.endAtom();
	}

	public void message(String s) {
		msg = s;
		msgtime = System.currentTimeMillis();
	}

	@Override
	public void mouseDragged(MouseEvent env) {
		mx = env.getX();
		my = env.getY();
		mshift = true;
		edit.repaint();
	}

	@Override
	public void mousePressed(MouseEvent env) {
		mx = env.getX();
		my = env.getY();
		mshift = env.isShiftDown();
		mcount = env.getClickCount();
		edit.repaint();
		// System.out.println("m press");
	}

	@Override
	public void scroll(int amount) {
		sy += amount;
		if (sy >= ptEdit.getLinesize()) {
			sy = ptEdit.getLinesize() - 1;
		}
		if (sy < 0) {
			sy = 0;
		}
		edit.repaint();
	}

	@Override
	public void xpaint(Graphics g, Dimension size) {
		ui.xpaint(g, size);
	}
}
