package neoe.ne;

public class Main {
	public static void main(String[] args) throws Exception {
		EditWindow editor = new EditWindow();
		if (args.length > 0) {
			editor.openFile(args[0]);
		}else{
			editor.newEmptyFile(null);
		}
		editor.show(true);
	}
}
