package neoe.ime.cn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import neoe.ime.Consts;
import neoe.ime.ImeLib;
import neoe.ime.ImeUnit;
import neoe.ime.Utils;

public class CharPyLib implements ImeLib {
    public CharPyLib() throws IOException {
        if (pydata == null) {
            pydata = "";
            
                        readPy();
                 
        }
    }

    private static String pydata;

    public List find(String py) {
        String s = finds(py);
        List r = new ArrayList();
        int len = py.length();
        for (int i = 0; i < s.length(); i++) {
            r.add(new ImeUnit("" + s.charAt(i), len));
        }
        return r;
    }

    private static void readPy() throws IOException {
        pydata = Utils.readString(Utils.getInstalledReader(Consts.PY_CHAR_FN));
        System.out.println("pydata inited");
    }

    public static String finds(String a) {
        String cur;
        // System.out.println("find [" + a + "]");
        int p = pydata.indexOf(" " + a + " ");
        if (p < 0) {
            cur = "";
        } else {
            int p2 = pydata.lastIndexOf(" ", p - 1);
            if (p2 < 0) {
                p2 = -1;
            }
            cur = pydata.substring(p2 + 1, p);
        }
        return cur;

    }

    /** char 2 py */
    public static String reverse(String c) {
        int p = pydata.indexOf(c);
        if (p < 0) {
            return null;
        }
        int p2 = pydata.indexOf(" ", p + 1);
        int p3 = pydata.indexOf(" ", p2 + 1);
        return pydata.substring(p2 + 1, p3);
    }
}
