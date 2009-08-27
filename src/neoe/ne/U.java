package neoe.ne;

import java.awt.Graphics2D;
import java.awt.Image;
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
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import neoe.ne.PlainPage.PageInfo;

/**
 * util
 */
public class U {
	/**
	 * read-only stringbuffer. changes should done with undo manner.
	 */
	public static class RoSb {

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

		public StringBuffer sb() {
			return sb;
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

	public static Image TabImg;

	public static final int TABWIDTH = 60;

	static {
		try {
			loadTabImage();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String f(String s) {
		if (s.endsWith("\r")) {
			return s.substring(0, s.length() - 1);
		}
		return s;
	}

	public static void gc() {
		System.out.println(km(Runtime.getRuntime().freeMemory()) + "/"
				+ km(Runtime.getRuntime().totalMemory()));
		Runtime.getRuntime().gc();
		System.out.println(km(Runtime.getRuntime().freeMemory()) + "/"
				+ km(Runtime.getRuntime().totalMemory()));
	}

	public static String km(long v) {
		float m = 1024 * 1024f;
		if (v > m) {
			return String.format("%.1fMB", v / m);
		} else if (v > 1024) {
			return String.format("%.1fKB", v / 1024);
		}
		return "" + v;
	}

	private static void loadTabImage() throws Exception {
		BufferedImage img = ImageIO.read(U.class
				.getResourceAsStream("/icontab.png"));
		TabImg = img.getScaledInstance(TABWIDTH, 8, Image.SCALE_SMOOTH);
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

	static String guessEncodingForEditor(String fn){
		try {
			String s = guessEncoding(fn);
			if (s == null) {//unknow
				s = UTF8;
			}
			return s;
		} catch (Exception e) {
			return UTF8;
		}
	}

	static List<StringBuffer> readFileForEditor(String fn, String encoding) {
		List<StringBuffer> lines= new ArrayList<StringBuffer>();
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

	static final String UTF8 = "utf8";

	public static int nextHalf(int i) {
		if (i<=1){
			return i;
		}
		return i/2;
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

	static boolean isAllDigital(String s) {
		for (char c : s.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
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

	static boolean isSkipChar(char ch, char ch1) {
		if (Character.isSpaceChar(ch1) || ch1 == '\t') {
			return Character.isSpaceChar(ch) || ch == '\t';
		} else {
			return Character.isJavaIdentifierPart(ch);
		}
	}

	static boolean saveFile(PageInfo info) throws Exception {
		PlainPage p = (PlainPage) info.page;
		if (info.fn == null) {
			JFileChooser chooser = new JFileChooser(info.workPath);
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

	static boolean savePageToFile(PageInfo info) throws Exception {
		System.out.println("save " + info.fn);
		PlainPage p = (PlainPage) info.page;
		if (p.encoding == null) {
			p.encoding = UTF8;
		}
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(info.fn), p.encoding));
		for(int i=0;i<p.ptEdit.getLinesize();i++){
			out.write(p.ptEdit.getline(i).toString());
			out.write("\n");
		}
		out.close();
	
		return true;
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
							line = line.substring(0, PlainPage.MAX_SHOW_CHARS_IN_LINE)
									+ "...";
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

	static String getIndent(String s) {
		int p = 0;
		while (p < s.length() && (s.charAt(p) == ' ' || s.charAt(p) == '\t')) {
			p += 1;
		}
		return s.substring(0, p);
	}

	public static void log(String s) {
		Date t = new Date();
		System.out.println("[d " + t + "]" + s);
	}
}
