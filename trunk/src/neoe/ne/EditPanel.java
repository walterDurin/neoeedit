package neoe.ne;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class EditPanel extends JComponent implements MouseMotionListener,
        MouseListener, MouseWheelListener, KeyListener {

    private static final long serialVersionUID = -1667283144475200365L;

    private boolean debugFPS = false;

    private EditPanel() {
        setFocusable(true);
        addMouseMotionListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
        setOpaque(false);
        setCursor(new Cursor(Cursor.TEXT_CURSOR));
        setFocusTraversalKeysEnabled(false);
    }

    public EditPanel(File f) throws Exception {
        this();
        page = new PlainPage(this, f);        
    }

    public EditPanel(String text) throws Exception {
        this();
        page = new PlainPage(this, text);
    }

    public void paint(Graphics g) {
        long t1 = 0;
        if (debugFPS) {
            t1 = System.currentTimeMillis();
        }
        try {
            if (page != null) {
                page.xpaint(g, this.getSize());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (debugFPS) {
            System.out.println("p " + (System.currentTimeMillis() - t1));
        }
    }

    PlainPage page;
    JFrame frame;

    public void openWindow() {
        if (frame != null)
            return;
        frame = new JFrame(PlainPage.WINDOW_NAME);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(new Dimension(800, 600));
        frame.getContentPane().add(this);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                PlainPage pp = page;
                pp.closed = true;
                if (pp.fn != null) {
                    try {
                        U.saveFileHistory(pp.fn, pp.cy);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        frame.setVisible(true);
        page.getFindWindow();
        changeTitle();
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent env) {
        page.mouseDragged(env);
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mousePressed(MouseEvent env) {
        page.mousePressed(env);
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent env) {
        page.mouseWheelMoved(env);
    }

    @Override
    public void keyPressed(KeyEvent env) {
        page.keyPressed(env);
    }

    @Override
    public void keyReleased(KeyEvent env) {
        page.keyReleased(env);
    }

    @Override
    public void keyTyped(KeyEvent env) {
        page.keyTyped(env);
    }

    String getWorkPath() {
        return page.workPath;
    }

    void changeTitle() {
        if (frame == null)
            return;
        String fn = page.fn;
        if (fn != null) {
            frame.setTitle(new File(fn).getName() + " "
                    + new File(fn).getParent() + " - " + PlainPage.WINDOW_NAME);
        } else {
            frame.setTitle(PlainPage.WINDOW_NAME);
        }
    }

}