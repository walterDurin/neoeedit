package neoe.ne;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JOptionPane;

import neoe.ne.U.RoSb;

public class PlainPage {
	private static final String REV = "$Rev$";
	static final String WINDOW_NAME = "neoeedit r"
			+ REV.substring(6, REV.length() - 2);

	static enum BasicAction {
		Delete, DeleteEmtpyLine, Insert, InsertEmptyLine, MergeLine
	}

	static class BasicEdit {
		boolean record;
		private PlainPage page;

		BasicEdit(boolean record, PlainPage page) {
			this.record = record;
			this.page = page;
		}

		void deleteEmptyLine(int y) {
			StringBuffer sb = lines().get(y);
			if (sb.length() > 0) {
				throw new RuntimeException("not a empty line " + y + ":" + sb);
			}
			if (lines().size() > 1) {
				lines().remove(y);
				if (record) {
					history().addOne(
							new HistoryCell(BasicAction.DeleteEmtpyLine, -1,
									-1, y, -1, null));
				}
			}
		}

		void deleteInLine(int y, int x1, int x2) {
			StringBuffer sb = lines().get(y);
			x2 = Math.min(x2, sb.length());
			String d = sb.substring(x1, x2);
			if (d.length() > 0) {
				sb.delete(x1, x2);
				if (record) {
					history().addOne(
							new HistoryCell(BasicAction.Delete, x1, x2, y, -1,
									d));
				}
			}
		}

		void insertEmptyLine(int y) {
			lines().add(y, new StringBuffer());
			if (record) {
				history().addOne(
						new HistoryCell(BasicAction.InsertEmptyLine, -1, -1, y,
								-1, null));
			}
		}

		void insertInLine(int y, int x, String s) {
			if (s.indexOf("\n") >= 0 || s.indexOf("\r") >= 0) {
				throw new RuntimeException("cannot contains line-seperator:["
						+ s + "]" + s.indexOf("\n"));
			}
			if (y == page.roLines.getLinesize()) {
				page.editRec.insertEmptyLine(y);
			}
			StringBuffer sb = lines().get(y);
			if (x > sb.length()) {
				sb.setLength(x);
			}
			sb.insert(x, s);
			if (record) {
				history().addOne(
						new HistoryCell(BasicAction.Insert, x, x + s.length(),
								y, -1, null));
			}
		}

		void mergeLine(int y) {
			StringBuffer sb1 = lines().get(y);
			StringBuffer sb2 = lines().get(y + 1);
			int x1 = sb1.length();
			sb1.append(sb2);
			lines().remove(y + 1);
			if (record) {
				history().addOne(
						new HistoryCell(BasicAction.MergeLine, x1, -1, y, -1,
								null));
			}
		}

		private History history() {
			return page.history;
		}

		private List<StringBuffer> lines() {
			return page.lines;
		}
	}

	class Cursor {
		public void setSafePos(int x, int y) {
			cy = Math.max(0, Math.min(roLines.getLinesize() - 1, y));
			cx = Math.max(0, Math.min(roLines.getline(cy).length(), x));
		}

		public void moveDown() {
			cy += 1;
			if (cy >= roLines.getLinesize()) {
				if (rectSelectMode) {
					editRec.insertEmptyLine(cy);
					if (cx > 0) {
						editRec.insertInLine(cy, 0, U.spaces(cx));
					}
				} else {
					cy = roLines.getLinesize() - 1;
				}
			}
		}

		public void moveEnd() {
			String line = roLines.getline(cy).toString();
			String lx = line.trim();
			int p1 = line.lastIndexOf(lx) + lx.length();
			if (cx < p1 || cx >= line.length()) {
				cx = p1;
			} else {
				cx = Integer.MAX_VALUE;
			}
		}

		public void moveHome() {
			String line = roLines.getline(cy).toString();
			String lx = line.trim();
			int p1 = line.indexOf(lx);
			if (cx > p1 || cx == 0) {
				cx = p1;
			} else {
				cx = 0;
			}
		}

		public void moveLeft() {
			cx -= 1;
			if (cx < 0) {
				if (cy > 0) {
					cy -= 1;
					cx = roLines.getline(cy).length();
				} else {
					cx = 0;
				}
			}
		}

		public void moveLeftWord() {
			RoSb line = roLines.getline(cy);
			cx = Math.max(0, cx - 1);
			char ch1 = line.charAt(cx);
			while (cx > 0 && U.isSkipChar(line.charAt(cx), ch1)) {
				cx--;
			}
		}

		public void movePageDown() {
			cy += showLineCnt;
			if (cy >= roLines.getLinesize()) {
				if (rectSelectMode) {
					String SP = U.spaces(cx);
					int cnt = cy - roLines.getLinesize() + 1;
					int p = roLines.getLinesize();
					for (int i = 0; i < cnt; i++) {
						editRec.insertEmptyLine(p);
						if (cx > 0) {
							editRec.insertInLine(p, 0, SP);
						}
					}
				} else {
					cy = roLines.getLinesize() - 1;
				}
			}
		}

		public void movePageUp() {
			cy -= showLineCnt;
			if (cy < 0) {
				cy = 0;
			}
		}

		public void moveRight() {
			cx += 1;
			if (cx > roLines.getline(cy).length()
					&& cy < roLines.getLinesize() - 1) {
				cy += 1;
				cx = 0;
			}
		}

