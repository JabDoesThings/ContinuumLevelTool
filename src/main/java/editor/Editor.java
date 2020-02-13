package editor;

import java.awt.Image;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import editor.loaders.BitMap;
import editor.loaders.LevelFile;
import editor.lvz.ProgressFrame;
import editor.xml.XMLNode;

public class Editor extends JScrollPane
{
	// The file where the lvl was opened from
	private File m_file;

	// The main class so we can add windows
	public Main m_main;

	// The levelfile
	public LevelFile m_lvlFile;

	protected String m_fileName;

	protected Image m_tileset;

	protected Image[] m_tiles;

	protected short[][] m_map;

	// The window for which the scrollpane views
	protected LevelWindow m_window;

	/**
	 * Creates a new editor instance, this constructor should be called for a
	 * new lvl file.
	 */
	public Editor(Main main)
	{
		m_main = main;
		// Set unit increments
		getHorizontalScrollBar().setUnitIncrement(16);
		getVerticalScrollBar().setUnitIncrement(16);

		// Create our lvl file
		BitMap bmp = loadDefaultTileset();
		m_lvlFile = new LevelFile(bmp);
		m_tileset = m_lvlFile.getTileSet();

		m_map = m_lvlFile.getMap();

		// Create our display are and set to view it
		m_window = new LevelWindow(this, m_main.getSelectedTool());
		setViewportView(m_window);

		m_tiles = m_lvlFile.getTiles();

		// Set file name
		m_fileName = "(Untitled Map)";
	}

	public Editor(File file, Main main)
	{

		m_file = file;
		m_main = main;

		// Set unit increments
		getHorizontalScrollBar().setUnitIncrement(16);
		getVerticalScrollBar().setUnitIncrement(16);

		// Set file name
		m_fileName = file.getName();
	}
	
	public void hideWindows()
	{
		m_window.hideWindows();
	}

	/**
	 * Load a map
	 * 
	 * @return true iff sucessful
	 */
	public boolean load()
	{

		ProgressFrame pf = new ProgressFrame("Progress");
		String errorWithELVL = null;

		try
		{

			pf.setProgress("Loading " + m_file.getAbsolutePath());

			// Read in bitmap portion of map
			BufferedInputStream bs = new BufferedInputStream(
					new FileInputStream(m_file));
			BitMap bmp = new BitMap(bs);
			bmp.readBitMap(false);
			
			bs.close();

			if (bmp.isBitMap())
			{
				m_lvlFile = new LevelFile(m_file, bmp, true, bmp.hasELVL);
			}
			else
			{
				bmp = loadDefaultTileset();
				m_lvlFile = new LevelFile(m_file, bmp, false, bmp.hasELVL);
			}
			errorWithELVL = m_lvlFile.readLevel();

			if (errorWithELVL != null)
			{

				m_lvlFile = new LevelFile(m_file, bmp, false, false); // attempt
																		// load
																		// without
																		// meta
																		// data
				String error = m_lvlFile.readLevel();

				if (error != null)
				{ // I give up
					System.out.println("First error = " + errorWithELVL);
					System.out.println("NON eLVL Load: " + error);

					throw new IOException("Corrupt LVL File");
				}
				else
				{
					System.out
							.println("NON eLVL Load sucessful! Previous error: "
									+ errorWithELVL);
				}
			}

			m_tileset = m_lvlFile.getTileSet();

			m_map = m_lvlFile.getMap();

			m_tiles = m_lvlFile.getTiles();

			// Create our display area and set to view it
			m_window = new LevelWindow(this, m_main.getSelectedTool());
			setViewportView(m_window);

			if (m_lvlFile.regions != null)
				m_window.m_asssRegions.setRegions(m_lvlFile.regions);

			m_window.m_lvzImages.loadLvzImages(m_file, pf);

			m_window.undoer = new Undoer(m_window);

			String name = m_file.getAbsolutePath();

			if (name.length() < 4)
			{
				pf.hide();
				return true;
			}

			String directory = name.substring(0, name.length() - 4) + " files"
					+ File.separator;

			if (!new File(directory + "setup.xml").exists())
			{
				autoDetectWall(m_map, m_window.m_autotool.getAutoTileset());

				pf.hide();
				return true;
			}

			pf.setProgress("Loading setup.xml");

			XMLNode document = XMLNode.readDocument(directory + "setup.xml");

			XMLNode setup = document.getChild("Setup");
			if (setup == null)
			{
				JOptionPane.showMessageDialog(null,
						"setup.xml problem: no Setup child");
			}
			else
			// setup exists
			{
				XMLNode regions = setup.getChild("Regions");

				if (regions != null)
				{
					m_window.m_asssRegions.loadRegions(regions);
				}

				XMLNode att = setup.getChild("AutotoolTiles");

				if (att != null)
				{
					m_window.m_autotool.loadFromXMLNode(att);
				}

				if (m_window.m_autotool.isEmpty())
				{// Process Map
					autoDetectWall(m_map, m_window.m_autotool.getAutoTileset());
				}
			}
			pf.hide();

			if (errorWithELVL != null)
			{
				System.out.println("Error with eLVL Data!");
				JOptionPane.showMessageDialog(null,
						"Error loading with eLVL Data: " + errorWithELVL);
			}

			return true;

		}
		catch (IOException e)
		{
			// Create our lvl file
			BitMap bmp = loadDefaultTileset();
			m_lvlFile = new LevelFile(bmp);

			m_tileset = m_lvlFile.getTileSet();
			m_map = m_lvlFile.getMap();
			m_tiles = m_lvlFile.getTiles();

			// Set file name
			m_fileName = "(Untitled Map)";

			JOptionPane.showMessageDialog(null, e);

		}

		pf.hide();
		return false;
	}

