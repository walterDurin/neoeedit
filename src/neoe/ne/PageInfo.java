package neoe.ne;

import java.io.File;

public class PageInfo {
	public long size;

	public PageInfo(String fn, long size) {
		this.fn = fn;
		this.size = size;
		if (fn!=null){
			this.defaultPath=new File(fn).getParent();
		}
	}

	public String fn;
	public Page page;
	public String defaultPath;

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

	public void initPage(Editor editor) throws Exception {
		if (page == null) {
			if (size < 10000000) {
				page = new PlainPage(editor, this);
			} else {
				page = new LargePage(editor, this);
			}
		}

	}

}
