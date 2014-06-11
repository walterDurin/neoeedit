package neoe.ime.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import neoe.ime.Consts;
import neoe.ime.Utils;

public class Kana {

    public Kana() throws IOException {
        init();
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new Kana().run();
        System.out.println("end");
    }

    

    private Map pyMap;

    private Map pyRevMap;

    private void run() throws IOException {
        println(show("ka"));
        println(show("yu"));
        //println(show("i"));
        println(revShow("ちゃくじつ"));
        println(revShow("ちゃくがんちゅう"));
        //println(revShow("ちゃっこうだんかい"));
        println(revShow("ちゃっこうだんかいm"));
        println(revShow("ちゃっこうだんかいっかk"));
    }

    private void println(Object s) {
        System.out.println(s);
    }

    private String revShow(String kana) {
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

    private String show(String py) {
        List l = (List) pyMap.get(py);
        if (l == null) {
            return "?";
        } else {
            return l.toString();
        }
    }

    private void init() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                ClassLoader.getSystemResourceAsStream(Consts.KANA_FN), "utf8"));
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

}
