package neoe.ne;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import neoe.ne.PlainPage.BasicEdit;
import neoe.util.FileIterator;

/**
 * util
 */
public class U {
	public static class PriorityList {

		private Map<Integer, Image> cache;
		private int cut;
		private List<Integer> ks;
		private int max;

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

	/**
	 * read-only stringbuffer.
	 */
	static class RoSb {

		private StringBuffer sb;

		public RoSb(StringBuffer sb) {
			this.sb = sb;
		}

		public char charAt(int i) {
			return sb.charAt(i);
		}

		public int length() {
			return sb.length();
		}

		public String substring(int i) {
			return sb.substring(i);
		}

		public String substring(int a, int b) {

			return sb.substring(a, b);
		}

		public String toString() {
			return sb.toString();
		}

		public String toString(boolean ignoreCase) {
			String s = sb.toString();
			if (ignoreCase) {
				return s.toLowerCase();
			} else {
				return s;
			}
		}

	}

	public static class SimpleLayout {
		JPanel curr;
		JPanel p;

		public SimpleLayout(JPanel p) {
			this.p = p;
			p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
			newCurrent();
		}

		public void add(JComponent co) {
			curr.add(co);
		}

		void newCurrent() {
			curr = new JPanel();
			curr.setLayout(new BoxLayout(curr, BoxLayout.LINE_AXIS));
		}

		public void newline() {
			p.add(curr);
			newCurrent();
		}
	}

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

	public static Image TabImg;

	public static final int TABWIDTH = 60;

	static final String UTF8 = "utf8";

