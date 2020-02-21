package editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class RadarWindow extends JInternalFrame {

  private Radar m_child;

  public RadarWindow(LevelWindow parent, Point l) {
    // set frame title, and closable
    super("Radar", false, true, false, false);

    // Just hide the frame when closed, parent class listens for event
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

    // Hide the 'java' icon
    try {
      setFrameIcon(null);
    } catch (Exception e) {
    }

    // Create our child and add it into the frame
    m_child = new Radar(parent);
    getContentPane().add(m_child);

    // Set the default location then show
    /** ** TO DO - Possibly make setLocation dependant on parent size *** */
    setLocation(l.x, l.y);
    pack();
  }

  /**
   * Set the new actual values being displayed, possibly redrawing the radar
   *
   * @param x the new x
   * @param y the new y
   * @param w the new width
   * @param h the new height
   */
  public void setValues(int x, int y, int w, int h) {
    m_child.setValues(x, y, w, h);
  }

  public void repaintRadar() {
    m_child.doRepaint();
  }

  public Image getMapImage() {
    m_child.redrawMapImage();
    return m_child.bufferedImage;
  }
}

class Radar extends JPanel implements MouseListener {

  private LevelWindow m_parent;

  // Represent the physical tile location which is viewed on radar
  private int startx = 0;

  private int starty = 0;

  // Holds the real x/y position and height/width being viewed
  private int rx, ry, rw, rh; // the current variables
  private int lx, ly, lw, lh; // the last update variables... possibly save
  // time by not repainting

  public Image bufferedImage = null; // the last repainted image

  private boolean forceRepaint = true; // should we force a repaint?

  private short[][] bufferedMap = new short[1024][1024];

  // colors
  static Color backgroundColor = new Color(10, 25, 10);

  static Color wallColor = new Color(90, 90, 90);

  public Radar(LevelWindow parent) {

    m_parent = parent;
    setSize(200, 200);
    setPreferredSize(new Dimension(200, 200));
    addMouseListener(this);
    setBackground(Color.black);

    for (int x = 0; x < 1024; ++x) for (int y = 0; y < 1024; ++y) bufferedMap[x][y] = 0;
  }

  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);

    if (forceRepaint || !(rx == lx && ry == ly && rw == lw && rh == lh)) // is a
    // draw
    // necessary?
    {
      forceRepaint = false;

      if (bufferedImage == null) {
        bufferedImage = this.createImage(1024, 1024);
        bufferedImage.getGraphics().setColor(backgroundColor);
        bufferedImage.getGraphics().fillRect(0, 0, 1024, 1024);
      }

      redrawMapImage();

      lx = rx;
      ly = ry;
      lw = rw;
      lh = rh;
    }

    startx = (int) Math.max(0, rx - 50);
    starty = (int) Math.max(0, ry - 50);
    int endx = (int) Math.min(1024, startx + 200);
    int endy = (int) Math.min(1024, starty + 200);

    if (endx == 1024) startx = 824;

    if (endy == 1024) starty = 824;

    // paint the buffer
    graphics.drawImage(bufferedImage, -startx, -starty, null);

    // paint the red rectangle outline of the current view
    graphics.setColor(Color.red);

    int x = m_parent.getStartX();
    int y = m_parent.getStartY();
    int w = m_parent.getEndX() - x;
    int h = m_parent.getEndY() - y;

    graphics.drawRect(x - startx, y - starty, w, h);
  }

  /** Redraw the buffered map image in the locations where it's necessary */
  public void redrawMapImage() {
    Graphics g = bufferedImage.getGraphics();

    // Paint the background

    short[][] m_map = m_parent.m_parent.m_map;

    g.setColor(new Color(90, 90, 90));

    for (int x = 0; x < 1024; x++) {
      for (int y = 0; y < 1024; y++) {
        if (m_map[x][y] != bufferedMap[x][y]) {
          bufferedMap[x][y] = m_map[x][y];

          if (m_map[x][y] >= 162 && m_map[x][y] <= 169) {
            g.setColor(new Color(173, 173, 173));
            g.drawRect(x, y, 0, 0);
          } else if (m_map[x][y] == 171) {
            g.setColor(new Color(24, 82, 24));
            g.drawRect(x, y, 0, 0);
          } else if (m_map[x][y] == 172) {
            g.setColor(new Color(255, 57, 8));
            g.drawRect(x, y, 0, 0);
          } else if (m_map[x][y] > 0 && m_map[x][y] < 191 || m_map[x][y] == 216) {
            g.setColor(wallColor);
            g.drawRect(x, y, 0, 0);
          } else if (m_map[x][y] == 217) {
            g.setColor(wallColor);
            g.drawRect(x, y, 1, 1);
          } else if (m_map[x][y] == 219) {
            g.setColor(new Color(90, 0, 0));
            g.fillRect(x, y, 5, 5);
          } else if (m_map[x][y] == 220) {
            g.setColor(wallColor);
            g.drawRect(x, y, 4, 4);
          } else if (m_map[x][y] == 0) {
            g.setColor(Color.black);
            g.drawRect(x, y, 0, 0);
          }
        }
      }
    }
  }

  /**
   * Set the values for the x, y, width, and height currently being viewed
   *
   * @param x the new x tile being viewed
   * @param y the new y being viewed
   * @param w the new width being viewed
   * @param h the new height being shown
   */
  public void setValues(int x, int y, int w, int h) {
    if (x != rx || y != ry || w != rw || h != rh) {
      rx = x;
      ry = y;
      rw = w;
      rh = h;

      doRepaint();
    }
  }

  static int count = 0;

  /** Force a repaint on this radar */
  public void doRepaint() {
    forceRepaint = true;
    repaint();
  }

  public void mousePressed(MouseEvent e) {

    // Reposition the screen based on radar window click.
    int x = e.getX();
    int y = e.getY();

    m_parent.setPosition(startx + x - (rw - rx) / 2, starty + y - (rh - ry) / 2);
    // m_parent.setPosition( startx+x-100, starty+y-100 );
  }

  public void mouseReleased(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {}
}
