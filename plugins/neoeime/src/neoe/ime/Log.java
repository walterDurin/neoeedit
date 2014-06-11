package neoe.ime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;

public class Log {

	private static PrintWriter p;

	public static void init() {
		if (p != null) {
			return;
		}
		System.out.println("work dir=" + new File(".").getPath());
		try {
			p = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
					new File(Utils.getUserHomeDir(), "neoeime.log")), "utf8"),
					true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String last = "";
	private static int repeatCnt = 0;

	public static void log(Object src, Object s) {
		String ss = "" + s;
		if (p != null) {
			if (last.equals(ss)) {
				repeatCnt++;
			} else {
				if (repeatCnt > 0) {
					p.println("[repeat " + repeatCnt + "]");
					repeatCnt = 0;
				}
				p.println(src(src) + time() + ss);
				last = ss;
			}

		}
	}

	public static void log(Object src, String s, Exception e) {
		if (p != null) {
			p.println(src(src) + time() + s + " - ");
			e.printStackTrace(p);
		}
	}

	private static String src(Object src) {
		return "[" + src + "]";
	}

	private static String time() {
		return "[" + new Date() + "]";
	}

}
