package neoe.ne;

import java.io.File;

public class Main {
	
	public static void main(String[] args) throws Exception {
		U.initKeys();
		EditPanel editor = new EditPanel();
		if (args.length > 0) {
			File f = new File(args[0]);
			if (U.isImageFile(f)) {
				new PicView(editor).show(f);
			} else {
				new PlainPage(editor,
						PageData.newFromFile(f.getCanonicalPath()));
				editor.openWindow();
			}
		} else {
			editor.openWindow();
		}
	}
}
