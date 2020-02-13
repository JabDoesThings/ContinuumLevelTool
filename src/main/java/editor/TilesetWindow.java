package editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class TilesetWindow extends JInternalFrame implements MouseListener
{
	public static final int TILE_EZ = 256;

	private Tileset m_child;

	/**
	 * Constructor for TilesetWindow which displays the current tileset. It
	 * reads the tileset from Editor.java which keeps it up to date even when
	 * the tileset may change through import or edit.
	 *
	 * @param parent
	 *            The parent class which creates this instance
	 */
	public TilesetWindow(LevelWindow parent, Point loc)
	{

		// set frame title, and closable
		super("Tileset", false, true, false, false);

		// Just hide the frame when closed, parent class listens for event
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		// Hide the 'java' icon
		try
		{
			setFrameIcon(null);
		}
		catch (Exception e)
		{
		}

		// Create our child and add it into the frame
		m_child = new Tileset(parent);
		m_child.addMouseListener(this);
		getContentPane().add(m_child);

		// Create our menu
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// File

		// Set the default location then show
		/** ** TO DO - Possibly make setLocation dependant on parent size *** */
		setLocation(loc.x, loc.y);
		pack();
		show();
	}

	/**
	 * Gets the currently selected tile for left or right click
	 *
	 * @param button
	 *            Button pressed from MouseEvent
	 * @return int The tile selected
	 */
	public int getTile(int button)
	{
		return m_child.getTile(button);
	}

	public void setTile(int button, int tile)
	{
		m_child.setTile(button, tile);
		repaint();
	}

	public void mousePressed(MouseEvent e)
	{
		// System.out.println("presed");
		m_child.mousePressed(e);
	}

	public void mouseReleased(MouseEvent e)
	{
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
}

class Tileset extends JPanel
{

	private LevelWindow m_parent;

	private int x_coordL = 0;

	private int y_coordL = 0;

	private int x_coordR = 1;

	private int y_coordR = 0;

	private Image imageErase = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
			+ "icons" + File.separator + "eraser.gif").getImage();

	private Image imageEZ = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
			+ File.separator + "autopen.gif").getImage();

	Font helpFont = getFont().deriveFont((float) 12.0);

	ImageIcon helpIcon = null;

	final Rectangle[] newTileBounds = {
			new Rectangle(16 + 16 * (0 * 2) - 4, 164, 24, 24),
			new Rectangle(16 + 16 * (1 * 2) - 4, 164, 24, 24),
			new Rectangle(16 + 16 * (2 * 2) - 4, 164, 24, 24),
			new Rectangle(16 + 16 * (3 * 2) - 4, 164, 24, 24),
			new Rectangle(16 + 16 * (4 * 2) - 4, 164, 24, 24),
			new Rectangle(16 + 16 * (5 * 2) - 4, 164, 24, 24),
			new Rectangle(16 + 16 * (6 * 2) - 4, 164, 24, 24),
			new Rectangle(16 + 16 * (7 * 2) - 4, 164, 24, 24) };

	final Rectangle helpRect = new Rectangle(264, 164, 36, 24);

	final int[] newTileIds = { 191, 192, 241, 242, 252, 253, 254, 255 };

	public Tileset(LevelWindow parent)
	{
		m_parent = parent;
		setSize(352, 192);
		setBackground(Color.black);
		setPreferredSize(new Dimension(352, 192));
	}

	protected void paintComponent(Graphics g)
	{

		super.paintComponent(g);

		paintAllTiles(g);

		paintSelectedTiles(g);
	}

	private void paintAllTiles(Graphics g)
	{
		int width = getWidth();
		int height = getHeight();

		g.setColor(Color.black);
		g.fillRect(0,0,width,height);

		if (m_parent.m_parent.m_tileset != null)
		{
			// draw tileset
			g.drawImage(m_parent.m_parent.m_tileset, 0, 0, 304, 160, this);

			// draw special tiles
			g.drawImage(m_parent.m_extras[0], 320, 40, 16, 16, this);
			g.drawImage(m_parent.m_extras[1], 312, 64, 32, 31, this);
			g.drawImage(m_parent.m_extras[3], 312, 96, 32, 31, this);
			g.drawImage(m_parent.m_extras[4], 312, 128, 32, 31, this);

			// draw erase image
			g.setColor(Color.lightGray);
			g.fillRect(328, 16, 16, 16);
			g.drawImage(imageErase, 328, 16, 16, 16, this);

			// draw EZ image
			g.setColor(Color.lightGray);
			g.fillRect(308, 16, 16, 16);
			g.drawImage(imageEZ, 308, 16, 16, 16, this);
			g.setColor(Color.white);

			if (m_parent.m_autotool != null)
				g.drawString("" + m_parent.m_autotool.getAutoTilesetNumber(), 314, 15);

			// draw extra tiles
			for (int x = 0; x < 8; ++x)
			{
				g.drawImage(Main.newTiles[x], 16 + 16 * (x * 2), 168, 16, 16, this);
			}

			// draw help button
			g.setColor(Color.lightGray);
			g.fillRect(264, 164, 36, 24);

			g.setColor(Color.gray);
			g.drawRect(264, 164, 36, 24);
			g.drawRect(265, 165, 34, 22);

			g.setColor(Color.black);
			g.setFont(helpFont);
			g.drawString("Help", 267, 180);
		}
	}

	private void paintSelectedTiles(Graphics g)
	{
		if (x_coordL == x_coordR && y_coordL == y_coordR)
		{
			if (x_coordL < 19) // if it's a tile
			{
				if (y_coordL < 10)
				{
					g.setColor(new Color(255, 255, 255));
					g.drawRect(x_coordR * 16, y_coordR * 16, 16, 16);
					g.setColor(new Color(200, 200, 200));
					g.drawRect(x_coordR * 16 + 1, y_coordR * 16 + 1, 14, 14);
				}
				else
				{ // selected other tile
					g.setColor(new Color(255, 255, 255));
					g.drawRect(16 + 16 * (x_coordL * 2) - 3, 168 - 3, 22, 22);
					g.setColor(new Color(200, 200, 200));
					g.drawRect(16 + 16 * (x_coordL * 2) - 2, 168 - 2, 20, 20);
				}
			}
			else if (y_coordL < 1) // erase or EZ Tool
			{
				if (x_coordL == 19)
				{ // ez tool
					g.setColor(new Color(255, 255, 255));
					g.drawRect(308, 16, 16, 16);
					g.setColor(new Color(255, 255, 255));
					g.drawRect(309, 17, 14, 14);
				}
				else
				{ // erase tool
					g.setColor(new Color(255, 255, 255));
					g.drawRect(328, 16, 16, 16);
					g.setColor(new Color(255, 255, 255));
					g.drawRect(329, 17, 14, 14);
				}
			}
			else
			// if it's erase / special tile
			{
				g.setColor(new Color(255, 255, 255));
				g.drawRect(8 + x_coordR * 16, y_coordR * 32, 32, 32);
				g.setColor(new Color(200, 200, 200));
				g.drawRect(8 + x_coordR * 16 + 1, y_coordR * 32 + 1, 30, 30);
			}
		}
		else
		{
			if (x_coordL < 19) // if it's a tile
			{
				if (y_coordL < 10)
				{
					g.setColor(new Color(255, 0, 0));
					g.drawRect(x_coordL * 16, y_coordL * 16, 16, 16);
					g.setColor(new Color(255, 100, 100));
					g.drawRect(x_coordL * 16 + 1, y_coordL * 16 + 1, 14, 14);
				}
				else
				{ // selected other tile
					g.setColor(new Color(255, 0, 0));
					g.drawRect(16 + 16 * (x_coordL * 2) - 3, 168 - 3, 22, 22);
					g.setColor(new Color(255, 100, 100));
					g.drawRect(16 + 16 * (x_coordL * 2) - 2, 168 - 2, 20, 20);
				}
			}
			else if (y_coordL < 1) // erase or EZ Tool
			{
				if (x_coordL == 19)
				{ // ez tool
					g.setColor(new Color(255, 0, 0));
					g.drawRect(308, 16, 16, 16);
					g.setColor(new Color(255, 100, 100));
					g.drawRect(309, 17, 14, 14);
				}
				else
				{ // erase tool
					g.setColor(new Color(255, 0, 0));
					g.drawRect(328, 16, 16, 16);
					g.setColor(new Color(255, 100, 100));
					g.drawRect(329, 17, 14, 14);
				}
			}
			else
			// if it's erase / special tile
			{
				g.setColor(new Color(255, 0, 0));
				g.drawRect(8 + x_coordL * 16, y_coordL * 32, 32, 32);
				g.setColor(new Color(255, 100, 100));
				g.drawRect(8 + x_coordL * 16 + 1, y_coordL * 32 + 1, 30, 30);
			}

			if (x_coordR < 19) // if it's a tile
			{
				if (y_coordR < 10)
				{
					g.setColor(new Color(0, 0, 255));
					g.drawRect(x_coordR * 16, y_coordR * 16, 16, 16);
					g.setColor(new Color(100, 100, 255));
					g.drawRect(x_coordR * 16 + 1, y_coordR * 16 + 1, 14, 14);
				}
				else
				{ // selected other tile
					g.setColor(new Color(0, 0, 255));
					g.drawRect(16 + 16 * (x_coordR * 2) - 3, 168 - 3, 22, 22);
					g.setColor(new Color(100, 100, 255));
					g.drawRect(16 + 16 * (x_coordR * 2) - 2, 168 - 2, 20, 20);
				}
			}
			else if (y_coordR < 1) // erase or EZ Tool
			{
				if (x_coordR == 19)
				{ // ez tool
					g.setColor(new Color(0, 0, 255));
					g.drawRect(308, 16, 16, 16);
					g.setColor(new Color(100, 100, 255));
					g.drawRect(309, 17, 14, 14);
				}
				else
				{ // erase tool
					g.setColor(new Color(0, 0, 255));
					g.drawRect(328, 16, 16, 16);
					g.setColor(new Color(100, 100, 255));
					g.drawRect(329, 17, 14, 14);
				}
			}
			else
			// it it's erase / special tile
			{
				g.setColor(new Color(0, 0, 255));
				g.drawRect(8 + x_coordR * 16, y_coordR * 32, 32, 32);
				g.setColor(new Color(100, 100, 255));
				g.drawRect(8 + x_coordR * 16 + 1, y_coordR * 32 + 1, 30, 30);
			}
		}
	}

	/**
	 * Used to listen to mousePressed events and set the selected tiles
	 */
	public void mousePressed(MouseEvent e)
	{

		Insets insets = getInsets();
		int x = e.getX() - insets.left;
		int y = e.getY() - insets.top;

		if (helpRect.contains(x, y))
		{
			File helpFile = new File(Main.rootDirectory + File.separator + "include" + File.separator + "Images"
					+ File.separator + "specialTilesHelp.PNG");

			if (helpIcon == null)
			{
				helpIcon = new ImageIcon(helpFile.getAbsolutePath());
			}

			JOptionPane.showMessageDialog(null, "", "Tools Help",
					JOptionPane.INFORMATION_MESSAGE, helpIcon);
		}
		else if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == 0)
		{
			int oldX = x_coordL;
			int oldY = y_coordL;

			x_coordL = x / 16;
			y_coordL = y / 16;

			if (y_coordL > 9)
			{
				y_coordL = 10;
				int found = -1;

				for (int c = 0; c < newTileBounds.length; ++c)
				{
					if (newTileBounds[c].contains(x, y))
					{
						found = c;
						break;
					}
				}

				if (found == -1)
				{
					x_coordL = oldX;
					y_coordL = oldY;
				}
				else
				{
					x_coordL = found;
				}
			}
			else if (x_coordL > 18) // attempting to select erase / special tile
			{
				y_coordL = y / 32;

				if (y_coordL != 0)
					x_coordL = 19;
				else
				{
					if (x > 328)
						x_coordL = 20;
					else
						x_coordL = 19;
				}
			}

			if (x_coordL < 0)
				x_coordL = 0;

			if (y_coordL < 0)
				y_coordL = 0;

		}
		else if (e.getButton() == MouseEvent.BUTTON3)
		{
			int oldX = x_coordR;
			int oldY = y_coordR;

			x_coordR = x / 16;
			y_coordR = y / 16;

			if (y_coordR > 9)
			{
				y_coordR = 10;
				int found = -1;

				for (int c = 0; c < newTileBounds.length; ++c)
				{
					if (newTileBounds[c].contains(x, y))
					{
						found = c;
						break;
					}
				}

				if (found == -1)
				{
					x_coordR = oldX;
					y_coordR = oldY;
				}
				else
				{
					x_coordR = found;
				}
			}
			else if (x_coordR > 18)// attempting to select erase / special tile
			{
				y_coordR = y / 32;

				if (y_coordR != 0)
					x_coordR = 19;
				else
				{
					if (x > 328)
						x_coordR = 20;
					else
						x_coordR = 19;
				}
			}

			if (x_coordR < 0)
				x_coordR = 0;

			if (y_coordR < 0)
				y_coordR = 0;
		}

		// Call a repaint so the new tile selected is shown
		repaint();
	}

	// 0 < tile < 191
	public void setTile(int button, int tile)
	{
		tile--;

		if (button == MouseEvent.BUTTON1 || button == 0)
		{
			x_coordL = tile % 19;
			y_coordL = tile / 19;
		}
		else
		{// right click
			x_coordR = tile % 19;
			y_coordR = tile / 19;
		}
	}

	/**
	 * Gets the currently selected tile for left or right click
	 *
	 * @param button
	 *            Button pressed from MouseEvent
	 * @return int The tile selected, possibly TilesetWindow.EZTOOL
	 */
	public int getTile(int button)
	{
		if (button == MouseEvent.BUTTON1 || button == 0)
		{
			if (x_coordL < 19)
			{
				if (y_coordL <= 9)// regular tile
					return y_coordL * 19 + x_coordL + 1;
				else
				{ // newTile
					return newTileIds[x_coordL];
				}
			}
			else
			// erase tool or special tile
			{
				if (y_coordL == 0) // erase or ez tile
				{
					if (x_coordL == 19)
						return TilesetWindow.TILE_EZ;
					else
						return 0; // erase
				}
				else if (y_coordL == 1)
					return 216; // small asteriod
				else if (y_coordL == 2)
					return 217; // medium asteriod
				else if (y_coordL == 3)
					return 219; // station
				else if (y_coordL == 4)
					return 220; // wormhole
			}
		}
		else
		{
			if (x_coordR < 19)
			{
				if (y_coordR <= 9)// regular tile
					return y_coordR * 19 + x_coordR + 1;
				else
				{ // newTile
					return newTileIds[x_coordR];
				}
			}
			else
			// erase tool or special tile
			{
				if (y_coordR == 0) // erase or ez tile
				{
					if (x_coordR == 19)
						return TilesetWindow.TILE_EZ;
					else
						return 0; // erase
				}
				else if (y_coordR == 1)
					return 216; // small asteriod
				else if (y_coordR == 2)
					return 217; // medium asteriod
				else if (y_coordR == 3)
					return 219; // station
				else if (y_coordR == 4)
					return 220; // wormhole
			}
		}

		return -1;
	}
}