	static {
		try {
			loadTabImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static void doReplace(PlainPage page, String text, boolean ignoreCase,
			boolean selected2, String text2, boolean all, boolean inDir,
			String dir) {

		page.text2find = text;
		if (page.text2find != null && page.text2find.length() > 0) {
			Point p = replace(page, page.text2find, page.cx, page.cy, text2,
					all, ignoreCase);
			if (p == null) {
				page.message("string not found");
			} else {
				page.cx = p.x;
				page.cy = p.y;
				// page.selectstartx = page.cx;
				// page.selectstarty = page.cy;
				// page.selectstopx = page.cx + text2.length();
				// page.selectstopy = page.cy;
				page.focusCursor();
				page.ptSelection.cancelSelect();
			}
		}
		page.editor.repaint();
	}

	static void doFindInDir(EditWindow editor, String text, boolean ignoreCase,
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
		showResult(editor, all, "dir " + dir, text);
		editor.repaint();
	}

	static void doFindInPage(PlainPage page, String text2find,
			boolean ignoreCase) throws Exception {
		if (text2find != null && text2find.length() > 0) {
			Point p = U.find(page, text2find, 0, 0, ignoreCase);
			if (p == null) {
				page.message("string not found");
			} else {
				List<String> all = new ArrayList<String>();
				while (true) {
					all.add(String.format("%s:%s", p.y + 1, page.roLines
							.getline(p.y)));
					Point p2 = U
							.find(page, text2find, p.x, p.y + 1, ignoreCase);
					if (p2 == null || p2.y < p.y) {
						break;
					} else {
						p = p2;
					}
				}
				showResult(page.editor, all, "file " + page.fn, text2find);
				page.editor.repaint();
			}
		}
	}

	static void doReplaceAll(PlainPage page, String text, boolean ignoreCase,
			boolean selected2, String text2, boolean inDir, String dir)
			throws Exception {
		if (inDir) {
			U.doReplaceInDir(page, text, ignoreCase, text2, inDir, dir);
		} else {
			U.doReplace(page, text, ignoreCase, selected2, text2, true, inDir,
					dir);
		}
	}

	static void doReplaceInDir(PlainPage page, String text,
			boolean ignoreCase2, String text2, boolean inDir, String dir)
			throws Exception {
		EditWindow editor = page.editor;
		Iterable<File> it = new FileIterator(dir);
		List<String> all = new ArrayList<String>();
		for (File f : it) {
			if (f.isDirectory()) {
				continue;
			}
			try {
				List<String> res = U.findInFile(f, text, page.ignoreCase);
				if (res.size() > 0) {

					PlainPage pi = editor.openFile(f.getAbsolutePath());

					if (pi != null) {
						doReplaceAll(pi, text, ignoreCase2, false, text2,
								false, null);
					}
				}
				all.addAll(res);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		showResult(editor, all, "dir " + dir, text);
		editor.repaint();
	}

	public static String f(String s) {
		while (s.endsWith("\r")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	static List<String> findInFile(File f, String text, boolean ignoreCase2) {
		List<String> a = new ArrayList<String>();
		try {
			String enc = guessEncoding(f.getAbsolutePath());
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
						if (line.length() > PlainPage.MAX_SHOW_CHARS_IN_LINE) {
							line = line.substring(0,
									PlainPage.MAX_SHOW_CHARS_IN_LINE)
									+ "...";
						}
						a.add(String.format("%s|%s:%s", fn, lineno, oline));
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return a;
	}

	public static void gc() {
		System.out.print(km(Runtime.getRuntime().freeMemory()) + "/"
				+ km(Runtime.getRuntime().totalMemory()) + " -> ");
		Runtime.getRuntime().gc();
		System.out.println(km(Runtime.getRuntime().freeMemory()) + "/"
				+ km(Runtime.getRuntime().totalMemory()));
	}

	static String getIndent(String s) {
		int p = 0;
		while (p < s.length() && (s.charAt(p) == ' ' || s.charAt(p) == '\t')) {
			p += 1;
		}
		return s.substring(0, p);
	}

	static String guessEncoding(String fn) throws Exception {
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

	static String guessEncodingForEditor(String fn) {
		try {
			String s = guessEncoding(fn);
			if (s == null) {// unknow
				s = UTF8;
			}
			return s;
		} catch (Exception e) {
			return UTF8;
		}
	}

	static boolean isAllDigital(String s) {
		for (char c : s.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	static boolean isSkipChar(char ch, char ch1) {
		if (Character.isSpaceChar(ch1) || ch1 == '\t') {
			return Character.isSpaceChar(ch) || ch == '\t';
		} else {
			return Character.isJavaIdentifierPart(ch);
		}
	}

	public static String km(long v) {
		float m = 1024 * 1024f;
		if (v > m) {
			return String.format("%.1fMB", v / m);
		} else if (v > 1024) {
			return String.format("%.1fKB", v / 1024f);
		}
		return "" + v;
	}

	private static void loadTabImage() throws Exception {
		BufferedImage img = ImageIO.read(U.class
				.getResourceAsStream("/icontab.png"));
		TabImg = img.getScaledInstance(TABWIDTH, 8, Image.SCALE_SMOOTH);
	}

	public static void log(String s) {
		Date t = new Date();
		System.out.println("[d " + t + "]" + s);
	}

	public static int nextHalf(int i) {
		if (i <= 1) {
			return i;
		}
		return i / 2;
	}

	static void openFile(PlainPage page) throws Exception {
		JFileChooser chooser = new JFileChooser();

		if (page.fn != null) {
			chooser.setSelectedFile(new File(page.fn));
		} else if (page.workPath != null) {
			chooser.setSelectedFile(new File(page.workPath));// Fixme:cannot
			// set
			// correctly
		}
		int returnVal = chooser.showOpenDialog(page.editor);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fn = chooser.getSelectedFile().getAbsolutePath();
			System.out.println("You chose to open this file: "
					+ chooser.getSelectedFile().getAbsolutePath());
			page.editor.openFileInNewWindow(fn);
		}

	}

	static void readFile(PlainPage page, String fn) {
		if (fn == null) {
			List<StringBuffer> lines = new ArrayList<StringBuffer>();
			lines.add(new StringBuffer("edit here..."));
			page.ptEdit.setLines(lines);
			return;
		}
		if (page.encoding == null) {
			page.encoding = U.guessEncodingForEditor(fn);
		}
		page.ptEdit.setLines(U.readFileForEditor(fn, page.encoding));
	}

	static List<StringBuffer> readFileForEditor(String fn, String encoding) {
		List<StringBuffer> lines = new ArrayList<StringBuffer>();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(fn), encoding));
			String line;
			while ((line = in.readLine()) != null) {
				lines.add(new StringBuffer(line));
			}
			in.close();
		} catch (Throwable e) {
			lines.add(new StringBuffer(e.toString()));
		}
		if (lines.size() == 0) {
			lines.add(new StringBuffer());
		}
		return lines;
	}

	static void removeTrailingSpace(PlainPage page) {
		for (int i = 0; i < page.roLines.getLinesize(); i++) {
			RoSb sb = page.roLines.getline(i);
			int p = sb.length() - 1;
			while (p >= 0 && "\r\n\t ".indexOf(sb.charAt(p)) >= 0) {
				p--;
			}
			if (p < sb.length() - 1) {
				page.editRec.deleteInLine(i, p + 1, sb.length());
			}
		}
	}

	static void repaintAfter(final long t, final JComponent edit) {
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

	static Point replace(PlainPage page, String s, int x, int y, String s2,
			boolean all, boolean ignoreCase) {
		int cnt = 0;
		BasicEdit editRec = page.editRec;
		if (ignoreCase) {
			s = s.toLowerCase();
		}
		// first half row

		int p1 = x;
		while (true) {
			p1 = page.roLines.getline(y).toString(ignoreCase).indexOf(s, p1);
			if (p1 >= 0) {
				cnt++;
				editRec.deleteInLine(y, p1, p1 + s.length());
				editRec.insertInLine(y, p1, s2);
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
		for (int i = 0; i < page.roLines.getLinesize() - 1; i++) {
			fy += 1;
			if (fy >= page.roLines.getLinesize()) {
				fy = 0;
			}
			p1 = 0;
			while (true) {
				p1 = page.roLines.getline(fy).toString(ignoreCase).indexOf(s,
						p1);
				if (p1 >= 0) {

					cnt++;
					editRec.deleteInLine(fy, p1, p1 + s.length());
					editRec.insertInLine(fy, p1, s2);
					if (!all) {
						return new Point(p1 + s2.length(), fy);
					}
					p1 = p1 + +s2.length();
				} else {
					break;
				}
			}
		}
		// last half row
		fy += 1;
		if (fy >= page.roLines.getLinesize()) {
			fy = 0;
		}
		p1 = 0;
		while (true) {
			p1 = page.roLines.getline(fy).toString(ignoreCase).substring(0, x)
					.indexOf(s, p1);
			if (p1 >= 0) {
				cnt++;
				editRec.deleteInLine(fy, p1, p1 + s.length());
				editRec.insertInLine(fy, p1, s2);
				if (!all) {
					return new Point(p1 + s2.length(), fy);
				}
				p1 = p1 + s2.length();
			} else {
				break;
			}
		}
		if (cnt > 0) {
			page.message("replaced " + cnt + " places");
			return new Point(x, y);
		} else {
			return null;
		}
	}

	static void saveAs(PlainPage page) throws Exception {
		EditWindow editor = page.editor;
		JFileChooser chooser = new JFileChooser(page.fn);
		int returnVal = chooser.showSaveDialog(editor);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			String fn = chooser.getSelectedFile().getAbsolutePath();
			if (new File(fn).exists()) {
				if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
						editor, "file exists, are you sure to overwrite?",
						"save as...", JOptionPane.YES_NO_OPTION)) {
					page.message("not renamed");
					return;
				}
			}
			page.fn = fn;
			editor.changeTitle();
			page.message("file renamed");
			savePageToFile(page);
		}
	}

	static boolean saveFile(PlainPage page) throws Exception {
		if (page.fn == null) {
			JFileChooser chooser = new JFileChooser(page.workPath);
			int returnVal = chooser.showSaveDialog(page.editor);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String fn = chooser.getSelectedFile().getAbsolutePath();
				if (new File(fn).exists()) {
					if (JOptionPane.YES_OPTION != JOptionPane
							.showConfirmDialog(page.editor,
									"Are you sure to overwrite?",
									"File exists", JOptionPane.YES_NO_OPTION)) {
						page.message("not saved");
						return false;
					}
				}
				page.fn = fn;
				page.editor.changeTitle();

			} else {
				return false;
			}
		}
		return savePageToFile(page);

	}

	static boolean savePageToFile(PlainPage page) throws Exception {
		System.out.println("save " + page.fn);
		if (page.encoding == null) {
			page.encoding = UTF8;
		}
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(page.fn), page.encoding));
		for (int i = 0; i < page.roLines.getLinesize(); i++) {
			out.write(page.roLines.getline(i).toString());
			out.write("\n");
		}
		out.close();

		return true;
	}

	static void setEncodingByUser(PlainPage plainPage) {
		String s = JOptionPane.showInputDialog(plainPage.editor,
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

	static void showResult(EditWindow editor, List<String> all, String dir,
			String text) throws Exception {
		PlainPage p2 = editor.newFileInNewWindow();
		List<StringBuffer> sbs = new ArrayList<StringBuffer>();
		sbs.add(new StringBuffer(String.format(
				"find %s results in %s for '%s'", all.size(), dir, text)));
		for (Object o : all) {
			sbs.add(new StringBuffer(o.toString()));
		}
		p2.ptEdit.setLines(sbs);
		gc();
	}

	static List<String> split(String s) {
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

	static int strWidth(Graphics2D g2, String s) {
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
					w += TABWIDTH;
					p1 = p2 + 1;
				}
			}
			return w;
		}
	}

	static int drawTwoColor(Graphics2D g2, String s, int x, int y, Color c1,
			Color c2) {
		g2.setColor(c2);
		g2.drawString(s, x + 1, y + 1);
		g2.setColor(c1);
		g2.drawString(s, x, y);
		return g2.getFontMetrics().stringWidth(s);

	}

	/**
	 * quick find how much char can be shown in width
	 * 
	 * @param width
	 * @param g2
	 * @return
	 */
	static int computeShowIndex(String s, int width, Graphics2D g2) {
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

	static void guessComment(PlainPage page) {
		String comment = null;
		String[] commentchars = { "#", "%", "'", "//", "!", ";", "--", "/*",
				"<!--" };
		int[] cnts = new int[commentchars.length];
		for (int i = 0; i < page.roLines.getLinesize(); i++) {
			RoSb sb = page.roLines.getline(i);
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
						break;
					}
				}
			}
		}
		if (comment == null) {
			page.message("no comment found" + Arrays.toString(cnts));
		} else {
			page.message("comment found:" + comment);
		}
		page.ui.comment = comment;
		page.editor.repaint();
	}

	static void findchar(PlainPage page, char ch, int inc, int[] c1, char chx) {
		int cx1 = c1[0];
		int cy1 = c1[1];
		RoSb csb = page.roLines.getline(cy1);
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
						csb = page.roLines.getline(cy1);
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
					if (cy1 >= page.roLines.getLinesize()) {
						c1[0] = -1;
						c1[1] = -1;
						return;
					} else {
						csb = page.roLines.getline(cy1);
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

	static Point find(PlainPage page, String s, int x, int y, boolean ignoreCase) {
		if (ignoreCase) {
			s = s.toLowerCase();
		}
		x = Math.min(x, page.roLines.getline(y).toString(ignoreCase).length());
		// first half row
		int p1 = page.roLines.getline(y).toString(ignoreCase).indexOf(s, x);
		if (p1 >= 0) {
			return new Point(p1, y);
		}
		// middle rows
		int fy = y;
		for (int i = 0; i < page.roLines.getLinesize() - 1; i++) {
			fy += 1;
			if (fy >= page.roLines.getLinesize()) {
				fy = 0;
			}
			p1 = page.roLines.getline(fy).toString(ignoreCase).indexOf(s);
			if (p1 >= 0) {
				return new Point(p1, fy);
			}
		}
		// last half row
		p1 = page.roLines.getline(y).toString(ignoreCase).substring(x).indexOf(
				s);
		if (p1 >= 0) {
			return new Point(p1, fy);
		}
		return null;
	}

	static String subs(RoSb sb, int a, int b) {
		return subs(sb.toString(), a, b);
	}

	static String subs(String sb, int a, int b) {
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

	static String getClipBoard() {
		String s;
		try {
			s = Toolkit.getDefaultToolkit().getSystemClipboard().getData(
					DataFlavor.stringFlavor).toString();
		} catch (Exception e) {
			s = "";
		}
		return s;
	}

	static void setClipBoard(String s) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				new StringSelection(s), null);
	}

	public static String spaces(int cx) {
		StringBuffer sb = new StringBuffer(cx);
		sb.setLength(cx);
		return sb.toString();
	}

	public static String[] split(String s, String sep) {
		List<String> s1 = new ArrayList<String>();
		int p1 = 0;
		while (true) {
			int p2 = s.indexOf(sep, p1);
			if (p2 < 0) {
				s1.add(U.f(s.substring(p1)));
				break;
			} else {
				s1.add(U.f(s.substring(p1, p2)));
				p1 = p2 + 1;
			}
		}
		return (String[]) s1.toArray(new String[s1.size()]);
	}

	public static void saveFileHistory(String fn, int line) throws IOException {
		File fhn = getFileHistoryName();
		if (fhn.getAbsoluteFile().equals(new File(fn).getAbsoluteFile()))
			return;
		OutputStream out = new FileOutputStream(fhn, true);
		out.write(String.format("\n%s|%s:", fn, line).getBytes("utf8"));
		out.close();
	}

	static File getFileHistoryName() throws IOException {
		String home = System.getProperty("user.home");
		File dir = new File(home, ".neoeedit");
		dir.mkdirs();

		File f = new File(dir, "fh.txt");
		if (!f.exists()) {
			new FileOutputStream(f).close();
		}
		return f;
	}

}
