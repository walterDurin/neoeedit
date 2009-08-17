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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import neoe.util.FileIterator;

public class PlainPage implements Page {

	public static class PriorityList {

		private Map<Integer, Image> cache;
		private List<Integer> ks;
		private int max;
		private int cut;

		public PriorityList(Map<Integer, Image> lineCache) {
			this.cache = lineCache;
			ks = new ArrayList<Integer>();
			max = 200;
			cut = 100;
		}

		public void touch(Integer k) {
			int p = ks.indexOf(k);
			if (p >= cut) {
				ks.remove(k);
				ks.add(0, k);
			} else if (p < 0) {
				ks.add(0, k);
			}
			if (ks.size() > max) {
				for (Integer rm : ks.subList(cut, ks.size())) {
					cache.remove(rm);
				}
				ks = ks.subList(0, cut);
			}

		}

	}

	private static final int MAX_SHOW_CHARS = 300;
	private static final String UTF8 = "utf8";
	private static final long VANISHTIME = 3000;
	private Editor edit;
	public PageInfo info;
	private Font font;
	private int lineHeight;
	private int lineGap;
	private Color color;
	private String encoding;
	private List<StringBuffer> lines;
	private Color bkColor;
	private Color currentLineColor;
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
	private boolean highlight = true;
	private boolean isCommentChecked = false;
	private boolean isCacheMode = false;// disabled because slower.

