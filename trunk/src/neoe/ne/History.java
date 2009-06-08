package neoe.ne;

import java.util.LinkedList;

public class History {

	public static final char INSERT = 'I';
	public static final char DELETE = 'D';
	public static final char REPLACEALL = 'R';

	public static int MAXSIZE = 200;

	private PlainPage page;
	private LinkedList<HistoryInfo> data;
	private int p;

	public History(PlainPage page) {
		// this.page=page;
		data = new LinkedList<HistoryInfo>();
		p=0;
	}

	public void add(HistoryInfo o) {
		if (p<data.size() && p>=0){
			for(int i=0;i<data.size()-p;i++){
				data.removeLast();
			}
		}
		//stem.out.println("o   =" + o);
		HistoryInfo last = data.peekLast();
		//stem.out.println("last=" + last);
		if (o.type == INSERT && last != null && last.type == INSERT) {
			if (o.y1 == last.y2 && o.x1 == last.x2) {
				// merge inserts
				last.x2 = o.x2;
				last.y2 = o.y2;
				return;
			}

		} else if (o.type == DELETE && last != null && last.type == DELETE) {
			if (o.y2 == last.y1 && o.x2 == last.x1) {
				last.x1 = o.x1;
				last.y1 = o.y1;
				last.s = o.s + last.s;
				return;
			} else if (last.x1 == o.x1 && last.y1 == o.y1) {
				last.x2 = o.x2;
				last.y2 = o.y2;
				last.s = last.s + o.s;
				return;
			}
		}
		data.add(o);
		if (data.size() > MAXSIZE) {
			data.removeFirst();
		}else{
			p+=1;
		}
	}

	public HistoryInfo get() {
		if (p<=0){
			return null;
		}
		p-=1;
		return data.get(p);
	}
	
	public HistoryInfo getRedo() {
		if (p<data.size()){
			p+=1;
			return data.get(p-1);
		}else{
			return null;
		}
	}

	public void clear() {
		data.clear();
		p=0;
	}

	public int size() {
		return p;
	}
	
	

}
