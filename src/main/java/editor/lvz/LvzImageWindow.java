// Stan Bak
// june 4th 04
// LvzImageWindow.java

package editor.lvz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import editor.LevelWindow;
import editor.Tools;
import editor.imageeditor.ImageDialog;

public class LvzImageWindow extends JInternalFrame implements ItemListener,
		ActionListener, ChangeListener
{
	final static JFileChooser fc = new JFileChooser();
	
	static
	{
		fc.setMultiSelectionEnabled(true);
	}
	
	JTabbedPane tabbedPane;

	// IMAGE PANEL
	JPanel panel_images;

	JComboBox lvzSelector;

	JButton newLvz;

	JButton importLvz;

	JButton removeLvz;

	JScrollPane scrollPane;

	ImagePanel imagePanel;

	JButton newImage;

	JButton importImage;

	JButton editImage;

	JButton removeImage;

	// END IMAGE PANEL

	// ADD PANEL
	JPanel panel_add;

	JLabel header;

	JLabel l_layer;

	JLabel l_mode;

	JLabel l_displayTime;

	JLabel l_id;

	JCheckBox ch_snap;

	JComboBox c_layer;

	JComboBox c_mode;

	JSpinner s_displayTime;

	JSpinner s_id;

	// END ADD PANEL

	// MAP OBJECTS Panel
	JPanel panel_mapobjects;

	JLabel l_mo_lvz;

	JLabel l_mo_num;

	JLabel l_mo_x;

	JLabel l_mo_y;

	JLabel l_mo_layer;

	JLabel l_mo_mode;

	JLabel l_mo_displayTime;

	JLabel l_mo_id;

	JLabel l_mo_imageId;

	JComboBox c_mo_lvz;

	JSpinner s_mo_num;

	JSpinner s_mo_x;

	JSpinner s_mo_y;

	JComboBox c_mo_layer;

	JComboBox c_mo_mode;

	JSpinner s_mo_displayTime;

	JSpinner s_mo_id;

	JSpinner s_mo_imageId;

	JButton b_mo_delete;

	Vector mapObjects = new Vector();

	// end map objects panel

	public HashMap selectedMO = new HashMap();

	public LevelWindow parent;

	final FileDialog fileDialog = new FileDialog(new Frame());

	final int WIDTH = 400;

	final int CENTERX = WIDTH / 2;

	final int HEIGHT = 250;

	final int CENTERY = HEIGHT / 2;

	public LvzImageWindow(LevelWindow p, Point l)
	{
		// title, resizable, closable, maximizable, minimizable
		super("Lvz", false, true, false, false);
		parent = p;
		setSize(WIDTH, HEIGHT);
		setLocation(l.x, l.y);

		// Hide the 'java' icon
		try
		{
			setFrameIcon(null);
		}
		catch (Exception e)
		{
		}

		tabbedPane = new JTabbedPane();

		panel_images = new JPanel();
		panel_images.setLayout(null);

		lvzSelector = new JComboBox();
		lvzSelector.setBounds(10, 10, 100, 20);
		lvzSelector.addItem("(none)");
		lvzSelector.setEditable(false);
		lvzSelector.addItemListener(this);
		panel_images.add(lvzSelector);

		newLvz = new JButton("New Lvz");
		newLvz.setBounds(115, 10, 80, 20);
		panel_images.add(newLvz);
		newLvz.addActionListener(this);

		importLvz = new JButton("Import Lvz");
		importLvz.setBounds(195, 10, 90, 20);
		panel_images.add(importLvz);
		importLvz.addActionListener(this);

		removeLvz = new JButton("Remove Lvz");
		removeLvz.setBounds(285, 10, 100, 20);
		panel_images.add(removeLvz);
		removeLvz.addActionListener(this);

		imagePanel = new ImagePanel(this);
		scrollPane = new JScrollPane(imagePanel);
		scrollPane.setBounds(5, 40, WIDTH - 25, 110);
		panel_images.add(scrollPane);

		newImage = new JButton("New Image");
		newImage.setBounds(2, 160, 88, 20);
		panel_images.add(newImage);
		newImage.addActionListener(this);

		importImage = new JButton("Import Image");
		importImage.setBounds(90, 160, 100, 20);
		panel_images.add(importImage);
		importImage.addActionListener(this);

		editImage = new JButton("Edit Image");
		editImage.setBounds(190, 160, 85, 20);
		panel_images.add(editImage);
		editImage.addActionListener(this);

		removeImage = new JButton("Remove Image");
		removeImage.setBounds(275, 160, 110, 20);
		panel_images.add(removeImage);
		removeImage.addActionListener(this);

		editImage.setEnabled(false);
		removeImage.setEnabled(false);
		newImage.setEnabled(false);
		importImage.setEnabled(false);
		removeLvz.setEnabled(false);

		tabbedPane.addTab("Images", panel_images);

		panel_add = new JPanel();
		panel_add.setLayout(null);

		header = new JLabel("New LVZ objects will be added with these options");
		header.setFont(new Font("large", Font.BOLD, 15));
		header.setBounds(CENTERX - 195, 10, 390, 20);
		panel_add.add(header);

		ch_snap = new JCheckBox("Snap To Tile");
		ch_snap.setBounds(CENTERX, 40, 140, 20);
		panel_add.add(ch_snap);

		l_layer = new JLabel("Layer:", SwingConstants.RIGHT);
		l_layer.setBounds(CENTERX - 160, 65, 150, 20);
		panel_add.add(l_layer);

		l_mode = new JLabel("Mode:", SwingConstants.RIGHT);
		l_mode.setBounds(CENTERX - 160, 90, 150, 20);
		panel_add.add(l_mode);

		l_displayTime = new JLabel("Display Time (seconds):",
				SwingConstants.RIGHT);
		l_displayTime.setBounds(CENTERX - 160, 115, 150, 20);
		panel_add.add(l_displayTime);

		l_id = new JLabel("Object ID:", SwingConstants.RIGHT);
		l_id.setBounds(CENTERX - 160, 140, 150, 20);
		panel_add.add(l_id);

		c_layer = new JComboBox();
		c_layer.setBounds(CENTERX, 65, 150, 20);
		c_layer.addItem("TopMost");
		c_layer.addItem("AfterChat");
		c_layer.addItem("AfterGuages");
		c_layer.addItem("AfterShips");
		c_layer.addItem("AfterWeapons");
		c_layer.addItem("AfterTiles");
		c_layer.addItem("AfterBackground");
		c_layer.addItem("BelowAll");
		c_layer.setEditable(false);
		panel_add.add(c_layer);

		c_mode = new JComboBox();
		c_mode.setBounds(CENTERX, 90, 150, 20);
		c_mode.addItem("ShowAlways");
		c_mode.addItem("EnterZone");
		c_mode.addItem("EnterArena");
		c_mode.addItem("Kill");
		c_mode.addItem("Death");
		c_mode.addItem("ServerControlled");
		c_mode.setEditable(false);
		panel_add.add(c_mode);

		s_displayTime = new JSpinner(new SpinnerNumberModel(0.0, 0.00, 320,
				0.01));
		s_displayTime.setBounds(CENTERX, 115, 50, 20);
		panel_add.add(s_displayTime);

		s_id = new JSpinner(new SpinnerNumberModel(0, 0, 32000, 1));
		s_id.setBounds(CENTERX, 140, 50, 20);
		panel_add.add(s_id);

		tabbedPane.addTab("Add Options", panel_add);

		JPanel panel_mapobjects = new JPanel();
		panel_mapobjects.setLayout(null);
		Font bold = new Font("bold", Font.BOLD, 12);

		l_mo_lvz = new JLabel("LVZ File:", SwingConstants.RIGHT);
		l_mo_lvz.setBounds(0, 20, 70, 20);
		l_mo_lvz.setFont(bold);
		panel_mapobjects.add(l_mo_lvz);

		l_mo_num = new JLabel("Number:", SwingConstants.RIGHT);
		l_mo_num.setBounds(CENTERX, 20, 70, 20);
		l_mo_num.setFont(bold);
		panel_mapobjects.add(l_mo_num);

		l_mo_x = new JLabel("X(pixel):", SwingConstants.RIGHT);
		l_mo_x.setBounds(WIDTH / 3 - 100, 45, 100, 20);
		panel_mapobjects.add(l_mo_x);

		l_mo_y = new JLabel("Y(pixel):", SwingConstants.RIGHT);
		l_mo_y.setBounds(2 * WIDTH / 3 - 100, 45, 100, 20);
		panel_mapobjects.add(l_mo_y);

		l_mo_imageId = new JLabel("Image ID:", SwingConstants.RIGHT);
		l_mo_imageId.setBounds(CENTERX - 160, 70, 150, 20);
		panel_mapobjects.add(l_mo_imageId);

		l_mo_layer = new JLabel("Layer:", SwingConstants.RIGHT);
		l_mo_layer.setBounds(CENTERX - 160, 90, 150, 20);
		panel_mapobjects.add(l_mo_layer);

		l_mo_layer = new JLabel("Mode:", SwingConstants.RIGHT);
		l_mo_layer.setBounds(CENTERX - 160, 110, 150, 20);
		panel_mapobjects.add(l_mo_layer);

		l_mo_displayTime = new JLabel("Display Time (seconds):",
				SwingConstants.RIGHT);
		l_mo_displayTime.setBounds(CENTERX - 160, 130, 150, 20);
		panel_mapobjects.add(l_mo_displayTime);

		l_mo_id = new JLabel("Object ID:", SwingConstants.RIGHT);
		l_mo_id.setBounds(CENTERX - 160, 150, 150, 20);
		panel_mapobjects.add(l_mo_id);

		c_mo_lvz = new JComboBox();
		c_mo_lvz.setBounds(80, 20, 100, 20);
		c_mo_lvz.addItem("(none)");
		c_mo_lvz.setEditable(false);
		c_mo_lvz.addItemListener(this);
		panel_mapobjects.add(c_mo_lvz);

		s_mo_num = new JSpinner(new SpinnerNumberModel(0, 0, 0, 1));
		s_mo_num.setBounds(CENTERX + 80, 20, 50, 20);
		panel_mapobjects.add(s_mo_num);
		s_mo_num.addChangeListener(this);

		s_mo_x = new JSpinner(new SpinnerNumberModel(0, 0, 16383, 1));
		s_mo_x.setBounds(WIDTH / 3, 45, 70, 20);
		panel_mapobjects.add(s_mo_x);
		s_mo_x.addChangeListener(this);

		s_mo_y = new JSpinner(new SpinnerNumberModel(0, 0, 16383, 1));
		s_mo_y.setBounds(2 * WIDTH / 3, 45, 70, 20);
		panel_mapobjects.add(s_mo_y);
		s_mo_y.addChangeListener(this);

		s_mo_imageId = new JSpinner(new SpinnerNumberModel(0, 0, 255, 1));
		s_mo_imageId.setBounds(CENTERX, 70, 70, 20);
		panel_mapobjects.add(s_mo_imageId);
		s_mo_imageId.addChangeListener(this);

		c_mo_layer = new JComboBox();
		c_mo_layer.setBounds(CENTERX, 90, 130, 20);
		c_mo_layer.addItem("TopMost");
		c_mo_layer.addItem("AfterChat");
		c_mo_layer.addItem("AfterGuages");
		c_mo_layer.addItem("AfterShips");
		c_mo_layer.addItem("AfterWeapons");
		c_mo_layer.addItem("AfterTiles");
		c_mo_layer.addItem("AfterBackground");
		c_mo_layer.addItem("BelowAll");
		c_mo_layer.setEditable(false);
		panel_mapobjects.add(c_mo_layer);
		c_mo_layer.addItemListener(this);

		c_mo_mode = new JComboBox();
		c_mo_mode.setBounds(CENTERX, 110, 130, 20);
		c_mo_mode.addItem("ShowAlways");
		c_mo_mode.addItem("EnterZone");
		c_mo_mode.addItem("EnterArena");
		c_mo_mode.addItem("Kill");
		c_mo_mode.addItem("Death");
		c_mo_mode.addItem("ServerControlled");
		c_mo_mode.setEditable(false);
		panel_mapobjects.add(c_mo_mode);
		c_mo_mode.addItemListener(this);

		s_mo_displayTime = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 320.0,
				0.01));
		s_mo_displayTime.setBounds(CENTERX, 130, 50, 20);
		panel_mapobjects.add(s_mo_displayTime);
		s_mo_displayTime.addChangeListener(this);

		s_mo_id = new JSpinner(new SpinnerNumberModel(0, 0, 32000, 1));
		s_mo_id.setBounds(CENTERX, 150, 50, 20);
		panel_mapobjects.add(s_mo_id);
		s_mo_id.addChangeListener(this);

		b_mo_delete = new JButton("Delete Selected Obj");
		b_mo_delete.setBounds(10, 70, 130, 20);
		panel_mapobjects.add(b_mo_delete);
		b_mo_delete.addActionListener(this);

		b_mo_delete.setEnabled(false);
		s_mo_num.setEnabled(false);
		s_mo_x.setEnabled(false);
		s_mo_y.setEnabled(false);
		c_mo_layer.setEnabled(false);
		c_mo_mode.setEnabled(false);
		s_mo_displayTime.setEnabled(false);
		s_mo_id.setEnabled(false);
		s_mo_imageId.setEnabled(false);

		tabbedPane.addTab("Map Objects", panel_mapobjects);

		JPanel panel_autotilebg = new AutoTileBGPanel(this);

		tabbedPane.addTab("Autotile Background", panel_autotilebg);
		getContentPane().add(tabbedPane);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setVisible(true);
	}

	public Image getSelectedImage()
	{
		if (imagePanel.selectedImage == -1)
			return null;

		Vector curImages = (Vector) imagePanel.imageFiles.get(lvzSelector
				.getSelectedIndex());

		LvzImage sIm = ((LvzImage) curImages.get(imagePanel.selectedImage));

		if (sIm.xFrames == 1 && sIm.yFrames == 1)
			return sIm.image.i;
		else
		{
			int w = sIm.image.i.getWidth(null) / sIm.xFrames;
			int h = sIm.image.i.getHeight(null) / sIm.yFrames;

			Image i = parent.createImage(w, h);
			Graphics g = i.getGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, w, h);
			g.drawImage(sIm.image.i, 0, 0, w, h, 0, 0, w, h, null);
			g.setColor(Color.RED);
			g.drawString("Animation", 2, h - 2);

			return ImageLoader.makeBlackTransparent(i);
		}
	}

	public void deleteMapObject(MapObject o)
	{
		parent.modified = true;
		for (int layer = MapObject.BELOWALL; layer < MapObject.NUMLAYERS; ++layer)
		{
			for (int num = 0; num < imagePanel.imageFiles.size(); ++num)
			{
				// for each lvz file
				Vector lvzImages = (Vector) imagePanel.imageFiles.get(num);
				Vector mapObjs = (Vector) mapObjects.get(num);

				for (int c = 0; c < mapObjs.size(); ++c)
				{ // for each map object
					MapObject mo = (MapObject) mapObjs.get(c);

					if (mo != o)
						continue;

					selectedMO.remove(mo);
					mapObjs.removeElementAt(c);
					updateMOgui();

					parent.repaint();

					return;
				}

			}
		}
	}

	public void deleteSelection()
	{
		parent.modified = true;
		for (int num = 0; num < imagePanel.imageFiles.size(); ++num)
		{
			// for each lvz file
			Vector lvzImages = (Vector) imagePanel.imageFiles.get(num);
			Vector mapObjs = (Vector) mapObjects.get(num);

			Set k = selectedMO.keySet();
			Iterator i = k.iterator();

			while (i.hasNext())
			{
				MapObject mo = (MapObject) i.next();
				boolean x = mapObjs.remove(mo);
				if (x)
					parent.undoer.deleteMapObject(parent, mo);

			}
			updateMOgui();

			parent.repaint();

			selectedMO.clear();
			// return;

		}
	}

	public void selectLvz(int xPos, int yPos)
	{
		// selectedRect = null;

		for (int layer = MapObject.BELOWALL; layer < MapObject.NUMLAYERS; ++layer)
		{
			for (int num = 0; num < imagePanel.imageFiles.size(); ++num)
			{
				// for each lvz file
				Vector lvzImages = (Vector) imagePanel.imageFiles.get(num);
				Vector mapObjs = (Vector) mapObjects.get(num);

				for (int c = 0; c < mapObjs.size(); ++c)
				{ // for each map object
					MapObject mo = (MapObject) mapObjs.get(c);

					if (mo.layer != layer)
						continue;

					LvzImage sIm = ((LvzImage) lvzImages.get(mo.imageIndex));

					int w = sIm.image.i.getWidth(null) / sIm.xFrames;
					int h = sIm.image.i.getHeight(null) / sIm.yFrames;

					double x = mo.x;
					double y = mo.y;

					Rectangle r = new Rectangle((int) x, (int) y, w, h);

					if (r.contains(xPos, yPos))
					{
						if (parent.m_parent.m_main.isSingleLVZSelectionMode())
							selectedMO.clear();

						selectedMO.put(mo, r);
						tabbedPane.setSelectedIndex(2);
						c_mo_lvz.setSelectedIndex(num);
						s_mo_num.setValue(new Integer(c));

					}

				}

			}
		}

		// if (selectedMO.size() != 0)
		// {
		// parent.m_parent.m_main.requestFocus();
		// }
	}

	public void saveLvz(File lvlFile, ProgressFrame pf)
	{
		String name = lvlFile.getAbsolutePath();

		if (name.length() < 4)
			return;

		String lvzDirectory = name.substring(0, name.length() - 4) + " files"
				+ File.separator;

		File lvzDir = new File(lvzDirectory);

		deleteDirectory(lvzDir); // clean it

		boolean made = lvzDir.mkdirs();

		if (!made)
		{
			if (!lvzDir.exists())
			{
				JOptionPane
						.showMessageDialog(
								null,
								"Problem creating directory "
										+ lvzDir.getAbsolutePath()
										+ "\nPerhaps you have the directory open in another window. Try closing that first\n"
										+ "(if it hasn't closed it on it's own already) and close all programs that may have files\n"
										+ "open in that directory(like paintshop), and then try to save again.");
			}
		}

		for (int x = 0; x < lvzSelector.getItemCount(); ++x)
		{
			String s = (String) lvzSelector.getItemAt(x);

			if (s.equals("(none)"))
				continue;

			pf.setProgress("Saving " + s + ".lvz");
			String newDir = lvzDirectory + s + File.separator;

			File dir = new File(newDir);

			made = dir.mkdirs();

			if (!made)
			{
				if (!dir.exists())
				{
					JOptionPane
							.showMessageDialog(
									null,
									"Problem creating directory "
											+ dir.getAbsolutePath()
											+ "\nPerhaps you have the directory open in another window. Try closing that first\n"
											+ "(if window's hasn't closed it on it's own) and close all programs that may have files\n"
											+ "open in that directory(like paintshop), and then try to save again.");

					continue;
				}
			}

			Vector images = (Vector) imagePanel.imageFiles.get(x);

			for (int y = 0; y < images.size(); ++y)
			{
				LvzImage li = (LvzImage) images.get(y);
				// Image i = li.image;
				String filename = dir.getAbsolutePath() + File.separator + s
						+ "_image" + y + li.image.getExtention();

				li.image.saveImage(filename, this);
			}

			Vector mapobjects = (Vector) mapObjects.get(x);

			makeIniFile(dir.getAbsolutePath() + File.separator + s + ".ini", s
					+ ".lvz", images, mapobjects, s);

			String thePath = dir.getAbsolutePath() + File.separator + s
					+ ".ini";

			int ok = LvzFiling.buildLvz(thePath);

			if (ok == CreateLvz.ERROR_NONE)
			{
				String lvzLocation = dir.getAbsolutePath() + File.separator + s
						+ ".lvz";
				LvzFiling.copyFile(lvzLocation, lvzDirectory + s + ".lvz");
			}
			else
				JOptionPane.showMessageDialog(null,
						"LVZ build failed/interrupted:\n"
								+ CreateLvz.errors[ok]);
		}

		return;
	}

	public void makeIniFile(String iniFile, String output, Vector images,
			Vector mapobjects, String lvzName)
	{

		PrintWriter out = null;
		try
		{

			out = new PrintWriter(new BufferedWriter(new FileWriter(iniFile)));

			out.println("; Generated By 2Dragon's / Bak's lvl/lvz editor");
			out.println("OutFile=" + output);
			out.println("");

			for (int x = 0; x < images.size(); ++x)
			{
				LvzImage l = (LvzImage) images.get(x);
				out.println("File=" + lvzName + "_image" + x
						+ l.image.getExtention());
			}
			out.println("");
			out.println("[objectimages]");

			for (int x = 0; x < images.size(); ++x)
			{
				LvzImage l = (LvzImage) images.get(x);
				out.println("IMAGE" + x + "=" + lvzName + "_image" + x
						+ l.image.getExtention() + "," + l.xFrames + ","
						+ l.yFrames + "," + l.animationTime);
			}

			out.println("");
			out.println("[mapobjects]");
			for (int x = 0; x < mapobjects.size(); ++x)
			{
				MapObject m = (MapObject) mapobjects.get(x);
				out.println(m.x + "," + m.y + ",IMAGE" + m.imageIndex + ","
						+ MapObject.layers[m.layer] + ","
						+ MapObject.modes[m.mode] + "," + m.displayTime + ","
						+ m.id);
			}

			out.println("");
			out.println("[screenobjects]");

		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, e.toString());

		}

		out.close();
	}

	public void deleteDirectory(File dir)
	{
		if (dir.isDirectory())
		{
			File[] dirFiles = dir.listFiles();

			for (int i = 0; i < dirFiles.length; i++)
			{
				if (dirFiles[i].isDirectory())
					deleteDirectory(dirFiles[i]);
				else
					dirFiles[i].delete();
			}

			dir.delete();
		}
	}

	public void loadLvzImages(File lvlFile, ProgressFrame pf)
	{
		String name = lvlFile.getAbsolutePath();

		if (name.length() < 4)
			return;

		String lvzDirectory = name.substring(0, name.length() - 4) + " files"
				+ File.separator;

		File dir = new File(lvzDirectory);

		if (!dir.exists())
			return;

		File[] files = dir.listFiles();

		for (int x = 0; x < files.length; ++x)
		{
			String path = files[x].getAbsolutePath();
			if (path.length() > 4)
			{
				String extention = path.substring(path.length() - 4);

				if (extention.equals(".lvz"))
				{
					pf.setProgress("Loading " + path);
					String iniFile = importLvz(files[x]);

					if (iniFile != null)
					{
						importFromIni(iniFile);
					}
				}
			}
		}

	}

	public boolean isSnapToTile()
	{
		return ch_snap.isSelected();
	}

	public void updateMOgui()
	{

		if (mapObjects.size() == 0) // no lvz
		{
			b_mo_delete.setEnabled(false);
			s_mo_num.setEnabled(false);
			s_mo_x.setEnabled(false);
			s_mo_y.setEnabled(false);
			c_mo_layer.setEnabled(false);
			c_mo_mode.setEnabled(false);
			s_mo_displayTime.setEnabled(false);
			s_mo_id.setEnabled(false);
			s_mo_imageId.setEnabled(false);
		}
		else
		{
			Vector selectedObjects = (Vector) mapObjects.get(c_mo_lvz
					.getSelectedIndex());

			if (selectedObjects == null || selectedObjects.size() == 0) // no
																		// objects
			{
				b_mo_delete.setEnabled(false);
				s_mo_num.setEnabled(false);
				s_mo_x.setEnabled(false);
				s_mo_y.setEnabled(false);
				c_mo_layer.setEnabled(false);
				c_mo_mode.setEnabled(false);
				s_mo_displayTime.setEnabled(false);
				s_mo_id.setEnabled(false);
				s_mo_imageId.setEnabled(false);
			}
			else
			{
				b_mo_delete.setEnabled(true);
				s_mo_num.setEnabled(true);
				s_mo_x.setEnabled(true);
				s_mo_y.setEnabled(true);
				c_mo_layer.setEnabled(true);
				c_mo_mode.setEnabled(true);
				s_mo_displayTime.setEnabled(true);
				s_mo_id.setEnabled(true);
				s_mo_imageId.setEnabled(true);

				s_mo_num.setModel(new SpinnerNumberModel(0, 0, selectedObjects
						.size() - 1, 1));

				Vector images = (Vector) imagePanel.imageFiles.get(c_mo_lvz
						.getSelectedIndex());

				if (images != null && images.size() > 0)
				{
					s_mo_imageId.setModel(new SpinnerNumberModel(0, 0, images
							.size() - 1, 1));
				}

				updateSelectedMapObject();
			}
		}
	}

	public void updateSelectedMapObject()
	{
		int index = ((Number) s_mo_num.getValue()).intValue();
		Vector mo = (Vector) mapObjects.get(c_mo_lvz.getSelectedIndex());
		MapObject m = (MapObject) mo.get(index);

		s_mo_x.setValue(new Integer(m.x));
		s_mo_y.setValue(new Integer(m.y));
		s_mo_displayTime.setValue(new Double((double) m.displayTime / 100.0));
		// s_mo_displayTime.setValue("0.00");

		s_mo_id.setValue(new Integer(m.id));
		s_mo_imageId.setValue(new Integer(m.imageIndex));

		if (m.layer == MapObject.AFTERBACKGROUND)
			c_mo_layer.setSelectedItem("AfterBackground");
		else if (m.layer == MapObject.AFTERCHAT)
			c_mo_layer.setSelectedItem("AfterChat");
		else if (m.layer == MapObject.AFTERGAUGES)
			c_mo_layer.setSelectedItem("AfterGauges");
		else if (m.layer == MapObject.AFTERSHIPS)
			c_mo_layer.setSelectedItem("AfterShips");
		else if (m.layer == MapObject.AFTERTILES)
			c_mo_layer.setSelectedItem("AfterTiles");
		else if (m.layer == MapObject.AFTERWEAPONS)
			c_mo_layer.setSelectedItem("AfterWeapons");
		else if (m.layer == MapObject.BELOWALL)
			c_mo_layer.setSelectedItem("BelowAll");
		else if (m.layer == MapObject.TOPMOST)
			c_mo_layer.setSelectedItem("TopMost");

		if (m.mode == MapObject.DEATH)
			c_mo_mode.setSelectedItem("Death");
		else if (m.mode == MapObject.ENTERARENA)
			c_mo_mode.setSelectedItem("EnterArena");
		else if (m.mode == MapObject.ENTERZONE)
			c_mo_mode.setSelectedItem("EnterZone");
		else if (m.mode == MapObject.KILL)
			c_mo_mode.setSelectedItem("Kill");
		else if (m.mode == MapObject.SHOWALWAYS)
			c_mo_mode.setSelectedItem("ShowAlways");
		else if (m.mode == MapObject.SERVERCONTROLLED)
			c_mo_mode.setSelectedItem("ServerControlled");
	}

	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == s_mo_num)
		{
			updateSelectedMapObject();
		}
		else
		{
			int index = ((Number) s_mo_num.getValue()).intValue();
			Vector mo = (Vector) mapObjects.get(c_mo_lvz.getSelectedIndex());
			MapObject m = (MapObject) mo.get(index);

			MapObject before = m.getCopy();

			if (e.getSource() == s_mo_x)
			{
				Vector im = (Vector) imagePanel.imageFiles.get(c_mo_lvz
						.getSelectedIndex());
				LvzImage l = (LvzImage) im.get(m.imageIndex);
				Rectangle r = new Rectangle(m.x, m.y, l.image.i.getWidth(null),
						l.image.i.getHeight(null));

				if (((Number) s_mo_x.getValue()).intValue() != m.x)
					parent.modified = true;

				m.x = ((Number) s_mo_x.getValue()).intValue();
				if (selectedMO.size() == 0)
					selectedMO.put(m, new Rectangle());

				Rectangle selectedRect = (Rectangle) selectedMO.get(m);

				if (selectedRect == null)
				{
					selectedMO.put(m, r);
					selectedRect = r;
				}
				selectedRect.x = m.x;
				selectedRect.y = m.y;
				selectedRect.width = l.image.getWidth(null) / l.xFrames;
				selectedRect.height = l.image.getHeight(null) / l.yFrames;

				repaintMap();
			}
			else if (e.getSource() == s_mo_y)
			{
				Vector im = (Vector) imagePanel.imageFiles.get(c_mo_lvz
						.getSelectedIndex());
				LvzImage l = (LvzImage) im.get(m.imageIndex);
				Rectangle r = new Rectangle(m.x, m.y, l.image.getWidth(null),
						l.image.getHeight(null));

				if (((Number) s_mo_y.getValue()).intValue() != m.y)
					parent.modified = true;

				m.y = ((Number) s_mo_y.getValue()).intValue();

				Rectangle selectedRect = (Rectangle) selectedMO.get(m);
				if (selectedRect == null)
				{
					selectedMO.put(m, r);
					selectedRect = r;
				}
				selectedRect.x = m.x;
				selectedRect.y = m.y;
				selectedRect.width = l.image.getWidth(null) / l.xFrames;
				selectedRect.height = l.image.getHeight(null) / l.yFrames;

				repaintMap();
			}
			else if (e.getSource() == s_mo_displayTime)
			{
				m.displayTime = (int) (((Number) s_mo_displayTime.getValue())
						.doubleValue() * 100);
			}
			else if (e.getSource() == s_mo_id)
			{
				m.id = ((Number) s_mo_id.getValue()).intValue();
			}
			else if (e.getSource() == s_mo_imageId)
			{
				m.imageIndex = ((Number) s_mo_imageId.getValue()).intValue();
				repaintMap();
			}

			if (!before.equals(m))
				parent.undoer.setLvzProperties(parent, before, m);
		}
	}

	public void itemStateChanged(ItemEvent evt)
	{
		if (evt.getSource() == lvzSelector)
		{
			imagePanel.repaint();
		}
		else if (evt.getSource() == c_mo_lvz)
		{
			updateMOgui();
		}
		else
		{
			int index = ((Number) s_mo_num.getValue()).intValue();
			Vector mo = (Vector) mapObjects.get(c_mo_lvz.getSelectedIndex());
			MapObject m = (MapObject) mo.get(index);

			if (evt.getSource() == c_mo_layer)
			{
				String s = (String) c_mo_layer.getSelectedItem();

				for (int x = 0; x < 8; ++x)
				{
					if (s.equals(MapObject.layers[x]))
					{
						m.layer = x;
						break;
					}
				}

				repaintMap();
			}
			else if (evt.getSource() == c_mo_mode)
			{
				String s = (String) c_mo_mode.getSelectedItem();

				for (int x = 0; x < 6; ++x)
				{
					if (s.equals(MapObject.modes[x]))
					{
						m.mode = x;
						break;
					}
				}
			}
		}
	}

	public void addMapObject(int x, int y)
	{
		parent.modified = true;
		Vector curImages = (Vector) imagePanel.imageFiles.get(lvzSelector
				.getSelectedIndex());

		LvzImage l = ((LvzImage) curImages.get(imagePanel.selectedImage));

		MapObject o = new MapObject();

		o.displayTime = (int) (((Number) s_displayTime.getValue())
				.doubleValue() * 100);

		o.id = (int) ((Number) s_id.getValue()).doubleValue();
		o.imageIndex = imagePanel.selectedImage;
		o.layer = 7 - c_layer.getSelectedIndex();
		o.mode = c_mode.getSelectedIndex();
		o.x = x;
		o.y = y;

		Vector selectedObjects = (Vector) mapObjects.get(lvzSelector
				.getSelectedIndex());
		selectedObjects.add(o);

		updateMOgui();

		repaintMap();

		parent.undoer.addMapObject(parent, o);
	}

	public void addMapObject(MapObject o)
	{
		parent.modified = true;
		Vector curImages = (Vector) imagePanel.imageFiles.get(lvzSelector
				.getSelectedIndex());

		LvzImage l = ((LvzImage) curImages.get(imagePanel.selectedImage));

		Vector selectedObjects = (Vector) mapObjects.get(lvzSelector
				.getSelectedIndex());
		selectedObjects.add(o);

		updateMOgui();

		repaintMap();

		// since this function is only called by the undoer, we don't need to
		// add map object to
		// the undoer
	}

	public void repaintMap()
	{
		parent.repaint();
	}

	public void paintUnderTilesLvz(Graphics g, int scale, int startx, int endx,
			int starty, int endy)
	{
		Rectangle view = new Rectangle(startx * 16, starty * 16,
				(endx - startx) * 16, (endy - starty) * 16);

		for (int layer = MapObject.BELOWALL; layer < MapObject.AFTERTILES; ++layer)
		{

			for (int num = 0; num < imagePanel.imageFiles.size(); ++num)
			{
				// for each lvz file
				Vector lvzImages = (Vector) imagePanel.imageFiles.get(num);
				Vector mapObjs = (Vector) mapObjects.get(num);

				if (mapObjs != null && lvzImages != null)
				{

					for (int c = 0; c < mapObjs.size(); ++c)
					{ // for each map object
						MapObject mo = (MapObject) mapObjs.get(c);

						if (mo.layer != layer)
							continue;

						LvzImage sIm = ((LvzImage) lvzImages.get(mo.imageIndex));

						int w = sIm.image.getWidth(null) / sIm.xFrames;
						int h = sIm.image.getHeight(null) / sIm.yFrames;

						double x = mo.x;
						double y = mo.y;

						Rectangle r = new Rectangle((int) x, (int) y, w, h);

						if (view.intersects(r))
						{ // draw it
							Image image = null;

							if (sIm.xFrames == 1 && sIm.yFrames == 1)
							{
								image = sIm.image.i;
							}
							else
							{
								Image i = parent.createImage(w, h);
								Graphics gr = i.getGraphics();
								gr.setColor(Color.black);
								gr.fillRect(0, 0, w, h);
								gr.drawImage(sIm.image.i, 0, 0, w, h, 0, 0, w,
										h, null);
								gr.setColor(Color.RED);
								gr.drawString("Animation", 2, h - 2);

								image = ImageLoader.makeBlackTransparent(i);
							}

							g.drawImage(image, (int) (x / 16.0 * scale),
									(int) (y / 16.0 * scale),
									(int) ((x + w) / 16.0 * scale),
									(int) ((y + h) / 16.0 * scale), 0, 0, w, h,
									null);
						}

					}
				}

			}
		}

	}

	public void paintAboveTilesLvz(Graphics g, int scale, int startx, int endx,
			int starty, int endy)
	{
		Rectangle view = new Rectangle(startx * 16, starty * 16,
				(endx - startx) * 16, (endy - starty) * 16);

		for (int layer = MapObject.AFTERTILES; layer <= MapObject.TOPMOST; ++layer)
		{

			for (int num = 0; num < imagePanel.imageFiles.size(); ++num)
			{
				// for each lvz file
				Vector lvzImages = (Vector) imagePanel.imageFiles.get(num);
				Vector mapObjs = (Vector) mapObjects.get(num);

				if (mapObjs != null && lvzImages != null)
				{

					for (int c = 0; c < mapObjs.size(); ++c)
					{ // for each map object
						MapObject mo = (MapObject) mapObjs.get(c);

						if (mo.layer != layer)
							continue;

						LvzImage sIm = ((LvzImage) lvzImages.get(mo.imageIndex));

						int w = sIm.image.getWidth(null) / sIm.xFrames;
						int h = sIm.image.getHeight(null) / sIm.yFrames;

						double x = mo.x;
						double y = mo.y;

						Rectangle r = new Rectangle((int) x, (int) y, w, h);

						if (view.intersects(r))
						{ // draw it
							Image image = null;

							if (sIm.xFrames == 1 && sIm.yFrames == 1)
							{
								image = sIm.image.i;
							}
							else
							{
								Image i = parent.createImage(w, h);
								Graphics gr = i.getGraphics();
								gr.setColor(Color.black);
								gr.fillRect(0, 0, w, h);
								gr.drawImage(sIm.image.i, 0, 0, w, h, 0, 0, w,
										h, null);
								gr.setColor(Color.RED);
								gr.drawString("Animation", 2, h - 2);

								image = ImageLoader.makeBlackTransparent(i);
							}

							g.drawImage(image, (int) (x / 16.0 * scale),
									(int) (y / 16.0 * scale),
									(int) ((x + w) / 16.0 * scale),
									(int) ((y + h) / 16.0 * scale), 0, 0, w, h,
									null);
						}

					}
				}

			}
		}

		if (parent.currentTool == Tools.SELECT
				|| parent.currentTool == Tools.LVZ_SELECTION)
		{
			g.setColor(Color.MAGENTA);
			Set k = selectedMO.keySet();

			Iterator i = k.iterator();
			while (i.hasNext())
			{
				MapObject mo = (MapObject) i.next();
				Rectangle rec = (Rectangle) selectedMO.get(mo);
				g.drawRect(rec.x * scale / 16, scale * rec.y / 16, rec.width
						* scale / 16, rec.height * scale / 16);
			}

		}

	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == b_mo_delete)
		{
			parent.modified = true;
			deleteSelection();

			repaintMap();
		}
		else if (e.getSource() == importLvz)
		{
			if (parent.m_parent.m_lvlFile.m_file == null)
			{
				int rv = JOptionPane.showConfirmDialog(null,
						"You must save the .lvl before you can import lvz files."
								+ "\nWould you like to save it now?",
						"Save LVL?", JOptionPane.YES_NO_OPTION);

				if (rv == JOptionPane.YES_OPTION)
					parent.m_parent.m_main.cmdSave();
			}

			if (parent.m_parent.m_lvlFile.m_file != null)
			{
				String iniFile = importLvz();
				if (iniFile != null)
				{
					parent.modified = true;
					importFromIni(iniFile);

				}
			}
		}
		else if (e.getSource() == newLvz)
		{
			boolean ok = false;
			String name = "Untitled";

			while (!ok)
			{

				name = (String) JOptionPane.showInputDialog(null,
						"LVZ Name (no .lvz):", name);

				if (name == null) // cancel
					return;

				ok = true;

				for (int x = 0; x < lvzSelector.getItemCount(); ++x)
				{
					if (name.toLowerCase().equals(
							((String) lvzSelector.getItemAt(x)).toLowerCase()))
					{
						JOptionPane
								.showMessageDialog(null,
										"An LVZ file with this name is already being used by this map.");

						ok = false;
					}
				}

				if (name.length() == 0)
				{
					ok = false;

					JOptionPane.showMessageDialog(null,
							"The name cannot be \"\"");
				}
				else if (name.charAt(name.length() - 1) == '.')
				{
					JOptionPane.showMessageDialog(null,
							"The name cannot end in a period.");
					ok = false;
				}
			}

			parent.modified = true;
			imagePanel.addLvzImages(new Vector());

			if (lvzSelector.getItemAt(0).equals("(none)"))
			{
				lvzSelector.removeItemAt(0);
				c_mo_lvz.removeItemAt(0);
			}

			lvzSelector.insertItemAt(name, 0);
			lvzSelector.setSelectedIndex(0);

			c_mo_lvz.insertItemAt(name, 0);
			c_mo_lvz.setSelectedIndex(0);

			Vector objs = new Vector();
			mapObjects.insertElementAt(objs, 0);
			updateMOgui();

			newImage.setEnabled(true);
			importImage.setEnabled(true);
			removeLvz.setEnabled(true);
		}
		else if (e.getSource() == removeLvz)
		{
			int rv = JOptionPane
					.showConfirmDialog(
							null,
							"Are you sure you want to remove the selected LVZ file from the map?",
							"Confirm Remove", JOptionPane.YES_NO_OPTION);

			if (rv != JOptionPane.YES_OPTION)
				return;

			parent.modified = true;

			imagePanel.removeLvz(lvzSelector.getSelectedIndex());
			mapObjects.remove(lvzSelector.getSelectedIndex());

			if (imagePanel.imageFiles.size() == 0)
			{
				importImage.setEnabled(false);
				newImage.setEnabled(false);
				removeLvz.setEnabled(false);
				lvzSelector.insertItemAt("(none)", 0);
				c_mo_lvz.insertItemAt("(none)", 0);
			}

			c_mo_lvz.removeItemAt(lvzSelector.getSelectedIndex());
			lvzSelector.removeItemAt(lvzSelector.getSelectedIndex());
			updateMOgui();

			repaintMap();
		}
		else if (e.getSource() == editImage)
		{
			Image i = imagePanel.getSelectedImage();

			Image newI = null;

			if (i.getWidth(null) > 512 || i.getHeight(null) > 512)
			{
				String path = new File("").getAbsolutePath();
				String filename = path + File.separator + "temp"
						+ File.separator + "temp.png";

				File outputFile = new File(filename);
				outputFile.mkdirs();

				try
				{
					int width = i.getWidth(null);
					int height = i.getHeight(null);

					BufferedImage bi = (BufferedImage) createImage(width,
							height);

					Graphics g = bi.getGraphics();
					g.setColor(Color.black);
					g.fillRect(0, 0, bi.getWidth(null), bi.getHeight(null));
					g.drawImage(i, 0, 0, null);

					ImageIO.write(bi, "PNG", outputFile);
					Process p = Runtime.getRuntime().exec(
							"cmd /c " + " \"" + filename + "\"");
					p.waitFor();

					File file = new File(filename);
					FileImageInputStream fiis = new FileImageInputStream(file);

					newI = (Image) ImageIO.read(fiis);
				}
				catch (Exception er)
				{
					JOptionPane.showMessageDialog(null, er.toString());
				}

			}
			else
			{
				newI = ImageDialog.getImage(parent.m_parent.m_main, i);
			}

			if (newI == null) // cancel
				return;

			parent.modified = true;
			imagePanel.changeSelectedImage(newI);
			repaintMap();

		}
		else if (e.getSource() == newImage)
		{
			Image i = ImageDialog.getImage(parent.m_parent.m_main, 512, 512);

			if (i == null) // cancel
				return;

			Object[] options = { "Image", "Animation" };
			int n = JOptionPane.showOptionDialog(null,
					"Would you like this to be an image or an animation?",
					"Image Type", JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE, null, // don't use a custom
														// Icon
					options, // the titles of buttons
					options[0]); // default button title
			int xFrames = 1, yFrames = 1;
			int animTime = 100;

			if (n == JOptionPane.NO_OPTION) // animation
			{
				boolean ok;

				do
				{
					String s = (String) JOptionPane.showInputDialog(null,
							"X Frames:", "1");

					if (s == null)
					{
						ok = false;
						continue;
					}

					try
					{
						xFrames = Integer.parseInt(s);

						if (xFrames > 0)
							ok = true;
						else
							ok = false;
					}
					catch (NumberFormatException er)
					{
						ok = false;
					}

					if (!ok)
						JOptionPane.showMessageDialog(null,
								"Please only enter positive whole numbers.");
				}
				while (!ok);

				do
				{
					String s = (String) JOptionPane.showInputDialog(null,
							"Y Frames:", "1");

					if (s == null)
					{
						ok = false;
						continue;
					}

					try
					{
						yFrames = Integer.parseInt(s);

						if (yFrames > 0)
							ok = true;
						else
							ok = false;
					}
					catch (NumberFormatException er)
					{
						ok = false;
					}

					if (!ok)
						JOptionPane.showMessageDialog(null,
								"Please only enter positive whole numbers.");
				}
				while (!ok);

				do
				{
					String s = (String) JOptionPane.showInputDialog(null,
							"Animation Time(seconds):", "1.0");

					if (s == null)
					{
						ok = false;
						continue;
					}

					try
					{
						animTime = (int) (Double.parseDouble(s) * 100);

						if (animTime > 0)
							ok = true;
						else
							ok = false;
					}
					catch (NumberFormatException er)
					{
						ok = false;
					}

					if (!ok)
						JOptionPane.showMessageDialog(null,
								"Please only enter positive numbers.");
				}
				while (!ok);
			}

			parent.modified = true;
			LvzImage l = new LvzImage();
			l.index = imagePanel.imageFiles.size();
			l.xFrames = xFrames;
			l.yFrames = yFrames;
			l.animationTime = animTime;
			l.image = new FileImage(i);

			imagePanel.addImage(l);

			editImage.setEnabled(true);
			removeImage.setEnabled(true);

		}
		else if (e.getSource() == removeImage)
		{
			parent.modified = true;
			imagePanel.removeSelectedImage();
			repaintMap();
		}
		else if (e.getSource() == importImage)
		{
			parent.modified = true;
			
			fc.setDialogTitle("Import Image");
			int returnVal = fc.showOpenDialog(this);

			if (returnVal != JFileChooser.APPROVE_OPTION) // user pressed cancel
				return;
			 
			File[] selected = fc.getSelectedFiles();
			
			for (int index = 0; index < selected.length; ++index)
			{
				FileImage i = ImageLoader.loadImage(selected[index].getAbsolutePath());

				if (i == null) // cancel
					continue;
	
				Object[] options = { "Image", "Animation" };
				int n = JOptionPane.showOptionDialog(null,
						"Would you like this to be an image or an animation?",
						selected[index].getName(), JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, // don't use a custom
															// Icon
						options, // the titles of buttons
						options[0]); // default button title
				int xFrames = 1, yFrames = 1;
				int animTime = 100;
	
				if (n == JOptionPane.NO_OPTION) // animation
				{
					boolean ok;
	
					do
					{
						String s = (String) JOptionPane.showInputDialog(null,
								"X Frames:", "1");
	
						if (s == null)
						{
							ok = false;
							continue;
						}
	
						try
						{
							xFrames = Integer.parseInt(s);
	
							if (xFrames > 0)
								ok = true;
							else
								ok = false;
						}
						catch (NumberFormatException er)
						{
							ok = false;
						}
	
						if (!ok)
							JOptionPane.showMessageDialog(null,
									"Please only enter positive whole numbers.");
					}
					while (!ok);
	
					do
					{
						String s = (String) JOptionPane.showInputDialog(null,
								"Y Frames:", "1");
	
						if (s == null)
						{
							ok = false;
							continue;
						}
	
						try
						{
							yFrames = Integer.parseInt(s);
	
							if (yFrames > 0)
								ok = true;
							else
								ok = false;
						}
						catch (NumberFormatException er)
						{
							ok = false;
						}
	
						if (!ok)
							JOptionPane.showMessageDialog(null,
									"Please only enter positive whole numbers.");
					}
					while (!ok);
	
					do
					{
						String s = (String) JOptionPane.showInputDialog(null,
								"Animation Time(seconds):", "1.0");
	
						if (s == null)
						{
							ok = false;
							continue;
						}
	
						try
						{
							animTime = (int) (Double.parseDouble(s) * 100);
	
							if (animTime > 0)
								ok = true;
							else
								ok = false;
						}
						catch (NumberFormatException er)
						{
							ok = false;
						}
	
						if (!ok)
							JOptionPane.showMessageDialog(null,
									"Please only enter positive numbers.");
					}
					while (!ok);
				}
	
				LvzImage l = new LvzImage();
				l.index = imagePanel.imageFiles.size();
				l.xFrames = xFrames;
				l.yFrames = yFrames;
				l.animationTime = animTime;
				l.image = i;
	
				imagePanel.addImage(l);
			}

			editImage.setEnabled(true);
			removeImage.setEnabled(true);
		}
	}

	// returns .ini filename or NULL if there was a problem / cancel pressed
	private String importLvz()
	{
		fileDialog.setMode(FileDialog.LOAD);
		fileDialog.setTitle("Import Lvz");
		fileDialog.show();
		String file = fileDialog.getFile();
		String dir = fileDialog.getDirectory();

		if (file == null) // user pressed cancel
			return null;

		if (file.length() < 5
				|| !file.substring(file.length() - 4).equals(".lvz"))
		{
			JOptionPane.showMessageDialog(null,
					"Only files with a .lvz extentions are allowed.");
			return null;
		}

		if (parent.m_parent.m_lvlFile.m_file == null)
		{
			JOptionPane.showMessageDialog(null,
					"You must save the .lvl before you can import lvz files.");
			return null;
		}

		if (parent.m_parent.m_lvlFile.m_file.getName().length() < 4)
		{
			JOptionPane.showMessageDialog(null,
					"The .lvl file doesn't end in .lvl?");
			return null;
		}

		String lvlDirectory = parent.m_parent.m_lvlFile.m_file.getParent()
				+ File.separator;
		String lvlName = parent.m_parent.m_lvlFile.m_file.getName().substring(
				0, parent.m_parent.m_lvlFile.m_file.getName().length() - 4);

		String selectedFile = dir + file;
		String destination = lvlDirectory + lvlName + " files" + File.separator
				+ file;

		boolean ok = LvzFiling.copyFile(selectedFile, destination);

		if (!ok)
			return null;

		// debuild this .lvz
		// ok = LvzFiling.debuildLvz(dir + File.separator + file);
		ok = LvzFiling.debuildLvz(destination);

		if (!ok)
			return null;

		File f = new File(destination);
		f.delete(); // so it doesn't get loaded, w/o being saved

		String noExtention = file.substring(0, file.length() - 4);

		String iniFile = lvlDirectory + lvlName + " files" + File.separator
				+ noExtention + File.separator + noExtention + ".ini";

		return iniFile;
	}

	private String importLvz(File file) // opens in place
	{
		String selectedFile = file.getAbsolutePath();

		// debuild this .lvz
		boolean ok = LvzFiling.debuildLvz(file.getAbsolutePath());

		if (!ok)
			return null;

		String noExtention = selectedFile.substring(0,
				selectedFile.length() - 4);

		String name = "";
		for (int x = noExtention.length() - 1; x >= 0; --x)
		{
			if (noExtention.charAt(x) == File.separatorChar)
				break;

			name = noExtention.charAt(x) + name;
		}

		String iniFile = noExtention + File.separator + name + ".ini";

		return iniFile;
	}

	private void importFromIni(String iniFile)
	{
		File f = new File(iniFile);

		if (f.exists())
		{
			String name = f.getName().substring(0, f.getName().length() - 4);

			for (int x = 0; x < lvzSelector.getItemCount(); ++x)
			{
				if (name.toLowerCase().equals(
						((String) lvzSelector.getItemAt(x)).toLowerCase()))
				{
					JOptionPane
							.showMessageDialog(null,
									"An LVZ file with this name is already being used by this map.");

					return;
				}
			}

			if (lvzSelector.getItemAt(0).equals("(none)"))
			{
				lvzSelector.removeItemAt(0);
				c_mo_lvz.removeItemAt(0);
			}

			Vector images = LvzFiling.loadImages(iniFile);

			if (images != null)
			{
				imagePanel.addLvzImages(images);

				lvzSelector.insertItemAt(name, 0);
				lvzSelector.setSelectedIndex(0);

				c_mo_lvz.insertItemAt(name, 0);
				c_mo_lvz.setSelectedIndex(0);

				Vector objs = LvzFiling.loadMapObjects(iniFile);

				mapObjects.insertElementAt(objs, 0);

				updateMOgui();

				newImage.setEnabled(true);
				importImage.setEnabled(true);
				removeLvz.setEnabled(true);

				int imageSize = 50;
				imagePanel.setPreferredSize(new Dimension((images == null ? 0
						: images.size())
						* (imageSize + 5) + 5, imageSize + 40));

				scrollPane.setViewportView(imagePanel);
			}

			repaintMap();
		}
	}
}

