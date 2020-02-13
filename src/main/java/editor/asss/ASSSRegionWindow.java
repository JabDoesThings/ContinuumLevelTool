// Stan Bak
// July 22nd, 2004
// ASSS Region internal frame

package editor.asss;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import editor.LevelWindow;
import editor.xml.XMLNode;

public class ASSSRegionWindow extends JInternalFrame implements ActionListener,
		ChangeListener, ItemListener, DocumentListener
{
	LevelWindow parent;

	final FileDialog fileChooser;

	MyTableModel tableModel; // access regions through getRegions()

	JTable table;

	JScrollPane scrollPane;

	JButton add, delete, deleteRect, importRgn;

	JComboBox rects;

	JLabel xLabel, yLabel, wLabel, hLabel;

	JSpinner xSpinner, ySpinner, wSpinner, hSpinner;

	// eLVL options
	JPanel boolPanel;

	JCheckBox isBase, isNoAnti, isNoWeps, isNoFlags, isAutoWarp;

	JSpinner autoX, autoY;

	JTextField autoArena;

	JPanel spinnerPanel;

	Vector singleRegionGUI;

	Vector singleRectGUI;

	public ASSSRegionWindow(LevelWindow parent, Point location)
	{
		super("ASSS Regions", false, true, false, false);

		this.parent = parent;

		fileChooser = new FileDialog(parent.m_parent.m_main);

		initialize(location);
	}

	private void initialize(Point location)
	{
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		try
		{
			setFrameIcon(null);
		}
		catch (Exception e)
		{
		}

		setSize(520, 210);
		setLocation(location.x, location.y);
		getContentPane().setLayout(null);

		singleRegionGUI = new Vector();
		singleRectGUI = new Vector();

		tableModel = new MyTableModel();
		table = new JTable(tableModel);
		table.getColumnModel().getColumn(0).setPreferredWidth(130);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		scrollPane = new JScrollPane(table);
		table.setDefaultRenderer(Color.class, new ColorRenderer(true));
		table.setDefaultEditor(Color.class, new ColorEditor());
		table.setDefaultEditor(String.class, new StringEditor());

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		ListSelectionModel rowSM = table.getSelectionModel();
		rowSM.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				// Ignore extra messages.

				if (e.getValueIsAdjusting())
					return;

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();
				if (lsm.isSelectionEmpty())
				{
					hideSingleRegionGUI();
					disableSingleRectGUI();

					repaint();
					// no rows are selected
				}
				else
				{
					int selectedRow = lsm.getMinSelectionIndex();
					populateComboBox(selectedRow);
					showSingleRegionGUI();
				}
			}
		});

		scrollPane.setBounds(10, 5, 230, 135);
		getContentPane().add(scrollPane);

		add = new JButton("Add Region");
		add.setBounds(10, 150, 110, 20);
		getContentPane().add(add);
		add.addActionListener(this);

		delete = new JButton("Delete Region");
		delete.setBounds(130, 150, 110, 20);
		getContentPane().add(delete);
		singleRegionGUI.add(delete);
		delete.addActionListener(this);

		rects = new JComboBox();
		rects.setEditable(false);
		rects.setBounds(250, 40, 150, 20);
		getContentPane().add(rects);
		singleRegionGUI.add(rects);
		rects.addActionListener(this);

		xLabel = new JLabel("X:", SwingConstants.RIGHT);
		xLabel.setBounds(250, 75, 20, 20);
		getContentPane().add(xLabel);
		singleRegionGUI.add(xLabel);

		xSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1024, 1));
		xSpinner.setBounds(270, 75, 50, 20);
		getContentPane().add(xSpinner);
		singleRegionGUI.add(xSpinner);
		singleRectGUI.add(xSpinner);
		xSpinner.addChangeListener(this);

		yLabel = new JLabel("Y:", SwingConstants.RIGHT);
		yLabel.setBounds(325, 75, 20, 20);
		getContentPane().add(yLabel);
		singleRegionGUI.add(yLabel);

		ySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1024, 1));
		ySpinner.setBounds(345, 75, 50, 20);
		getContentPane().add(ySpinner);
		singleRegionGUI.add(ySpinner);
		singleRectGUI.add(ySpinner);
		ySpinner.addChangeListener(this);

		wLabel = new JLabel("W:", SwingConstants.RIGHT);
		wLabel.setBounds(250, 105, 20, 20);
		getContentPane().add(wLabel);
		singleRegionGUI.add(wLabel);

		wSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1024, 1));
		wSpinner.setBounds(270, 105, 50, 20);
		getContentPane().add(wSpinner);
		singleRegionGUI.add(wSpinner);
		singleRectGUI.add(wSpinner);
		wSpinner.addChangeListener(this);

		hLabel = new JLabel("H:", SwingConstants.RIGHT);
		hLabel.setBounds(325, 105, 20, 20);
		getContentPane().add(hLabel);
		singleRegionGUI.add(hLabel);

		hSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 1024, 1));
		hSpinner.setBounds(345, 105, 50, 20);
		getContentPane().add(hSpinner);
		singleRegionGUI.add(hSpinner);
		singleRectGUI.add(hSpinner);
		hSpinner.addChangeListener(this);

		deleteRect = new JButton("Delete Rectangle");
		deleteRect.setBounds(265, 140, 120, 25);
		getContentPane().add(deleteRect);
		singleRegionGUI.add(deleteRect);
		singleRectGUI.add(deleteRect);
		deleteRect.addActionListener(this);

		importRgn = new JButton("Import Regions");
		importRgn.setBounds(265, 5, 130, 25);
		getContentPane().add(importRgn);
		importRgn.addActionListener(this);

		boolPanel = new JPanel();
		boolPanel.setBounds(400, 10, 110, 85);
		boolPanel.setLayout(null);

		isBase = new JCheckBox("Base", false);
		isBase.setBounds(0, 0, 100, 15);
		boolPanel.add(isBase);
		isBase.addItemListener(this);

		isNoAnti = new JCheckBox("No Antiwarp", false);
		isNoAnti.setBounds(0, 17, 100, 15);
		boolPanel.add(isNoAnti);
		isNoAnti.addItemListener(this);

		isNoWeps = new JCheckBox("No Weapons", false);
		isNoWeps.setBounds(0, 34, 100, 15);
		boolPanel.add(isNoWeps);
		isNoWeps.addItemListener(this);

		isNoFlags = new JCheckBox("No Flag Drops", false);
		isNoFlags.setBounds(0, 51, 100, 15);
		boolPanel.add(isNoFlags);
		isNoFlags.addItemListener(this);

		isAutoWarp = new JCheckBox("Auto Warp", false);
		isAutoWarp.setBounds(0, 68, 100, 15);
		boolPanel.add(isAutoWarp);
		isAutoWarp.addItemListener(this);

		spinnerPanel = new JPanel();
		spinnerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 3));
		spinnerPanel.setBounds(400, 95, 110, 70);

		spinnerPanel.add(new JLabel("Warp X:"));

		autoX = new JSpinner(new SpinnerNumberModel(0, -1, 1024, 1));
		autoX.setPreferredSize(new Dimension(50, 20));
		autoX.addChangeListener(this);
		spinnerPanel.add(autoX);

		spinnerPanel.add(new JLabel("Warp Y:"));
		autoY = new JSpinner(new SpinnerNumberModel(0, -1, 1024, 1));
		autoY.addChangeListener(this);
		autoY.setPreferredSize(new Dimension(50, 20));
		spinnerPanel.add(autoY);

		spinnerPanel.add(new JLabel("Arena:"));

		autoArena = new JTextField(5);
		autoArena.getDocument().addDocumentListener(this);
		spinnerPanel.add(autoArena);

		getContentPane().add(boolPanel);
		getContentPane().add(spinnerPanel);
		boolPanel.setVisible(false);
		spinnerPanel.setVisible(false);

		hideSingleRegionGUI();
		disableSingleRectGUI();
	}

	private void disableSingleRectGUI()
	{
		for (int x = 0; x < singleRectGUI.size(); ++x)
		{
			((Component) singleRectGUI.get(x)).setEnabled(false);
		}
	}

	private void enableSingleRectGUI()
	{
		for (int x = 0; x < singleRectGUI.size(); ++x)
		{
			((Component) singleRectGUI.get(x)).setEnabled(true);
		}
	}

	/**
	 * hide the gui that's enabled when a region is selected
	 * 
	 */
	private void hideSingleRegionGUI()
	{
		regionSelected(-1);

		for (int x = 0; x < singleRegionGUI.size(); ++x)
		{
			((Component) singleRegionGUI.get(x)).setVisible(false);
		}
	}

	/**
	 * show the gui when a region is selected
	 * 
	 */
	private void showSingleRegionGUI()
	{
		regionSelected(table.getSelectedRow());

		for (int x = 0; x < singleRegionGUI.size(); ++x)
		{
			((Component) singleRegionGUI.get(x)).setVisible(true);
		}
	}

	public Color getSelectedRegionColor()
	{
		int index = table.getSelectedRow();

		if (index == -1)
			return Color.white;

		Region r = (Region) tableModel.data.get(index);

		return r.color;
	}

	/**
	 * A region was selected, perform any actions necessary
	 * 
	 * @param whichOne
	 *            which region was selected(row)? or -1
	 */
	private void regionSelected(int whichOne)
	{
		if (whichOne != -1)
		{
			Region r = (Region) tableModel.data.get(whichOne);

			isBase.setSelected(r.isBase);
			isNoAnti.setSelected(r.isNoAnti);
			isNoWeps.setSelected(r.isNoWeps);
			isNoFlags.setSelected(r.isNoFlags);
			isAutoWarp.setSelected(r.isAutoWarp);

			autoX.setValue(new Integer(r.x));
			autoY.setValue(new Integer(r.y));
			autoArena.setText(r.arena);

			if (r.isAutoWarp)
			{
				spinnerPanel.setVisible(true);
			}
			else
			{
				spinnerPanel.setVisible(false);
			}

			boolPanel.setVisible(true);
		}
		else
		{
			boolPanel.setVisible(false);
			spinnerPanel.setVisible(false);
		}

		repaint();
	}

	public void addRectangleToCurrentRegion(Rectangle rect)
	{
		int index = table.getSelectedRow();

		if (index == -1)
		{
			if (createRegion())
				index = tableModel.data.size() - 1;
			else
				return;
		}

		Region r = (Region) tableModel.data.get(index);
		r.rects.add(rect);
		String s;

		if (rects.getItemCount() > 0)
			s = (String) rects.getItemAt(rects.getItemCount() - 1);
		else
			s = "Rectangle0";

		int num = Integer.parseInt(s.substring(9)) + 1;
		rects.addItem("Rectangle" + num);
		rects.setSelectedIndex(r.rects.size() - 1);

		parent.repaint();
		parent.modified = true;
	}

	/**
	 * Load the regions in this vector into the current regions
	 * 
	 * @param newRegions
	 *            the new regions we're adding
	 */
	public void setRegions(Vector newRegions)
	{
		Vector v = new Vector();

		v.addAll(newRegions);

		tableModel.data = v;
		table.revalidate();

		table.getSelectionModel().clearSelection();

		parent.repaint();
	}

	public void loadRegions(XMLNode regions)
	{
		Vector v = new Vector();

		for (int x = 0; x < regions.children.size(); ++x)
		{
			XMLNode aRegion = (XMLNode) regions.children.get(x);
			String name = aRegion.name;
			boolean base = false;

			XMLNode baseNode = aRegion.getChild("Base");
			if (baseNode != null && baseNode.getBooleanValue() == true)
				base = true;

			int r = 128, g = 128, b = 128;
			XMLNode colorNode = aRegion.getChild("Color");
			if (colorNode != null)
			{
				XMLNode redNode = colorNode.getChild("Red");
				if (redNode != null)
					r = redNode.getIntValue();
				XMLNode greenNode = colorNode.getChild("Green");
				if (greenNode != null)
					g = greenNode.getIntValue();
				XMLNode blueNode = colorNode.getChild("Blue");
				if (blueNode != null)
					b = blueNode.getIntValue();

			}

			Region reg = new Region(name, new Color(r, g, b));
			reg.isBase = base;

			for (int c = 0; c < aRegion.children.size(); ++c)
			{
				XMLNode aNode = (XMLNode) aRegion.children.get(c);
				String nodeName = aNode.name;

				if (nodeName.length() > 9)
				{
					if (nodeName.substring(0, 9).equals("Rectangle"))
					{
						int X = 0, Y = 0, W = 0, H = 0;

						XMLNode xNode = aNode.getChild("X");
						if (xNode != null)
							X = xNode.getIntValue();

						XMLNode yNode = aNode.getChild("Y");
						if (yNode != null)
							Y = yNode.getIntValue();

						XMLNode wNode = aNode.getChild("Width");
						if (wNode != null)
							W = wNode.getIntValue();

						XMLNode hNode = aNode.getChild("Height");
						if (hNode != null)
							H = hNode.getIntValue();

						reg.rects.add(new Rectangle(X, Y, W, H));
					}
				}
			}

			v.add(reg);
		}

		tableModel.data = v;
		table.revalidate();

		table.getSelectionModel().clearSelection();

		parent.repaint();
	}

	/**
	 * Get the regions as a Vector
	 */
	public Vector getRegions()
	{
		return tableModel.data;
	}

	public boolean createRegion()
	{
		boolean repeat = false; // is this name already in the table?
		String currentString = null;

		do
		{
			if (repeat == true)
			{
				JOptionPane.showMessageDialog(null,
						"That name is already used by another region.");
				repeat = false;
			}
			else if (currentString != null && currentString.equals(""))
			{
				JOptionPane.showMessageDialog(null,
						"You must supply a region name.");
			}

			currentString = JOptionPane.showInputDialog(null,
					"Enter New Region Name:", currentString);

			if (currentString != null && !currentString.equals("")) // didn't
																	// press
																	// cancel
			{
				String newName = currentString.replace(' ', '_');

				if (!newName.equals(currentString))
				{
					JOptionPane.showMessageDialog(null,
							"Spaces have been converted to underscores for region "
									+ newName);
					currentString = newName;
				}

				for (int x = 0; x < tableModel.data.size(); ++x)
				{
					String name = ((Region) tableModel.data.get(x)).name;

					if (name.equals(currentString))
					{
						repeat = true;
						break;
					}
				}
			}
		}
		while (repeat == true
				|| (currentString != null && currentString.equals("")));

		if (currentString == null)
			return false;

		Region r = new Region(currentString, ASSSFiling
				.getColorForRow(tableModel.data.size()));

		tableModel.data.add(r);
		table.revalidate();
		table.getSelectionModel().setSelectionInterval(
				tableModel.data.size() - 1, tableModel.data.size() - 1);
		repaint();

		return true;
	}

	private void populateComboBox(int row)
	{
		if (tableModel.data.size() > row)
		{
			Region r = (Region) tableModel.data.get(row);

			rects.removeAllItems();

			for (int x = 0; x < r.rects.size(); ++x)
			{
				rects.addItem("Rectangle" + (x + 1));
			}

			if (r.rects.size() > 0)
				enableSingleRectGUI();
			else
				disableSingleRectGUI();
		}
	}

	public void itemStateChanged(ItemEvent e)
	{
		int row = table.getSelectedRow();

		if (row == -1)
		{
			System.out.println("item state changed and no selected row???");
		}
		else
		{
			Region r = (Region) tableModel.data.get(row);

			if (e.getSource() == isBase)
			{
				r.isBase = isBase.isSelected();
			}
			else if (e.getSource() == isNoAnti)
			{
				r.isNoAnti = isNoAnti.isSelected();
			}
			else if (e.getSource() == isNoWeps)
			{
				r.isNoWeps = isNoWeps.isSelected();
			}
			else if (e.getSource() == isNoFlags)
			{
				r.isNoFlags = isNoFlags.isSelected();
			}
			else if (e.getSource() == isAutoWarp)
			{
				r.isAutoWarp = isAutoWarp.isSelected();

				if (r.isAutoWarp)
				{
					autoX.setValue(new Integer(r.x));
					autoY.setValue(new Integer(r.y));
					spinnerPanel.setVisible(true);
					repaint();
				}
				else
				{
					spinnerPanel.setVisible(false);
					repaint();
				}
			}
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == importRgn)
			importRegionFile();
		else if (e.getSource() == add)
			createRegion();
		else if (e.getSource() == rects)
		{
			int num = rects.getSelectedIndex();
			if (num != -1)
			{
				enableSingleRectGUI();
				updateSpinners(num);
			}
			else
			{
				disableSingleRectGUI();
			}
		}
		else if (e.getSource() == deleteRect)
		{
			int num = rects.getSelectedIndex();
			if (num == -1)
			{
				System.out.println("Deleting non-existant rectangle... ?");
			}
			else
			{
				int row = table.getSelectedRow();

				if (row == -1)
				{
					System.out
							.println("Deleting rect from non-existant region... ?");
				}
				else
				{
					Region r = (Region) tableModel.data.get(row);

					rects.removeItemAt(num);
					r.rects.remove(num);

					if (r.rects.size() == 0)
					{
						disableSingleRectGUI();
					}

					repaint();
					parent.repaint();
					parent.modified = true;
				}
			}
		}
		else if (e.getSource() == delete)// delete region
		{
			int row = table.getSelectedRow();

			if (row == -1)
			{
				System.out.println("Deleting non-existant region... ?");
			}
			else
			{
				table.getSelectionModel().clearSelection();
				table.removeEditor();
				tableModel.data.remove(row);
				table.revalidate();

				hideSingleRegionGUI();
				disableSingleRectGUI();

				repaint();
				parent.repaint();
				parent.modified = true;
			}
		}
	}

	public void stateChanged(ChangeEvent e)
	{
		int row = table.getSelectedRow();
		if (row == -1)
		{
			System.out.println("state changed on without selected row?");
			return;
		}

		Region r = (Region) tableModel.data.get(row);

		if (e.getSource() == autoX)
		{
			r.x = ((Integer) autoX.getValue()).intValue();
		}
		else if (e.getSource() == autoY)
		{
			r.y = ((Integer) autoY.getValue()).intValue();
		}
		else
		{
			int index = rects.getSelectedIndex();
			if (index == -1)
			{
				System.out
						.println("state changed with no rectangles in region?");
				return;
			}

			Rectangle rect = (Rectangle) r.rects.get(index);

			if (e.getSource() == xSpinner)
			{
				rect.x = ((Integer) xSpinner.getValue()).intValue();
			}
			else if (e.getSource() == ySpinner)
			{
				rect.y = ((Integer) ySpinner.getValue()).intValue();
			}
			else if (e.getSource() == wSpinner)
			{
				rect.width = ((Integer) wSpinner.getValue()).intValue();
			}
			else if (e.getSource() == hSpinner)
			{
				rect.height = ((Integer) hSpinner.getValue()).intValue();
			}

			parent.repaint();
		}
	}

	private void updateSpinners(int rectNum)
	{
		int row = table.getSelectedRow();

		if (row == -1)
			System.out
					.println("WTF? row == -1 while a rectangle is being selected?");
		else
		{
			Region r = (Region) tableModel.data.get(row);
			Rectangle rect = (Rectangle) r.rects.get(rectNum);

			xSpinner.setValue(new Integer(rect.x));
			ySpinner.setValue(new Integer(rect.y));
			wSpinner.setValue(new Integer(rect.width));
			hSpinner.setValue(new Integer(rect.height));
		}
	}

	private void importRegionFile()
	{
		fileChooser.setMode(FileDialog.LOAD);
		fileChooser.setTitle("Open Regions From .RGN or .LVL");
		fileChooser.setVisible(true);
		String filename = fileChooser.getFile();
		String dir = fileChooser.getDirectory();

		if (filename == null) // user pressed cancel
			return;

		String path = dir + filename;

		Vector v = ASSSFiling.loadRegions(path);

		if (v == null) // error loading
			return;

		tableModel.data = v;
		table.revalidate();

		table.getSelectionModel().clearSelection();

		parent.repaint();
	}

	/**
	 * paint all the regions
	 * 
	 * @param g
	 *            the grapgics to paint with
	 * @param scale
	 *            the pixels per tile
	 * @param startx
	 *            the startx of our drawing arena
	 * @param endx
	 *            the endx of our drawing area
	 * @param starty
	 *            the starty of our drawing area
	 * @param endy
	 *            the endy of our drawing area
	 */
	public void paintRegions(Graphics g, int scale, int startx, int endx,
			int starty, int endy)
	{
		Rectangle view = new Rectangle(startx * 16, starty * 16,
				(endx - startx) * 16, (endy - starty) * 16);

		Color preColor = g.getColor();

		for (int row = 0; row < table.getRowCount(); ++row)
		{
			Region r = (Region) tableModel.data.get(row);
			g.setColor(r.color);

			for (int x = 0; x < r.rects.size(); ++x)
			{
				Rectangle rect = (Rectangle) r.rects.get(x);

				Rectangle port = new Rectangle(rect.x * 16, rect.y * 16,
						rect.width * 16, rect.height * 16);

				if (view.intersects(port))
				{
					g.fillRect((rect.x * scale), (rect.y * scale),
							(rect.width * scale), (rect.height * scale));
				}
			}

		}

		g.setColor(preColor);
	}

	/**
	 * paint the selected region
	 * 
	 * @param g
	 *            the grapgics to paint with
	 * @param scale
	 *            the pixels per tile
	 * @param startx
	 *            the startx of our drawing arena
	 * @param endx
	 *            the endx of our drawing area
	 * @param starty
	 *            the starty of our drawing area
	 * @param endy
	 *            the endy of our drawing area
	 */
	public void paintSelectedRegion(Graphics g, int scale, int startx,
			int endx, int starty, int endy)
	{
		int row = table.getSelectedRow();

		if (row != -1)
		{
			Rectangle view = new Rectangle(startx * 16, starty * 16,
					(endx - startx) * 16, (endy - starty) * 16);

			Color preColor = g.getColor();

			Region r = (Region) tableModel.data.get(row);
			g.setColor(r.color);

			for (int x = 0; x < r.rects.size(); ++x)
			{
				Rectangle rect = (Rectangle) r.rects.get(x);

				Rectangle port = new Rectangle(rect.x * 16, rect.y * 16,
						rect.width * 16, rect.height * 16);

				if (view.intersects(port))
				{
					g.fillRect((rect.x * scale), (rect.y * scale),
							(rect.width * scale), (rect.height * scale));
				}
			}

			g.setColor(preColor);
		}
	}

	class ColorRenderer extends JLabel implements TableCellRenderer
	{
		Border unselectedBorder = null;

		Border selectedBorder = null;

		boolean isBordered = true;

		public ColorRenderer(boolean isBordered)
		{
			this.isBordered = isBordered;
			setOpaque(true); // MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(JTable table,
				Object color, boolean isSelected, boolean hasFocus, int row,
				int column)
		{
			Color newColor = (Color) color;
			setBackground(newColor);
			if (isBordered)
			{
				if (isSelected)
				{
					if (selectedBorder == null)
					{
						selectedBorder = BorderFactory.createMatteBorder(2, 5,
								2, 5, table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				}
				else
				{
					if (unselectedBorder == null)
					{
						unselectedBorder = BorderFactory.createMatteBorder(2,
								5, 2, 5, table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}

			return this;
		}
	}

	public class StringEditor extends AbstractCellEditor implements
			TableCellEditor, MouseListener
	{
		String currentString;

		JLabel label;

		public StringEditor()
		{
			label = new JLabel();
			label.addMouseListener(this);
			label.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, table
					.getSelectionBackground()));
			label.setOpaque(true);
			label.setBackground(table.getSelectionBackground());
			label.setForeground(table.getSelectionForeground());
		}

		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2)
			{
				boolean repeat = false; // is this name already in the table?

				do
				{
					if (repeat == true)
					{
						JOptionPane.showMessageDialog(label,
								"That name is already used by another region.");
						repeat = false;
					}

					String rv = JOptionPane.showInputDialog(label,
							"Enter Region Name:", currentString);

					if (rv != null) // didn't press cancel
					{
						for (int x = 0; x < tableModel.data.size(); ++x)
						{
							if (x == table.getSelectedRow()) // can't repeat
																// yourself
								continue;

							String name = ((Region) tableModel.data.get(x)).name;

							if (name.equals(rv))
							{
								repeat = true;
								break;
							}
						}

						if (repeat == false)
							currentString = rv;
					}
				}
				while (repeat == true);

				String newName = currentString.replace(' ', '_');

				if (!newName.equals(currentString))
				{
					JOptionPane.showMessageDialog(null,
							"Spaces have been converted to underscores for region "
									+ newName);
					currentString = newName;
				}

				fireEditingStopped(); // Make the renderer reappear.
			}
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}

		public void mousePressed(MouseEvent e)
		{
		}

		public void mouseReleased(MouseEvent e)
		{
		}

		// Implement the one CellEditor method that AbstractCellEditor doesn't.
		public Object getCellEditorValue()
		{
			return currentString;
		}

		// Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column)
		{
			currentString = (String) value;
			label.setText(currentString);

			return label;
		}
	}

	public class ColorEditor extends AbstractCellEditor implements
			TableCellEditor, MouseListener
	{
		Color currentColor;

		JLabel label;

		public ColorEditor()
		{
			label = new JLabel();
			label.setOpaque(true);
			label.setBorder(BorderFactory.createMatteBorder(2, 5, 2, 5, table
					.getSelectionBackground()));
			label.addMouseListener(this);
		}

		public void mouseClicked(MouseEvent e)
		{
			if (e.getClickCount() == 2)
			{
				label.setBackground(currentColor);

				Color c = JColorChooser.showDialog(label,
						"Choose Color for Region", currentColor);

				if (c != null)
					currentColor = c;

				fireEditingStopped(); // Make the renderer reappear.
			}
		}

		public void mouseEntered(MouseEvent e)
		{
		}

		public void mouseExited(MouseEvent e)
		{
		}

		public void mousePressed(MouseEvent e)
		{
		}

		public void mouseReleased(MouseEvent e)
		{
		}

		// Implement the one CellEditor method that AbstractCellEditor doesn't.
		public Object getCellEditorValue()
		{
			return currentColor;
		}

		// Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column)
		{
			currentColor = (Color) value;
			label.setBackground(currentColor);
			return label;
		}
	}

	class MyTableModel extends AbstractTableModel
	{
		String[] columnNames = { "Name", "Color" };

		public Vector data = new Vector();

		public int getColumnCount()
		{
			return columnNames.length;
		}

		public int getRowCount()
		{
			return data.size();
		}

		public String getColumnName(int col)
		{
			return columnNames[col];
		}

		public Object getValueAt(int row, int col)
		{
			if (col == 0)
				return ((Region) data.get(row)).name;

			return ((Region) data.get(row)).color;
		}

		public Class getColumnClass(int c)
		{
			if (c == 0)
				return String.class;
			else
				return Color.class;
		}

		public boolean isCellEditable(int row, int col)
		{
			return true;
		}

		public void setValueAt(Object value, int row, int col)
		{
			Region r = (Region) data.get(row);

			if (col == 0)
				r.name = (String) value;
			else if (col == 1)
				r.color = (Color) value;

			fireTableCellUpdated(row, col);
		}
	}

	public void insertUpdate(DocumentEvent e)
	{
		arenaChanged();
	}

	public void removeUpdate(DocumentEvent e)
	{
		arenaChanged();
	}

	public void changedUpdate(DocumentEvent e)
	{
	}

	/**
	 * the text field warpArena was changed, update the current region
	 */
	public void arenaChanged()
	{
		int row = table.getSelectedRow();
		if (row == -1)
		{
			System.out.println("key typed without selected row?");
			return;
		}

		Region r = (Region) tableModel.data.get(row);

		r.arena = autoArena.getText();
	}

}