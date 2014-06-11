/*
 * Created on 2006-2-25
 *
 * 
 */
package neoe.ime.jp;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import neoe.ime.Consts;
import neoe.ime.ImeLib;
import neoe.ime.ImeUnit;
import neoe.ime.Utils;

/**
 * @author Fantasy
 * 
 * 
 */
public class KanaLib implements ImeLib {
    private static HashMap pyMap;

    private static HashMap pyRevMap;

    public KanaLib() throws IOException {
        if (pyMap == null) {
            pyMap = new HashMap();
           
                        KanaLib.init();
                 
        }
    }

    /**
     * @throws IOException
     * 
     */
    private static void init() throws IOException {
        BufferedReader in = new BufferedReader(Utils
                .getInstalledReader(Consts.KANA_FN));
        String line;
        pyMap = new HashMap();
        pyRevMap = new HashMap();
        while ((line = in.readLine()) != null) {
            line = line.trim();
            String line2 = in.readLine();
            String line3 = in.readLine();
            Utils.putMultiValueMap(pyMap, line3, line);
            Utils.putMultiValueMap(pyMap, line3, line2);
            Utils.putMultiValueMap(pyRevMap, line, line3);
            Utils.putMultiValueMap(pyRevMap, line2, line3);
        }
        in.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see neoe.ime.ImeLib#find(java.lang.String)
     */
    public List find(String py) {
        List res = new ArrayList();
        List l = (List) pyMap.get(py);
        if (l == null) {
            return res;
        } else {
            int len = py.length();
            for (int i = 0; i < l.size(); i++) {
                String s = (String) l.get(i);
                res.add(new ImeUnit(s, len));
            }
        }
        return res;
    }
    String revShow(String kana) {
        StringBuffer sb = new StringBuffer();
        boolean xtu = false;
        for (int i = 0; i < kana.length(); i++) {
            String c = "" + kana.charAt(i);
            if (c.equals("っ")) {
                xtu = true;
                if (i==kana.length()-1){
                    sb.append("xtu");
                }else{
                    continue;
                }
            }
            if (i < kana.length() - 1) {
                String c2 = c + kana.charAt(i + 1);
                List list = (List) pyRevMap.get(c2);
                if (list != null && list.size() > 0) {
                    i++;
                    if (xtu) {
                        xtu = false;
                        sb.append(((String) list.get(0)).charAt(0));
                    }
                    sb.append(list.get(0));
                    continue;
                }

            }
            List list = (List) pyRevMap.get(c);
            if (list != null && list.size() > 0) {
                if (xtu) {
                    xtu = false;
                    sb.append(((String) list.get(0)).charAt(0));
                }
                sb.append(list.get(0));
            } else {
                if (xtu != true) {
                    //System.out.println("cannot convert ["+c+"]");
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

}
