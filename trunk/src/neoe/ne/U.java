package neoe.ne;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class U {
	public static final int TABWIDTH = 60;
	public static Image TabImg;
	static{		
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

	private static void loadTabImage() throws Exception {
		BufferedImage img = ImageIO.read(U.class.getResourceAsStream("/icontab.png"));
		TabImg=img.getScaledInstance(TABWIDTH,8,Image.SCALE_SMOOTH);		
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

}