	public void save()
	{
		String name = m_lvlFile.m_file.getAbsolutePath();

		if (name.length() < 4)
			return;

		ProgressFrame pf = new ProgressFrame("Progress");
		pf.setProgress("Saving " + m_lvlFile.m_file.getAbsolutePath());

		if (m_window.selection != null) // place selection if it exists
		{
			m_window.placeSelection();
			m_window.selection = null;
		}

		m_lvlFile.saveLevel(m_tileset, m_map, m_window.m_asssRegions
				.getRegions());

		m_window.m_lvzImages.saveLvz(m_lvlFile.m_file, pf); // also makes
															// directory

		pf.setProgress("Saving setup.xml");

		XMLNode document = new XMLNode();
		XMLNode setup = document.addChild("Setup");
		setup.children = new Vector();

		// XMLNode regionNode =
		// m_window.m_asssRegions.saveRegions(m_lvlFile.m_file,pf);
		// if (regionNode != null)
		// setup.children.add(regionNode);

		XMLNode attNode = m_window.m_autotool.toXMLNode();
		if (attNode != null)
			setup.children.add(attNode);

		String directory = name.substring(0, name.length() - 4) + " files"
				+ File.separator;

		XMLNode.saveDocument(document, directory + "setup.xml");

		pf.hide();

		m_window.modified = false;

		m_main.buttonPen.doClick();
	}

	public void saveAs(File where)
	{
		m_lvlFile.m_file = where;
		m_fileName = where.getName();

		save();

	}

	/**
	 * Try to find a wall in the map
	 * 
	 * @param map
	 *            the map given
	 * @param wall
	 *            where the wall will be stored
	 */
	private void autoDetectWall(short[][] map, short[][] wall)
	{
		short horz = findLongestHorizonalRun(map);
		short vert = findLongestVerticalRun(map);

		if (horz != -1 && vert != -1)
		{
			short blank = 0;
			// then find the intersections for corners / T / +
			short lower_left = findTile(vert, blank, blank, horz);
			short lower_right = findTile(vert, blank, horz, blank);
			short upper_left = findTile(blank, vert, blank, horz);
			short upper_right = findTile(blank, vert, horz, blank);

			short T_left = findTile(vert, vert, blank, horz);
			short T_right = findTile(vert, vert, horz, blank);
			short T_up = findTile(vert, blank, horz, horz);
			short T_down = findTile(blank, vert, horz, horz);
			short cross = findTile(vert, vert, horz, horz);

			wall[0][0] = upper_left;
			wall[1][0] = T_up;
			wall[2][0] = upper_right;
			wall[0][1] = T_left;
			wall[1][1] = cross;
			wall[2][1] = T_right;
			wall[0][2] = lower_left;
			wall[1][2] = T_down;
			wall[2][2] = lower_right;

			short capLeftHorz = findTile(blank, blank, blank, horz);
			short capRightHorz = findTile(blank, blank, horz, blank);
			short capTopVert = findTile(blank, vert, blank, blank);
			short capBottomVert = findTile(vert, blank, blank, blank);

			wall[0][3] = capLeftHorz;
			wall[1][3] = horz;
			wall[2][3] = capRightHorz;

			wall[3][0] = capTopVert;
			wall[3][1] = vert;
			wall[3][2] = capBottomVert;
			wall[3][3] = wall[1][1];

			for (int y = 0; y < 4; ++y)
				for (int x = 0; x < 4; ++x)
				{
					if (wall[x][y] == -1)
						wall[x][y] = 0;
				}

		}
	}

