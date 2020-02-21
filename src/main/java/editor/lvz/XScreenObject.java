package editor.lvz;

/** @author baks */
public class XScreenObject {
  int id;

  int x_type;

  int x_coord;

  int y_type;

  int y_coord;

  int imageNum;

  int layer;

  int time;

  int mode;

  public XScreenObject() {}

  public String toString() {
    // <x coord>,<y coord>,<image>,<layer>,<mode>,<display time>,<object id>

    String imageString;
    if (imageNum < 10) imageString = "IMAGE" + imageNum + "  ";
    else if (imageNum < 100) imageString = "IMAGE" + imageNum + " ";
    else imageString = "IMAGE" + imageNum;

    return ExtractLvz.screenObjOffsets[x_type]
        + x_coord
        + ",\t"
        + ExtractLvz.screenObjOffsets[y_type]
        + y_coord
        + ",\t"
        + imageString
        + ",\t"
        + ExtractLvz.layers[layer]
        + ",\t"
        + ExtractLvz.modes[mode]
        + ",\t"
        + time
        + ",\t"
        + id;
  }
}
