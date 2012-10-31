package neoe.ne;

import java.io.File;

public class Main {
	public static void main(String[] args) throws Exception {
		EditPanel editor = new EditPanel();
		if (args.length > 0) {
			new PlainPage(editor, PageData.newFromFile(new File(args[0]).getCanonicalPath()));
		} else {			
			editor.getPage().ptSelection.selectAll();			
		}
		editor.openWindow();
	}
}
