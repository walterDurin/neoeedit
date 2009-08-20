package neoe.ne;

public class Main {
	public static void main(String[] args) throws Exception {
		Editor editor = new Editor();
		if (args.length > 0) {
			editor.openFile(args[0]);
		}else{
			editor.newFile();
		}
		editor.show(true);
	}
}
