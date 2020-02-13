package editor.lvz;

import editor.Main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * This class reperesents an image. It knows how to save itself and may copy
 * from original source if it's a jpeg
 *
 * @author Stan
 *
 */
public class FileImage
{
	public Image i = null;

	private boolean isJPEG = false;

	private String originalFilename = null;

	FileImage(Image i)
	{
		this.i = i;
	}

	/**
	 * Constructor for loading an image from a path
	 *
	 * @param filename
	 *            the path to load from
	 */
	FileImage(String filename)
	{
		File f = new File(filename);
		if (!f.exists())
		{
			JOptionPane
					.showMessageDialog(
							null,
							"Image File not found: "
									+ filename
									+ ". Chances are you are trying to import a lvz where\n"
									+ "not all of the images are included in the File= section as the IMAGE= section. These\n"
									+ "kinds of lvz files are not supported, please change it to include the images you need and\n"
									+ "try to import it again.");
			return;
		}

		try
		{
			RenderedImage r_image = JAI.create("fileload", filename);

			BufferedImage testimage = new BufferedImage(
					r_image.getColorModel(), (WritableRaster) (r_image
							.getData()), false, null);

			i = (Image) testimage;
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Warning, JAI Error: "
					+ filename);

			i = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "noimage.png")
					.getImage();
		}

		i = ImageLoader.makeBlackTransparent(i);

		if (filename.toLowerCase().endsWith("jpg"))
		{
			isJPEG = true;
			originalFilename = filename;
		}
	}

	/**
	 * Save this image to the filesystem
	 *
	 * @param saveTo
	 *            the filepath to save to
	 * @param creator
	 *            an internal frame we can use to create images in memory
	 */
	public void saveImage(String saveTo, JInternalFrame creator)
	{
		boolean saveRegular = false;
		if (isJPEG)
		{
			File f = new File(originalFilename);

			if (!f.exists())
				saveRegular = true;
			else
			{
				if (!LvzFiling.copyFile(originalFilename, saveTo))
					saveRegular = true;
			}
		}
		else
		{
			saveRegular = true;
		}

		if (saveRegular)
		{
			int width = i.getWidth(null);
			int height = i.getHeight(null);

			BufferedImage bi = (BufferedImage) creator.createImage(width,
					height);

			Graphics g = bi.getGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, bi.getWidth(null), bi.getHeight(null));
			g.drawImage(i, 0, 0, null);

			try
			{
				if (isJPEG)
					ImageIO.write(bi, "JPG", new File(saveTo));
				else
					ImageIO.write(bi, "PNG", new File(saveTo));
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(null, "Image Save Failed: "
						+ e.toString());
			}
		}

	}

	/**
	 * Get the preferred extention for saving this file
	 *
	 * @return ".jpg" or ".png", depending on the type of image we loaded
	 */
	public String getExtention()
	{
		String rv = ".png";

		if (isJPEG)
			rv = ".jpg";

		return rv;
	}

	/**
	 * get the width of the image
	 *
	 * @param o
	 *            the image observer to use
	 * @return the width of the image
	 */
	public int getWidth(ImageObserver o)
	{
		return i.getWidth(o);
	}

	/**
	 * get the height of the image
	 *
	 * @param o
	 *            the image observer to use
	 * @return the height of the image
	 */
	public int getHeight(ImageObserver o)
	{
		return i.getHeight(o);
	}

}
