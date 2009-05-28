package neoe.ne;

public class RoSb {

	private StringBuffer sb;

	public RoSb(StringBuffer sb) {
		this.sb=sb;
	}

	public int length() {
		return sb.length();
	}

	public char charAt(int i) {
		return sb.charAt(i);
	}

	public String substring(int a, int b) {
		
		return sb.substring(a,b);
	}
	public String toString() {
		return sb.toString();
	}

	public String substring(int i) {
		return sb.substring(i);
	}

	public StringBuffer sb() {		
		return sb;
	}
	
}
