package neoe.ne;

public class HistoryInfo {
	public char type;
	public int y1;
	public int y2;
	public int x1;
	public int x2;
	public String s;

	public HistoryInfo(char type, int y1, int x1, int x2, String s, int y2) {
		this.type = type;
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.s = s;
	}

	public String toString() {
		return "[" + type + "," + x1 + "," + y1 + "," + x2 + "," + y2 + ","
				+ shorten(s) + "]";
	}

	private String shorten(String s) {
		if (s == null) {
			return null;
		}
		int p1 = s.indexOf("\n");
		if (p1 >= 0) {
			return s.substring(0, p1) + "...(" + s.length() + ")";
		}
		if (s.length() > 60) {
			return s.substring(0, 60) + "...(" + s.length() + ")";
		}
		return s;
	}
}