		public void moveRightWord() {
			RoSb line = roLines.getline(cy);
			cx = Math.min(line.length(), cx + 1);
			if (cx < line.length()) {
				char ch1 = line.charAt(cx);
				while (cx < line.length() && U.isSkipChar(line.charAt(cx), ch1)) {
					cx++;
				}
			}
		}

		public void moveUp() {
			cy -= 1;
			if (cy < 0) {
				cy = 0;
			}
		}
	}

	class EasyEdit {
		public void deleteLine(int cy) {
			cx = 0;
			int len = roLines.getline(cy).length();
			if (len > 0) {
				editRec.deleteInLine(cy, 0, len);
			}
			editRec.deleteEmptyLine(cy);
		}

		public void deleteRect(Rectangle r) {
			int x1 = r.x;
			int y1 = r.y;
			int x2 = r.width;
			int y2 = r.height;
			if (rectSelectMode) {
				for (int i = y1; i <= y2; i++) {
					editRec.deleteInLine(i, x1, x2);
				}
				selectstartx = x1;
				selectstopx = x1;
			} else {
				if (y1 == y2 && x1 < x2) {
					editRec.deleteInLine(y1, x1, x2);
				} else if (y1 < y2) {
					editRec.deleteInLine(y1, x1, Integer.MAX_VALUE);
					editRec.deleteInLine(y2, 0, x2);
					for (int i = y1 + 1; i < y2; i++) {
						deleteLine(y1 + 1);
					}
					editRec.mergeLine(y1);
				}
			}
			cx = x1;
			cy = y1;
			if (y2 - y1 > 400) {
				U.gc();
			}
			focusCursor();
		}

		void insert(char ch) {
			if (ch == KeyEvent.VK_ENTER) {
				if (ptSelection.isSelected()) {
					ptEdit.deleteRect(ptSelection.getSelectRect());
				}
				RoSb sb = roLines.getline(cy);
				String indent = U.getIndent(sb.toString());
				String s = sb.substring(cx, sb.length());
				editRec.insertEmptyLine(cy + 1);
				editRec.insertInLine(cy + 1, 0, indent + s);
				editRec.deleteInLine(cy, cx, Integer.MAX_VALUE);
				cy += 1;
				cx = indent.length();
			} else if (ch == KeyEvent.VK_BACK_SPACE) {
				if (ptSelection.isSelected()) {
					ptEdit.deleteRect(ptSelection.getSelectRect());
				} else {
					if (rectSelectMode) {
						if (cx > 0) {
							Rectangle r = ptSelection.getSelectRect();
							for (int i = r.y; i <= r.height; i++) {
								editRec.deleteInLine(i, cx - 1, cx);
							}
							cx--;
							selectstartx = cx;
							selectstopx = cx;
						}
					} else {
						if (cx > 0) {
							editRec.deleteInLine(cy, cx - 1, cx);
							cx -= 1;
						} else {
							if (cy > 0) {
								cx = roLines.getline(cy - 1).length();
								editRec.mergeLine(cy - 1);
								cy -= 1;
							}
						}
					}
				}
			} else if (ch == KeyEvent.VK_DELETE) {
				if (ptSelection.isSelected()) {
					ptEdit.deleteRect(ptSelection.getSelectRect());
				} else {
					if (rectSelectMode) {
						Rectangle r = ptSelection.getSelectRect();
						for (int i = r.y; i <= r.height; i++) {
							editRec.deleteInLine(i, cx, cx + 1);
						}
						selectstartx = cx;
						selectstopx = cx;
					} else {
						if (cx < roLines.getline(cy).length()) {
							editRec.deleteInLine(cy, cx, cx + 1);
						} else {
							if (cy < roLines.getLinesize() - 1) {
								editRec.mergeLine(cy);
							}
						}
					}
				}
			} else if (ch == KeyEvent.VK_ESCAPE) {
				ptSelection.cancelSelect();
			} else {
				if (ptSelection.isSelected()) {
					ptEdit.deleteRect(ptSelection.getSelectRect());
				}
				if (rectSelectMode) {
					Rectangle r = ptSelection.getSelectRect();
					for (int i = r.y; i <= r.height; i++) {
						editRec.insertInLine(i, cx, "" + ch);
					}
					cx += 1;
					selectstartx = cx;
					selectstopx = cx;
				} else {
					editRec.insertInLine(cy, cx, "" + ch);
					cx += 1;
				}
			}
			focusCursor();
			if (!rectSelectMode) {
				ptSelection.cancelSelect();
			}
			editor.repaint();
		}

		void insertString(String s) {
			String[] ss = U.split(s, "\n");

			if (rectSelectMode) {
				int iy = cy;
				for (String s1 : ss) {
					editRec.insertInLine(iy, cx, s1);
					iy++;
				}
			} else {
				if (ss.length == 1) {
					editRec.insertInLine(cy, cx, ss[0]);
					cx += ss[0].length();
				} else {
					String rem = roLines.getInLine(cy, cx, Integer.MAX_VALUE);
					editRec.deleteInLine(cy, cx, Integer.MAX_VALUE);
					editRec.insertInLine(cy, cx, ss[0]);
					for (int i = 1; i < ss.length; i++) {
						editRec.insertEmptyLine(cy + i);
						editRec.insertInLine(cy + i, 0, ss[i]);
					}
					cy += ss.length - 1;
					cx = ss[ss.length - 1].length();
					editRec.insertInLine(cy, cx, rem);
				}
			}
			focusCursor();
		}