	/**
	 * find a tile in the map with the specified tile in the specified locations
	 * 
	 * @param t_up
	 *            the tile above ours
	 * @param t_down
	 *            the tile below ours
	 * @param t_left
	 *            the tile left of ours
	 * @param t_right
	 *            the tile right of ours
	 * @return the most frequently occuring tile between these four
	 */
	short findTile(short t_up, short t_down, short t_left, short t_right)
	{
		HashMap m = new HashMap();

		for (int x = 1; x < 1023; ++x)
			for (int y = 1; y < 1023; ++y)
			{
				short value = m_map[x][y];

				if (value != 0)
				{
					short right = m_map[x + 1][y];
					short left = m_map[x - 1][y];
					short up = m_map[x][y - 1];
					short down = m_map[x][y + 1];

					if (right == t_right && up == t_up && left == t_left
							&& down == t_down)
					{
						incrementMap(m, value);
					}
				}
			}

		short maxTile = -1;
		int maxCount = 0;
		Iterator i2 = m.keySet().iterator();
		Iterator i = m.values().iterator();
		while (i.hasNext())
		{
			Short s = (Short) i2.next();
			Integer count = (Integer) i.next();

			if (count.intValue() > maxCount)
			{
				maxCount = count.intValue();
				maxTile = s.shortValue();
			}

		}

		return maxTile;
	}

	private void incrementMap(HashMap m, short key)
	{
		Short s = new Short(key);
		Object o = m.get(s);
		int count = 1;

		if (o != null)
		{
			count = ((Integer) o).intValue() + 1;
		}

		m.put(s, new Integer(count));
	}

	short findLongestHorizonalRun(short[][] map)
	{
		short maxRun = 0;
		short maxTile = 0;

		short curRun = 0;
		short curTile = 0;

		for (int y = 0; y < 1024; ++y)
		{
			for (int x = 0; x < 1024; ++x)
			{
				short now = map[x][y];

				if (curTile != now)
				{
					if (curTile != 0 && curRun > maxRun)
					{
						maxRun = curRun;
						maxTile = curTile;
					}

					curTile = now;
					curRun = 0;
				}

				++curRun;
			}

			if (curTile != 0 && curRun > maxRun)
			{
				maxRun = curRun;
				maxTile = curTile;
			}

			curTile = 0;
			curRun = 0;
		}

		if (maxRun < 5) // probably not a wall
			maxTile = -1;

		return maxTile;
	}

	short findLongestVerticalRun(short[][] map)
	{
		short maxRun = 0;
		short maxTile = 0;

		short curRun = 0;
		short curTile = 0;

		for (int x = 0; x < 1024; ++x)
		{
			for (int y = 0; y < 1024; ++y)
			{
				short now = map[x][y];

				if (curTile != now)
				{
					if (curTile != 0 && curRun > maxRun)
					{
						maxRun = curRun;
						maxTile = curTile;
					}

					curTile = now;
					curRun = 0;
				}

				++curRun;
			}

			if (curTile != 0 && curRun > maxRun)
			{
				maxRun = curRun;
				maxTile = curTile;
			}

			curTile = 0;
			curRun = 0;
		}

		if (maxRun < 5) // probably not a wall
			maxTile = -1;

		return maxTile;
	}

	private BitMap loadDefaultTileset()
	{
		try
		{
			File f = new File("");
			File file = new File(f.getAbsolutePath() + "/include/default.bmp");
			BufferedInputStream bs = new BufferedInputStream(
					new FileInputStream(file));
			BitMap bmp = new BitMap(bs);
			bmp.readBitMap(false);
			return bmp;
		}
		catch (FileNotFoundException e)
		{
		}
		return null;
	}

	public void gotFocus()
	{
		m_window.gotFocus();
	}

	public void lostFocus()
	{
		m_window.lostFocus();
	}
}