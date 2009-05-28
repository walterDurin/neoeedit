package neoe.ne;

import java.io.File;

public class PageInfo {
	public long size;

	public PageInfo(String fn, long size) {
		this.fn = fn;
		this.size = size;
	}

	public String fn;
	public Page page;

	public String toString() {
		String s;
		if (fn == null) {
			s = "New Text...";
		} else {
			File f = new File(fn);
			s = f.getName() + " " + f.getParent();
		}
		return s;
	}

}