		public void moveLineLeft(int cy) {
			String s = roLines.getline(cy).toString();
			if (s.length() > 0 && (s.charAt(0) == '\t' || s.charAt(0) == ' ')) {
				editRec.deleteInLine(cy, 0, 1);
			}
			cx -= 1;
			if (cx < 0) {
				cx = 0;
			}
		}

		public void moveLineRight(int cy) {
			editRec.insertInLine(cy, 0, "\t");
			cx += 1;
		}

		public void moveRectLeft(int from, int to) {
			for (int i = from; i <= to; i++) {
				ptEdit.moveLineLeft(i);
			}
		}

		public void moveRectRight(int from, int to) {
			for (int i = from; i <= to; i++) {
				ptEdit.moveLineRight(i);
			}
		}

		void reloadWithEncodingByUser() {
			if (fn == null) {
				message("file not saved.");
				return;
			}
			U.setEncodingByUser(PlainPage.this);
			U.readFile(PlainPage.this, fn);
		}

		void setLines(List<StringBuffer> newLines) {
			lines = newLines;
			history.clear();
		}
	}

	class FindAndReplace {
		void doFind(String text, boolean ignoreCase, boolean selected2,
				boolean inDir, String dir) throws Exception {
			if (!inDir) {
				PlainPage.this.text2find = text;
				PlainPage.this.ignoreCase = ignoreCase;
				findNext();
				editor.repaint();
			} else {
				U.doFindInDir(editor, text, ignoreCase, selected2, inDir, dir);
			}
		}

