package editor.lvz;

import editor.Main;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;

/**
 * This class reperesents an image. It knows how to save itself and may copy from original source if
 * it's a jpeg
 *
 * @author Stan
 */
public class FileImage {

  public Image image = null;
  private String originalFilename = null;
  private boolean isJPEG = false;

  FileImage(Image image) {
    this.image = image;
  }

  /**
   * Constructor for loading an image from a path
   *
   * @param filename the path to load from
   */
  FileImage(String filename) {
    File f = new File(filename);
    if (!f.exists()) {
      JOptionPane.showMessageDialog(
          null,
          "Image File not found: "
              + filename
              + ". Chances are you are trying to import a lvz where\n"
              + "not all of the images are included in the File= section as the IMAGE= section. These\n"
              + "kinds of lvz files are not supported, please change it to include the images you need and\n"
              + "try to import it again.");
      return;
    }

    try {

      RenderedImage r_image = JAI.create("fileload", filename);

      ColorModel colorModel = r_image.getColorModel();
      WritableRaster raster = (WritableRaster) r_image.getData();
      image = new BufferedImage(colorModel, raster, false, null);

    } catch (Exception e) {

      JOptionPane.showMessageDialog(null, "Warning, JAI Error: " + filename);

      String path =
          Main.rootDirectory + File.separator + "include" + File.separator + "noimage.png";
      image = new ImageIcon(path).getImage();
    }

    image = ImageLoader.makeBlackTransparent(image);

    if (filename.toLowerCase().endsWith("jpg")) {
      isJPEG = true;
      originalFilename = filename;
    }
  }

  /**
   * Save this image to the filesystem
   *
   * @param path the filepath to save to
   * @param creator an internal frame we can use to create images in memory
   */
  void saveImage(String path, JInternalFrame creator) {
    boolean saveRegular = false;
    if (isJPEG) {
      File f = new File(originalFilename);

      if (!f.exists()) saveRegular = true;
      else {
        if (!LvzFiling.copyFile(originalFilename, path)) saveRegular = true;
      }
    } else {
      saveRegular = true;
    }

    if (saveRegular) {
      int width = image.getWidth(null);
      int height = image.getHeight(null);

      BufferedImage bi = (BufferedImage) creator.createImage(width, height);

      Graphics g = bi.getGraphics();
      g.setColor(Color.black);
      g.fillRect(0, 0, bi.getWidth(null), bi.getHeight(null));
      g.drawImage(image, 0, 0, null);

      try {
        String format = isJPEG ? "JPG" : "PNG";
        ImageIO.write(bi, format, new File(path));
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Image Save Failed: " + e.toString());
      }
    }
  }

  /**
   * Get the preferred extention for saving this file
   *
   * @return ".jpg" or ".png", depending on the type of image we loaded
   */
  String getExtention() {
    return isJPEG ? ".jpg" : ".png";
  }

  /**
   * get the width of the image
   *
   * @param o the image observer to use
   * @return the width of the image
   */
  public int getWidth(ImageObserver o) {
    return image.getWidth(o);
  }

  /**
   * get the height of the image
   *
   * @param o the image observer to use
   * @return the height of the image
   */
  public int getHeight(ImageObserver o) {
    return image.getHeight(o);
  }
}
