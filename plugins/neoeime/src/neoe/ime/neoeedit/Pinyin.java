/* neoe */
package neoe.ime.neoeedit;

import java.util.ArrayList;
import neoe.ime.ImeLib;
import neoe.ime.cn.CharPyLib;
import neoe.ime.cn.WordPyLib;
import neoe.ne.Ime;

/**
 *
 * @author neoedmund
 */
public class Pinyin extends GeneralIme implements Ime.ImeInterface {
    public static final String NAME = "拼音";
    @Override
    void initLibs() throws Exception {
        libs = new ArrayList();
        ImeLib libaryChar;
        libs.add(libaryChar =new CharPyLib());
        libs.add(new WordPyLib((CharPyLib) libaryChar));
    }

    @Override
    public String getImeName() {
        return NAME;
    }

}