		void findNext() {
			if (text2find != null && text2find.length() > 0) {
				Point p = U.find(PlainPage.this, text2find, cx + 1, cy,
						ignoreCase);
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

		void showFindDialog() {
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
	}

	static class History {
		public static int MAXSIZE = 200;
		private List<HistoryCell> atom;
		private LinkedList<List<HistoryCell>> data;
		private int p;
		private PlainPage page;

		void redo() throws Exception {
			List<HistoryCell> os = getRedo();
			if (os == null) {
				return;
			}
			for (HistoryCell o : os) {
				o.redo();
			}
		}

		void undo() throws Exception {
			List<HistoryCell> os = get();
			if (os == null) {
				return;
			}
			for (int i = os.size() - 1; i >= 0; i--) {
				HistoryCell o = os.get(i);
				o.undo();
			}
		}

		public History(PlainPage page) {
			data = new LinkedList<List<HistoryCell>>();
			p = 0;
			atom = new ArrayList<HistoryCell>();
			this.page = page;
		}

		private void add(List<HistoryCell> o) {
			if (p < data.size() && p >= 0) {
				for (int i = 0; i < data.size() - p; i++) {
					data.removeLast();
				}
			}
			List<HistoryCell> last = data.peekLast();
			// stem.out.println("last=" + last);
			if (!append(last, o)) {
				// System.out.println("add:" + o);
				data.add(o);
				if (data.size() > MAXSIZE) {
					data.removeFirst();
				} else {
					p += 1;
				}
			} else {
				// System.out.println("append:" + o);
			}
		}

		public void addOne(HistoryCell historyInfo) {
			historyInfo.page = this.page;
			atom.add(historyInfo);
		}

		/**
		 * try to append this change to the last ones
		 */
		private boolean append(List<HistoryCell> lasts, List<HistoryCell> os) {
			if (lasts == null) {
				return false;
			}
			boolean ret = false;
			if (os.size() == 1) {
				HistoryCell o = os.get(0);
				HistoryCell last = lasts.get(lasts.size() - 1);
				if (o.canAppend(last)) {
					lasts.add(o);
					ret = true;
				}
			}
			return ret;
		}

		public void beginAtom() {
			if (atom.size() > 0) {
				endAtom();
			}
		}

		public void clear() {
			atom.clear();
			data.clear();
			p = 0;
		}

		public void endAtom() {
			if (atom.size() > 0) {
				// System.out.println("end atom");
				add(atom);
				atom = new ArrayList<HistoryCell>();
			}
		}

		public List<HistoryCell> get() {
			if (p <= 0) {
				return null;
			}
			p -= 1;
			// System.out.println("undo:" + data.get(p));
			return data.get(p);
		}

		public List<HistoryCell> getRedo() {
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

	static class HistoryCell {
		BasicAction action;
		String s1;
		int x1, x2, y1, y2;
		PlainPage page;

		public HistoryCell(BasicAction action, int x1, int x2, int y1, int y2,
				String s1) {
			super();
			this.s1 = s1;
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			this.action = action;
		}

		public boolean canAppend(HistoryCell last) {
			return ((last.action == BasicAction.Delete
					&& this.action == BasicAction.Delete && // 
			((last.x1 == this.x1 || last.x1 == this.x2) && last.y1 == this.y1))// 
			|| (last.action == BasicAction.Insert
					&& this.action == BasicAction.Insert && // 
			((last.x1 == this.x1 || last.x2 == this.x1) && last.y1 == this.y1)));
		}

		public void redo() {
			// System.out.println("redo:" + toString());
			switch (action) {
			case Delete:
				s1 = roLines().getInLine(y1, x1, x2);
				editNoRec().deleteInLine(y1, x1, x2);
				page.cursor.setSafePos(x1, y1);
				break;
			case DeleteEmtpyLine:
				editNoRec().deleteEmptyLine(y1);
				page.cursor.setSafePos(0, y1);
				break;
			case Insert:
				editNoRec().insertInLine(y1, x1, s1);
				page.cursor.setSafePos(x1 + s1.length(), y1);
				s1 = null;
				break;
			case InsertEmptyLine:
				editNoRec().insertEmptyLine(y1);
				page.cursor.setSafePos(0, y1 + 1);
				break;
			case MergeLine:
				editNoRec().mergeLine(y1);
				page.cursor.setSafePos(x1, y1);
				break;
			default:
				throw new RuntimeException("unkown action " + action);
			}
		}

		private BasicEdit editNoRec() {
			return page.editNoRec;
		}

		private ReadonlyLines roLines() {
			return page.roLines;
		}

		@Override
		public String toString() {
			return "HistoryInfo [action=" + action + ", x1=" + x1 + ", x2="
					+ x2 + ", y1=" + y1 + ", y2=" + y2 + ", s1=" + s1 + "]\n";
		}

		public void undo() {
			// System.out.println("undo:" + toString());
			switch (action) {
			case Delete:
				editNoRec().insertInLine(y1, x1, s1);
				page.cursor.setSafePos(x1 + s1.length(), y1);
				s1 = null;
				break;
			case DeleteEmtpyLine:
				editNoRec().insertEmptyLine(y1);
				page.cursor.setSafePos(0, y1 + 1);
				break;
			case Insert:
				s1 = roLines().getInLine(y1, x1, x2);
				editNoRec().deleteInLine(y1, x1, x2);
				page.cursor.setSafePos(0, y1);
				break;
			case InsertEmptyLine:
				editNoRec().deleteEmptyLine(y1);
				page.cursor.setSafePos(0, y1);
				break;
			case MergeLine:
				String s2 = roLines().getInLine(y1, x1, Integer.MAX_VALUE);
				editNoRec().deleteInLine(y1, x1, Integer.MAX_VALUE);
				editNoRec().insertEmptyLine(y1 + 1);
				editNoRec().insertInLine(y1 + 1, 0, s2);
				page.cursor.setSafePos(0, y1 + 1);
				break;
			default:
				throw new RuntimeException("unkown action " + action);
			}
		}
	}

	static class PageTabs {
		private PlainPage page;

		PageTabs(PlainPage page) {
			this.page = page;
		}

		private void changePage() {
			EditWindow editor = page.editor;
			Object[] possibilities = editor.pages.toArray();
			PlainPage p = (PlainPage) JOptionPane.showInputDialog(editor,
					"Select Document:", "Select Document",
					JOptionPane.QUESTION_MESSAGE, null, possibilities, null);
			if (p != null) {
				int i = editor.pages.indexOf(p);
				if (i >= 0) {
					editor.changePage(i);
				}
			}
		}

		private void closePage() throws Exception {
		
			EditWindow editor = page.editor;
			if (page.history.size() != 0) {
				if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
						editor, "Are you sure to SAVE and close?", "Changes made",
						JOptionPane.YES_NO_OPTION)) {
					return;
				}
			}			
			if (page.fn != null) {
				U.saveFile(page);
				U.saveFileHistory(page.fn, page.cy);
			}
			editor.pages.remove(editor.pageNo);
			if (editor.pageNo >= editor.pages.size()) {
				editor.pageNo = editor.pages.size() - 1;
			}
			page.closed=true;
			if (editor.pages.size() == 0) {
				// window closing event not fire	
				editor.frame.dispose();
				return;
			}
			editor.changePage(editor.pageNo);
		}

		private void saveAllFiles() throws Exception {
			EditWindow editor = page.editor;
			int total = 0;
			for (PlainPage pi : editor.pages) {
				if (U.saveFile(pi)) {
					total++;
				}
			}
			System.out.println(total + " files saved");
			page.message(total + " files saved");
		}
	}

	static class ReadonlyLines {
		private PlainPage page;

		ReadonlyLines(PlainPage page) {
			this.page = page;
		}

		String getInLine(int y, int x1, int x2) {
			RoSb sb = getline(y);
			if (x2 > sb.length()) {
				x2 = sb.length();
			}
			if (x1 > sb.length()) {
				x1 = sb.length();
			}
			return sb.substring(x1, x2);
		}

		RoSb getline(int i) {
			return new RoSb(page.lines.get(i));
		}

		int getLinesize() {
			return page.lines.size();
		}

		String getTextInRect(Rectangle r) {
			int x1 = r.x;
			int y1 = r.y;
			int x2 = r.width;
			int y2 = r.height;
			StringBuffer sb = new StringBuffer();
			if (page.rectSelectMode) {
				for (int i = y1; i <= y2; i++) {
					if (i != y1) {
						sb.append(page.lineSep);
					}
					sb.append(getInLine(i, x1, x2));
				}
			} else {
				if (y1 == y2 && x1 < x2) {
					sb.append(getInLine(y1, x1, x2));
				} else if (y1 < y2) {
					sb.append(getInLine(y1, x1, Integer.MAX_VALUE));
					for (int i = y1 + 1; i < y2; i++) {
						sb.append(page.lineSep);
						sb.append(getline(i));
					}
					sb.append(page.lineSep);
					sb.append(getInLine(y2, 0, x2));
				}
			}
			return sb.toString();
		}
	}

	class Selection {
		void cancelSelect() {
			selectstartx = cx;
			selectstarty = cy;
			selectstopx = cx;
			selectstopy = cy;
		}

		void copySelected() {
			String s = getSelected();
			U.setClipBoard(s);
			message("copied " + s.length());
		}

		void cutSelected() {
			copySelected();
			ptEdit.deleteRect(getSelectRect());
			cancelSelect();
		}

		String getSelected() {
			return roLines.getTextInRect(getSelectRect());
		}

		Rectangle getSelectRect() {
			int x1, x2, y1, y2;
			if (rectSelectMode) {
				y1 = selectstopy;
				y2 = selectstarty;
				x1 = selectstopx;
				x2 = selectstartx;
				if (y1 > y2) {
					int t = y1;
					y1 = y2;
					y2 = t;
				}
				if (x1 > x2) {
					int t = x1;
					x1 = x2;
					x2 = t;
				}
			} else {
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
			}
			return new Rectangle(x1, y1, x2, y2);
		}

		boolean isSelected() {
			Rectangle r = getSelectRect();
			int x1 = r.x;
			int y1 = r.y;
			int x2 = r.width;
			int y2 = r.height;
			if (rectSelectMode) {
				return x1 < x2;
			} else {
				if (y1 == y2 && x1 < x2) {
					return true;
				} else if (y1 < y2) {
					return true;
				}
				return false;
			}
		}

		void mouseSelection(RoSb sb) {
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

		void selectAll() {
			selectstartx = 0;
			selectstarty = 0;
			selectstopy = roLines.getLinesize() - 1;
			selectstopx = roLines.getline(selectstopy).length();
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

			Image createLineImage(Graphics2D g3, String s, int x, int y,
					int lineno) {
				BufferedImage im = new BufferedImage(
						ui.dim.width - gutterWidth, lineHeight + lineGap,
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2 = (Graphics2D) im.getGraphics();
				g2.setFont(g3.getFont());
				y = lineHeight;
				ui.drawStringLine(g2, s, x, y);
				g2.dispose();
				return im;
			}

			void drawStringLine(Graphics2D g2, String s, int x, int y,
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
			private void markBox(Graphics2D g2, int x, int y) {
				if (y >= sy && y <= sy + showLineCnt && x >= sx) {
					RoSb sb = roLines.getline(y);
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
				U.findchar(PlainPage.this, ch, inc, c1, ch2);
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
		private Dimension dim;
		private Font font = new Font("Monospaced", Font.PLAIN, 12);
		private int gutterWidth = 40;
		private int lineGap = 5;
		private int lineHeight = 10;
		float scalev = 1;

		private void drawGutter(Graphics2D g2) {
			g2.setColor(new Color(0x115511));
			for (int i = 0; i < showLineCnt; i++) {
				if (sy + i + 1 > roLines.getLinesize()) {
					break;
				}
				g2.drawString("" + (sy + i + 1), 0, lineHeight
						+ (lineHeight + lineGap) * i);
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
				String s = roLines.getline(y1).toString();
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
				if (x1 == x2) {
					int w1 = U.strWidth(g2, s.substring(0, x1));
					g2.fillRect(w1, scry * (lineHeight + lineGap), 3,
							lineHeight + lineGap);
				} else {
					int w1 = U.strWidth(g2, s.substring(0, x1));
					int w2 = U.strWidth(g2, s.substring(0, x2));
					g2.fillRect(w1, scry * (lineHeight + lineGap), (w2 - w1),
							lineHeight + lineGap);
				}
			}
		}

		private void drawStringLine(Graphics2D g2, String s, int x, int y) {
			int commentPos = comment == null ? -1 : s.indexOf(comment);
			if (commentPos >= 0) {
				String s1 = s.substring(0, commentPos);
				String s2 = s.substring(commentPos);
				int w1 = drawText(g2, s1, x, y, false);
				if (w1 < dim.width - gutterWidth) {
					drawText(g2, s2, x + w1, y, true);
				}
			} else {
				drawText(g2, s, x, y, false);
			}
		}

		private int drawStringLine(Graphics2D g2, String s, int x, int y,
				int lineno) {
			if (cacheMode != null) {
				cacheMode.drawStringLine(g2, s, x, y, lineno);
			} else {
				drawStringLine(g2, s, x, y);
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
					w += U.drawTwoColor(g2, s1, x + w, y, c1, c2);
					if (w > dim.width - gutterWidth) {
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
					if (w > dim.width - gutterWidth) {
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
				if (y >= roLines.getLinesize()) {
					break;
				}
				RoSb sb = roLines.getline(y);
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
			String s1 = "<F1>:Help, Line:" + roLines.getLinesize() + ", Doc:"
					+ editor.pages.size() + ", byte:" + PlainPage.this.size
					+ ", " + encoding +(lineSep.equals("\n")?", U":", W")+ ", X:" + (cx + 1) + ", his:"
					+ history.size() + ", " + (rectSelectMode ? "R, " : "")
					+ fn;
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
					g2.fillRect(dim.width - w, 0, dim.width, lineHeight
							+ lineGap);
					g2.setColor(Color.YELLOW);
					g2.drawString(msg, dim.width - w, lineHeight);
					U.repaintAfter(MSG_VANISH_TIME, editor);
				}
			}
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
				this.dim = size;
				if (!isCommentChecked) {// find comment pattern
					isCommentChecked = true;
					new Thread() {
						public void run() {
							U.guessComment(PlainPage.this);
						}
					}.start();
				}
				Graphics2D g2 = (Graphics2D) g;
				g2.setFont(font);
				showLineCnt = (int) ((size.height - toolbarHeight)
						/ (lineHeight + lineGap) / scalev);
				int charCntInLine = (int) ((size.width - gutterWidth)
						/ (lineHeight) * 2 / scalev);
				// change sx if needed
				cx = Math.min(roLines.getline(cy).length(), cx);
				if (cx < sx) {
					sx = Math.max(0, cx - charCntInLine / 2);
				} else {
					if (U.strWidth(g2, U.subs(roLines.getline(cy), sx, cx)) > size.width
							- lineHeight * 3) {
						sx = Math.max(0, cx - charCntInLine / 2);
						int xx = charCntInLine / 4;
						while (xx > 0
								&& U.strWidth(g2, U.subs(roLines.getline(cy),
										sx, cx)) > size.width - lineHeight * 3) {
							sx = Math.max(0, cx - xx - 1);
							xx /= 2; // quick guess
						}
					}
				}
				// apply mouse click position
				if (my > 0 && my < toolbarHeight) {
					if (fn != null) {
						U.setClipBoard(fn);
						message("filename copied");
						my = 0;
					}
				} else if (my > 0 && mx >= gutterWidth && my >= toolbarHeight) {
					mx -= gutterWidth;
					my -= toolbarHeight;
					mx = (int) (mx / scalev);
					my = (int) (my / scalev);
					cy = sy + my / (lineHeight + lineGap);// (int)((sy + my /
					// (lineHeight +
					// lineGap))/scalev);
					if (cy >= roLines.getLinesize()) {
						cy = roLines.getLinesize() - 1;
					}
					RoSb sb = roLines.getline(cy);
					sx = Math.min(sx, sb.length());
					cx = sx + U.computeShowIndex(sb.substring(sx), mx, g2);
					my = 0;
					ptSelection.mouseSelection(sb);
				}
				g2.setColor(bkColor);
				g2.fillRect(0, 0, size.width, size.height);
				if (noise) {
					paintNoise(g2);
				}
				// draw toolbar
				drawToolbar(g2);
				// draw gutter
				g2.translate(0, toolbarHeight);
				g2.setColor(Color.WHITE);
				g2.drawRect(gutterWidth, -1, dim.width - gutterWidth,
						dim.height - toolbarHeight);

				g2.scale(scalev, scalev);
				drawGutter(g2);
				// draw text
				g2.translate(gutterWidth / scalev, 0);

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
				if (rectSelectMode) {
					Rectangle r = ptSelection.getSelectRect();
					int x1 = r.x;
					int y1 = r.y;
					int x2 = r.width;
					int y2 = r.height;
					for (int i = y1; i <= y2; i++) {
						g2.setColor(Color.BLUE);
						g2.setXORMode(new Color(0xf0f030));
						drawSelect(g2, i, x1, x2);
					}
				} else {// select mode
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
					if (cx - 1 < roLines.getline(cy).length() && cx - 1 >= 0) {
						char c = roLines.getline(cy).charAt(cx - 1);
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
					String s = U.subs(roLines.getline(cy), sx, cx);
					int w = U.strWidth(g2, s);
					g2.fillRect(w, (cy - sy) * (lineHeight + lineGap), 2,
							lineHeight);
				}

			} catch (Throwable th) {
				th.printStackTrace();
				message("Bug:" + th);
			}
		}

		void paintNoise(Graphics2D g2) {
			int cnt = 1000;
			int w = dim.width;
			int h = dim.height;
			int cs = 0xffffff;
			for (int i = 0; i < cnt; i++) {
				int x = random.nextInt(w);
				int y = random.nextInt(h);
				g2.setColor(new Color(random.nextInt(cs)));
				g2.drawLine(x, y, x + 1, y);
			}
		}
	}

	static Random random = new Random();
	static final int MAX_SHOW_CHARS_IN_LINE = 300;
	private static final long MSG_VANISH_TIME = 3000;
	Cursor cursor = new Cursor();
	int cx;
	int cy;
	BasicEdit editNoRec = new BasicEdit(false, this);
	EditWindow editor;
	BasicEdit editRec = new BasicEdit(true, this);
	String encoding;
	private FindReplaceWindow findWindow;
	public String fn;
	History history;
	boolean ignoreCase = true;
	private boolean isCommentChecked = false;
	private List<StringBuffer> lines;
	public String lineSep = "\n";
	private int mcount;
	private String msg;
	private long msgtime;
	private boolean mshift;
	private int mx, my;
	EasyEdit ptEdit = new EasyEdit();
	FindAndReplace ptFind = new FindAndReplace();
	PageTabs ptPages = new PageTabs(this);
	Selection ptSelection = new Selection();
	private boolean rectSelectMode = false;
	ReadonlyLines roLines = new ReadonlyLines(this);
	int selectstartx, selectstarty, selectstopx, selectstopy;
	private int showLineCnt;
	public long size;
	private int sy, sx;
	int noisesleep = 500;
	String text2find;
	private int toolbarHeight = 40;
	UI ui = new UI();
	public String workPath;
	boolean noise = false;
	boolean closed = false;

	public PlainPage(EditWindow editor, String fn) throws Exception {
		this.editor = editor;
		editor.frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				closed = true;
			}
		});
		this.fn = fn;
		if (fn != null) {
			File f = new File(fn);
			this.workPath = f.getParent();
			this.size = f.length();
		}
		history = new History(this);
		U.readFile(this, fn);
		findWindow = new FindReplaceWindow(editor.frame, this);
		
	}
	private void startNoiseThread(){
		new Thread() {
			public void run() {
				try {// noise thread
					while (true) {
						if (noise&&!closed) {
							PlainPage.this.editor.repaint();
							//System.out.println("paint noise");
							Thread.sleep(noisesleep);
						}else{
							break;
						}						
					}
					System.out.println("noise stopped");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	void focusCursor() {
		if (cy < sy) {
			sy = Math.max(0, cy - showLineCnt / 2 + 1);
		}
		if (showLineCnt > 0) {
			if (sy + showLineCnt - 1 < cy) {
				sy = Math.max(0, cy - showLineCnt / 2 + 1);
			}
		}
	}

	void gotoLine() {
		String s = JOptionPane.showInputDialog("Goto Line");
		int line = -1;
		try {
			line = Integer.parseInt(s);
		} catch (Exception e) {
			line = -1;
		}
		if (line > roLines.getLinesize()) {
			line = -1;
		}
		if (line > 0) {
			line -= 1;
			sy = Math.max(0, line - showLineCnt / 2 + 1);
			cy = line;
			cx = 0;
			focusCursor();
		}
	}

	private void help() {
		String url = "http://code.google.com/p/neoeedit/";
		U.setClipBoard(url);
		message("visit " + url + " for more info.(url copied)");
	}

	public void keyPressed(KeyEvent env) {
		history.beginAtom();
		try {
			// System.out.println("press " + env.getKeyChar());
			int ocx = cx;
			int ocy = cy;
			int kc = env.getKeyCode();
			if (kc == KeyEvent.VK_F1) {
				help();
			} else if (kc == KeyEvent.VK_F2) {
				U.saveAs(this);
			} else if (kc == KeyEvent.VK_F3) {
				ptFind.findNext();
			} else if (kc == KeyEvent.VK_F5) {
				ptEdit.reloadWithEncodingByUser();
			}
			if (env.isAltDown()) {
				if (kc == KeyEvent.VK_LEFT) {
					ptEdit.moveLineLeft(cy);
					focusCursor();
				} else if (kc == KeyEvent.VK_RIGHT) {
					ptEdit.moveLineRight(cy);
					focusCursor();
				} else if (kc == KeyEvent.VK_BACK_SLASH) {
					rectSelectMode = !rectSelectMode;
				} else if (kc == KeyEvent.VK_N) {
					noise = !noise;
					if (noise){
						startNoiseThread();
					}
				} else if (kc == KeyEvent.VK_S) {
					if (lineSep.equals("\n"))
						lineSep = "\r\n";
					else
						lineSep = "\n";
				}
			} else if (env.isControlDown()) {
				if (kc == KeyEvent.VK_C) {
					ptSelection.copySelected();
				} else if (kc == KeyEvent.VK_V) {
					if (ptSelection.isSelected()) {
						ptEdit.deleteRect(ptSelection.getSelectRect());
					}
					ptEdit.insertString(U.getClipBoard());
				} else if (kc == KeyEvent.VK_X) {
					ptSelection.cutSelected();
				} else if (kc == KeyEvent.VK_A) {
					ptSelection.selectAll();
				} else if (kc == KeyEvent.VK_D) {
					if (ptSelection.isSelected()) {
						ptEdit.deleteRect(ptSelection.getSelectRect());
					} else {
						ptEdit.deleteLine(cy);
					}
					focusCursor();
				} else if (kc == KeyEvent.VK_O) {
					U.openFile(this);
				} else if (kc == KeyEvent.VK_N) {
					editor.newFileInNewWindow();
				} else if (kc == KeyEvent.VK_S && env.isShiftDown()) {
					ptPages.saveAllFiles();
				} else if (kc == KeyEvent.VK_S) {
					if (U.saveFile(this)) {
						System.out.println("saved");
						message("saved");
					}
				} else if (kc == KeyEvent.VK_L) {
					gotoLine();
				} else if (kc == KeyEvent.VK_Z) {
					history.undo();
				} else if (kc == KeyEvent.VK_F) {
					ptFind.showFindDialog();
				} else if (kc == KeyEvent.VK_TAB) {
					ptPages.changePage();
				} else if (kc == KeyEvent.VK_Y) {
					history.redo();
				} else if (kc == KeyEvent.VK_W) {
					ptPages.closePage();
				} else if (kc == KeyEvent.VK_E) {
					U.setEncodingByUser(this);
				} else if (kc == KeyEvent.VK_PAGE_UP) {
					cy = 0;
					cx = 0;
					focusCursor();
				} else if (kc == KeyEvent.VK_PAGE_DOWN) {
					cy = roLines.getLinesize() - 1;
					cx = 0;
					focusCursor();
				} else if (kc == KeyEvent.VK_R) {
					U.removeTrailingSpace(PlainPage.this);
				} else if (kc == KeyEvent.VK_LEFT) {
					cursor.moveLeftWord();
					focusCursor();
				} else if (kc == KeyEvent.VK_RIGHT) {
					cursor.moveRightWord();
					focusCursor();
				} else if (kc == KeyEvent.VK_UP) {
					sy = Math.max(0, sy - 1);
				} else if (kc == KeyEvent.VK_DOWN) {
					sy = Math.min(sy + 1, roLines.getLinesize() - 1);
				} else if (kc == KeyEvent.VK_0) {
					ui.scalev = 1;
				} else if (kc == KeyEvent.VK_G) {
					gotoFileLine();
				} else if (kc == KeyEvent.VK_H) {
					openFileHistory(editor);
				}
			} else {
				if (kc == KeyEvent.VK_LEFT) {
					cursor.moveLeft();
					focusCursor();
				} else if (kc == KeyEvent.VK_RIGHT) {
					cursor.moveRight();
					focusCursor();
				} else if (kc == KeyEvent.VK_UP) {
					cursor.moveUp();
					focusCursor();
				} else if (kc == KeyEvent.VK_DOWN) {
					cursor.moveDown();
					focusCursor();
				} else if (kc == KeyEvent.VK_HOME) {
					cursor.moveHome();
					focusCursor();
				} else if (kc == KeyEvent.VK_END) {
					cursor.moveEnd();
					focusCursor();
				} else if (kc == KeyEvent.VK_PAGE_UP) {
					cursor.movePageUp();
					focusCursor();
				} else if (kc == KeyEvent.VK_PAGE_DOWN) {
					cursor.movePageDown();
					focusCursor();
				} else if (kc == KeyEvent.VK_CONTROL || kc == KeyEvent.VK_SHIFT
						|| kc == KeyEvent.VK_ALT) {
					return;
				}
			}
			boolean cmoved = !(ocx == cx && ocy == cy);
			if (cmoved) {
				if (env.isShiftDown()) {
					selectstopx = cx;
					selectstopy = cy;
				} else {
					ptSelection.cancelSelect();
				}
			}
			editor.repaint();
		} catch (Exception e) {
			message("err:" + e);
			e.printStackTrace();
		}
		history.endAtom();
	}

	private void gotoFileLine() throws Exception {
		if (cy < lines.size()) {
			StringBuffer sb = lines.get(cy);
			int p1, p2;
			if ((p1 = sb.indexOf("|")) >= 0) {
				if ((p2 = sb.indexOf(":", p1)) >= 0) {
					String fn = sb.substring(0, p1);
					int line = -1;
					try {
						line = Integer.parseInt(sb.substring(p1 + 1, p2));
					} catch (Exception e) {
					}
					if (line >= 0) {
						openFile(fn, line);
					}
				}
			}
		}

	}

	private void openFile(String fn, int line) throws Exception {
		final PlainPage pp = editor.openFileInNewWindow(fn);
		if (pp != null && pp.lines.size() > 0) {
			line -= 1;
			pp.cx = 0;
			pp.cy = Math.min(line, pp.lines.size() - 1);
			pp.sy = Math.max(0, pp.cy - 3);
			pp.editor.repaint();
		}
	}

	public void keyReleased(KeyEvent env) {
	}

	public void keyTyped(KeyEvent env) {
		history.beginAtom();
		char kc = env.getKeyChar();
		if (kc == KeyEvent.VK_TAB && env.isShiftDown()) {
			Rectangle r = ptSelection.getSelectRect();
			if (r.y < r.height) {
				ptEdit.moveRectLeft(r.y, r.height);
			} else {
				ptEdit.moveLineLeft(cy);
			}
		} else if (kc == KeyEvent.VK_TAB && !env.isShiftDown()
				&& selectstarty != selectstopy && !rectSelectMode) {
			Rectangle r = ptSelection.getSelectRect();
			ptEdit.moveRectRight(r.y, r.height);
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

	public void mouseDragged(MouseEvent env) {
		mx = env.getX();
		my = env.getY();
		mshift = true;
		editor.repaint();
	}

	public void mousePressed(MouseEvent env) {
		mx = env.getX();
		my = env.getY();
		mshift = env.isShiftDown();
		mcount = env.getClickCount();
		editor.repaint();
		// System.out.println("m press");
	}

	public void scroll(int amount) {
		sy += amount;
		if (sy >= roLines.getLinesize()) {
			sy = roLines.getLinesize() - 1;
		}
		if (sy < 0) {
			sy = 0;
		}
		editor.repaint();
	}

	public void xpaint(Graphics g, Dimension size) {
		ui.xpaint(g, size);
	}

	public void mouseWheelMoved(MouseWheelEvent env) {
		int amount = env.getWheelRotation() * env.getScrollAmount();
		if (env.isControlDown()) {// scale
			scale(amount);
		} else {// scroll
			scroll(amount);
		}

	}

	private void scale(int amount) {
		if (amount > 0) {
			ui.scalev *= 1.1f;
		} else if (amount < 0) {
			ui.scalev *= 0.9f;
		}
	}

	private static void openFileHistory(EditWindow ed) throws Exception {
		File fhn = U.getFileHistoryName();
		PlainPage pp = ed.openFileInNewWindow(fhn.getAbsolutePath());
		pp.cy = Math.max(0, pp.lines.size() - 1);
		pp.sy = Math.max(0, pp.cy - 5);
		pp.editor.repaint();
	}
}
