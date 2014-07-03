package neoe.ime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utils {
	public static List readLines(String fn) throws IOException {
		BufferedReader in = new BufferedReader(Utils.getInstalledReader(fn));
		String line;
		Set list = new HashSet();
		while ((line = in.readLine()) != null) {
			line = line.trim();
			list.add(line);
		}
		return new ArrayList(list);
	}

	public static String getMyDir() {
		String dir = getUserHomeDir() + "/.neoeime";
		new File(dir).mkdirs();
		return dir;
	}

	public static Reader getInstalledReader(String fn) throws IOException {
		String installedFn = getMyDir() + "/" + fn;
		if (!new File(installedFn).exists()) {
			try {
				copy(getJarReader(fn), installedFn);
			} catch (IOException e) {
				e.printStackTrace();
				return getJarReader(fn);
			}
		}
		return getFilesystemReader(installedFn);
	}

	private static Reader getFilesystemReader(String fn) throws IOException {
		return new InputStreamReader(new FileInputStream(fn), "utf8");
	}

	/**
	 * @param reader
	 * @param installedFn
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private static void copy(Reader in, String toFn) throws IOException {
		File f = new File(toFn);
		File dir = f.getParentFile();
		dir.mkdirs();
		int i;
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(f), "utf8"));
		while ((i = in.read()) != -1) {
			out.write(i);
		}
		in.close();
		out.close();
	}

	/**
	 * @return
	 */
	public static String getUserHomeDir() {
		return System.getProperty("user.home");
	}

	public static void putMultiValueMap(Map map, Object key, Object value) {
		List list = (List) map.get(key);
		if (list == null) {
			list = new ArrayList();
			map.put(key, list);
		}
		list.add(value);
	}

	/**
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static String readString(Reader inr) throws IOException {
		BufferedReader in = new BufferedReader(inr);
		StringBuffer sb = new StringBuffer();
		int i;
		while ((i = in.read()) != -1) {
			sb.append((char) i);
		}
		in.close();
		return sb.toString();
	}

	private static Reader getJarReader(String fn)
			throws UnsupportedEncodingException {
            if (!fn.startsWith("/"))fn = "/"+fn;
            return new InputStreamReader(Utils.class.getResourceAsStream(fn),"utf8");
	}
}
