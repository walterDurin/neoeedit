/*
 * Created on 2005-10-22
 *
 * 
 */
package neoe.ime.fix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordSort {

    public static void main(String[] args) throws IOException {
        long t1=System.currentTimeMillis();
        new WordSort().run();
        System.out.println("use time "+(System.currentTimeMillis()-t1)+" ms.");
        System.out.println("program end "+new Date());
    }

    /**
     * @throws IOException
     * 
     */
    private void run() throws IOException {
        initLogFile();
        readChars();
        readWords();
        String dir="D:/mass2/0916/wolf/main";
            //"D:/mass2/nov_chengjisihan/nov_chengjisihan";
        //String dir="D:/mass2/nov_chengjisihan/test";
        learnFromDir(dir, "gbk");
        saveWords();
        closeLogFile();
    }



    /**
     * @throws IOException 
     * 
     */
    private void closeLogFile() throws IOException {
        abondOut.close();        
    }

    /**
     * 
     */
    private void initLogFile() throws IOException {
        abondOut=new BufferedWriter(
                new OutputStreamWriter(
                       new FileOutputStream("abond.txt"),"utf8" ));
        
    }

    /**
     * 
     */
    private void saveWords() throws IOException {
        debug("active words "+vote.size());
        debug("sort...");
        Collections.sort(words, new Comparator() {
            public int compare(Object s1, Object s2) {
                Integer i1 = (Integer) vote.get(s1);
                Integer i2 = (Integer) vote.get(s2);
                int v1=i1==null?0:i1.intValue();
                int v2=i2==null?0:i2.intValue();
                return v2-v1;
            }
        });
        debug("write...");
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("w2.txt"), "UTF8"));
        for (int i = 0; i < words.size(); i++) {
            out.write(words.get(i) + "\n");
        }
        out.close();
        debug("OK");
    }

    /**
     * @param string
     * @param string2
     * @throws IOException
     */
    private void learnFromDir(String dir, String enc) throws IOException {
        File f = new File(dir);
        File[] fs = f.listFiles();
        for (int i = 0; i < fs.length; i++) {
            File af = (File) fs[i];
            debug(af);
            learnFromFile(af, enc);
        }

    }

    /**
     * @param af
     * @param enc
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void learnFromFile(File f, String enc) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(f), enc));
        int i;
        StringBuffer sb = new StringBuffer();
        while ((i = in.read()) != -1) {
            char c = (char) i;
            if (!isRegChar(c)){
                continue;
            }
            sb.append(c);
            if (!find(sb.toString())) {
                if (sb.length() == 1) {
                    debug("skip " + sb);
                } else {
                    learn(sb.substring(0, sb.length() - 1));
                    String left = sb.substring(sb.length() - 1);
                    sb.setLength(0);
                    sb.append(left);
                }
            }
        }
    }
    String regChars;

    private BufferedWriter abondOut;
    /**
     * @param c
     * @return
     */
    private boolean isRegChar(char c) {        
        return regChars.indexOf(c)>=0;
    }

    /**
     * @param string
     * @throws IOException 
     */
    private void learn(String s) throws IOException {
        int p = 0;
        int len = s.length();
        while (p < len) {
            String left = s.substring(0, len - p);
            if (words.contains(left)) {
                inc(left);
                if (p > 0) {
                    String right = s.substring(len - p, len);
                    learn(right);                    
                }
                return;
            }
            p++;
        }
        //debug("abond [" + s + "]");
        abond(s);
    }

    /**
     * @param s
     * @throws IOException 
     */
    private void abond(String s) throws IOException {
        abondOut.write(s+"\n");        
    }

    /**
     * @param left
     */
    private void inc(String s) {
        //debug("inc " + s);
        Integer i = (Integer) vote.get(s);
        int v = i == null ? 0 : i.intValue();
        v++;
        vote.put(s, new Integer(v));
    }

    Map vote = new HashMap();

    List words;

    private int debugLine;

    /**
     * @param string
     * @return
     */
    private boolean find(String s) {
        if (words.contains(s)) {
            return true;
        }
        int size = words.size();
        for (int i = 0; i < size; i++) {
            String w = (String) words.get(i);
            if (w.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param string
     */
    private void debug(Object s) {
        System.out.println("[d]" + s);
    }

    /**
     * 
     */
    private void readWords() throws IOException {
        debug("read words...");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream("words.txt"), "UTF8"));
        String line;
        words = new ArrayList();
        while ((line = in.readLine()) != null) {
            words.add(line.trim());
        }
        in.close();
        debug("read words OK");
    }
    /**
     * @throws IOException 
     * 
     */
    private void readChars() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream("py.tab.txt"), "UTF8"));
        StringBuffer sb=new StringBuffer();
        int i;
        while ((i = in.read()) != -1) {
            char c=(char)i;
            if (c==' '|| (c>='a' && c<='z')|| c=='\r'|| c=='\n'){
                continue;
            }
            sb.append(c);
        }
        in.close();
        regChars=sb.toString();
        debug(regChars);
    }
}
