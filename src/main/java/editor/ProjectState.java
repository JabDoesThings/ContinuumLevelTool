package editor;

import java.awt.*;

public class ProjectState {

  public boolean m_radar;

  public boolean m_tileset;

  public boolean m_autotool;

  public boolean m_lvzImages;

  public boolean m_asssRegions;

  // show radar by default?
  public boolean showRadar = false;

  // use alternate hotkeys
  public boolean useAlt = false;

  // initial positions
  public Point pos_Radar; // 200 x 200

  public Point pos_Tileset; // 352 x 160

  public Point pos_Autotool; // 64 x 64

  public Point pos_LvzImages; // 400 x 250

  public Point pos_AsssRegions; // 420 x 180

  public boolean lvzDrag = false;
  public boolean lvzSelectSingle = true;

  public ProjectState() {
    m_radar = showRadar;
    m_tileset = true;
    m_lvzImages = false;
    m_autotool = false;
    m_asssRegions = false;

    int inset = 50;
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int left = 5;
    int right = screenSize.width - 2 * inset - 45;
    int bottom = screenSize.height - 2 * inset - 100;

    pos_Radar = new Point(right - 200, bottom - 200);
    pos_Tileset = new Point(left, bottom - 160);
    pos_Autotool = new Point(left + 352 + 15, bottom - 64);
    pos_LvzImages = new Point(left, bottom - 210);
    pos_AsssRegions = new Point(left, bottom - 140);
  }
}
