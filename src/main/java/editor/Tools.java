package editor;

import editor.lvz.MapObject;

import java.awt.*;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class Tools {

  // Tool statics
  public static final int PEN = 1;

  public static final int SELECT = 2;

  public static final int EYEDROP = 3;

  public static final int AUTO_PEN = 4;

  public static final int LVZ = 5;

  public static final int LVZ_SELECTION = 6;

  public static final int ZOOM = 7;

  public static final int RGN = 8;

  public static final int LINE = 9;

  public static final int SQUARE = 10;

  public static final int SQUAREFILL = 11;

  public static final int ELLIPSE = 12;

  public static final int ELLIPSEFILL = 13;

  public static final int FILL = 14;

  // Auto tile statics
  public static final int TILE_TOP_LEFT = 1;

  public static final int TILE_TOP_CENTER = 2;

  public static final int TILE_TOP_RIGHT = 3;

  public static final int TILE_MIDDLE_LEFT = 4;

  public static final int TILE_MIDDLE_CENTER = 5;

  public static final int TILE_MIDDLE_RIGHT = 6;

  public static final int TILE_BOTTOM_LEFT = 7;

  public static final int TILE_BOTTOM_CENTER = 8;

  public static final int TILE_BOTTOM_RIGHT = 9;

  public static final int TILE_VERT_TOP = 10;

  public static final int TILE_VERT_MIDDLE = 11;

  public static final int TILE_VERT_BOTTOM = 12;

  public static final int TILE_HORZ_LEFT = 13;

  public static final int TILE_HORZ_CENTER = 14;

  public static final int TILE_HORZ_RIGHT = 15;

  public static final int TILE_SINGLE = 16;

  public static boolean toolRequiresStart(int currentTool) {

    if (currentTool == SELECT
        || currentTool == RGN
        || currentTool == LINE
        || currentTool == SQUARE
        || currentTool == SQUAREFILL
        || currentTool == ELLIPSE
        || currentTool == ELLIPSEFILL
        || currentTool == LVZ_SELECTION) return true;

    return false;
  }

  public static Color getToolColor(LevelWindow lw, int tool) {
    if (tool == SELECT) return Color.white;
    else if (tool == RGN) {
      return lw.m_asssRegions.getSelectedRegionColor();
    }

    return null;
  }

  public static void endDrag(LevelWindow lw) { // values are in tiles

    int tool = lw.currentTool;
    int startX = lw.minX;
    int startY = lw.minY;
    int width = lw.width;
    int height = lw.height;

    // make everything legal
    if (startX < 0) startX = 0;
    else if (startX > 1023) startX = 1023;

    if (startY < 0) startY = 0;
    else if (startY > 1023) startY = 1023;

    if (startX + width > 1024) width = 1024 - startX;

    if (startY + height > 1024) height = 1024 - startY;

    if (tool == RGN) {
      regionDragEnded(lw, startX, startY, width, height);
    } else if (tool == SELECT) {
      selectionDragEnded(lw, startX, startY, width, height);
    } else if (tool == LINE) {
      lineDragEnded(lw);
    } else if (tool == SQUARE) {
      squareDragEnded(lw);
    } else if (tool == SQUAREFILL) {
      squareFillDragEnded(lw);
    } else if (tool == ELLIPSE) {
      ellipseDragEnded(lw);
    } else if (tool == ELLIPSEFILL) {
      ellipseFillDragEnded(lw);
    } else if (tool == LVZ_SELECTION) {
      lvzDragEnded(lw);
    }
  }

  /**
   * A square drag ended
   *
   * @param lw the levelwindow we're drawing on
   */
  private static void lvzDragEnded(LevelWindow lw) {
    if (lw.m_parent.m_main.isLVZdragEnabled()) {
      int width = (lw.toolX_end - lw.toolX_start) * 16;
      int height = (lw.toolY_end - lw.toolY_start) * 16;

      Set k = lw.m_lvzImages.selectedMO.keySet();
      Iterator i = k.iterator();
      while (i.hasNext()) {
        MapObject mo = (MapObject) i.next();
        mo.x += width;
        mo.y += height;
        Rectangle rect = (Rectangle) lw.m_lvzImages.selectedMO.get(mo);
        rect.x += width;
        rect.y += height;
      }
      lw.repaint();
    }
  }

  private static void squareDragEnded(LevelWindow lw) {
    Point p1 = new Point(lw.toolX_start, lw.toolY_start);
    Point p2 = new Point(lw.toolX_end, lw.toolY_end);

    if (p1.x > p2.x) {
      int temp = p2.x;
      p2.x = p1.x;
      p1.x = temp;
    }

    if (p1.y > p2.y) {
      int temp = p2.y;
      p2.y = p1.y;
      p1.y = temp;
    }

    lw.modified = true;

    for (int x = p1.x; x <= p2.x; ++x) {
      if (x < 0 || x > 1024) continue;

      int tile = lw.m_tileset.getTile(lw.lastMouseClick);

      if (p1.y >= 0 || p1.y < 1024) lw.drawTileAt(tile, x, p1.y);

      if (p2.y >= 0 || p2.y < 1024) lw.drawTileAt(tile, x, p2.y);
    }

    for (int y = p1.y; y <= p2.y; ++y) {
      if (y < 0 || y > 1024) continue;

      int tile = lw.m_tileset.getTile(lw.lastMouseClick);

      if (p1.x >= 0 || p1.x < 1024) lw.drawTileAt(tile, p1.x, y);

      if (p2.x >= 0 || p2.x < 1024) lw.drawTileAt(tile, p2.x, y);
    }

    lw.clearDrag();
    lw.repaint();
  }

  /**
   * A square fill drag ended
   *
   * @param lw the level window we're drawing on
   */
  private static void squareFillDragEnded(LevelWindow lw) {
    Point p1 = new Point(lw.toolX_start, lw.toolY_start);
    Point p2 = new Point(lw.toolX_end, lw.toolY_end);

    if (p1.x > p2.x) {
      int temp = p2.x;
      p2.x = p1.x;
      p1.x = temp;
    }

    if (p1.y > p2.y) {
      int temp = p2.y;
      p2.y = p1.y;
      p1.y = temp;
    }

    lw.modified = true;

    for (int x = p1.x; x <= p2.x; ++x)
      for (int y = p1.y; y <= p2.y; ++y) {
        if (x < 0 || x >= 1024 || y < 0 || y >= 1024) continue;

        int tile = lw.m_tileset.getTile(lw.lastMouseClick);
        lw.drawTileAt(tile, x, y);
      }

    lw.clearDrag();
    lw.repaint();
  }

  /**
   * A line drag ended
   *
   * @param lw the level window we drew on
   */
  private static void lineDragEnded(LevelWindow lw) {
    Point p1 = new Point(lw.toolX_start, lw.toolY_start);
    Point p2 = new Point(lw.toolX_end, lw.toolY_end);
    Vector line = getPointsInLine(p1, p2);

    lw.modified = true;

    for (int x = 0; x < line.size(); ++x) {
      Point p = (Point) line.get(x);
      if (p.x < 0 || p.x >= 1024 || p.y < 0 || p.y >= 1024) continue;

      int tile = lw.m_tileset.getTile(lw.lastMouseClick);

      lw.drawTileAt(tile, p.x, p.y);
    }

    lw.clearDrag();
    lw.repaint();
  }

  private static void selectionDragEnded(
      LevelWindow lw, int startX, int startY, int width, int height) {
    lw.modified = true;
    lw.selection = new Vector();
    lw.m_parent.m_main.editCopy.setEnabled(true);
    lw.m_parent.m_main.editCut.setEnabled(true);
    lw.xOffset = -1;
    lw.yOffset = -1;
    lw.m_lvzImages.selectedMO.clear();

    for (int x = startX; x < startX + width; ++x) {
      Vector col = new Vector();

      for (int y = startY; y < startY + height; ++y) {
        lw.m_lvzImages.selectLvz(x * 16, y * 16);
        if (lw.m_parent.m_map[x][y] != -1) col.add(new Integer(lw.m_parent.m_map[x][y]));
        else
          // -1
          col.add(new Integer(0));

        if (lw.m_parent.m_map[x][y] == 217) {
          for (int i = 0; i < 2; i++)
            for (int j = 0; j < 2; j++) lw.m_parent.m_map[x + i][y + j] = 0;
        } else if (lw.m_parent.m_map[x][y] == 220) {
          for (int i = 0; i < 5; i++)
            for (int j = 0; j < 5; j++) lw.m_parent.m_map[x + i][y + j] = 0;
        } else if (lw.m_parent.m_map[x][y] == 219) {
          for (int i = 0; i < 6; i++)
            for (int j = 0; j < 6; j++) lw.m_parent.m_map[x + i][y + j] = 0;
        } else lw.m_parent.m_map[x][y] = 0;
      }

      lw.selection.add(col);
    }

    lw.undoer.snapShot(lw);

    lw.repaint();
  }

  private static void regionDragEnded(
      LevelWindow lw, int startX, int startY, int width, int height) {
    if (width > 0 && height > 0) {
      Rectangle r = new Rectangle(startX, startY, width, height);

      lw.m_asssRegions.addRectangleToCurrentRegion(r);
      lw.clearDrag();
      lw.repaint();
    }
  }

  public static void setTile(int x, int y, short[][] m_map, int tile) {
    if (x > 1023 || y > 1023) return;

    if (tile == 0) { // erase
      if (m_map[x][y] == -1) // erase special tile
      {
        for (int c_x = -5; c_x < 1; ++c_x)
          for (int c_y = -5; c_y < 1; ++c_y) { // find the upper left corner of the special tile
            if (x + c_x < 0) continue;

            if (y + c_y < 0) continue;

            if (m_map[x + c_x][y + c_y] > 216) // special tile
            {
              if (specialTileContains(x + c_x, y + c_y, x, y, m_map[x + c_x][y + c_y])) {
                setTile(x + c_x, y + c_y, m_map, 0);

                return;
              }
            }
          }
      } else if (m_map[x][y] < 191) // erase tile
      m_map[x][y] = 0;
      else if (m_map[x][y] == 216) // small asteriod
      m_map[x][y] = 0;
      else if (m_map[x][y] == 217) // medium asteriod
      {
        for (int X = 0; X < 2; ++X) for (int Y = 0; Y < 2; ++Y) m_map[x + X][y + Y] = 0;
      } else if (m_map[x][y] == 219) // station
      {
        for (int X = 0; X < 6; ++X) for (int Y = 0; Y < 6; ++Y) m_map[x + X][y + Y] = 0;
      } else if (m_map[x][y] == 220) // wormhole
      {
        for (int X = 0; X < 5; ++X) for (int Y = 0; Y < 5; ++Y) m_map[x + X][y + Y] = 0;
      } else m_map[x][y] = 0;
    } else if (tile == -1) { // erase, but replace with -1 instead of 0
      if (m_map[x][y] == -1) // erase special tile
      {
        for (int c_x = -5; c_x < 1; ++c_x)
          for (int c_y = -5; c_y < 1; ++c_y) { // find the upper left corner of the special tile
            if (x + c_x < 0) continue;

            if (y + c_y < 0) continue;

            if (m_map[x + c_x][y + c_y] > 216) // special tile
            {
              if (specialTileContains(x + c_x, y + c_y, x, y, m_map[x + c_x][y + c_y])) {
                setTile(x + c_x, y + c_y, m_map, -1);

                return;
              }
            }
          }
      } else if (m_map[x][y] < 191) // erase tile
      m_map[x][y] = -1;
      else if (m_map[x][y] == 216) // small asteriod
      m_map[x][y] = -1;
      else if (m_map[x][y] == 217) // medium asteriod
      {
        for (int X = 0; X < 2; ++X) for (int Y = 0; Y < 2; ++Y) m_map[x + X][y + Y] = -1;
      } else if (m_map[x][y] == 219) // station
      {
        for (int X = 0; X < 6; ++X) for (int Y = 0; Y < 6; ++Y) m_map[x + X][y + Y] = -1;
      } else if (m_map[x][y] == 220) // wormhole
      {
        for (int X = 0; X < 5; ++X) for (int Y = 0; Y < 5; ++Y) m_map[x + X][y + Y] = -1;
      }
    } else if (tile < 191) { // regular tile
      m_map[x][y] = (short) tile;
    } else { // special tile

      // first clear the space for the special tile
      if (tile == 217) // medium asteriod
      {
        for (int X = 0; X < 2; ++X) for (int Y = 0; Y < 2; ++Y) setTile(x + X, y + Y, m_map, -1);
      } else if (tile == 219) // station
      {
        for (int X = 0; X < 6; ++X) for (int Y = 0; Y < 6; ++Y) setTile(x + X, y + Y, m_map, -1);
      } else if (tile == 220) // wormhole
      {
        for (int X = 0; X < 5; ++X) for (int Y = 0; Y < 5; ++Y) setTile(x + X, y + Y, m_map, -1);
      }

      // then assign it
      m_map[x][y] = (short) tile;
    }
  }

  /**
   * Test wheter special tile with top left corner specX, specY of type tile contains the tile x,y
   * only meant for medium asteriods, wormholes, and stations
   *
   * @return true iff specail tile with specified coords would include (x, y)
   */
  public static boolean specialTileContains(int xd, int yd, int x, int y, int tile) {
    int xDif = x - xd;
    int yDif = y - yd;

    int maxDif = xDif;

    if (yDif > xDif) maxDif = yDif;

    switch (tile) {
      case 217: // medium asteriod
        return maxDif < 2;
      case 219: // station
        return maxDif < 6;
      case 220: // wormhole
        return maxDif < 5;
    }

    return false;
  }

  private static boolean isAutoTile(int num, short[][] auto) {
    if (num == 0) return false;

    for (int x = 0; x < 4; ++x) {
      for (int y = 0; y < 4; ++y) {
        if (num == auto[x][y]) return true;
      }
    }

    return false;
  }

  public static int getWhatAutoTileShouldBe(int[][] eightNebs, short[][] auto) {
    boolean above = false, below = false, left = false, right = false;
    int rv;

    if (isAutoTile(eightNebs[1][0], auto)) above = true;
    if (isAutoTile(eightNebs[1][2], auto)) below = true;
    if (isAutoTile(eightNebs[0][1], auto)) left = true;
    if (isAutoTile(eightNebs[2][1], auto)) right = true;

    if (above && below && left && right) rv = get(auto, TILE_MIDDLE_CENTER);
    else if (above && below && left) rv = get(auto, TILE_MIDDLE_RIGHT);
    else if (above && below && right) rv = get(auto, TILE_MIDDLE_LEFT);
    else if (above && left && right) rv = get(auto, TILE_BOTTOM_CENTER);
    else if (below && left && right) rv = get(auto, TILE_TOP_CENTER);
    else if (right && below) rv = get(auto, TILE_TOP_LEFT);
    else if (left && below) rv = get(auto, TILE_TOP_RIGHT);
    else if (right && above) rv = get(auto, TILE_BOTTOM_LEFT);
    else if (left && above) rv = get(auto, TILE_BOTTOM_RIGHT);
    else if (above && below) rv = get(auto, TILE_VERT_MIDDLE);
    else if (left && right) rv = get(auto, TILE_HORZ_CENTER);
    else if (below) rv = get(auto, TILE_VERT_TOP);
    else if (above) rv = get(auto, TILE_VERT_BOTTOM);
    else if (right) rv = get(auto, TILE_HORZ_LEFT);
    else if (left) rv = get(auto, TILE_HORZ_RIGHT);
    else rv = get(auto, TILE_SINGLE);

    if (rv == 0 && eightNebs[1][1] != 0) rv = eightNebs[1][1];

    return rv;
  }

  public static void autoSetTile(int x, int y, short[][] m_map, short[][] auto, boolean repeat) {

    boolean above = false, below = false, left = false, right = false;
    if (isAutoTile(m_map[x][y - 1], auto)) above = true;
    if (isAutoTile(m_map[x][y + 1], auto)) below = true;
    if (isAutoTile(m_map[x - 1][y], auto)) left = true;
    if (isAutoTile(m_map[x + 1][y], auto)) right = true;

    if (above && below && left && right) m_map[x][y] = get(auto, TILE_MIDDLE_CENTER);
    else if (above && below && left) m_map[x][y] = get(auto, TILE_MIDDLE_RIGHT);
    else if (above && below && right) m_map[x][y] = get(auto, TILE_MIDDLE_LEFT);
    else if (above && left && right) m_map[x][y] = get(auto, TILE_BOTTOM_CENTER);
    else if (below && left && right) m_map[x][y] = get(auto, TILE_TOP_CENTER);
    else if (right && below) m_map[x][y] = get(auto, TILE_TOP_LEFT);
    else if (left && below) m_map[x][y] = get(auto, TILE_TOP_RIGHT);
    else if (right && above) m_map[x][y] = get(auto, TILE_BOTTOM_LEFT);
    else if (left && above) m_map[x][y] = get(auto, TILE_BOTTOM_RIGHT);
    else if (above && below) m_map[x][y] = get(auto, TILE_VERT_MIDDLE);
    else if (left && right) m_map[x][y] = get(auto, TILE_HORZ_CENTER);
    else if (below) m_map[x][y] = get(auto, TILE_VERT_TOP);
    else if (above) m_map[x][y] = get(auto, TILE_VERT_BOTTOM);
    else if (right) m_map[x][y] = get(auto, TILE_HORZ_LEFT);
    else if (left) m_map[x][y] = get(auto, TILE_HORZ_RIGHT);
    else m_map[x][y] = get(auto, TILE_SINGLE);

    if (above && repeat) autoSetTile(x, y - 1, m_map, auto, false);
    if (below && repeat) autoSetTile(x, y + 1, m_map, auto, false);
    if (left && repeat) autoSetTile(x - 1, y, m_map, auto, false);
    if (right && repeat) autoSetTile(x + 1, y, m_map, auto, false);
  }

  public static short get(short[][] auto, int type) {

    // System.out.println( type );

    switch (type) {
      case TILE_TOP_LEFT:
        return auto[0][0];
      case TILE_TOP_CENTER:
        return auto[1][0];
      case TILE_TOP_RIGHT:
        return auto[2][0];
      case TILE_MIDDLE_LEFT:
        return auto[0][1];
      case TILE_MIDDLE_CENTER:
        return auto[1][1];
      case TILE_MIDDLE_RIGHT:
        return auto[2][1];
      case TILE_BOTTOM_LEFT:
        return auto[0][2];
      case TILE_BOTTOM_CENTER:
        return auto[1][2];
      case TILE_BOTTOM_RIGHT:
        return auto[2][2];
      case TILE_VERT_TOP:
        return auto[3][0];
      case TILE_VERT_MIDDLE:
        return auto[3][1];
      case TILE_VERT_BOTTOM:
        return auto[3][2];
      case TILE_HORZ_LEFT:
        return auto[0][3];
      case TILE_HORZ_CENTER:
        return auto[1][3];
      case TILE_HORZ_RIGHT:
        return auto[2][3];
      case TILE_SINGLE:
        return auto[3][3];
    }

    return 0;
  }

  public static void drawToolPreview(Graphics g, LevelWindow lw) {
    if (lw.currentTool == SELECT || lw.currentTool == RGN) {
      if (lw.width > 0 || lw.height > 0) {
        g.setColor(getToolColor(lw, lw.currentTool));

        int scale = lw.scaleList[lw.scaleSpot];

        g.drawRect(lw.minX * scale, lw.minY * scale, lw.width * scale, lw.height * scale);
      }
    } else if (lw.currentTool == LINE) {
      Point p1 = new Point(lw.toolX_start, lw.toolY_start);
      Point p2 = new Point(lw.toolX_end, lw.toolY_end);
      Vector line = getPointsInLine(p1, p2);
      int scale = lw.scaleList[lw.scaleSpot];

      g.setColor(Color.white);

      for (int x = 0; x < line.size(); ++x) {
        Point p = (Point) line.get(x);
        g.drawRect(p.x * scale, p.y * scale, scale, scale);
      }
    } else if (lw.currentTool == SQUARE) {
      Point p1 = new Point(lw.toolX_start, lw.toolY_start);
      Point p2 = new Point(lw.toolX_end, lw.toolY_end);
      int scale = lw.scaleList[lw.scaleSpot];

      if (p1.x > p2.x) {
        int temp = p2.x;
        p2.x = p1.x;
        p1.x = temp;
      }

      if (p1.y > p2.y) {
        int temp = p2.y;
        p2.y = p1.y;
        p1.y = temp;
      }

      g.setColor(Color.white);

      for (int x = p1.x; x <= p2.x; ++x) {
        g.drawRect(x * scale, p1.y * scale, scale, scale);
        g.drawRect(x * scale, p2.y * scale, scale, scale);
      }

      for (int y = p1.y; y <= p2.y; ++y) {
        g.drawRect(p1.x * scale, y * scale, scale, scale);
        g.drawRect(p2.x * scale, y * scale, scale, scale);
      }
    } else if (lw.currentTool == SQUAREFILL) {
      Point p1 = new Point(lw.toolX_start, lw.toolY_start);
      Point p2 = new Point(lw.toolX_end, lw.toolY_end);
      int scale = lw.scaleList[lw.scaleSpot];

      if (p1.x > p2.x) {
        int temp = p2.x;
        p2.x = p1.x;
        p1.x = temp;
      }

      if (p1.y > p2.y) {
        int temp = p2.y;
        p2.y = p1.y;
        p1.y = temp;
      }

      g.setColor(Color.white);

      if (scale > 4) {
        for (int x = p1.x; x <= p2.x; ++x)
          for (int y = p1.y; y <= p2.y; ++y) {
            g.drawRect(x * scale, y * scale, scale, scale);
          }
      } else {
        int w = p2.x - p1.x;
        int h = p2.y - p1.y;
        g.fillRect(p1.x * scale, p1.y * scale, w * scale, h * scale);
      }
    } else if (lw.currentTool == ELLIPSE) {
      Point p1 = new Point(lw.toolX_start, lw.toolY_start);
      Point p2 = new Point(lw.toolX_end, lw.toolY_end);
      int scale = lw.scaleList[lw.scaleSpot];

      if (p1.x > p2.x) {
        int temp = p2.x;
        p2.x = p1.x;
        p1.x = temp;
      }

      if (p1.y > p2.y) {
        int temp = p2.y;
        p2.y = p1.y;
        p1.y = temp;
      }

      g.setColor(Color.blue);

      if (scale > 4) {
        for (int x = p1.x; x <= p2.x; ++x)
          for (int y = p1.y; y <= p2.y; ++y) {

            if (ellipseValue(p1, p2, x, y) < 1) {
              if (ellipseValue(p1, p2, x + 1, y) >= 1
                  || ellipseValue(p1, p2, x - 1, y) >= 1
                  || ellipseValue(p1, p2, x, y + 1) >= 1
                  || ellipseValue(p1, p2, x, y - 1) >= 1
                  || ellipseValue(p1, p2, x, y) == 1) {
                g.drawRect(x * scale, y * scale, scale, scale);
              }
            }
          }
      } else {
        int w = p2.x - p1.x;
        int h = p2.y - p1.y;
        g.drawOval(p1.x * scale, p1.y * scale, w * scale, h * scale);
      }
    } else if (lw.currentTool == ELLIPSEFILL) {
      Point p1 = new Point(lw.toolX_start, lw.toolY_start);
      Point p2 = new Point(lw.toolX_end, lw.toolY_end);
      int scale = lw.scaleList[lw.scaleSpot];

      if (p1.x > p2.x) {
        int temp = p2.x;
        p2.x = p1.x;
        p1.x = temp;
      }

      if (p1.y > p2.y) {
        int temp = p2.y;
        p2.y = p1.y;
        p1.y = temp;
      }

      g.setColor(Color.blue);

      if (scale > 4) {

        for (int x = p1.x; x <= p2.x; ++x)
          for (int y = p1.y; y <= p2.y; ++y) {

            if (ellipseValue(p1, p2, x, y) < 1) {
              g.drawRect(x * scale, y * scale, scale, scale);
            }
          }
      } else {
        int w = p2.x - p1.x;
        int h = p2.y - p1.y;
        g.fillOval(p1.x * scale, p1.y * scale, w * scale, h * scale);
      }
    }
  }

  // get all the points in the line from p1 to p2
  private static Vector getPointsInLine(Point p1, Point p2) {
    Vector rv = new Vector();

    if (p1 == p2) // just a single point
    {
      rv.add(p1);

      return rv;
    }

    if (p1.getX() > p2.getX()) { // make p1 be with a smaller x
      Point temp = p1;
      p1 = p2;
      p2 = temp;
    }

    double deltaY = (p1.getY() - p2.getY());
    double deltaX = (p1.getX() - p2.getX());

    if (deltaY == 0) // horizontal line
    {
      for (int x = (int) p1.getX(); x <= (int) p2.getX(); ++x) {
        Point p = new Point(x, (int) p1.getY());

        rv.add(p);
      }
    } else if (deltaX == 0) // vertical line
    {
      if (p1.getY() > p2.getY()) { // make p1 be with a smaller y
        Point temp = p1;
        p1 = p2;
        p2 = temp;
      }

      for (int y = (int) p1.getY(); y <= (int) p2.getY(); ++y) {
        Point p = (new Point((int) p1.getX(), y));

        rv.add(p);
      }
    } else
    // diagnol line
    {
      double slope = deltaY / deltaX;

      if (Math.abs(slope) < 1) {

        int c = 0;
        for (int x = (int) p1.getX(); x <= (int) p2.getX(); ++x) {
          Point nextPart = new Point(x, (int) (p1.getY() + c++ * slope));

          rv.add(nextPart);
        }
      } else {
        if (p1.getY() > p2.getY()) { // make p1 be with a smaller y
          Point temp = p1;
          p1 = p2;
          p2 = temp;
        }

        double inverseSlope = 1 / slope;

        int c = 0;
        for (int y = (int) p1.getY(); y <= (int) p2.getY(); ++y) {
          Point nextPart = new Point((int) (p1.getX() + c++ * inverseSlope), y);

          rv.add(nextPart);
        }
      }
    }

    return rv;
  }

  /*
   * ellipseDragEnded
   *
   * This function actualy draws the ellipse
   */

  private static void ellipseDragEnded(LevelWindow lw) {
    Point p1 = new Point(lw.toolX_start, lw.toolY_start);
    Point p2 = new Point(lw.toolX_end, lw.toolY_end);

    if (p1.x > p2.x) {
      int temp = p2.x;
      p2.x = p1.x;
      p1.x = temp;
    }

    if (p1.y > p2.y) {
      int temp = p2.y;
      p2.y = p1.y;
      p1.y = temp;
    }

    lw.modified = true;

    int tile = lw.m_tileset.getTile(lw.lastMouseClick);

    for (int x = p1.x; x <= p2.x; ++x)
      for (int y = p1.y; y <= p2.y; ++y) {
        if ((x < 0 || x > 1024) && (y < 0 || y > 1024)) continue;

        if (ellipseValue(p1, p2, x, y) < 1) {
          if (ellipseValue(p1, p2, x + 1, y) >= 1
              || ellipseValue(p1, p2, x - 1, y) >= 1
              || ellipseValue(p1, p2, x, y + 1) >= 1
              || ellipseValue(p1, p2, x, y - 1) >= 1
              || ellipseValue(p1, p2, x, y) == 1) {
            lw.drawTileAt(tile, x, y);
          }
        }
      }

    lw.clearDrag();
    lw.repaint();
  }

  /*
   * ellipseFillDragEnded
   *
   * This function actually draws the filled ellipse
   */

  private static void ellipseFillDragEnded(LevelWindow lw) {
    Point p1 = new Point(lw.toolX_start, lw.toolY_start);
    Point p2 = new Point(lw.toolX_end, lw.toolY_end);

    if (p1.x > p2.x) {
      int temp = p2.x;
      p2.x = p1.x;
      p1.x = temp;
    }

    if (p1.y > p2.y) {
      int temp = p2.y;
      p2.y = p1.y;
      p1.y = temp;
    }

    lw.modified = true;
    int tile = lw.m_tileset.getTile(lw.lastMouseClick);

    for (int x = p1.x; x <= p2.x; ++x)
      for (int y = p1.y; y <= p2.y; ++y) {
        if ((x < 0 || x > 1024) && (y < 0 || y > 1024)) continue;

        if (ellipseValue(p1, p2, x, y) < 1) {
          lw.drawTileAt(tile, x, y);
        }
      }

    lw.clearDrag();
    lw.repaint();
  }

  /*
   * EllipseValue
   *
   * Gets the value for a point given bounds of the ellipse and a point.
   *
   * p1: Upper left bound p2: Lower left bound x, y: Point coordinates to
   * calculate for
   */
  private static double ellipseValue(Point p1, Point p2, int x, int y) {
    double cx = (p1.x + p2.x) / 2.0;
    double cy = (p1.y + p2.y) / 2.0;
    double r1 = Math.abs((0.5) * (p1.y - p2.y));
    double r2 = Math.abs((0.5) * (p1.x - p2.x));
    double Dx = Math.abs(x - cx);
    double Dy = Math.abs(y - cy);
    double term1 = ((1 / (r1 * r1))) * (Dy * Dy);
    double term2 = ((1 / (r2 * r2))) * (Dx * Dx);
    return Math.sqrt(term1 + term2);
  }
}