	final static String[] kws = { "ArithmeticError", "AssertionError",
			"AttributeError", "BufferType", "BuiltinFunctionType",
			"BuiltinMethodType", "ClassType", "CodeType", "ComplexType",
			"DeprecationWarning", "DictProxyType", "DictType",
			"DictionaryType", "EOFError", "EllipsisType", "EnvironmentError",
			"Err", "Exception", "False", "FileType", "FloatType",
			"FloatingPointError", "FrameType", "FunctionType", "GeneratorType",
			"IOError", "ImportError", "IndentationError", "IndexError",
			"InstanceType", "IntType", "KeyError", "KeyboardInterrupt",
			"LambdaType", "ListType", "LongType", "LookupError", "MemoryError",
			"MethodType", "ModuleType", "NameError", "None", "NoneType",
			"NotImplemented", "NotImplementedError", "OSError", "ObjectType",
			"OverflowError", "OverflowWarning", "ReferenceError",
			"RuntimeError", "RuntimeWarning", "SliceType", "StandardError",
			"StopIteration", "StringType", "StringTypes", "SyntaxError",
			"SyntaxWarning", "SystemError", "SystemExit", "TabError",
			"TracebackType", "True", "TupleType", "TypeError", "TypeType",
			"UnboundLocalError", "UnboundMethodType", "UnicodeError",
			"UnicodeType", "UserWarning", "ValueError", "Warning",
			"WindowsError", "XRangeType", "ZeroDivisionError", "__abs__",
			"__add__", "__all__", "__author__", "__bases__", "__builtins__",
			"__call__", "__class__", "__cmp__", "__coerce__", "__contains__",
			"__debug__", "__del__", "__delattr__", "__delitem__",
			"__delslice__", "__dict__", "__div__", "__divmod__", "__doc__",
			"__docformat__", "__eq__", "__file__", "__float__", "__floordiv__",
			"__future__", "__ge__", "__getattr__", "__getattribute__",
			"__getitem__", "__getslice__", "__gt__", "__hash__", "__hex__",
			"__iadd__", "__import__", "__imul__", "__init__", "__int__",
			"__invert__", "__iter__", "__le__", "__len__", "__long__",
			"__lshift__", "__lt__", "__members__", "__metaclass__", "__mod__",
			"__mro__", "__mul__", "__name__", "__ne__", "__neg__", "__new__",
			"__nonzero__", "__oct__", "__or__", "__path__", "__pos__",
			"__pow__", "__radd__", "__rdiv__", "__rdivmod__", "__reduce__",
			"__repr__", "__rfloordiv__", "__rlshift__", "__rmod__", "__rmul__",
			"__ror__", "__rpow__", "__rrshift__", "__rsub__", "__rtruediv__",
			"__rxor__", "__self__", "__setattr__", "__setitem__",
			"__setslice__", "__slots__", "__str__", "__sub__", "__truediv__",
			"__version__", "__xor__", "abs", "abstract", "all", "and", "any",
			"apply", "array", "as", "asc", "ascb", "ascw", "asm", "assert",
			"atn", "auto", "bool", "boolean", "break", "buffer", "byref",
			"byte", "byval", "call", "callable", "case", "catch", "cbool",
			"cbyte", "ccur", "cdate", "cdbl", "char", "chr", "chrb", "chrw",
			"cint", "class", "classmethod", "clng", "cmp", "coerce", "compile",
			"complex", "const", "continue", "cos", "createobject", "csng",
			"cstr", "date", "dateadd", "datediff", "datepart", "dateserial",
			"datevalue", "day", "def", "default", "del", "delattr", "dict",
			"dim", "dir", "divmod", "do", "double", "each", "elif", "else",
			"elseif", "empty", "end", "enum", "enumerate", "erase", "error",
			"eval", "except", "exec", "execfile", "execute", "exit", "exp",
			"explicit", "extends", "extern", "false", "file", "filter",
			"final", "finally", "fix", "float", "for", "formatcurrency",
			"formatdatetime", "formatnumber", "formatpercent", "from",
			"frozenset", "function", "get", "getattr", "getobject", "getref",
			"global", "globals", "goto", "hasattr", "hash", "hex", "hour",
			"id", "if", "imp", "implements", "import", "in", "inline", "input",
			"inputbox", "instanceof", "instr", "instrb", "instrrev", "int",
			"interface", "intern", "is", "isarray", "isdate", "isempty",
			"isinstance", "isnull", "isnumeric", "isobject", "issubclass",
			"iter", "join", "lambda", "lbound", "lcase", "left", "leftb",
			"len", "lenb", "let", "list", "loadpicture", "locals", "log",
			"long", "loop", "ltrim", "map", "max", "mid", "midb", "min",
			"minute", "mod", "month", "monthname", "msgbox", "native", "new",
			"next", "not", "nothing", "now", "null", "object", "oct", "on",
			"open", "option", "or", "ord", "package", "pass", "pow",
			"preserve", "print", "private", "property", "protected", "public",
			"raise", "randomize", "range", "raw_input", "redim", "reduce",
			"register", "reload", "rem", "replace", "repr", "resume", "return",
			"reversed", "rgb", "right", "rightb", "rnd", "round", "rtrim",
			"scriptengine", "scriptenginebuildversion",
			"scriptenginemajorversion", "scriptengineminorversion", "second",
			"select", "self", "set", "setattr", "sgn", "short", "signed",
			"sin", "sizeof", "slice", "sorted", "space", "split", "sqr",
			"static", "staticmethod", "step", "str", "strcomp", "strictfp",
			"string", "strreverse", "struct", "sub", "sum", "super", "switch",
			"synchronized", "tan", "then", "this", "throw", "throws", "time",
			"timeserial", "timevalue", "to", "transient", "trim", "true",
			"try", "tuple", "type", "typedef", "typename", "ubound", "ucase",
			"unichr", "unicode", "union", "unsigned", "until", "vars",
			"vartype", "vbAbort", "vbAbortRetryIgnore", "vbApplicationModal",
			"vbCancel", "vbCritical", "vbDefaultButton1", "vbDefaultButton2",
			"vbDefaultButton3", "vbDefaultButton4", "vbExclamation", "vbFalse",
			"vbGeneralDate", "vbIgnore", "vbInformation", "vbLongDate",
			"vbLongTime", "vbNo", "vbOK", "vbOKCancel", "vbOKOnly",
			"vbObjectError", "vbQuestion", "vbRetry", "vbRetryCancel",
			"vbShortDate", "vbShortTime", "vbSystemModal", "vbTrue",
			"vbUseDefault", "vbYes", "vbYesNo", "vbYesNoCancel", "vbarray",
			"vbblack", "vbblue", "vbboolean", "vbbyte", "vbcr", "vbcrlf",
			"vbcurrency", "vbcyan", "vbdataobject", "vbdate", "vbdecimal",
			"vbdouble", "vbempty", "vberror", "vbformfeed", "vbgreen",
			"vbinteger", "vblf", "vblong", "vbmagenta", "vbnewline", "vbnull",
			"vbnullchar", "vbnullstring", "vbobject", "vbred", "vbsingle",
			"vbstring", "vbtab", "vbvariant", "vbverticaltab", "vbwhite",
			"vbyellow", "void", "volatile", "weekday", "weekdayname", "wend",
			"while", "with", "xor", "xrange", "year", "yield", "zip" };

