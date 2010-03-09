package neoe.ne;

import java.io.File;

public class Main {
	public static void main(String[] args) throws Exception {
		EditPanel editor = null;
		if (args.length > 0) {
			editor=new EditPanel(new File(args[0]));
		}else{
			editor=new EditPanel("neoeedit");
			editor.page.ptSelection.selectAll();
		}
		editor.openWindow();
	}
}
