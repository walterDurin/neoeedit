package neoe.ne;

import java.util.Date;

public class Log {

	public static void debug(String s) {
		Date t = new Date();
		System.out.println("[d " + t + "]" + s);
	}

}