	public PlainPage(Editor editor, PageInfo pi) throws Exception {
		this.edit = editor;
		this.info = pi;
		sx = 0;
		sy = 0;
		cx = 0;
		cy = 0;
		lineCache = new HashMap<Integer, Image>();
		lineCacheKey = new PriorityList(lineCache);
		this.font = new Font("Monospaced", Font.PLAIN, 12);
		this.lineHeight = 10;
		this.lineGap = 5;
		this.color = Color.BLACK;
		this.bkColor = new Color(0xe0e0f0);
		this.currentLineColor = new Color(0xF0F0F0);
		this.encoding = null;
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
		if (encoding == null) {
			try {
				encoding = guessEncoding(fn);
			} catch (Exception e) {
				e.printStackTrace();
				encoding = UTF8;
			}
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
		if (lines.size() == 0) {
			lines.add(new StringBuffer());
		}
		return lines;
	}

	private static String guessEncoding(String fn) throws Exception {
		String s = guessEncoding_(fn);
		if (s == null) {
			s = UTF8;
		}
		return s;
	}

	private static String guessEncoding_(String fn) throws Exception {
		// S/ystem.out.println("guessing encoding");
		String[] encodings = { UTF8, "sjis", "gbk", };

		FileInputStream in = new FileInputStream(fn);
		final int defsize = 4096;
		int len = Math.min(defsize, (int) new File(fn).length());
		try {
			// S/ystem.out.println("len:" + len);
			byte[] buf = new byte[len];
			len = in.read(buf);
			// S/ystem.out.println("len2:" + len);
			if (len != defsize) {
				byte[] b2 = new byte[len];
				System.arraycopy(buf, 0, b2, 0, len);
				buf = b2;
			}
			for (String enc : encodings) {
				byte[] b2 = new String(buf, enc).getBytes(enc);
				if (b2.length != buf.length) {
					continue;
				}
				int nlen = Math.min(0, len - 1);// for not last complete char
				if (Arrays.equals(Arrays.copyOf(buf, nlen), Arrays.copyOf(b2,
						nlen))) {
					return enc;
				}
			}
		} finally {
			in.close();
		}

		return null;
	}

	public void xpaint(Graphics g, Dimension size) {
		try {
			this.size = size;

			if (!isCommentChecked) {
				isCommentChecked = true;
				new Thread() {
					public void run() {
						checkComment();
					}
				}.start();
			}

			Graphics2D g2 = (Graphics2D) g;
			g2.setFont(font);

			showLineCnt = (size.height - toolbarHeight)
					/ (lineHeight + lineGap);
			int charCntInLine = (size.width - gutterWidth) / (lineHeight) * 2;

			// change sx if needed
			cx = Math.min(getline(cy).length(), cx);
			if (cx < sx) {
				sx = Math.max(0, cx - charCntInLine / 2);
			} else {

				if (strWidth(g2, subs(getline(cy), sx, cx)) > size.width
						- lineHeight * 3) {
					sx = Math.max(0, cx - charCntInLine / 2);
					int xx = charCntInLine / 4;
					while (xx > 0
							&& strWidth(g2, subs(getline(cy), sx, cx)) > size.width
									- lineHeight * 3) {
						sx = Math.max(0, cx - xx - 1);
						xx /= 2;
					}
				}
			}

			// apply mouse click position
			if (my > 0 && my < toolbarHeight) {
				if (info.fn != null) {
					Toolkit.getDefaultToolkit().getSystemClipboard()
							.setContents(new StringSelection(info.fn), null);
					message("filename copied");
					System.out.println("filename copied");
					my = 0;
				}
			} else if (my > 0 && mx >= gutterWidth && my >= toolbarHeight) {
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
							&& Character
									.isJavaIdentifierPart(sb.charAt(x1 - 1))) {
						x1 -= 1;
					}
					while (x2 < sb.length() - 1
							&& Character
									.isJavaIdentifierPart(sb.charAt(x2 + 1))) {
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
				my = 0;
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
			int y = sy;
			int py = lineHeight;
			int notHit = 0;
			for (int i = 0; i < showLineCnt; i++) {

				if (y >= getLinesize()) {
					break;
				}
				RoSb sb = getline(y);

				if (sx < sb.length()) {
					int chari2 = Math.min(charCntInLine + sx, sb.length());
					String s = subs(sb, sx, chari2);
					g2.setColor(color);
					notHit += drawStringLine(g2, s, 0, py, y);
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
			if (isCacheMode) {
				System.out.println("not hit lines=" + notHit);
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
			if (true) {// (){}[]<> pair marking
				if (cx - 1 < getline(cy).length() && cx - 1 >= 0) {
					char c = getline(cy).charAt(cx - 1);
					String pair = "(){}[]<>";
					int p1 = pair.indexOf(c);
					if (p1 >= 0) {
						if (p1 % 2 == 0) {
							pairMark(g2, cx - 1, cy, pair.charAt(p1 + 1), c, 1);
						} else {
							pairMark(g2, cx - 1, cy, pair.charAt(p1 - 1), c, -1);
						}
					}
				}

			}

			// draw cursor
			if (cy >= sy && cy <= sy + showLineCnt) {
				g2.setXORMode(new Color(0x30f0f0));
				String s = subs(getline(cy), sx, cx);
				int w = strWidth(g2, s);
				g2.fillRect(w, (cy - sy) * (lineHeight + lineGap), 2,
						lineHeight);
			}
		} catch (Throwable th) {
			th.printStackTrace();
			message("Bug:" + th);
		}
	}

	protected void checkComment() {
		String[] commentchars = { "#", "%", "'", "//", "!", ";" };
		int[] cnts = new int[commentchars.length];
		for (int i = 0; i < lines.size(); i++) {
			RoSb sb = getline(i);
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

	String comment = null;

	private void pairMark(Graphics2D g2, int cx2, int cy2, char ch, char ch2,
			int inc) {
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

	private void markBox(Graphics2D g2, int x, int y) {
		if (y >= sy && y <= sy + showLineCnt && x >= sx) {
			RoSb sb = getline(y);
			int w1 = x > 0 ? strWidth(g2, sb.substring(sx, x)) : 0;
			String c = sb.substring(x, x + 1);
			int w2 = strWidth(g2, c);
			g2.setColor(Color.WHITE);
			g2.drawRect(w1 - 1, (y - sy) * (lineHeight + lineGap) - 4, w2, 16);
			g2.setColor(color);
			g2.drawRect(w1, (y - sy) * (lineHeight + lineGap) - 3, w2, 16);

			g2
					.drawString(c, w1, lineHeight + (y - sy)
							* (lineHeight + lineGap));
		}
	}

	private void findchar(char ch, int inc, int[] c1, char chx) {
		int cx1 = c1[0];
		int cy1 = c1[1];
		RoSb csb = getline(cy1);
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
						csb = getline(cy1);
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
					if (cy1 >= getLinesize()) {
						c1[0] = -1;
						c1[1] = -1;
						return;
					} else {
						csb = getline(cy1);
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

	Color c1 = new Color(200, 80, 50), c2 = Color.WHITE;
	private Map<Integer, Image> lineCache;
	PriorityList lineCacheKey;

	private int drawStringLine(Graphics2D g2, String s, int x, int y, int lineno) {
		int notHit = 0;
		if (isCacheMode) {
			Image im = lineCache.get(lineno);
			if (im == null) {
				im = createLineImage(g2, s, x, y, lineno);
				lineCache.put(lineno, im);
				notHit = 1;
			}
			lineCacheKey.touch(lineno);
			g2.drawImage(im, x, y - lineHeight, null);
		} else {
			_drawLine(g2, s, x, y);
		}
		return notHit;
	}

	private Image createLineImage(Graphics2D g3, String s, int x, int y,
			int lineno) {
		BufferedImage im = new BufferedImage(size.width - gutterWidth,
				lineHeight + lineGap, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = (Graphics2D) im.getGraphics();
		g2.setFont(g3.getFont());
		y = lineHeight;
		_drawLine(g2, s, x, y);
		g2.dispose();
		return im;
	}

	private void _drawLine(Graphics2D g2, String s, int x, int y) {
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
			List<String> s1x = split(s);
			for (String s1 : s1x) {
				if (s1.equals("\t")) {
					g2.drawImage(U.TabImg, x + w, y - lineHeight, null);
					w += U.TABWIDTH;
				} else {
					int highlightid = getHighLightID(s1, g2);
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

	private int drawTwoColor(Graphics2D g2, String s, int x, int y, Color c1,
			Color c2) {
		g2.setColor(c2);
		g2.drawString(s, x + 1, y + 1);
		g2.setColor(c1);
		g2.drawString(s, x, y);
		return g2.getFontMetrics().stringWidth(s);

	}

	private List<String> split(String s) {
		StringBuffer sb = new StringBuffer();
		List<String> sl = new ArrayList<String>();
		for (char c : s.toCharArray()) {
			if (!Character.isJavaIdentifierPart(c)) {
				if (sb.length() > 0) {
					sl.add(sb.toString());
					sb.setLength(0);
				}
				sl.add("" + c);
			} else {
				sb.append(c);
			}
		}
		if (sb.length() > 0) {
			sl.add(sb.toString());
			sb.setLength(0);
		}
		return sl;
	}

	private int getHighLightID(String s, Graphics2D g2) {
		if (Arrays.binarySearch(kws, s) >= 0
				|| Arrays.binarySearch(kws, s.toLowerCase()) >= 0) {
			g2.setColor(Color.BLUE);
		} else if (isDigital(s)) {
			g2.setColor(Color.RED);
		} else {
			g2.setColor(color);
		}
		return 0;
	}

	private boolean isDigital(String s) {
		for (char c : s.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
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
		String s1 = "<F1>:Help, Line:" + lines.size() + ", Doc:"
				+ edit.pages.size() + ", byte:" + info.size + ", " + encoding
				+ ", X:" + (cx + 1) + ", his:" + history.size() + ", "
				+ info.fn;
		g2.setColor(Color.WHITE);
		g2.drawString(s1, 2, lineHeight + 2);
		g2.setColor(Color.BLACK);
		g2.drawString(s1, 1, lineHeight + 1);
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
		try {
			// System.out.println("press " + env.getKeyChar());

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
					edit.newFileInNewWindow();
				} else if (kc == KeyEvent.VK_S && env.isShiftDown()) {
					saveAllFiles();

				} else if (kc == KeyEvent.VK_S) {
					if (saveFile(info)) {
						System.out.println("saved");
						message("saved");
					}
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
				} else if (kc == KeyEvent.VK_R) {
					removeTrailingSpace();
				} else if (kc == KeyEvent.VK_LEFT) {
					RoSb line = getline(cy);
					cx = Math.max(0, cx - 1);
					char ch1 = line.charAt(cx);
					while (cx > 0 && isSkipChar(line.charAt(cx), ch1)) {
						cx--;
					}
				} else if (kc == KeyEvent.VK_RIGHT) {
					RoSb line = getline(cy);
					cx = Math.min(line.length(), cx + 1);
					if (cx < line.length()) {
						char ch1 = line.charAt(cx);
						while (cx < line.length()
								&& isSkipChar(line.charAt(cx), ch1)) {
							cx++;
						}
					}
				} else if (kc == KeyEvent.VK_UP) {
					sy = Math.max(0, sy - 1);
				} else if (kc == KeyEvent.VK_DOWN) {
					sy = Math.min(sy + 1, getLinesize() - 1);
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
					if (cx > getline(cy).length() && cy < lines.size() - 1) {
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
					if (cy >= getLinesize()) {
						cy = getLinesize() - 1;
					}
					focusCursor();
					cmoved = true;
				} else if (kc == KeyEvent.VK_HOME) {
					String line = getline(cy).toString();
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
					String line = getline(cy).toString();
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
		} catch (Exception e) {
			message("err:" + e);
			e.printStackTrace();
		}
	}

	private boolean isSkipChar(char ch, char ch1) {
		if (Character.isSpaceChar(ch1) || ch1 == '\t') {
			return Character.isSpaceChar(ch) || ch == '\t';
		} else {
			return Character.isJavaIdentifierPart(ch);
		}
	}

	private void removeTrailingSpace() {
		for (int i = 0; i < getLinesize(); i++) {// no record, why? ...emm...
			RoSb sb = getline(i);
			int p = sb.length() - 1;
			while (p >= 0 && "\r\n\t ".indexOf(sb.charAt(p)) >= 0) {
				p--;
			}
			if (p < sb.length() - 1) {
				sb.sb().setLength(p + 1);
			}
		}

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

	private void saveAs() throws Exception {
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
			savePageToFile(info);
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
			doReplaceAll(o.s, true, false, o.s2, false, false, null);// bug
			// expected!
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

	private void find() {
		String t = getSelected();
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
		int p1 = getline(y).toString(ignoreCase).indexOf(s, x);
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
			sy = Math.max(0, line - showLineCnt / 2 + 1);
			cy = line;
			cx = 0;
			focusCursor();
			edit.repaint();
		}
	}

	private void saveAllFiles() throws Exception {
		int total = 0;
		for (PageInfo pi : edit.pages) {
			if (saveFile(pi)) {
				total++;
			}
		}
		System.out.println(total + " files saved");
		message(total + " files saved");
	}

	private static boolean saveFile(PageInfo info) throws Exception {
		PlainPage p = (PlainPage) info.page;
		if (info.fn == null) {
			JFileChooser chooser = new JFileChooser(info.defaultPath);
			int returnVal = chooser.showSaveDialog(p.edit);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fn = chooser.getSelectedFile().getAbsolutePath();
				if (new File(fn).exists()) {
					if (JOptionPane.YES_OPTION != JOptionPane
							.showConfirmDialog(p.edit,
									"Are you sure to overwrite?",
									"File exists", JOptionPane.YES_NO_OPTION)) {
						p.message("not saved");
						return false;
					}
				}
				info.fn = fn;
				p.edit.changeTitle();

			} else {
				return false;
			}
		}
		return savePageToFile(info);

	}

	private static boolean savePageToFile(PageInfo info) throws Exception {
		System.out.println("save " + info.fn);
		PlainPage p = (PlainPage) info.page;
		if (p.encoding == null) {
			p.encoding = UTF8;
		}
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(info.fn), p.encoding));
		for (StringBuffer sb : p.lines) {
			out.write(sb.toString());
			out.write("\n");
		}
		out.close();

		return true;
	}

	private void message(String s) {
		msg = s;
		msgtime = System.currentTimeMillis();

	}

	private void openFile() {
		JFileChooser chooser = new JFileChooser();
		if (info.fn != null) {
			chooser.setSelectedFile(new File(info.fn));
		}else if(info.defaultPath!=null){
			chooser.setSelectedFile(new File(info.defaultPath));//Fixme:cannot set correctly
		}
		int returnVal = chooser.showOpenDialog(edit);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fn = chooser.getSelectedFile().getAbsolutePath();
			System.out.println("You chose to open this file: "
					+ chooser.getSelectedFile().getAbsolutePath());
			edit.openFileInNewWindow(fn);
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
			sy = Math.max(0, cy - showLineCnt / 2 + 1);
		}
		if (showLineCnt > 0) {
			if (sy + showLineCnt - 1 < cy) {
				sy = Math.max(0, cy - showLineCnt / 2 + 1);
			}
		}

	}

	@Override
	public void keyTyped(KeyEvent env) {
		char kc = env.getKeyChar();
		// System.out.println("type " + kc);
		if (kc == KeyEvent.VK_TAB && env.isShiftDown()) {
			for (int i = selectstarty; i <= selectstopy; i++) {
				if (getline(i).length() > 0) {
					char ch = getline(i).charAt(0);
					if (ch == ' ' || ch == '\t') {
						getline(i).sb().delete(0, 1);
					}
				}
			}
			focusCursor();
		} else if (kc == KeyEvent.VK_TAB && !env.isShiftDown()
				&& selectstarty < selectstopy) {

			if (selectstarty < selectstopy) {
				for (int i = selectstarty; i <= selectstopy; i++) {
					getline(i).sb().insert(0, "\t");
				}
				focusCursor();
			}
		} else if (env.isControlDown() || env.isAltDown()) {
			// ignore
		} else {
			insert(kc);
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
		} else if (ch == KeyEvent.VK_ESCAPE) {
			selectstopy = selectstarty;
			selectstopx = selectstartx;
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
		// System.out.println("m press");
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
			doReplaceAll(o.s2, true, false, o.s, false, false, null);// bug
			// expected!
		} else {
			System.out.println("not supported " + o);
		}

	}

	public void doFind(String text, boolean ignoreCase, boolean selected2,
			boolean inDir, String dir) {
		if (!inDir) {
			text2find = text;
			this.ignoreCase = ignoreCase;
			findNext();
			edit.repaint();
		} else {
			doFindInDir(text, ignoreCase, selected2, inDir, dir);
		}
	}

	private static List findInFile(File f, String text, boolean ignoreCase2) {
		List a = new ArrayList();
		try {
			String enc = guessEncoding_(f.getAbsolutePath());
			if (enc != null) {// skip binary
				String fn = f.getAbsolutePath();
				if (ignoreCase2) {
					text = text.toLowerCase();
				}
				BufferedReader in = new BufferedReader(new InputStreamReader(
						new FileInputStream(f), enc));
				String line;
				int lineno = 0;
				while ((line = in.readLine()) != null) {
					lineno++;
					String oline = line;
					if (ignoreCase2) {
						line = line.toLowerCase();
					}

					if (line.indexOf(text) >= 0) {
						if (line.length() > MAX_SHOW_CHARS) {
							line = line.substring(0, MAX_SHOW_CHARS) + "...";
						}
						a.add(String.format("[%s]%s:%s", fn, lineno, oline));
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return a;
	}

	public void doReplace(String text, boolean ignoreCase, boolean selected2,
			String text2, boolean record) {
		_doReplace(text, ignoreCase, selected2, text2, false, record, false,
				null);

	}

	private void _doReplace(String text, boolean ignoreCase, boolean selected2,
			String text2, boolean all, boolean record, boolean inDir, String dir) {

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

	private void doReplaceInDir(String text, boolean ignoreCase2, String text2,
			boolean inDir, String dir) {
		Iterable<File> it = new FileIterator(dir);
		List all = new ArrayList();
		for (File f : it) {
			if (f.isDirectory()) {
				continue;
			}
			try {
				List res = findInFile(f, text, ignoreCase);
				if (res.size() > 0) {
					PageInfo pi = edit.openFile(f.getAbsolutePath());

					pi.initPage(edit);

					if (pi != null) {
						((PlainPage) pi.page).doReplaceAll(text, ignoreCase2,
								false, text2, true, false, null);
					}
				}
				all.addAll(res);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		PageInfo pi = edit.newFile();
		PlainPage p2 = (PlainPage) pi.page;
		p2.lines.clear();
		p2.appendLine(String.format(
				"replaced %s results in dir %s for '%s', not saved.", all
						.size(), dir, text));
		for (Object o : all) {
			p2.appendLine(o.toString());
		}
		edit.repaint();
	}

	private void doFindInDir(String text, boolean ignoreCase,
			boolean selected2, boolean inDir, String dir) {
		Iterable<File> it = new FileIterator(dir);
		List all = new ArrayList();
		for (File f : it) {
			if (f.isDirectory()) {
				continue;
			}
			List res = findInFile(f, text, ignoreCase);
			all.addAll(res);
		}
		PageInfo pi = edit.newFile();
		PlainPage p2 = (PlainPage) pi.page;
		p2.lines.clear();
		p2.appendLine(String.format("find %s results in dir %s for '%s'", all
				.size(), dir, text));
		for (Object o : all) {
			p2.appendLine(o.toString());
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
		int p1 = x;
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
			boolean selected2, String text2, boolean record, boolean inDir,
			String dir) {
		if (inDir) {
			doReplaceInDir(text, ignoreCase, text2, inDir, dir);
		} else {
			_doReplace(text, ignoreCase, selected2, text2, true, record, inDir,
					dir);
		}
	}

	public void appendLine(String s) {
		lines.add(new StringBuffer(s));
	}
}