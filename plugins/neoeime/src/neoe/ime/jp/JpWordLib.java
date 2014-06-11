package neoe.ime.jp;

import java.io.IOException;
import java.util.*;

import neoe.ime.Consts;
import neoe.ime.ImeLib;
import neoe.ime.ImeUnit;
import neoe.ime.Utils;

public class JpWordLib implements ImeLib {
    public static void main(String[] args) throws IOException {
        JpWordLib lib = new JpWordLib(new KanaLib());

    }

    private static KanaLib charLib;

    public JpWordLib(KanaLib charLib) throws IOException {
        if (map == null) {
            map = new HashMap();
            JpWordLib.charLib = charLib;
           
                        JpWordLib.init();
                        
        }
    }

    /**
     * @throws IOException
     * 
     */
    protected static void init() throws IOException {
        System.out.print("load jp...");
        long t1 = System.currentTimeMillis();
        map = new HashMap();
        List words = Utils.readLines(Consts.JP_WORDS_FN);
        int wc = 0;
        int wcc = 0;
        for (int i = 0; i < words.size(); i++) {
            String w = (String) words.get(i);
            int p1 = w.indexOf(" ");
            String kanaStr;
            if (p1 > 0) {
                // set spell
                kanaStr = w.substring(p1 + 1);
                w = w.substring(0, p1);
            } else {
                // must be a pure kana word
                kanaStr = w;
            }
            if (kanaStr != null) {
                String[] kanaArr = kanaStr.split("\\／");
                for (int yi = 0; yi < kanaArr.length; yi++) {
                    String kana = kanaArr[yi];
                    String py = charLib.revShow(kana);
                    addImeUnit(w, py, kana);
                }
                wcc++;
            } else {
                System.out.println("drop " + w + " " + (wc++));
            }
        }
        // out.close();
        System.out.println("" + wcc + " words in "
                + (System.currentTimeMillis() - t1) + " ms");

    }

    /**
     * @param kana
     * @param py
     * @param kana
     * @return
     */
    private static void addImeUnit(String w, String py, String kana) {
        int len = py.length();

        char lastc = kana.charAt(kana.length() - 1);
        if (lastc >= 'a' && lastc <= 'z') {
            len--;
            Utils.putMultiValueMap(map, py, new ImeUnit(w, len));
        } else {
            Utils.putMultiValueMap(map, py, new ImeUnit(w, len));
            if (!kana.equals(w)){
                Utils.putMultiValueMap(map, py, new ImeUnit(kana, len));
            }
        }
    }

    private static Map map;

    public List find(String py) {
        List r = (List) map.get(py);
        if (r == null) {
            r = new ArrayList();
        }
        return r;
    }

}
