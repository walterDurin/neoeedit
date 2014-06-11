/* neoe */
package neoe.ime.neoeedit;

import java.util.ArrayList;
import neoe.ime.ImeLib;
import neoe.ime.jp.JpWordLib;
import neoe.ime.jp.KanaLib;
import neoe.ne.Ime;

/**
 *
 * @author neoedmund
 */
public class Jp extends GeneralIme implements Ime.ImeInterface {


    public static final String NAME = "日本語";

    @Override
    void initLibs() throws Exception {
        libs = new ArrayList();
        ImeLib kanaLib = new KanaLib();
        ImeLib jpWordLib = new JpWordLib((KanaLib) kanaLib);
        libs.add(kanaLib);
        libs.add(jpWordLib);
    }

    @Override
    public String getImeName() {
        return NAME;
    }
 

}