class ImagePanel extends JPanel implements MouseListener
{
	public Vector imageFiles = new Vector();

	Timer animationTimer;

	int selectedImage = -1;

	int lastDisplayedImages = -1;

	LvzImageWindow parent;

	public ImagePanel(LvzImageWindow p)
	{
		addMouseListener(this);
		parent = p;

		animationTimer = new Timer();
		// animationTimer.schedule(new Repainter(),0, 50); // 50 milliseconds
	}

	public void removeLvz(int index)
	{
		imageFiles.remove(index);
		selectedImage = -1;
		parent.removeImage.setEnabled(false);
		parent.editImage.setEnabled(false);
	}

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		if (imageFiles != null)
		{
			if (imageFiles.size() == 0
					|| ((Vector) imageFiles.get(parent.lvzSelector
							.getSelectedIndex())).size() == 0)
			{
				if (lastDisplayedImages != -1)
				{
					lastDisplayedImages = -1;
					selectedImage = -1;

					parent.editImage.setEnabled(false);
					parent.removeImage.setEnabled(false);

					setPreferredSize(new Dimension(1, 1));

					parent.scrollPane.setViewportView(this);
				}

				return;
			}

			int imageSize = 50;

			Vector selectedImages = ((Vector) imageFiles.get(parent.lvzSelector
					.getSelectedIndex()));

			if (lastDisplayedImages != parent.lvzSelector.getSelectedIndex())
			{ // resize required
				lastDisplayedImages = parent.lvzSelector.getSelectedIndex();
				int index = parent.lvzSelector.getSelectedIndex();
				setPreferredSize(new Dimension(selectedImages.size()
						* (imageSize + 5) + 5, imageSize + 40));

				parent.scrollPane.setViewportView(this);

				selectedImage = -1;

				parent.editImage.setEnabled(false);
				parent.removeImage.setEnabled(false);
			}

			g.fillRect(0, 0, getWidth(), imageSize + 6);

			if (selectedImages != null)
			{
				for (int x = 0; x < selectedImages.size(); ++x)
				{
					LvzImage l = (LvzImage) selectedImages.get(x);

					int xCoord = x * (imageSize + 5) + 5;
					int yCoord = 3;

					int numFrames = (l.xFrames * l.yFrames);

					if (l.image == null)
						continue;

					if (numFrames == 1)
					{
						g.drawString("Image", xCoord, imageSize + 27);
						g.drawImage(l.image.i, xCoord, yCoord, xCoord
								+ imageSize, yCoord + imageSize, // destination
								0, 0, l.image.getWidth(null), l.image
										.getHeight(null), null);// source
						g.drawString(l.image.getWidth(null) + "x"
								+ l.image.getHeight(null), xCoord,
								imageSize + 15);
					}
					else if (numFrames > 1 && (l.animationTime / numFrames) > 0)
					{
						g.drawString("Animation", xCoord, imageSize + 27);
						g.drawString((l.image.getWidth(null) / l.xFrames) + "x"
								+ (l.image.getHeight(null) / l.yFrames),
								xCoord, imageSize + 15);

						long centiseconds = (Calendar.getInstance().getTime()
								.getTime()) / 10;
						long frame = (centiseconds / (l.animationTime / numFrames))
								% numFrames;

						int width = (l.image.getWidth(null) / l.xFrames);
						int height = (l.image.getHeight(null) / l.yFrames);

						int xLocation = (int) (frame % l.xFrames) * width;
						int yLocation = (int) (frame / l.xFrames) * height;

						g.drawImage(l.image.i, xCoord, yCoord, xCoord
								+ imageSize, yCoord + imageSize, // destination
								xLocation, yLocation, xLocation + width,
								yLocation + height, null);// source

						g.drawString((frame + 1) + "/" + numFrames, xCoord,
								imageSize + 39);
					}

					if (x == selectedImage)
					{
						Rectangle r = new Rectangle(xCoord - 2, yCoord - 2,
								imageSize + 4, imageSize + 4);
						Graphics2D g2d = (Graphics2D) g;

						g2d.setStroke(new BasicStroke(2));
						g2d.setColor(Color.red);
						g2d.draw(r);
						g2d.setColor(Color.black);
					}
				}
			}
		}
	}

	public void addLvzImages(Vector v)
	{
		imageFiles.insertElementAt(v, 0);

		repaint();
	}

	public void addImage(LvzImage i)
	{
		parent.parent.modified = true;
		Vector selectedImages = ((Vector) imageFiles.get(parent.lvzSelector
				.getSelectedIndex()));

		// i.image = ImageLoader.makeBlackTransparent(i.image);
		selectedImages.add(i);
		int imageSize = 50;

		setPreferredSize(new Dimension(selectedImages.size() * (imageSize + 5)
				+ 5, imageSize + 40));
		parent.scrollPane.setViewportView(this);

		selectedImage = selectedImages.size() - 1;

		repaint();
	}

	public void removeSelectedImage()
	{
		parent.parent.modified = true;

		Vector mos = (Vector) parent.mapObjects.get(parent.lvzSelector
				.getSelectedIndex());

		for (int x = 0; x < mos.size(); ++x)
		{
			while (x < mos.size())
			{
				MapObject m = (MapObject) mos.get(x);

				if (m.imageIndex == selectedImage)
				{
					mos.remove(x);
					continue;
				}
				else if (m.imageIndex > selectedImage)
				{
					m.imageIndex--;
					// from the while loop
				}

				break;
			}
		}

		Vector selectedImages = ((Vector) imageFiles.get(parent.lvzSelector
				.getSelectedIndex()));

		selectedImages.remove(selectedImage);
		selectedImage = -1;

		int imageSize = 50;

		setPreferredSize(new Dimension(selectedImages.size() * (imageSize + 5)
				+ 5, imageSize + 40));
		parent.scrollPane.setViewportView(this);

		repaint();

		parent.editImage.setEnabled(false);
		parent.removeImage.setEnabled(false);
	}

	public Image getSelectedImage()
	{
		Vector selectedImages = ((Vector) imageFiles.get(parent.lvzSelector
				.getSelectedIndex()));

		return ((LvzImage) selectedImages.get(selectedImage)).image.i;
	}

	public void changeSelectedImage(Image newImage)
	{
		Vector selectedImages = ((Vector) imageFiles.get(parent.lvzSelector
				.getSelectedIndex()));
		LvzImage l = (LvzImage) selectedImages.get(selectedImage);

		l.image = new FileImage(newImage);

		repaint();
	}

	public void mouseReleased(MouseEvent e)
	{
	}

	public void mousePressed(MouseEvent e)
	{
		selectedImage = -1;
		parent.editImage.setEnabled(false);
		parent.removeImage.setEnabled(false);

		int imageSize = 50;

		if (imageFiles == null)
			return;

		Vector selectedImages = ((Vector) imageFiles.get(parent.lvzSelector
				.getSelectedIndex()));

		for (int x = 0; x < selectedImages.size(); ++x)
		{
			int xCoord = x * (imageSize + 5) + 5;
			int yCoord = 3;
			Rectangle r = new Rectangle(xCoord, yCoord, imageSize, imageSize);

			if (r.contains(e.getPoint()))
			{
				selectedImage = x;
				parent.parent.m_parent.m_main.setButtons(
						parent.parent.m_parent.m_main.buttonLvz, Tools.LVZ);
				parent.editImage.setEnabled(true);
				parent.removeImage.setEnabled(true);
			}
		}

		repaint();
	}

	public void mouseExited(MouseEvent e)
	{
	}

	public void mouseEntered(MouseEvent e)
	{
	}

	public void mouseClicked(MouseEvent e)
	{
	}

	/*
	 * class Repainter extends TimerTask { public void run() {
	 * //parent.repaintMap(); repaint(); } }
	 */
}