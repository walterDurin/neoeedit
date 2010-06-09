package neoe.ne;

import java.io.File;

public class Main {
	public static void main(String[] args) throws Exception {

		if (args.length > 0) {
			File f = new File(args[0]);
			String fn = f.getName().toLowerCase();
			if (fn.endsWith(".gif") || fn.endsWith(".jpg")
					|| fn.endsWith(".png") || fn.endsWith(".bmp")) {
				new PicView().show(f);
			} else {
				EditPanel editor = new EditPanel(f);
				editor.openWindow();
			}
		} else {
			EditPanel editor = new EditPanel("neoeedit");
			editor.page.ptSelection.selectAll();
			editor.openWindow();
		}

	}
}
