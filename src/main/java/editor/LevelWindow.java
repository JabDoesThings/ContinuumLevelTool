package editor;

import editor.asss.ASSSRegionWindow;
import editor.lvz.LvzImageWindow;

import javax.swing.*;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Vector;

public class LevelWindow extends JPanel
    implements ActionListener, MouseListener, MouseMotionListener, InternalFrameListener {
  // constants
  static final Color wallColor = new Color(90, 90, 90);

  static final Color specialColor = new Color(180, 180, 180);

  static final Color darkGreen = new Color(0, 180, 0);

  static final Color lighter = Color.darkGray.brighter();

  // The parent class for this instance of LevelWindow
  // Used to reference global variables for the editor
  public Editor m_parent;

  public Undoer undoer; // controls undo / redo functions for this
  // levelwindow

  // The child windows this instance of LevelWindow creates
  public RadarWindow m_radar;

  protected TilesetWindow m_tileset;

  public AutoToolWindow m_autotool;

  public LvzImageWindow m_lvzImages;

  public ASSSRegionWindow m_asssRegions;

  // Project state
  public ProjectState m_projectState;

  protected Image[] m_extras;

  protected static Image[] newTiles = Main.newTiles;; // out of range tiles

  // Scaling variables and grid information
  public int scaleList[] = {1, 2, 4, 8, 12, 16, 20, 24};

  // 4 levels of grid to display
  private int gridList[] = {2, 2, 3, 4, 4, 5, 5, 5};

  public int scaleSpot = 5;

  // To know if it should be saved
  public boolean modified = false;

  protected boolean hasFocus = true;

  public int toolX_start = 0;

  public int toolY_start = 0;

  public int toolX_end = 0;

  public int toolY_end = 0;

  // these are in tiles
  public int minX = 0, minY = 0, width = 0, height = 0;

  // Mouse move event variables
  public int lastMouseClick;

  private Point mousePosition;

  private Image zoomToolImage =
      new ImageIcon(
              Main.rootDirectory
                  + File.separator
                  + "include"
                  + File.separator
                  + "Images"
                  + File.separator
                  + "magnifyingglass.gif")
          .getImage();

  private Image dropperImage =
      new ImageIcon(
              Main.rootDirectory
                  + File.separator
                  + "include"
                  + File.separator
                  + "Images"
                  + File.separator
                  + "droppermouse.gif")
          .getImage();

  private Image fillerImage =
      new ImageIcon(
              Main.rootDirectory
                  + File.separator
                  + "include"
                  + File.separator
                  + "Images"
                  + File.separator
                  + "filltool.gif")
          .getImage();

  public int currentTool;

  public Vector selection = null;

  // used for moving selection around
  public int xOffset = -1;

  public int yOffset = -1;

  // used when zooming in and out to assert proper centering
  int correctCenterX;

  int correctCenterY;

  boolean shouldZoomToCorrectCenters = false;

  int resizeCount = 0;

  public LevelWindow(Editor parent, int curTool) {
    // Set to the size of a map at standard zoom factor

    setPreferredSize(new Dimension(1024 * scaleList[scaleSpot], 1024 * scaleList[scaleSpot]));

    // Set the background black
    setBackground(new Color(0, 0, 0));

    m_parent = parent;
    m_extras = m_parent.m_main.extras;

    // Create project state
    m_projectState = parent.m_main.ps;
    // load preferrences into project state

    // Create a radar window for this display instance
    m_radar = new RadarWindow(this, m_projectState.pos_Radar);
    m_radar.addInternalFrameListener(this);
    m_parent.m_main.addComponent(m_radar);

    // Create a tileset window for this display instance
    m_tileset = new TilesetWindow(this, m_projectState.pos_Tileset);
    m_tileset.addInternalFrameListener(this);
    m_parent.m_main.addComponent(m_tileset);

    m_autotool = new AutoToolWindow(this, m_projectState);
    m_autotool.addInternalFrameListener(this);
    m_parent.m_main.addComponent(m_autotool);

    m_lvzImages = new LvzImageWindow(this, m_projectState.pos_LvzImages);
    m_lvzImages.addInternalFrameListener(this);
    m_parent.m_main.addComponent(m_lvzImages);

    m_asssRegions = new ASSSRegionWindow(this, m_projectState.pos_AsssRegions);
    m_asssRegions.addInternalFrameListener(this);
    m_parent.m_main.addComponent(m_asssRegions);

    currentTool = curTool;

    if (currentTool == Tools.ZOOM) {
      setCursor(
          Toolkit.getDefaultToolkit()
              .createCustomCursor(zoomToolImage, new Point(7, 7 + 8), "zoom tool"));
    } else {
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    addMouseListener(this);
    addMouseMotionListener(this);

    undoer = new Undoer(this);
  }

  public void hideWindows() {
    m_radar.setVisible(false);
    m_tileset.setVisible(false);
    m_autotool.setVisible(false);
    m_lvzImages.setVisible(false);
    m_asssRegions.setVisible(false);
  }

  // get's the width of the tile of type "tile" in pixels
  private int getPixelSize(int tile) {
    switch (tile) {
      case 217: // medium asteriod
        return 32;
      case 219: // station
        return 16 * 6;
      case 220: // wormhole
        return 16 * 5;
    }

    return 16;
  }

  /**
   * Get the startX for the current view. This is the x tile that we start painting on
   *
   * @return the startX
   */
  public int getStartX() {
    int scale = scaleList[scaleSpot];

    return (int) Math.max(0, Math.floor(m_parent.getHorizontalScrollBar().getValue() / scale - 5));
  }

  /**
   * Get the startY for the current view. This is the y tile that we start painting on
   *
   * @return the startY
   */
  public int getStartY() {
    int scale = scaleList[scaleSpot];

    return (int) Math.max(0, Math.floor(m_parent.getVerticalScrollBar().getValue() / scale - 5));
  }

  /**
   * Get the endX for the current view. This is the x tile that we end painting on
   *
   * @return the endX
   */
  public int getEndX() {
    int scale = scaleList[scaleSpot];

    return Math.min(1024, getStartX() + 5 + (int) Math.ceil(m_parent.getWidth() / scale));
  }

  /**
   * Get the endY for the current view. This is the y tile that we end painting on
   *
   * @return the endY
   */
  public int getEndY() {
    int scale = scaleList[scaleSpot];

    return Math.min(1024, getStartY() + 5 + (int) Math.ceil(m_parent.getHeight() / scale));
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    // center the zoom if necessary
    if (shouldZoomToCorrectCenters) {
      centerZoom();

      repaint();
    }

    // Determine the area to draw, include 5 tiles extra on the left/top in
    // case of wormholes,etc.
    int scale = scaleList[scaleSpot];
    int startx = getStartX();
    int starty = getStartY();
    int endx = getEndX();
    int endy = getEndY();

    // Paint the background
    g.setColor(new Color(0, 0, 0));
    g.fillRect(startx * scale, starty * scale, endx * scale, endy * scale);

    g.setColor(new Color(100, 100, 100));

    short[][] m_map = m_parent.m_map;
    Image[] m_tiles = m_parent.m_tiles;

    m_lvzImages.paintUnderTilesLvz(g, scale, startx, endx, starty, endy);

    if (currentTool == Tools.RGN) m_asssRegions.paintRegions(g, scale, startx, endx, starty, endy);

    // paint grid
    if (m_parent.m_main.gridOn) drawGrid(g, starty, endy, startx, endx, scale);

    // paint tiles / special tiles
    g.setColor(wallColor);

    for (int x = startx; x < endx; x++) {
      for (int y = starty; y < endy; y++) {
        if (selection != null
            && x >= minX
            && x < minX + selection.size()
            && y >= minY
            && y < minY + ((Vector) selection.get(0)).size()) {
          drawTileInSelection(g, x, y, scale);
        } else if (m_map[x][y] > 0 && m_map[x][y] < 191) {
          if (scale > 4)
            g.drawImage(m_tiles[m_map[x][y] - 1], x * scale, y * scale, scale, scale, this);
          else g.fillRect(x * scale, y * scale, scale, scale);
        } else if (m_map[x][y] == 216) {
          if (scale > 4) g.drawImage(m_extras[0], x * scale, y * scale, scale, scale, this);
          else {
            g.setColor(specialColor);
            g.fillRect(x * scale, y * scale, scale, scale);
            g.setColor(wallColor);
          }
        } else if (m_map[x][y] == 217) {
          if (scale > 4) g.drawImage(m_extras[1], x * scale, y * scale, scale * 2, scale * 2, this);
          else {
            g.setColor(specialColor);
            g.fillRect(x * scale, y * scale, scale * 2, scale * 2);
            g.setColor(wallColor);
          }

          m_map[x + 1][y] = -1;
          m_map[x][y + 1] = -1;
          m_map[x + 1][y + 1] = -1;
        } else if (m_map[x][y] == 219) {
          if (scale > 4) g.drawImage(m_extras[3], x * scale, y * scale, scale * 6, scale * 6, this);
          else {
            g.setColor(specialColor);
            g.fillRect(x * scale, y * scale, scale * 6, scale * 6);
            g.setColor(wallColor);
          }

          for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++) if (i != 0 || j != 0) m_map[x + i][y + j] = -1;
        } else if (m_map[x][y] == 220) {
          if (scale > 4) g.drawImage(m_extras[4], x * scale, y * scale, scale * 5, scale * 5, this);
          else {
            g.setColor(specialColor);
            g.fillRect(x * scale, y * scale, scale * 5, scale * 5);
            g.setColor(wallColor);
          }

          for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) if (i != 0 || j != 0) m_map[x + i][y + j] = -1;
        } else if (m_map[x][y] > 190) {
          Image i = Main.TileIdToImage(m_map[x][y]);

          g.drawImage(i, x * scale, y * scale, scale, scale, this);
        }
      }
    }

    // draw the white selection border
    if (selection != null && selection.size() > 0) {
      int x = minX;
      int w = selection.size();
      int y = minY;
      int h = ((Vector) selection.get(0)).size();

      Color pre = g.getColor();
      g.setColor(Color.white);
      g.drawRect(x * scale, y * scale, w * scale, h * scale);
      g.setColor(pre);
    }

    m_lvzImages.paintAboveTilesLvz(g, scale, startx, endx, starty, endy);

    // draw tool preview
    if (Tools.toolRequiresStart(currentTool) && minX != -1) {
      if (xOffset == -1) // we're not moving a selection around
      {
        Tools.drawToolPreview(g, this);
      }
    }

    // Draw mouse tile box
    // if( hasFocus() )
    // {
    // g.setColor( Color.green );
    // g.fillRect( x_coord*scale, y_coord*scale, scale*2, scale*2 );
    // g.drawImage( m_extras[1], x_coord*scale, y_coord*scale, scale*2,
    // scale*2, this );
    // }

    m_radar.setValues(startx, starty, endx, endy);

    // draw a preview of the special tile
    int selectedTile = m_tileset.getTile(MouseEvent.BUTTON1);
    if (mousePosition != null
        && currentTool == Tools.PEN
        && selectedTile != TilesetWindow.TILE_EZ) {
      if (selectedTile > 216 && selectedTile <= 220) drawSpeicalTilePreview(g, scale);
    }

    // draw selected Lvz image if there is one
    if (currentTool == Tools.LVZ && mousePosition != null) {
      drawLvzPreview(g, scale);
    }

    // draw current region on top if there is one selected
    if (currentTool == Tools.RGN)
      m_asssRegions.paintSelectedRegion(g, scale, startx, endx, starty, endy);
  }

  /**
   * Draw a tile in the selection
   *
   * @param g the graphics to draw with
   * @param x the x tile we're drawing
   * @param y the y tile we're drawing
   * @param scale the # of pixels per tile in the current zoom
   */
  public void drawTileInSelection(Graphics g, int x, int y, int scale) {
    Image[] m_tiles = m_parent.m_tiles;
    int xPos = x - (minX);
    int yPos = y - (minY);

    int tile = ((Integer) ((Vector) selection.get(xPos)).get(yPos)).intValue();

    if (tile > 0 && tile < 191) {
      if (scale > 4) g.drawImage(m_tiles[tile - 1], x * scale, y * scale, scale, scale, this);
      else {
        g.fillRect(x * scale, y * scale, scale, scale);
      }
    } else if (tile == 216) {
      if (scale > 4) {
        Color pre = g.getColor();
        g.setColor(Color.black);
        g.fillRect(x * scale, y * scale, scale, scale);
        g.setColor(pre);

        g.drawImage(m_extras[0], x * scale, y * scale, scale, scale, this);

      } else {
        g.setColor(specialColor);
        g.fillRect(x * scale, y * scale, scale, scale);
        g.setColor(wallColor);
      }
    } else if (tile == 217) {
      if (scale > 4) {
        Color pre = g.getColor();
        g.setColor(Color.black);
        g.fillRect(x * scale, y * scale, scale * 2, scale * 2);
        g.setColor(pre);

        g.drawImage(m_extras[1], x * scale, y * scale, scale * 2, scale * 2, this);
      } else {
        g.setColor(specialColor);
        g.fillRect(x * scale, y * scale, scale * 2, scale * 2);
        g.setColor(wallColor);
      }
    } else if (tile == 219) {
      if (scale > 4) {
        Color pre = g.getColor();
        g.setColor(Color.black);
        g.fillRect(x * scale, y * scale, scale * 6, scale * 6);
        g.setColor(pre);

        g.drawImage(m_extras[3], x * scale, y * scale, scale * 6, scale * 6, this);
      } else {
        g.setColor(specialColor);
        g.fillRect(x * scale, y * scale, scale * 6, scale * 6);
        g.setColor(wallColor);
      }
    } else if (tile == 220) {
      if (scale > 4) {
        Color pre = g.getColor();
        g.setColor(Color.black);
        g.fillRect(x * scale, y * scale, scale * 5, scale * 5);
        g.setColor(pre);

        g.drawImage(m_extras[4], x * scale, y * scale, scale * 5, scale * 5, this);
      } else {

        g.setColor(specialColor);
        g.fillRect(x * scale, y * scale, scale * 5, scale * 5);
        g.setColor(wallColor);
      }
    } else if (tile > 190) {
      if (scale > 4) {
        Image i = Main.TileIdToImage(tile);

        g.drawImage(i, x * scale, y * scale, scale, scale, this);
      } else {
        g.setColor(specialColor);
        g.fillRect(x * scale, y * scale, scale, scale);
        g.setColor(wallColor);
      }
    } else if (!isPartOfBigTileInSelection(xPos, yPos)) {
      Color saved = g.getColor();
      g.setColor(Color.black);
      g.fillRect(x * scale, y * scale, scale, scale);
      g.setColor(saved);
    }
  }

  /**
   * Is this part of a big tile (asteriod, station, wormhole ect) in the selection?
   *
   * @param x the x pos in the selection
   * @param y the y pos in the selection
   * @return true iff it's part of the selection
   */
  private boolean isPartOfBigTileInSelection(int x, int y) {
    boolean found = false;

    for (int counterx = x - 5; counterx <= x && !found; ++counterx)
      for (int countery = y - 5; countery <= y; ++countery) {
        if (counterx >= 0 && countery >= 0) {
          int tile = ((Integer) ((Vector) selection.get(counterx)).get(countery)).intValue();

          if (Tools.specialTileContains(counterx, countery, x, y, tile)) {
            found = true;
            break;
          }
        }
      }

    return found;
  }

  public void drawSpeicalTilePreview(Graphics g, int scale) {
    int centerX = mousePosition.x;
    int centerY = mousePosition.y;
    int tile = m_tileset.getTile(MouseEvent.BUTTON1);
    int w = getPixelSize(tile);
    int h = w;

    double tileW = w / 16.0;
    double tileH = h / 16.0;

    int left = (int) (centerX - scale * tileW / 2);
    int top = (int) (centerY - scale * tileH / 2);

    if (top < 0) top = 0;
    if (left < 0) left = 0;

    if (left + w > 1024 * 16) left = 1024 * 16 - w;

    if (top + h > 1024 * 16) top = 1024 * 16 - h;

    // snap it to a tile
    if (top % scale < scale / 2) top = top - top % scale;
    else top = top + (scale - top % scale);

    if (left % scale < scale / 2) left = left - left % scale;
    else left = left + (scale - left % scale);

    g.setColor(new Color(255, 0, 0));
    g.drawRect(left, top, (int) (scale * tileW), (int) (scale * tileH));
    g.setColor(new Color(255, 100, 100));
    g.drawRect(left + 1, top + 1, (int) (scale * tileW) - 2, (int) (scale * tileH) - 2);
  }

  public void drawLvzPreview(Graphics g, int scale) {
    Image image = m_lvzImages.getSelectedImage();
    if (image != null) {
      int centerX = mousePosition.x;
      int centerY = mousePosition.y;
      int w = image.getWidth(null);
      int h = image.getHeight(null);

      double tileW = w / 16.0;
      double tileH = h / 16.0;

      int left = (int) (centerX - scale * tileW / 2);
      int top = (int) (centerY - scale * tileH / 2);

      if (m_lvzImages.isSnapToTile()) {
        if (top % scale < scale / 2) top = top - top % scale;
        else top = top + (scale - top % scale);

        if (left % scale < scale / 2) left = left - left % scale;
        else left = left + (scale - left % scale);
      }

      g.drawImage(image, left, top, (int) (scale * tileW), (int) (scale * tileH), null);

      g.setColor(Color.magenta);
      g.drawRect(left, top, (int) (scale * tileW), (int) (scale * tileH));
    }
  }

  public void mouseMoved(MouseEvent e) {

    mousePosition = e.getPoint();

    Insets insets = getInsets();
    int x = e.getX() - insets.left;
    int y = e.getY() - insets.top;

    int scale = scaleList[scaleSpot];

    x /= scale;
    y /= scale;

    m_parent.m_main.status.setText("(" + x + ", " + y + ")");

    if (currentTool == Tools.LVZ) {
      Image image = m_lvzImages.getSelectedImage();

      if (image == null) return;

      repaint();
    }

    int selectedTile = m_tileset.getTile(MouseEvent.BUTTON1);

    if (currentTool == Tools.PEN
        && selectedTile > 216
        && selectedTile != TilesetWindow.TILE_EZ) { // user has a special tile selected
      repaint();
    }
  }

  public void drawTileAt(int tile, int x, int y) {
    short m_map[][] = m_parent.m_map;

    if (tile == TilesetWindow.TILE_EZ) {
      Tools.autoSetTile(x, y, m_map, m_autotool.getAutoTileset(), true);
    } else {
      Tools.setTile(x, y, m_map, tile);
    }
  }

  private void drawTile(MouseEvent e, int tile) {
    Insets insets = getInsets();
    int x = e.getX() - insets.left;
    int y = e.getY() - insets.top;

    int scale = scaleList[scaleSpot];

    x /= scale;
    y /= scale;

    // short m_map[][] = m_parent.m_map;

    if (currentTool == Tools.PEN) {
      if (!(tile >= 217 && tile <= 220)) {
        modified = true;
        drawTileAt(tile, x, y);
        // Tools.setTile( x, y, m_map, tile );
      } else
      // special tile, centered
      {
        int centerX = mousePosition.x;
        int centerY = mousePosition.y;
        int w = getPixelSize(tile);
        int h = w;

        double tileW = w / 16.0;
        double tileH = h / 16.0;

        int left = (int) (centerX - scale * tileW / 2);
        int top = (int) (centerY - scale * tileH / 2);

        if (top < 0) top = 0;
        if (left < 0) left = 0;

        if (left + w > 1024 * 16) left = 1024 * 16 - w;

        if (top + h > 1024 * 16) top = 1024 * 16 - h;

        // snap it to a tile
        if (top % scale < scale / 2) top = top - top % scale;
        else top = top + (scale - top % scale);

        if (left % scale < scale / 2) left = left - left % scale;
        else left = left + (scale - left % scale);

        int tileX = left / scale;
        int tileY = top / scale;

        drawTileAt(tile, tileX, tileY);
        // Tools.setTile( tileX, tileY, m_map, tile );
        modified = true;
      }

    } else if (currentTool == Tools.AUTO_PEN) {
      drawTileAt(TilesetWindow.TILE_EZ, x, y);
      // Tools.autoSetTile( x, y, m_map, m_projectState.autoTileset, true
      // );
      modified = true;
    }

    repaint();
  }

  /*
   * private void autoFill( int x, int y, boolean cont ) {
   *
   * short m_map[][] = m_parent.m_map; boolean above = false, below = false,
   * left = false, right = false; if( m_map[x][y-1] >= 0 ) above = true; if(
   * m_map[x][y+1] >= 0 ) below = true; if( m_map[x-1][y] >= 0 ) left = true;
   * if( m_map[x+1][y] >= 0 ) right = true; }
   */

  public void mouseDragged(MouseEvent e) {
    drawTile(e, m_tileset.getTile(lastMouseClick));

    Insets insets = getInsets();
    int x = e.getX() - insets.left;
    int y = e.getY() - insets.top;

    int scale = scaleList[scaleSpot];

    x /= scale;
    y /= scale;

    toolX_end = x;
    toolY_end = y;

    m_parent.m_main.status.setText("(" + x + ", " + y + ")");

    if (Tools.toolRequiresStart(currentTool)) {
      // save spot

      if (xOffset != -1) // we're moving selection
      {
        minX = (x - xOffset);
        minY = (y - yOffset);

        repaint();
      } else {
        minX = (int) Math.min(x, toolX_start);
        minY = (int) Math.min(y, toolY_start);
        width = (int) Math.abs(x - toolX_start);
        height = (int) Math.abs(y - toolY_start);
      }
    }
  }

  /**
   * Draw the grid
   *
   * @param g the graphics object to use
   * @param starty the top y tile to draw
   * @param endy the bottom y tile to draw
   * @param startx the left x tile to draw
   * @param endx the right x tile to draw
   * @param scale the current scale of the zoom
   */
  private void drawGrid(Graphics g, int starty, int endy, int startx, int endx, int scale) {
    int gridDetail = gridList[scaleSpot]; // 1-4 different grids to draw

    Color pre = g.getColor();
    boolean draw;
    for (int y = starty; y < endy; y++) {
      draw = false;

      if (y % 512 == 0 && gridDetail > 0) {
        g.setColor(Color.red);
        draw = true;
      } else if (y % 128 == 0 && gridDetail > 1) {
        g.setColor(Color.blue);
        draw = true;
      } else if (y % 32 == 0 && gridDetail > 2) {
        g.setColor(darkGreen);

        draw = true;
      } else if (y % 8 == 0 && gridDetail > 3) {
        g.setColor(lighter);
        draw = true;
      } else if (gridDetail > 4) {
        g.setColor(Color.darkGray);
        draw = true;
      }

      if (draw) g.drawLine(startx * scale, y * scale, endx * scale, y * scale);
    }

    for (int x = startx; x < endx; x++) {
      draw = false;

      if (x % 512 == 0 && gridDetail > 0) {
        g.setColor(Color.red);
        draw = true;
      } else if (x % 128 == 0 && gridDetail > 1) {
        g.setColor(Color.blue);
        draw = true;
      } else if (x % 32 == 0 && gridDetail > 2) {
        g.setColor(darkGreen);
        draw = true;
      } else if (x % 8 == 0 && gridDetail > 3) {
        g.setColor(lighter);
        draw = true;
      } else if (gridDetail > 4) {
        g.setColor(Color.darkGray);
        draw = true;
      }

      if (draw) g.drawLine(x * scale, starty * scale, x * scale, endy * scale);
    }

    // erase grid over selection
    if ((currentTool == Tools.RGN) && minX != -1) {
      g.setColor(Color.black);
      g.fillRect(minX * scale, minY * scale, width * scale, height * scale);
    }

    g.setColor(pre);
  }

  private void handleZoomClicked(MouseEvent e) {
    int scale = scaleList[scaleSpot];

    int tileX = e.getX() / scale;
    int tileY = e.getY() / scale;

    if (e.getButton() == MouseEvent.BUTTON1) zoomIn();
    else zoomOut();

    // and center the zoom
    centerZoomAboutTile(tileX, tileY);
  }

  /** actually move the scroll pane borders to center the zoom */
  private void centerZoom() {
    Dimension size = this.m_parent.getSize();
    int scale = scaleList[scaleSpot];

    int w = (size.width / scale);
    int h = (size.height / scale);

    int x = getStartX() + w / 2;
    int y = getStartY() + h / 2;

    if (resizeCount > 10 || (Math.abs(x - correctCenterX) < 5 && Math.abs(y - correctCenterY) < 5))
      shouldZoomToCorrectCenters = false;
    else { // move scroll pane's bars
      JScrollBar xBar = m_parent.getHorizontalScrollBar();
      JScrollBar yBar = m_parent.getVerticalScrollBar();

      int maxX = xBar.getMaximum() - xBar.getModel().getExtent();
      int xValue = (int) (maxX * ((double) correctCenterX / 1024.0));
      xBar.setValue(xValue);

      int maxY = yBar.getMaximum() - yBar.getModel().getExtent();
      int yValue = (int) (maxY * ((double) correctCenterY / 1024.0));
      yBar.setValue(yValue);

      resizeCount++;

      revalidate();
    }
  }

  /**
   * make the zoom center about this tile
   *
   * @param x the x tile
   * @param y the y tile
   */
  public void centerZoomAboutTile(int x, int y) {
    int scale = scaleList[scaleSpot];

    Dimension size = this.m_parent.getSize();

    int w = (size.width / scale);
    int h = (size.height / scale);

    if (x > 1023 - w / 2) x = 1023 - w / 2;
    if (x < w / 2) x = w / 2;
    if (y > 1023 - h / 2) y = 1023 - h / 2;
    if (y < h / 2) y = h / 2;

    correctCenterX = x;
    correctCenterY = y;
    shouldZoomToCorrectCenters = true;
    resizeCount = 0;
  }

  public void mouseReleased(MouseEvent e) {
    if (currentTool == Tools.PEN
        || currentTool == Tools.AUTO_PEN
        || currentTool == Tools.LINE
        || currentTool == Tools.SELECT
        || currentTool == Tools.SQUARE
        || currentTool == Tools.SQUAREFILL
        || currentTool == Tools.ELLIPSE
        || currentTool == Tools.ELLIPSEFILL) {
      m_radar.repaintRadar();
    }

    if (Tools.toolRequiresStart(currentTool)) {
      Insets insets = getInsets();
      int x = e.getX() - insets.left;
      int y = e.getY() - insets.top;

      int scale = scaleList[scaleSpot];

      x /= scale;
      y /= scale;

      minX = (int) Math.min(x, toolX_start);
      minY = (int) Math.min(y, toolY_start);
      width = (int) Math.abs(x - toolX_start);
      height = (int) Math.abs(y - toolY_start);

      if (selection != null) { // we're just moving a selection around
        minX = (x - xOffset);
        minY = (y - yOffset);
      } else {
        Tools.endDrag(this);
      }
    }

    undoer.snapShot(this);
  }

  public void clearDrag() {
    minX = -1;
  }

  public void mousePressed(MouseEvent e) {
    lastMouseClick = e.getButton();
    drawTile(e, m_tileset.getTile(e.getButton()));

    if (Tools.toolRequiresStart(currentTool)) {
      // save spot

      Insets insets = getInsets();
      int x = e.getX() - insets.left;
      int y = e.getY() - insets.top;

      int scale = scaleList[scaleSpot];

      x /= scale;
      y /= scale;
      toolX_start = x;
      toolY_start = y;
      toolX_end = x;
      toolY_end = y;

      if (selection != null) { // x and y are in tiles
        if (x >= minX
            && x < minX + selection.size()
            && y >= minY
            && y < minY + ((Vector) selection.get(0)).size()) { // inside selection, we're moving it
          xOffset = x - minX;
          yOffset = y - minY;

        } else {
          placeSelection();
          selection = null;
          m_parent.m_main.editCopy.setEnabled(false);
          m_parent.m_main.editCut.setEnabled(false);
          xOffset = -1;
          yOffset = -1;
          width = 0;
          height = 0;
        }
      }
    }

    if (currentTool == Tools.ZOOM) {
      handleZoomClicked(e);
    } else if (currentTool == Tools.EYEDROP) {
      Insets insets = getInsets();
      int x = e.getX() - insets.left;
      int y = e.getY() - insets.top;

      int scale = scaleList[scaleSpot];

      x /= scale;
      y /= scale;

      if (x < 0) x = 0;
      else if (x > 1023) x = 1023;

      if (y < 0) y = 0;
      else if (y > 1023) y = 1023;

      int tile = m_parent.m_map[x][y];

      if (tile > 0 && tile < 191) {
        m_tileset.setTile(lastMouseClick, tile);
        m_parent.m_main.buttonPen.doClick();
      }
    } else if (currentTool == Tools.LVZ && mousePosition != null) {
      mousePosition = e.getPoint();

      Image image = m_lvzImages.getSelectedImage();
      if (image != null) {
        int scale = scaleList[scaleSpot];
        // scale = pixels per tile

        int centerX = mousePosition.x;
        int centerY = mousePosition.y;
        int w = image.getWidth(null);
        int h = image.getHeight(null);

        double tileW = w / 16.0;
        double tileH = h / 16.0;

        int left = (int) (centerX - scale * tileW / 2);
        int top = (int) (centerY - scale * tileH / 2);

        if (m_lvzImages.isSnapToTile()) {
          if (top % scale < scale / 2) top = top - top % scale;
          else top = top + (scale - top % scale);

          if (left % scale < scale / 2) left = left - left % scale;
          else left = left + (scale - left % scale);
        }

        int xPixel = (16 * left / scale);
        int yPixel = (16 * top / scale);

        m_lvzImages.addMapObject(xPixel, yPixel);
      }
    } else if (currentTool == Tools.LVZ_SELECTION) {
      mousePosition = e.getPoint();

      int scale = scaleList[scaleSpot];
      // scale = pixels per tile

      int x = 16 * mousePosition.x / scale;
      int y = 16 * mousePosition.y / scale;
      m_lvzImages.selectedMO.clear();

      m_lvzImages.selectLvz(x, y);
    } else if (currentTool == Tools.FILL) {
      mousePosition = e.getPoint();

      int scale = scaleList[scaleSpot];

      int x = mousePosition.x / scale;
      int y = mousePosition.y / scale;

      if (m_parent.m_map[x][y] != m_tileset.getTile(e.getButton())
          && m_tileset.getTile(e.getButton()) != 216
          && m_tileset.getTile(e.getButton()) != 217
          && m_tileset.getTile(e.getButton()) != 219
          && m_tileset.getTile(e.getButton()) != 220) // bad tiles
      // suck.
      {
        short originalTile = m_parent.m_map[x][y];
        Vector tiles = new Vector();
        tiles.add(new Point(x, y));

        while (tiles.size() != 0) {
          Point cur = (Point) tiles.lastElement();
          tiles.remove(tiles.size() - 1);

          if (m_parent.m_map[cur.x][cur.y] == originalTile) {
            drawTileAt(m_tileset.getTile(e.getButton()), cur.x, cur.y);

            if (cur.x < 1023 && m_parent.m_map[cur.x + 1][cur.y] == originalTile) // right
            // side
            {
              tiles.add(new Point(cur.x + 1, cur.y));
            }

            if (cur.x > 0 && m_parent.m_map[cur.x - 1][cur.y] == originalTile) // left
            // side
            {
              tiles.add(new Point(cur.x - 1, cur.y));
            }

            if (cur.y > 0 && m_parent.m_map[cur.x][cur.y - 1] == originalTile) // top
            {
              tiles.add(new Point(cur.x, cur.y - 1));
            }

            if (cur.y < 1023 && m_parent.m_map[cur.x][cur.y + 1] == originalTile) // bottop
            // side
            {
              tiles.add(new Point(cur.x, cur.y + 1));
            }
          }
        } // end while
      }
    }
  }

  public void mouseExited(MouseEvent e) {
    m_parent.m_main.status.setText("");
    mousePosition = null;
    repaint();
  }

  public void mouseEntered(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {}

  public void internalFrameOpened(InternalFrameEvent e) {}

  public void internalFrameIconified(InternalFrameEvent e) {}

  public void internalFrameDeiconified(InternalFrameEvent e) {}

  public void internalFrameDeactivated(InternalFrameEvent e) {}

  public void internalFrameClosing(InternalFrameEvent e) {
    if (!hasFocus) return;

    if (e.getInternalFrame() == m_radar) {
      m_projectState.showRadar = m_projectState.m_radar = false;
      m_parent.m_main.windowRadar.setEnabled(true);
    } else if (e.getInternalFrame() == m_tileset) {
      m_projectState.m_tileset = false;
      m_parent.m_main.windowTileset.setEnabled(true);
    } else if (e.getInternalFrame() == m_autotool) {
      m_projectState.m_autotool = false;
      m_parent.m_main.windowAutoTile.setEnabled(true);
    } else if (e.getInternalFrame() == m_lvzImages) {
      m_projectState.m_lvzImages = false;
      m_parent.m_main.windowLvz.setEnabled(true);
    } else if (e.getInternalFrame() == m_asssRegions) {
      m_projectState.m_asssRegions = false;
      m_parent.m_main.windowAsssRegions.setEnabled(true);
    }
  }

  public void internalFrameClosed(InternalFrameEvent e) {}

  public void internalFrameActivated(InternalFrameEvent e) {}

  public void setPosition(int i, int j) {
    int scale = scaleList[scaleSpot];
    JScrollBar jscrollbar = m_parent.getHorizontalScrollBar();
    JScrollBar jscrollbar1 = m_parent.getVerticalScrollBar();
    jscrollbar.setValue(i * scale);
    jscrollbar1.setValue(j * scale);
    repaint();
  }

  public void gotFocus() {
    undoer.setButtons(this);

    m_radar.setVisible(m_projectState.m_radar);
    m_tileset.setVisible(m_projectState.m_tileset);
    m_lvzImages.setVisible(m_projectState.m_lvzImages);
    m_autotool.setVisible(m_projectState.m_autotool);
    hasFocus = true;
  }

  public void lostFocus() {
    undoer.setButtons(this);

    hasFocus = false;
    m_radar.setVisible(false);
    m_tileset.setVisible(false);
    m_lvzImages.setVisible(false);
    m_autotool.setVisible(false);
  }

  public boolean zoomIn() {
    scaleSpot++;
    if (scaleSpot >= 7) {
      scaleSpot = 7;
      reSizeAndValidate();
      return false;
    }
    reSizeAndValidate();
    return true;
  }

  public boolean zoomOut() {
    scaleSpot--;
    if (scaleSpot <= 0) {
      scaleSpot = 0;
      reSizeAndValidate();
      return false;
    }
    reSizeAndValidate();
    return true;
  }

  private void reSizeAndValidate() {
    setPreferredSize(new Dimension(1024 * scaleList[scaleSpot], 1024 * scaleList[scaleSpot]));
    setSize(1024 * scaleList[scaleSpot], 1024 * scaleList[scaleSpot]);
    // m_parent.revalidate();
    revalidate();
  }

  /** Place the selection onto the map */
  public void placeSelection() {
    int x = minX;
    int y = minY;

    if (x < 0) x = 0;
    if (y < 0) y = 0;

    for (int cx = 0; cx < selection.size() && x + cx < 1024; ++cx) {
      Vector col = (Vector) selection.get(cx);
      for (int cy = 0; cy < col.size() && y + cy < 1024; ++cy) {
        int tile = ((Integer) col.get(cy)).intValue();

        if (tile == 217) // 2 tiles needed
        {
          if (x + cx + 1 > 1023) tile = 0;
          else if (y + cy + 1 > 1023) tile = 0;
        } else if (tile == 219) // 6 tiles needed
        {
          if (x + cx + 5 > 1023) tile = 0;
          else if (y + cy + 5 > 1023) tile = 0;
        } else if (tile == 220) // 5 tiles needed
        {
          if (x + cx + 4 > 1023) tile = 0;
          else if (y + cy + 4 > 1023) tile = 0;
        }

        m_parent.m_map[x + cx][y + cy] = (short) tile;
      }
    }

    m_radar.repaintRadar();
    selection = null;

    undoer.snapShot(this);
  }

  /**
   * A new tool was selected, do any events we have to before it gets selected
   *
   * @param i the integer representing the tool that was selected
   */
  public void setTool(int i) {
    if (selection != null) // there is a selection, so place it
    {
      placeSelection();
      selection = null;
      m_parent.m_main.editCopy.setEnabled(false);
      m_parent.m_main.editCut.setEnabled(false);
    }

    currentTool = i;

    m_lvzImages.selectedMO.clear();
    minX = -1;
    minY = 0;
    width = 0;
    height = 0;
    xOffset = -1;
    yOffset = -1;

    // windowRadar, windowTileset, windowAutoTile, windowLvz,
    // windowAsssRegions;

    m_parent.m_main.windowAsssRegions.setEnabled(true);
    m_asssRegions.setVisible(false);

    m_parent.m_main.windowAutoTile.setEnabled(true);
    m_autotool.setVisible(false);

    m_parent.m_main.windowLvz.setEnabled(true);
    m_lvzImages.setVisible(false);

    m_parent.m_main.windowTileset.setEnabled(true);
    m_tileset.setVisible(false);

    switch (i) {
      case Tools.AUTO_PEN:
        {
          m_parent.m_main.windowTileset.setEnabled(false);
          m_tileset.setVisible(true);

          m_parent.m_main.windowAutoTile.setEnabled(false);
          m_autotool.setVisible(true);

          break;
        }
      case Tools.LVZ:
        {
          m_parent.m_main.windowLvz.setEnabled(false);
          m_lvzImages.setVisible(true);
          break;
        }
      case Tools.LVZ_SELECTION:
        {
          m_parent.m_main.windowLvz.setEnabled(false);
          m_lvzImages.setVisible(true);
          break;
        }
      case Tools.PEN:
        {
          m_parent.m_main.windowTileset.setEnabled(false);
          m_tileset.setVisible(true);
          break;
        }
      case Tools.RGN:
        {
          m_parent.m_main.windowAsssRegions.setEnabled(false);
          m_asssRegions.setVisible(true);
          break;
        }
      case Tools.LINE:
        {
          m_parent.m_main.windowTileset.setEnabled(false);
          m_tileset.setVisible(true);
          break;
        }
      case Tools.SQUARE:
        {
          m_parent.m_main.windowTileset.setEnabled(false);
          m_tileset.setVisible(true);
          break;
        }
      case Tools.SQUAREFILL:
        {
          m_parent.m_main.windowTileset.setEnabled(false);
          m_tileset.setVisible(true);
          break;
        }
      case Tools.ELLIPSE:
        {
          m_parent.m_main.windowTileset.setEnabled(false);
          m_tileset.setVisible(true);
        }
      case Tools.ELLIPSEFILL:
        {
          m_parent.m_main.windowTileset.setEnabled(false);
          m_tileset.setVisible(true);
        }
      case Tools.FILL:
        {
          m_parent.m_main.windowTileset.setEnabled(false);
          m_tileset.setVisible(true);
        }
    }

    // cursor
    if (i == Tools.ZOOM) {
      setCursor(
          Toolkit.getDefaultToolkit()
              .createCustomCursor(zoomToolImage, new Point(7, 7 + 8), "zoom tool"));
    } else if (i == Tools.EYEDROP) {
      setCursor(
          Toolkit.getDefaultToolkit()
              .createCustomCursor(dropperImage, new Point(3, 21 + 8), "dropper tool"));
    } else if (i == Tools.FILL) {
      setCursor(
          Toolkit.getDefaultToolkit()
              .createCustomCursor(fillerImage, new Point(3, 21 + 8), "Fill Tool"));

    } else {
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    repaint();
  }

  /** Place the selected map part onto the screen, called by paste */
  public void putSelectionOnScreen() {
    int scale = scaleList[scaleSpot];
    int startx =
        (int) Math.max(0, Math.floor(m_parent.getHorizontalScrollBar().getValue() / scale));
    int starty = (int) Math.max(0, Math.floor(m_parent.getVerticalScrollBar().getValue() / scale));

    minX = startx;
    minY = starty;
  }

  public ProjectState getProjectState() {
    // update position values
    m_projectState.pos_AsssRegions = m_asssRegions.getLocation();
    m_projectState.pos_Autotool = m_autotool.getLocation();
    m_projectState.pos_LvzImages = m_lvzImages.getLocation();
    m_projectState.pos_Radar = m_radar.getLocation();
    m_projectState.pos_Tileset = m_tileset.getLocation();

    return m_projectState;
  }

  public void showWindow(int window) {

    if (window == Main.RADAR_WINDOW) {
      m_radar.setVisible(true);
      m_projectState.showRadar = m_projectState.m_radar = true;
    } else if (window == Main.TILESET_WINDOW) m_tileset.setVisible(true);
    else if (window == Main.AUTOTILE_WINDOW) m_autotool.setVisible(true);
    else if (window == Main.LVZ_WINDOW) m_lvzImages.setVisible(true);
    else if (window == Main.REGION_WINDOW) m_asssRegions.setVisible(true);
  }

  public void actionPerformed(ActionEvent ae) {
    // System.out.println( ae.getSource() );
  }

  public void deletePressed() {
    if (currentTool == Tools.LVZ_SELECTION) {
      m_lvzImages.deleteSelection();
    }
  }
}
