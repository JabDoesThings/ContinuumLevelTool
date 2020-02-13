package editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import editor.xml.XMLNode;

public class AutoToolWindow extends JInternalFrame implements ActionListener,
		ChangeListener
{
	private LevelWindow parent = null;

	private ProjectState state = null;

	private JButton add = new JButton("Add");

	private JButton del = new JButton("Delete");

	private ArrayList panels = new ArrayList();

	private JTabbedPane tabbedPane = new JTabbedPane();

	/**
	 * Constructor for TilesetWindow which displays the current tileset. It
	 * reads the tileset from Editor.java which keeps it up to date even when
	 * the tileset may change through import or edit.
	 *
	 * @param parent
	 *            The parent class which creates this instance
	 */
	public AutoToolWindow(LevelWindow parent, ProjectState state)
	{
		// set frame title, and closable
		super("Walls", false, true, false, false);
		this.parent = parent;
		this.state = state;

		setLayout(null);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		try
		{
			setFrameIcon(null);
		}
		catch (Exception e)
		{
		}
		Container c = getContentPane();

		panels.add(makeBlankPanel());
		tabbedPane.addTab("1", (Component) panels.get(0));
		JScrollPane scroller = new JScrollPane(tabbedPane);
		scroller.setBounds(0, 0, 100, 110);
		tabbedPane.addChangeListener(this);

		add.setBounds(5, 120, 90, 20);
		add.addActionListener(this);
		del.setBounds(5, 145, 90, 20);
		del.addActionListener(this);

		c.add(scroller);
		c.add(add);
		c.add(del);

		setLocation(state.pos_Autotool.x, state.pos_Autotool.y);
		setSize(110, 205);
		setVisible(true);
	}

	public short[][] getAutoTileset()
	{
		AutoTool child = getCurrentAutoTool();

		return child.autoTileset;
	}

	public int getAutoTilesetNumber()
	{
		return tabbedPane.getSelectedIndex() + 1;
	}

	private JPanel makeBlankPanel()
	{
		JPanel p = new JPanel();
		p.add(new AutoTool(parent, state));

		return p;
	}

	// for saving
	public XMLNode toXMLNode()
	{
		XMLNode autoToolAll = new XMLNode();
		autoToolAll.name = "AutotoolTiles";

		autoToolAll.addChild("Count", "" + panels.size());

		for (int c = 0; c < panels.size(); ++c)
		{
			XMLNode autoToolTiles = autoToolAll.addChild("Wall_" + c);

			AutoTool child = (AutoTool) ((JPanel) panels.get(c))
					.getComponent(0);

			for (int y = 0; y < 4; y++)
				for (int x = 0; x < 4; x++)
				{
					autoToolTiles.addChild("Tile" + (x + y * 4), ""
							+ child.autoTileset[x][y]);
				}
		}

		return autoToolAll;
	}

	private AutoTool getCurrentAutoTool()
	{
		int index = tabbedPane.getSelectedIndex();
		AutoTool child = (AutoTool) ((JPanel) panels.get(index))
				.getComponent(0);

		return child;
	}

	/**
	 * Are there no walls defined
	 *
	 * @return true iff there are no walls defined
	 */
	public boolean isEmpty()
	{
		boolean rv = true;

		if (panels.size() > 1)
			rv = false;
		else
		{
			short[][] wall = getAutoTileset();

			for (int x = 0; x < 4; ++x)
				for (int y = 0; y < 4; ++y)
				{
					if (wall[x][y] != 0)
					{
						rv = false;
						break;
					}
				}
		}

		return rv;
	}

	public void loadFromXMLNode(XMLNode autoToolTiles)
	{
		XMLNode countNode = autoToolTiles.getChild("Count");

		if (countNode != null)
		{
			int count = countNode.getIntValue();

			panels.clear();
			while (tabbedPane.getTabCount() > 0)
				tabbedPane.removeTabAt(0);

			for (int c = 0; c < count; ++c)
			{
				XMLNode wall = autoToolTiles.getChild("Wall_" + c);

				if (wall != null)
				{
					JPanel p = makeBlankPanel();
					panels.add(p);
					tabbedPane.add(p, "" + (c + 1));
					AutoTool at = (AutoTool) (p.getComponent(0));

					for (int y = 0; y < 4; y++)
						for (int x = 0; x < 4; x++)
						{
							short tile = 0;

							XMLNode num = wall.getChild("Tile" + (x + y * 4));

							if (num != null)
							{
								tile = (short) num.getIntValue();
							}

							at.autoTileset[x][y] = tile;
						}
				}
			}
		}

		repaint();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == add)
		{ // add wall
			JPanel p = makeBlankPanel();

			panels.add(p);
			int size = panels.size();

			tabbedPane.addTab("" + size, p);
			tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
		}
		else if (e.getSource() == del)
		{ // delete wall
			int index = getAutoTilesetNumber() - 1;

			tabbedPane.removeTabAt(index);
			panels.remove(index);

			if (tabbedPane.getTabCount() == 0)
			{
				panels.add(makeBlankPanel());
				tabbedPane.addTab("1", (Component) panels.get(0));
			}

		}
	}

	public void stateChanged(ChangeEvent e)
	{ // current tab changed
		parent.m_tileset.repaint();
	}

}

class AutoTool extends JPanel implements MouseListener
{

	private static final long serialVersionUID = -1092905724278348311L;

	private LevelWindow m_parent;

	public ProjectState m_state;

	private Image autoBackground = null;

	public short[][] autoTileset = new short[4][4];;

	public AutoTool(LevelWindow parent, ProjectState state)
	{
		m_parent = parent;
		m_state = state;
		setSize(64, 64);
		setPreferredSize(new Dimension(64, 64));
		addMouseListener(this);
		setBackground(Color.black);

		ImageIcon ii = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "Images" + File.separator + "autoBackground.png");

		autoBackground = ii != null ? ii.getImage() : null;
	}

	protected void paintComponent(Graphics g)
	{

		super.paintComponent(g);

		if (autoBackground != null)
			g.drawImage(autoBackground, 0, 0, null);

		Image[] m_tiles = m_parent.m_parent.m_tiles;

		// draw tiles
		for (int x = 0; x < 4; x++)
			for (int y = 0; y < 4; y++)
			{
				if (autoTileset[x][y] > 0)
				{
					int index = autoTileset[x][y] - 1;

					if (index < m_tiles.length)
						g.drawImage(m_tiles[index], x * 16, y * 16, 16, 16,
								this);
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

		x /= 16;
		y /= 16;

		if (x > 4 || y > 4)
			return;

		int tile = m_parent.m_tileset.getTile(e.getButton());

		if (tile > 191) // don't allow special tiles
			return;

		autoTileset[x][y] = (short) tile;

		// Call a repaint so the new tile selected is shown
		repaint();
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