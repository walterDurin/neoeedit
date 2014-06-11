package neoe.ime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import neoe.ime.cn.CharPyLib;
import neoe.ime.cn.WordPyLib;
import neoe.ime.jp.JpWordLib;
import neoe.ime.jp.KanaLib;
import neoe.ime.spi.NeoeInputMethod;

public class ImeImpB implements Ime {

    private static boolean inited = false;

    private NeoeInputMethod method;

    public ImeImpB(NeoeInputMethod method) {
        this.method = method;
        if (!inited) {
            inited = true;
            init();
        }
    }

    public void init() {
        try {
            libaryChar = new CharPyLib();
            libaryWord = new WordPyLib((CharPyLib) libaryChar);
            kanaLib = new KanaLib();
            jpWordLib = new JpWordLib((KanaLib) kanaLib);
            System.out.println("imeB init ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final int MAX_OUT_LEN = 30;

    private static final int MAX_SELECTION = 10;

    private static ImeLib libaryChar;

    private static ImeLib libaryWord;

    private static ImeLib kanaLib;

    private static ImeLib jpWordLib;

    private int start;

    private List result = new ArrayList();

    private String cur = "";

    private int c;

    public void find(String py) {
        int len = py.length();
        cur = py;
        result.clear();
        for (int i = len; i > 0; i--) {
            String sub = py.substring(0, i);
            if (pySelected()) {
                result.addAll(libaryWord.find(sub));
                result.addAll(libaryChar.find(sub));
            } else {// jp selected
                result.addAll(kanaLib.find(sub));
                result.addAll(jpWordLib.find(sub));
            }
        }
        // System.out.println("count=" + result.size());
        start = 0;
    }

    /**
     * @return
     */
    private boolean pySelected() {
        return method.lookup.jcn.isSelected();
    }

    public String out() {
        StringBuffer sb = new StringBuffer();
        c = 0;
        for (; start + c < result.size();) {
            String s = getTxt(start + c);
            String h = c == 9 ? "0" : "" + (c + 1);
            s = h + s + " ";
            c++;
            if (sb.length() + s.length() >= MAX_OUT_LEN) {
                c--;
                break;
            } else {
                sb.append(s);
            }
            if (c >= MAX_SELECTION) {
                break;
            }
        }

        if (start > 0) {
            sb.append("<< ");
        }
        if (start + c < result.size()) {
            sb.append(">> ");
        }
        return sb.toString();
    }

    public void prev() {
        StringBuffer sb = new StringBuffer();
        c = 0;
        for (; start - c >= 0;) {
            String s = getTxt(start - c);
            String h = c == 9 ? "0" : "" + (c + 1);
            s = h + s + " ";
            c++;
            if (sb.length() + s.length() >= MAX_OUT_LEN) {
                c--;
                break;
            } else {
                sb.append(s);
            }
            if (c >= MAX_SELECTION) {
                break;
            }
        }
        if (start - c >= 0) {
            start -= c;
        }
        // System.out.println(start);
    }

    public void next() {
        StringBuffer sb = new StringBuffer();
        c = 0;
        for (; start + c < result.size();) {
            String s = getTxt(start + c);
            String h = c == 9 ? "0" : "" + (c + 1);
            s = h + s + " ";
            c++;
            if (sb.length() + s.length() >= MAX_OUT_LEN) {
                c--;
                break;
            } else {
                sb.append(s);
            }
            if (c >= MAX_SELECTION) {
                break;
            }
        }

        if (start + c < result.size()) {
            start += c;
        }
        // System.out.println(start);
    }

    private String getTxt(int i) {
        if (i >= 0 && i < result.size()) {
            return ((ImeUnit) result.get(i)).txt;
        } else {
            return "NULL";
        }
    }

    public String select(int index) {
        // System.out.println("C=" + c);
        if (index < 0 || index > c - 1) {
            return "";
        }
        index += start;
        String res = "";
        // if (index >= 0 && index < result.size()) {
        ImeUnit u = (ImeUnit) result.get(index);
        res = u.txt;
        int v = cur.length() - u.pylen;
        if (v > 0) {
            cur = cur.substring(cur.length() - v);
            find(cur);
        } else {
            find("");
        }
        // }
        return res;
    }

    public static void main(String[] args) throws IOException {
        ImeImpB b = new ImeImpB(null);
        b.init();
        b.find("yisiniuxiaodong");
        for (int i = 1; i < 10; i++) {
            System.out.println(b.out());
            b.next();
            System.out.println("s=" + b.select(0));
        }
        for (int i = 1; i < 10; i++) {
            System.out.println(b.out());
            b.prev();
            // System.out.println("s="+b.select(0));
        }
    }

    public String getCurrentPy() {
        return cur;
    }

    /*
     * (non-Javadoc)
     * 
     * @see neoe.py.IME#getCount()
     */
    public int getCount() {
        return result.size();
    }

}
