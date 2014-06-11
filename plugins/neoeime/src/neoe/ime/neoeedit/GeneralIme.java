/* neoe */
package neoe.ime.neoeedit;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import neoe.ime.ImeLib;
import neoe.ime.ImeUnit;
import neoe.ne.Ime;
import neoe.ne.U;

/**
 *
 * @author neoedmund
 */
public abstract class GeneralIme implements Ime.ImeInterface {

    protected List<ImeLib> libs;

    boolean inited = false;

    public void init() {
        if (inited) {
            return;
        }
        try {
            inited = true;
            initLibs();

            System.out.println("IME init ok:" + getImeName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List find(String py) {
        if (!inited) {
            init();
        }

        List result = new ArrayList();
        int len = py.length();

        result.clear();
        for (int i = len; i > 0; i--) {
            String sub = py.substring(0, i);

            for (Object o : libs) {
                ImeLib lib = (ImeLib) o;
                result.addAll(lib.find(sub));
            }
        }
        return result;
    }

    abstract void initLibs() throws Exception;

    public void keyPressed(KeyEvent env, Ime.Out param) {
        if (env.isAltDown() || env.isControlDown()) {
            return;
        }
        int kc = env.getKeyCode();
        if (sb.length() > 0 && kc == KeyEvent.VK_ESCAPE) {
            sb.setLength(0);
            param.consumed = true;
            return;
        }
        if (sb.length() > 0 && res.size() > 0 && kc == KeyEvent.VK_PAGE_UP) {
            if (start >= 9) {
                start -= 9;
            }
            param.consumed = true;
        } else if (sb.length() > 0 && res.size() > 0 && kc == KeyEvent.VK_PAGE_DOWN) {//next page 
            if (start + 9 < res.size()) {
                start += 9;
            }
            param.consumed = true;
        }
    }

    public void keyTyped(KeyEvent env, Ime.Out param) {
        if (env.isAltDown() || env.isControlDown()) {
            return;
        }
        char c = env.getKeyChar();

        if (c == KeyEvent.VK_BACK_SPACE) {
            int len = sb.length();
            if (len > 0) {
                sb.setLength(len - 1);
                consumePreedit(param);
            }
        } else if (Character.isLetter(c)) { // ? "'"
            sb.append(Character.toLowerCase(c));
            consumePreedit(param);
        } else if (Character.isDigit(c)) {
            if (sb.length() == 0 || res == null || res.isEmpty()) {
                return;
            }
            int index = c - '0' + start;
            if (index > 0 && index <= res.size()) {
                consumeYield(index - 1, param);
            }
        } else if (c == ' ' || c == '\n') {
            if (sb.length()>0 && (res==null||res.isEmpty())){
                // yield unknow letters
                param.yield = sb.toString();
                param.consumed = true;
                sb.setLength(0);
                return;
            }
            if (sb.length() == 0 || res == null || res.isEmpty()) {
                return;
            }
            consumeYield(start, param);
        } else if (c == '-' && res.size() > 0) {//prev page
            if (start >= 9) {
                start -= 9;
            }
            param.consumed = true;
        } else if (c == '=' && res.size() > 0) {//next page 
            if (start + 9 < res.size()) {
                start += 9;
            }
            param.consumed = true;
        }
    }
    StringBuffer sb = new StringBuffer();

    public void setEnabled(boolean b) {
        if (!inited) {
            new Thread() {
                public void run() {
                    init();
                }
            }.start();
        }
        sb.setLength(0);
    }

    abstract public String getImeName();

    public void paint(Graphics2D g1, Font[] fonts, int cursorX, int cursorY, Rectangle clipBounds) {

        if (res == null || res.isEmpty() || sb.length() == 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g1.create();
        g2.setPaintMode();
        int len = res.size();
        int end = start + 8;
        if (end >= len) {
            end = len - 1;
        }
        if (start > end) {
            start = end;
        }
        int index = 1;
        int maxWidth = 0;
        int curWidth = 0;
        for (int i = start; i <= end; i++) {
            ImeUnit unit = (ImeUnit) res.get(i);
            int w = U.stringWidth(g2, fonts, index + ":" + unit.txt);
            index++;
            curWidth += w;
            if (index % 3 == 1) {
                if (curWidth > maxWidth) {
                    maxWidth = curWidth;
                }
                curWidth = 0;
            }
        }
        if (index % 3 != 1) {
            if (curWidth > maxWidth) {
                maxWidth = curWidth;                                
            }
        }
        int lineCnt = 1 + (end - start) / 3;
        maxWidth += U.charWidth(g2, fonts, ' ') * 2;
        maxWidth = Math.max(maxWidth, U.stringWidth(g2, fonts, sb.toString()));
        maxWidth += 5;
        int lineHeight = fonts[0].getSize();
        int height = lineHeight * (lineCnt + 1) + 5;
        Rectangle box = new Rectangle(cursorX, cursorY, maxWidth, height);
        if (cursorX + maxWidth - clipBounds.x > clipBounds.width) {
            box.x = clipBounds.width + clipBounds.x - maxWidth;
        }
        if (cursorY + height - clipBounds.y > clipBounds.height) {
            box.y = clipBounds.height + clipBounds.y - height;
        }
        g2.setColor(c0);
        g2.fill(box);
        g2.setColor(c1);
        int x = box.x + 2;
        int y = box.y + 2 + lineHeight;
        U.drawString(g2, fonts, sb.toString(), x, y);

        index = 1;
        for (int i = start; i <= end; i++) {
            if (index % 3 == 1) {
                y += lineHeight;
                x = box.x + 2;
            }
            ImeUnit unit = (ImeUnit) res.get(i);
            g2.setColor(c1);
            int w = U.drawString(g2, fonts, index + ":", x, y);
            x += w;
            g2.setColor(c2);
            x += U.drawString(g2, fonts, unit.txt + " ", x, y);
            index++;
        }
        g2.dispose();
    }
    Color c0 = Color.decode("0xaaaaff");
    Color c1 = Color.decode("0x005500");
    Color c2 = Color.decode("0x222222");
    List res;
    int start = 0;

    private void consumePreedit(Ime.Out param) {
        param.consumed = true;
        res = find(sb.toString());
        start = 0;
        if (!res.isEmpty()) {
            ImeUnit unit = (ImeUnit) res.get(0);
            param.preedit = unit.txt;
        } else {
            param.preedit = sb.toString();
        }
    }

    private void consumeYield(int index, Ime.Out param) {
        if (!res.isEmpty()) {
            ImeUnit unit = (ImeUnit) res.get(index);
            param.yield = unit.txt;
            param.consumed = true;
            sb.delete(0, unit.pylen);
            param.preedit = sb.toString();
            if (sb.length() > 0) {
                res = find(sb.toString());
                start = 0;
            }
        } else {
            param.yield = sb.toString();
            param.consumed = true;
            sb.setLength(0);
        }
    }
}
