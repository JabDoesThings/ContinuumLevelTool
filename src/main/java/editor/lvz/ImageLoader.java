// Stan Bak
// image loader wrapper
// 5-31-04

package editor.lvz;

import editor.loaders.BitMap;
import editor.loaders.BitmapSaving;

import javax.swing.*;
import java.awt.*;
import java.awt.image.MemoryImageSource;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ImageLoader {
  public static FileImage loadImage(String filename) {
    return new FileImage(filename);
  }

  public static Image loadBitmap(String path) {
    try {
      Image extra;

      File f = new File(path);
      BufferedInputStream bs = new BufferedInputStream(new FileInputStream(f));
      BitMap b = new BitMap(bs);
      b.readBitMap(true);

      extra = b.getImage();
      return extra;

    } catch (FileNotFoundException e) {
      System.out.println("File not found: " + e.toString());
      return null;
    }
  }

  public static Image makeBlackTransparent(Image source) {
    int w = source.getWidth(null);
    int h = source.getHeight(null);

    // System.out.println("width = " + w);

    int[] cols = BitmapSaving.getAllColors(source);

    for (int x = 0; x < w; ++x)
      for (int y = 0; y < h; ++y) {
        int color = cols[x + y * w];

        int[] rgb = BitmapSaving.getRGB(color);
        if (rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0) { // black
          cols[x + y * w] = 0;
        } else cols[x + y * w] = (255 << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
      }

    return (new JPanel()).createImage(new MemoryImageSource(w, h, cols, 0, w));
  }
}
