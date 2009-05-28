package neoe.ne;

import java.util.LinkedList;

public class History {

	public static final char INSERT = 'I';
	public static final char DELETE = 'D';

	public static int MAXSIZE = 200;

	private PlainPage page;
	private LinkedList<HistoryInfo> data;

	public History(PlainPage page) {
		// this.page=page;
		data = new LinkedList<HistoryInfo>();
	}

	public void add(HistoryInfo o) {
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
		}
	}

	public HistoryInfo get() {
		return data.removeLast();
	}

	public void clear() {
		data.clear();
	}

	public int size() {
		return data.size();
	}

}
