// Stan Bak
// UndoVector.java 5-27-04

package editor.imageeditor;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;

public class UndoVector // actually stored as a linked list... but who cares
						// right?
{
	private Image initial = null;

	LinkedList l = null;

	int curFrame = 0;

	DrawPanel dp = null;

	Image drawHere = null;

	public UndoVector(Image i, DrawPanel newDP)
	{
		dp = newDP;
		l = new LinkedList();
		initial = dp.createImage(i.getWidth(null), i.getHeight(null));
		Graphics g = initial.getGraphics();
		g.drawImage(i, 0, 0, null);
	}

	public Image copyImage(Image i)
	{
		if (i == null)
			return null;

		Graphics g = drawHere.getGraphics();
		g.drawImage(i, 0, 0, null);

		return drawHere;

	}

	// add to end
	public void add(Object o)
	{
		if (curFrame != l.size())
		{
			for (int x = l.size() - 1; x >= curFrame; --x)
			{
				l.removeLast();
			}
		}

		l.addLast(o);
		curFrame = l.size();

		if (drawHere == null)
		{
			drawHere = dp.createImage(512, 512);
		}

		if (l.size() > 1000)
		{
			reduceSize();
		}
	}

	// this reduces the size of the undo vector when it reaches 1000 to be
	// approx 500
	public void reduceSize()
	{
		curFrame = 500;

		Object o = l.get(curFrame);

		while (o.getClass() != Color.class)
		{
			curFrame++;
			o = l.get(curFrame);
		}

		initial.getGraphics().drawImage(draw(), 0, 0, null);

		for (int x = 0; x < curFrame; ++x)
		{
			o = l.removeFirst();
		}

		curFrame = l.size();
	}

	public Image undo()
	{
		for (int x = curFrame - 1; x >= 0; --x)
		{
			Object one = l.get(x);
			if (one.getClass() == Color.class) // start
			{
				curFrame = x;
				break;
			}
			else if (x == 0)
			{
				curFrame = 0;
			}
		}

		return draw();
	}

	public Image redo()
	{
		for (int x = curFrame + 1; x < l.size(); ++x)
		{
			Object one = l.get(x);
			if (one.getClass() == Color.class) // start
			{
				curFrame = x;
				break;
			}
			else if (x == l.size() - 1)
			{
				curFrame = l.size() - 1;
			}
		}

		return draw();
	}

	private int[] getAllColors()
	{
		int[] storeHere = new int[512 * 512];
		PixelGrabber pg = new PixelGrabber(drawHere, 0, 0, 512, 512, storeHere,
				0, 512);
		try
		{
			pg.grabPixels();
		}
		catch (InterruptedException e)
		{
			JOptionPane.showMessageDialog(null,
					"interrupted waiting for pixels!");
			storeHere = null;
			return null;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0)
		{
			JOptionPane.showMessageDialog(null,
					"image fetch aborted or errored");
			storeHere = null;
			return null;
		}

		return storeHere;
	}

	private void fillAtPoint(Dimension d, Point start, Graphics g,
			int sourcePixel, int[] pixels)
	{
		LinkedList myList = new LinkedList();

		int notCurrentColor = sourcePixel;
		if (notCurrentColor == 500)
			notCurrentColor = 501;
		else
			notCurrentColor = 500;

		myList.add(start);

		Rectangle r = new Rectangle(512 / 2 - d.width / 2,
				512 / 2 - d.height / 2, d.width, d.height);

		while (!myList.isEmpty())
		{
			Point where = (Point) myList.removeFirst();

			if (pixels[where.x + where.y * 512] == sourcePixel)
			{ // color is a match!
				pixels[where.x + where.y * 512] = notCurrentColor;

				g.fillRect(where.x, where.y, 1, 1);

				if (where.x > r.x) // left case
					myList.add(new Point(where.x - 1, where.y));

				if (where.x < r.x + r.width - 1) // right case
					myList.add(new Point(where.x + 1, where.y));

				if (where.y > r.y) // up case
					myList.add(new Point(where.x, where.y - 1));

				if (where.y < r.y + r.height - 1) // down case
					myList.add(new Point(where.x, where.y + 1));
			}
		}
	}

	// PENCIL:
	// color, line, stroke

	private Image draw()
	{
		Image i = copyImage(initial);
		Graphics2D g = (Graphics2D) i.getGraphics();

		for (int x = 0; x < curFrame; ++x)
		{
			Object one = l.get(x);

			if (one.getClass() == Color.class)
			{ // color
				g.setColor((Color) one);
			}
			else if (one.getClass() == Line2D.Float.class)
			{ // line/pencil
				++x;
				Stroke two = (Stroke) l.get(x);
				g.setStroke(two);

				g.draw((Line2D.Float) one);
			}
			else if (one.getClass() == Rectangle.class)
			{ // rectangle
				++x;
				Stroke two = (Stroke) l.get(x);
				g.setStroke(two);
				++x;
				Boolean three = (Boolean) l.get(x);

				if (three.booleanValue() == false)
					g.draw((Rectangle) one);
				else
					g.fill((Rectangle) one);
			}
			else if (one.getClass() == Ellipse2D.Float.class)
			{ // oval
				++x;
				Stroke two = (Stroke) l.get(x);
				g.setStroke(two);
				++x;
				Boolean three = (Boolean) l.get(x);

				if (three.booleanValue() == false)
					g.draw((Ellipse2D.Float) one);
				else
					g.fill((Ellipse2D.Float) one);
			}
			else if (one.getClass() == String.class)
			{ // text
				++x;
				Font two = (Font) l.get(x);
				++x;
				Point three = (Point) l.get(x);

				g.setFont(two);
				g.drawString((String) one, three.x, three.y);
			}
			else if (one.getClass() == Point.class)
			{ // Fill
				++x;
				Dimension d = (Dimension) l.get(x);

				Point imageP = (Point) one;

				int[] allPixels = null;
				allPixels = getAllColors();

				if (allPixels != null)
					fillAtPoint(d, imageP, g, allPixels[imageP.x + imageP.y
							* 512], allPixels);
			}
			else
			// if (one.getClass() == Image.class ||
			// one.getClass() == sun.awt.windows.Win32OffScreenImage.class)
			{ // Selection\
				++x;
				// try
				// {
				Point selectedPoint = (Point) l.get(x);
				Image selectedImage = (Image) one;
				g.drawImage(selectedImage, selectedPoint.x, selectedPoint.y,
						null);
				// }
				// catch (java.lang.ClassCastException e)
				// {
				// System.out.println(one.getClass() + "<- x = " + x);
				// }

			}
		}

		return i;
	}
}