package editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;

import editor.asss.ELvlAttrEditor;
import editor.imageeditor.TilesetDialog;
import editor.loaders.BitMap;
import editor.loaders.LevelFile;
import editor.lvz.MapObject;
import editor.xml.XMLNode;

// This code is distributed under GNU's public listence, which is included with the release version.
// Don't copyright it and sue me for using it, or remove this warning.

public class Main extends JFrame implements ActionListener, ChangeListener,
		WindowListener
{
	final FileDialog m_fileChooser;

	public final static String VERSION = "1.2";

	public boolean gridOn = true;

	public static String untitledMapName = "(Untitled Map)";
	public static String rootDirectory = "";

	private JToolBar toolBarManager;

	private JTabbedPane contentManager;

	// Toolbar buttons
	private JButton managerNew, managerOpen, managerSave, managerSaveAll;

	public JToggleButton buttonPen, buttonLine, buttonSelect, buttonEyedrop,
			buttonAutoPen, buttonLvz, buttonLvzSelection, buttonZoom,
			buttonRgn, buttonSquare, buttonSquareFill, buttonFill,
			buttonEllipse, buttonEllipseFill;

	public JLabel status;

	// Menu variables
	private JMenuBar menuBar;

	private JMenu file;

	private JMenuItem fileNew, fileOpen, fileClose, fileSave, fileSaveAs,
			fileExit, fileSaveAll;

	public JMenuItem editDelete, editCut, editCopy, editPaste, editUndo,
			editRedo, editTileset;

	private JMenuItem viewZoomIn, viewZoomOut;

	private JMenuItem style1, style2, style3, style4, style5, style6;

	public JMenuItem windowRadar, windowTileset, windowAutoTile, windowLvz,
			windowAsssRegions;

	private JMenuItem helpTools, helpLvz, helpAbout, helpManual;

	private JMenuItem eLvlAttrs;

	private JMenuItem flipHoriz, flipVert, rotate, retile, applyEzTile,
			randomFill, generateScript;

	private JCheckBoxMenuItem showGrid;
	private JCheckBoxMenuItem altHotKey;

	private JCheckBoxMenuItem singleSelectionLvz;
	private JCheckBoxMenuItem dragLvz;

	private ImageIcon toolsHelpIcon = null;

	private ImageIcon elvlIcon = null;

	public ProjectState ps;

	// for copy / paste
	Vector copied = null;

	HashMap copied_lvz = null;

	// Extra images for maps
	protected Image extras[];

	static protected Image newTiles[];

	// Last Loaded maps
	String lastMaps[] = new String[3];

	JMenuItem lastMI[] = new JMenuItem[3];

	private int scriptStartOffset = 0;
	private String defaultScriptString = "Spawn#%=$";
	private String scriptString = defaultScriptString;

	// show eLVL save window?
	boolean confirmELVL = true;

	// Windows
	public static final int RADAR_WINDOW = 1;

	public static final int TILESET_WINDOW = 2;

	public static final int AUTOTILE_WINDOW = 3;

	public static final int LVZ_WINDOW = 4;

	public static final int REGION_WINDOW = 5;

	private ListDialog ld = null;

	public Main()
	{
		// Set the title
		super("Continuum Level / Lvz Tool (" + VERSION + ")");

		// Find root directory...
		// Our path will start with either "path\to\CLT.jar" OR "path\to\root"
		// This may or may not include additional paths, so drop the stuff we don't need.
		String strClassPath = System.getProperty("java.class.path");

		if(strClassPath == null)
			Main.rootDirectory = new File(".").getAbsolutePath();
		else
		{
			int intIndex = strClassPath.indexOf(File.separator);
			
			if(intIndex != -1)
				strClassPath = strClassPath.substring(0, intIndex);
	
			// Check if we're running as a .jar or .class
			if(strClassPath.matches("(?i:.*\\.jar)"))
			{
				// We know where the jar is, so grab the parent directory...
				File objCurDir = new File(strClassPath).getParentFile();
	
				if(objCurDir == null)
					objCurDir = new File(".");
	
				Main.rootDirectory = objCurDir.getAbsolutePath();
			} 
			else 
			{
				// Check if the include directory is in our classpath.
				File objCurDir = new File(strClassPath);
	
				while(objCurDir != null)
				{
					File objCheck = new File(objCurDir, "include");
	
					if(objCheck.isDirectory())
						break;
	
					objCurDir = objCurDir.getParentFile();
				}
	
				if(objCurDir == null)
					objCurDir = new File(".");
	
				Main.rootDirectory = objCurDir.getAbsolutePath();
			}
		}

		// Add components
		init();

		// Load external files that may be needed
		load();
		setVisible(true);

		// Create file chooser
		m_fileChooser = new FileDialog(this);

		// add this window listener
		addWindowListener(this);

		setFocusable(true);

		// setFocusTraversalKeysEnabled(false);

		requestFocus();

		ld = new ListDialog(this);
	}

	private void init()
	{
		// Get our container and set a boxlayout
		Container cp = getContentPane();
		cp.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// cp.setLayout( new BoxLayout( cp, BoxLayout.PAGE_AXIS ) );

		// Create our Toolbar to manage files
		toolBarManager = new JToolBar();
		toolBarManager.setFloatable(false);
		toolBarManager.setRollover(true);
		c.anchor = GridBagConstraints.WEST;
		cp.add(toolBarManager, c);
		ImageIcon iconNew = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "new.gif", "New");
		managerNew = new JButton(iconNew);
		managerNew.addActionListener(this);
		ImageIcon iconOpen = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "open.gif", "New");
		managerOpen = new JButton(iconOpen);
		managerOpen.addActionListener(this);
		ImageIcon iconSave = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "save.gif", "New");
		managerSave = new JButton(iconSave);
		managerSave.addActionListener(this);
		managerSave.setEnabled(false);
		ImageIcon iconSaveAll = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "saveall.gif", "New");
		managerSaveAll = new JButton(iconSaveAll);
		managerSaveAll.addActionListener(this);
		managerSaveAll.setEnabled(false);
		toolBarManager.add(managerNew);
		toolBarManager.add(managerOpen);
		toolBarManager.add(managerSave);
		toolBarManager.add(managerSaveAll);

		toolBarManager.addSeparator(new Dimension(30, 24));

		ImageIcon iconPenTool = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "pen.gif", "New");
		buttonPen = new JToggleButton(iconPenTool, true);
		buttonPen.setPreferredSize(new Dimension(24, 24));
		buttonPen.addActionListener(this);
		toolBarManager.add(buttonPen);

		ImageIcon iconLineTool = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "line.gif", "New");
		buttonLine = new JToggleButton(iconLineTool, false);
		buttonLine.setPreferredSize(new Dimension(24, 24));
		buttonLine.addActionListener(this);
		toolBarManager.add(buttonLine);

		ImageIcon iconSquareTool = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "square.gif", "New");
		buttonSquare = new JToggleButton(iconSquareTool, false);
		buttonSquare.setPreferredSize(new Dimension(24, 24));
		buttonSquare.addActionListener(this);
		toolBarManager.add(buttonSquare);

		ImageIcon iconSquareFillTool = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "squareFill.gif", "New");
		buttonSquareFill = new JToggleButton(iconSquareFillTool, false);
		buttonSquareFill.setPreferredSize(new Dimension(24, 24));
		buttonSquareFill.addActionListener(this);
		toolBarManager.add(buttonSquareFill);

		ImageIcon iconEllipseTool = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "ellipse.gif", "New");
		buttonEllipse = new JToggleButton(iconEllipseTool, false);
		buttonEllipse.setPreferredSize(new Dimension(24, 24));
		buttonEllipse.addActionListener(this);
		toolBarManager.add(buttonEllipse);

		ImageIcon iconEllipseFillTool = new ImageIcon(Main.rootDirectory + File.separator +
				"include" + File.separator + "icons" + File.separator
						+ "ellipseFill.gif", "New");
		buttonEllipseFill = new JToggleButton(iconEllipseFillTool, false);
		buttonEllipseFill.setPreferredSize(new Dimension(24, 24));
		buttonEllipseFill.addActionListener(this);
		toolBarManager.add(buttonEllipseFill);

		ImageIcon iconFillTool = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "fill.gif", "New");
		buttonFill = new JToggleButton(iconFillTool, false);
		buttonFill.setPreferredSize(new Dimension(24, 24));
		buttonFill.addActionListener(this);
		toolBarManager.add(buttonFill);

		toolBarManager.addSeparator(new Dimension(30, 24));

		ImageIcon iconSelectTool = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "select.gif", "New");
		buttonSelect = new JToggleButton(iconSelectTool, false);
		buttonSelect.setPreferredSize(new Dimension(24, 24));
		buttonSelect.addActionListener(this);
		toolBarManager.add(buttonSelect);

		ImageIcon iconEyedropTool = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "eyedrop.gif", "New");
		buttonEyedrop = new JToggleButton(iconEyedropTool, false);
		buttonEyedrop.setPreferredSize(new Dimension(24, 24));
		buttonEyedrop.addActionListener(this);
		toolBarManager.add(buttonEyedrop);

		ImageIcon iconAutoPen = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "autopen.gif", "New");
		buttonAutoPen = new JToggleButton(iconAutoPen, false);
		buttonAutoPen.setPreferredSize(new Dimension(24, 24));
		buttonAutoPen.addActionListener(this);
		toolBarManager.add(buttonAutoPen);

		ImageIcon iconLvz = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "lvzstamp.gif", "New");
		buttonLvz = new JToggleButton(iconLvz, false);
		buttonLvz.setPreferredSize(new Dimension(24, 24));
		buttonLvz.addActionListener(this);
		toolBarManager.add(buttonLvz);

		ImageIcon iconLvzSelection = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
				+ "icons" + File.separator + "lvzselection.gif", "New");
		buttonLvzSelection = new JToggleButton(iconLvzSelection, false);
		buttonLvzSelection.setPreferredSize(new Dimension(24, 24));
		buttonLvzSelection.addActionListener(this);
		toolBarManager.add(buttonLvzSelection);

		ImageIcon iconZoom = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "zoom.gif", "New");
		buttonZoom = new JToggleButton(iconZoom, false);
		buttonZoom.setPreferredSize(new Dimension(24, 24));
		buttonZoom.addActionListener(this);
		toolBarManager.add(buttonZoom);

		ImageIcon iconRgn = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "rgn.gif", "New");
		buttonRgn = new JToggleButton(iconRgn, false);
		buttonRgn.setPreferredSize(new Dimension(24, 24));
		buttonRgn.addActionListener(this);
		toolBarManager.add(buttonRgn);

		toolBarManager.addSeparator(new Dimension(30, 24));

		status = new JLabel();
		status.setSize(new Dimension(200, 24));
		toolBarManager.add(status);

		// Create our Content area
		contentManager = new JTabbedPane();
		contentManager.addChangeListener(this);

		c.fill = GridBagConstraints.BOTH;
		c.gridy = 2;
		c.weightx = 1;
		c.weighty = 1;
		cp.add(contentManager, c);

		// Create our menu
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// File
		file = new JMenu("File");
		file.setMnemonic(KeyEvent.VK_F);
		menuBar.add(file);

		// New
		fileNew = new JMenuItem("New");
		fileNew.setMnemonic(KeyEvent.VK_N);
		fileNew.addActionListener(this);
		file.add(fileNew);

		// Open
		fileOpen = new JMenuItem("Open");
		fileOpen.setMnemonic(KeyEvent.VK_O);
		fileOpen.addActionListener(this);
		file.add(fileOpen);

		// Close
		fileClose = new JMenuItem("Close");
		fileClose.setMnemonic(KeyEvent.VK_C);
		fileClose.addActionListener(this);
		fileClose.setEnabled(false);
		file.add(fileClose);

		file.addSeparator();

		// Save
		fileSave = new JMenuItem("Save");
		fileSave.setMnemonic(KeyEvent.VK_S);
		fileSave.addActionListener(this);
		fileSave.setEnabled(false);
		file.add(fileSave);

		// SaveAs
		fileSaveAs = new JMenuItem("Save As...");
		fileSaveAs.setMnemonic(KeyEvent.VK_A);
		fileSaveAs.addActionListener(this);
		fileSaveAs.setEnabled(false);
		file.add(fileSaveAs);

		// SaveAll
		fileSaveAll = new JMenuItem("Save All");
		fileSaveAll.setMnemonic(KeyEvent.VK_L);
		fileSaveAll.addActionListener(this);
		fileSaveAll.setEnabled(false);
		file.add(fileSaveAll);

		file.addSeparator();

		file.addSeparator();

		// Exit
		fileExit = new JMenuItem("Exit");
		fileExit.setMnemonic(KeyEvent.VK_X);
		fileExit.addActionListener(this);
		file.add(fileExit);

		// Edit
		JMenu edit = new JMenu("Edit");
		menuBar.add(edit);

		// Undo
		editUndo = new JMenuItem("Undo");
		editUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				ActionEvent.CTRL_MASK));
		editUndo.setEnabled(false);
		editUndo.addActionListener(this);
		edit.add(editUndo);

		// Redo
		editRedo = new JMenuItem("Redo");
		editRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				ActionEvent.CTRL_MASK));
		editRedo.setEnabled(false);
		editRedo.addActionListener(this);
		edit.add(editRedo);

		edit.addSeparator();

		editCut = new JMenuItem("Cut");
		editCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
				ActionEvent.CTRL_MASK));
		editCut.setEnabled(false);
		editCut.addActionListener(this);
		edit.add(editCut);

		editDelete = new JMenuItem("Delete");
		editDelete
				.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		editDelete.addActionListener(this);
		edit.add(editDelete);

		editCopy = new JMenuItem("Copy");
		editCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
				ActionEvent.CTRL_MASK));
		editCopy.setEnabled(false);
		editCopy.addActionListener(this);
		edit.add(editCopy);

		editPaste = new JMenuItem("Paste");
		editPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
				ActionEvent.CTRL_MASK));
		editPaste.setEnabled(false);
		editPaste.addActionListener(this);
		edit.add(editPaste);

		edit.addSeparator();

		eLvlAttrs = new JMenuItem("Edit eLVL Attributes");
		eLvlAttrs.setEnabled(false);
		eLvlAttrs.addActionListener(this);
		edit.add(eLvlAttrs);

		edit.addSeparator();

		editTileset = new JMenuItem("Edit Tileset");
		// editTileset.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Y,
		// ActionEvent.CTRL_MASK) );
		editTileset.setEnabled(false);
		editTileset.addActionListener(this);
		edit.add(editTileset);

		JMenu selection = new JMenu("Selection");
		menuBar.add(selection);

		flipHoriz = new JMenuItem("Flip Horizontally");
		flipHoriz.addActionListener(this);
		selection.add(flipHoriz);

		flipVert = new JMenuItem("Flip Vertically");
		flipVert.addActionListener(this);
		selection.add(flipVert);

		rotate = new JMenuItem("Rotate by Angle...");
		rotate.addActionListener(this);
		selection.add(rotate);

		retile = new JMenuItem("Swap Tiles (Retile)");
		retile.addActionListener(this);
		selection.add(retile);

		applyEzTile = new JMenuItem("Apply EZ Tiles Inside Selection");
		applyEzTile.addActionListener(this);
		selection.add(applyEzTile);

		randomFill = new JMenuItem(
				"Randomly Fill Selection With Selected Tile (by %)");
		randomFill.addActionListener(this);
		selection.add(randomFill);

		generateScript = new JMenuItem(
				"Generate Script");
		generateScript.addActionListener(this);
		selection.add(generateScript);

		// View
		JMenu view = new JMenu("View");
		menuBar.add(view);

		// Zoom in
		viewZoomIn = new JMenuItem("Zoom In");
		viewZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP,
				ActionEvent.CTRL_MASK));
		viewZoomIn.setEnabled(false);
		viewZoomIn.addActionListener(this);
		view.add(viewZoomIn);

		// Zoom out
		viewZoomOut = new JMenuItem("Zoom Out");
		viewZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
				ActionEvent.CTRL_MASK));
		viewZoomOut.setEnabled(false);
		viewZoomOut.addActionListener(this);
		view.add(viewZoomOut);

		// Set Desired Look
		JMenu desiredLook = new JMenu("Set Look");
		view.add(desiredLook);

		style1 = new JCheckBoxMenuItem("Default");
		style1.setSelected(true);
		style1.addActionListener(this);
		desiredLook.add(style1);

		style2 = new JCheckBoxMenuItem("Java");
		style2.addActionListener(this);
		desiredLook.add(style2);

		style3 = new JCheckBoxMenuItem("GTK");
		style3.addActionListener(this);
		desiredLook.add(style3);

		style4 = new JCheckBoxMenuItem("Metal");
		style4.addActionListener(this);
		desiredLook.add(style4);

		style5 = new JCheckBoxMenuItem("Motif");
		style5.addActionListener(this);
		desiredLook.add(style5);

		style6 = new JCheckBoxMenuItem("SCE");
		style6.addActionListener(this);
		desiredLook.add(style6);

		// Window
		JMenu window = new JMenu("Window");
		menuBar.add(window);

		// Radar
		windowRadar = new JMenuItem("Radar");
		windowRadar.setEnabled(false);
		windowRadar.addActionListener(this);
		window.add(windowRadar);

		// Tileset
		windowTileset = new JMenuItem("Tileset");
		windowTileset.setEnabled(false);
		windowTileset.addActionListener(this);
		window.add(windowTileset);

		// Auto Tile Window
		windowAutoTile = new JMenuItem("Auto Tile");
		windowAutoTile.setEnabled(false);
		windowAutoTile.addActionListener(this);
		window.add(windowAutoTile);

		windowLvz = new JMenuItem("Lvz");
		windowLvz.setEnabled(false);
		windowLvz.addActionListener(this);
		window.add(windowLvz);

		windowAsssRegions = new JMenuItem("ASSS Regions");
		windowAsssRegions.setEnabled(false);
		windowAsssRegions.addActionListener(this);
		window.add(windowAsssRegions);

		JMenu preferencesMenu = new JMenu("Preferences");
		menuBar.add(preferencesMenu);

		showGrid = new JCheckBoxMenuItem("Show Grid", true);
		showGrid.addActionListener(this);
		preferencesMenu.add(showGrid);

		altHotKey = new JCheckBoxMenuItem("Use Alternate Hotkeys", false);
		altHotKey.addActionListener(this);
		preferencesMenu.add(altHotKey);

		preferencesMenu.addSeparator();

		singleSelectionLvz = new JCheckBoxMenuItem("Select Only Top LVZ", true);
		preferencesMenu.add(singleSelectionLvz);

		dragLvz = new JCheckBoxMenuItem("Enable Lvz Dragging", false);
		preferencesMenu.add(dragLvz);


		JMenu help = new JMenu("Help");
		menuBar.add(help);

		helpTools = new JMenuItem(
				"What are all of these tools, and how do I use them?");
		helpTools.addActionListener(this);
		help.add(helpTools);

		helpLvz = new JMenuItem("Where do my LVZ files save?");
		helpLvz.addActionListener(this);
		help.add(helpLvz);

		helpManual = new JMenuItem("View Manual");
		helpManual.addActionListener(this);
		help.add(helpManual);

		help.addSeparator();

		helpAbout = new JMenuItem("About Continuum Level / Lvz Tool");
		helpAbout.addActionListener(this);
		help.add(helpAbout);
	}

	private void load()
	{
		// Loads in extra images: asteroids, wormhole, etc...

		try
		{
			extras = new Image[6];
			for (int i = 1; i < 6; i++)
			{
				File file = new File(Main.rootDirectory + File.separator + "include" + File.separator + "over" + i + ".bm2");
				BufferedInputStream bs = new BufferedInputStream(
						new FileInputStream(file));
				BitMap b = new BitMap(bs);
				b.readBitMap(true);

				if (i == 1 || i == 3)
					extras[i - 1] = b.getImage(16);
				else if (i == 2)
					extras[i - 1] = b.getImage(32);
				else if (i == 4)
					extras[i - 1] = b.getImage(96);
				else if (i == 5)
					extras[i - 1] = b.getImage(80);

			}
		}
		catch (FileNotFoundException e)
		{
			JOptionPane.showMessageDialog(null, e);
		}

		newTiles = new Image[8];

		newTiles[0] = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "tile191.PNG").getImage();

		newTiles[1] = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "tile192.PNG").getImage();

		newTiles[2] = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "tile241.PNG").getImage();

		newTiles[3] = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "tile242.PNG").getImage();

		newTiles[4] = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "tile252.PNG").getImage();

		newTiles[5] = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "tile253.PNG").getImage();

		newTiles[6] = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "tile254.PNG").getImage();

		newTiles[7] = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator + "icons"
				+ File.separator + "tile255.PNG").getImage();

		// also load the preferences
		XMLNode doc = XMLNode.readDocumentNoError(Main.rootDirectory + File.separator + "include" + File.separator
				+ "preferences.xml");

		ps = new ProjectState(); // use default locations

		if (doc != null)
		{
			doc = doc.getChild("Preferences");

			if (doc == null)
			{
				JOptionPane
						.showMessageDialog(null,
								"Preferences.xml has been tampered with, no Preferences Node.");
			}
			else
			{
				// and override whatever the user wants

				XMLNode x;

				x = doc.getChild("loc_asssRegions_x");
				if (x != null)
					ps.pos_AsssRegions.x = x.getIntValue();

				x = doc.getChild("loc_asssRegions_y");
				if (x != null)
					ps.pos_AsssRegions.y = x.getIntValue();

				x = doc.getChild("loc_autoTile_x");
				if (x != null)
					ps.pos_Autotool.x = x.getIntValue();

				x = doc.getChild("loc_autoTile_y");
				if (x != null)
					ps.pos_Autotool.y = x.getIntValue();

				x = doc.getChild("loc_lvzImages_x");
				if (x != null)
					ps.pos_LvzImages.x = x.getIntValue();

				x = doc.getChild("loc_lvzImages_y");
				if (x != null)
					ps.pos_LvzImages.y = x.getIntValue();

				x = doc.getChild("loc_radar_x");
				if (x != null)
					ps.pos_Radar.x = x.getIntValue();

				x = doc.getChild("loc_radar_y");
				if (x != null)
					ps.pos_Radar.y = x.getIntValue();

				x = doc.getChild("loc_tileset_x");
				if (x != null)
					ps.pos_Tileset.x = x.getIntValue();

				x = doc.getChild("loc_tileset_y");
				if (x != null)
					ps.pos_Tileset.y = x.getIntValue();

				x = doc.getChild("confirm_elvl");
				if (x != null)
					confirmELVL = x.getIntValue() != 0;

				x = doc.getChild("show_radar");
				if (x != null)
				{
					ps.m_radar = ps.showRadar = x.getIntValue() != 0;
				}

				x = doc.getChild("lvzDrag");

				if (x != null)
				{
					ps.lvzDrag = x.getIntValue() != 0;
					dragLvz.setSelected(ps.lvzDrag);
				}

				x = doc.getChild("lvzSelectSingle");

				if (x != null)
				{
					ps.lvzSelectSingle = x.getIntValue() != 0;
					singleSelectionLvz.setSelected(ps.lvzSelectSingle);
				}

				x = doc.getChild("useAlt"); // alternate hotkeys
				if (x != null)
				{
					ps.useAlt = x.getIntValue() != 0;

					if (ps.useAlt)
					{
						altHotKey.setState(true);
						setHotKeys(true);
					}
				}

				x = doc.getChild("last_map_0");
				if (x != null)
				{
					lastMaps[0] = x.value.replace('?', ' ');
					File f = new File(lastMaps[0]);
					lastMI[0] = new JMenuItem(f.getName());
					lastMI[0].addActionListener(this);
					addMenuItemToFile(lastMI[0]);

					x = doc.getChild("last_map_1");
					if (x != null)
					{
						lastMaps[1] = x.value.replace('?', ' ');
						f = new File(lastMaps[1]);
						lastMI[1] = new JMenuItem(f.getName());
						lastMI[1].addActionListener(this);
						addMenuItemToFile(lastMI[1]);

						x = doc.getChild("last_map_2");
						if (x != null)
						{
							lastMaps[2] = x.value.replace('?', ' ');
							f = new File(lastMaps[2]);
							lastMI[2] = new JMenuItem(f.getName());
							lastMI[2].addActionListener(this);
							addMenuItemToFile(lastMI[2]);
						}
					}
				}

				// set window location
				int inset = 50;
				Dimension screenSize = Toolkit.getDefaultToolkit()
						.getScreenSize();
				Rectangle bounds = new Rectangle(inset, inset, screenSize.width
						- inset * 2, screenSize.height - inset * 2);

				x = doc.getChild("window_x");
				if (x != null)
				{
					int loc = x.getIntValue();
					if (loc > 0)
						bounds.x = loc;
					else
						bounds.x = 0;
				}

				x = doc.getChild("window_y");
				if (x != null)
				{
					int loc = x.getIntValue();
					if (loc > 0)
						bounds.y = loc;
					else
						bounds.y = 0;
				}

				x = doc.getChild("window_w");
				if (x != null)
				{
					int loc = x.getIntValue();
					if (bounds.x + loc > screenSize.width)
						bounds.width = screenSize.width - bounds.x;
					else
						bounds.width = loc;
				}

				x = doc.getChild("window_h");
				if (x != null)
				{
					int loc = x.getIntValue();
					if (bounds.y + loc > screenSize.height)
						bounds.height = screenSize.height - bounds.y;
					else
						bounds.height = loc;
				}

				setBounds(bounds.x, bounds.y, bounds.width, bounds.height);
			}
		}
		else
		{ // doc == null, first time running it
			JOptionPane
					.showMessageDialog(
							null,
							"Welcome to Continuum Level Tool by Team 12! This message should only show\n"
									+ "up the first time you are running the editor. CLT is a map editor for SubSpace/Continuum\n"
									+ "which supports WYSIWYG LVZ Editng and eLVL Attributes and Regions. If you want\n"
									+ "to suggest a feature, or report a bug feel free (emails are on the website where you \n"
									+ "downloaded this). Although this isn't too much different from SSME or CLE, there are a few\n"
									+ "additional tools you should get acquainted with, which I'll show you in a minute. To see \n"
									+ "the tools help again, just look in the Help menu in the menubar. Thanks trying\n"
									+ "Continuum Level Tool! You are using version "
									+ VERSION + ".");

			help_tools();

			saveProjectState(new ProjectState());

			int inset = 50;
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Rectangle bounds = new Rectangle(inset, inset, screenSize.width
					- inset * 2, screenSize.height - inset * 2);

			setBounds(bounds);
		}
	}

	private void setHotKeys(boolean alternate)
	{
		if (alternate)
		{
			viewZoomIn.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_EQUALS, ActionEvent.CTRL_MASK));
			viewZoomOut.setAccelerator(KeyStroke.getKeyStroke(
					KeyEvent.VK_MINUS, ActionEvent.CTRL_MASK));
		}
		else
		{
			viewZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP,
					ActionEvent.CTRL_MASK));
			viewZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
					ActionEvent.CTRL_MASK));
		}

		ps.useAlt = alternate;
	}

	public void flipHoiz()
	{
		int index = contentManager.getSelectedIndex();

		if (index != -1)
		{
			Editor e = (Editor) contentManager.getComponent(index);

			Vector flipped = new Vector();
			Vector selection = e.m_window.selection;

			if (selection != null)
			{
				for (int x = selection.size() - 1; x >= 0; --x)
				{
					flipped.add(selection.get(x));
				}

				e.m_window.selection = flipped;

				e.m_window.repaint();
				e.m_window.m_radar.repaintRadar();

				e.m_window.undoer.snapShot(e.m_window);
			}
		}
	}

	private void doScriptWork()
	{
		// scriptString, scriptStartOffset
		int index = contentManager.getSelectedIndex();

		if (index != -1)
		{
			Editor e = (Editor) contentManager.getComponent(index);
			int primary = e.m_window.m_tileset.getTile(MouseEvent.BUTTON1);

			Vector selection = e.m_window.selection;
			ArrayList <Point> pos = new ArrayList <Point>();

			if (selection != null && selection.size() > 0)
			{
				int w = selection.size();
				int h = ((Vector) selection.get(0)).size();

				for (int x = 0; x < w; ++x)
				{
					for (int y = 0; y < h; ++y)
					{
						int x_pos = e.m_window.minX + x;
						int y_pos = e.m_window.minY + y;

						if (((Integer)((Vector) selection.get(x)).get(y)).intValue() == primary)
							pos.add(new Point(x_pos,y_pos));
					}
				}
			}
			else
			{
				// no selection, use the whole map
				for (int y = 0; y < 1024; ++y) for (int x = 0; x < 1024; ++x)
				{
					if (e.m_map[x][y] == primary)
					{
						pos.add(new Point(x,y));
					}
				}
			}

			String s = "";

			for (int x = 0; x < pos.size(); ++x)
			{
				Point p = pos.get(x);

				int num = x + scriptStartOffset;

				String one = scriptString;
				one = one.replace("#", "" + num);
				one = one.replace("%", "X");
				one = one.replace("\\n", "\n");
				one = one.replace("$","" + p.x);

				s += one + "\n";

				one = scriptString;
				one = one.replace("#", "" + num);
				one = one.replace("%", "Y");
				one = one.replace("\\n", "\n");
				one = one.replace("$","" + p.y);

				s += one + "\n";
			}

			ld.setText(s);
			ld.setVisible(true);
		}
		else
			JOptionPane.showMessageDialog(null,"You don't have a map open.");
	}

	public void doGenerateScript()
	{
		int n = JOptionPane.showConfirmDialog(
			    null,
			    "Often times when editing settings files you must specify things on the map "
			    + "in tiles, which can get boring when doing by hand. \nThis option will take every " +
		    		"instance of the primary tile in the selection (or map if there is no selection)" +
		    		"\nand convert it to text in the " +
		    		"format you specify. So to generate things like:\nSpawn0X=512\nSpawn0Y=154\n" +
		    		"Spawn1X=520\nSpawn1Y=200\n...\n\nYou should use input string \"Spawn#%=$\" " +
		    		"where # is the number," +
		    		" and % gets replaced by X or Y and $ is the value.\n Set start offset to " +
		    		"0 to start at 0, or use" +
		    		" a higher number to start higher up. Press Ok to continue.",
			    "Generate Script",
			    JOptionPane.OK_CANCEL_OPTION);

		if (n == JOptionPane.OK_OPTION)
		{
			String inputString = (String)JOptionPane.showInputDialog(
                    null,
                    "Input String: ",
                    "#=num, %= X/Y, \\n=new line, $=val",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    scriptString);

			if (inputString != null)
			{
				if (!inputString.contains("#"))
					JOptionPane.showMessageDialog(null,"Your input string must contain a '#'.");
				else if (!inputString.contains("%"))
					JOptionPane.showMessageDialog(null,"Your input string must contain a '%'.");
				else
				{
					scriptString = inputString;

					String offsetString = (String)JOptionPane.showInputDialog(
		                    null,
		                    "Start Offset:",
		                    "Generate Script # Offset",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    null,
		                    "" + scriptStartOffset);

					if (offsetString != null)
					{
						boolean err = false;
						try
						{
							scriptStartOffset = Integer.parseInt(offsetString);
						}
						catch (Exception e)
						{
							scriptStartOffset = 0;
							JOptionPane.showMessageDialog(null,e);
							err = true;
						}

						if (!err)
						{
							doScriptWork();
						}
					}
				}
			}
		}
	}

	public void randomFill()
	{
		int index = contentManager.getSelectedIndex();

		if (index != -1)
		{
			String s = JOptionPane.showInputDialog(null,
					"How much percent do you want to be filled?", "5.0");

			if (s != null)
			{
				try
				{
					double d = Double.parseDouble(s);

					Editor e = (Editor) contentManager.getComponent(index);

					Vector selection = e.m_window.selection;
					int selectedTile = e.m_window.m_tileset.getTile(0);

					if (selectedTile == TilesetWindow.TILE_EZ)
					{
						JOptionPane
								.showMessageDialog(null,
										"You can't fill by percent with the EZ tile. Sorry.");
					}
					else
					{
						if (selection != null)
						{
							for (int x = 0; x < selection.size(); ++x)
							{
								Vector col = (Vector) selection.get(x);

								for (int y = 0; y < col.size(); ++y)
								{
									if (Math.random() * 100 < d)
										col.setElementAt(new Integer(
												selectedTile), y);
								}
							}
						}

						e.m_window.repaint();
						e.m_window.m_radar.repaintRadar();

						e.m_window.undoer.snapShot(e.m_window);
					}
				}
				catch (NumberFormatException e)
				{
					JOptionPane.showMessageDialog(null,
							"Error parsing string into number: " + s);
				}
			}
		}
	}

	public void undo()
	{
		int index = contentManager.getSelectedIndex();

		if (index != -1)
		{
			Editor e = (Editor) contentManager.getComponent(index);

			e.m_window.undoer.doUndo(e.m_window);
		}
		else
			editUndo.setEnabled(false);
	}

	public void redo()
	{
		int index = contentManager.getSelectedIndex();

		if (index != -1)
		{
			Editor e = (Editor) contentManager.getComponent(index);

			e.m_window.undoer.doRedo(e.m_window);
		}
		else
			editRedo.setEnabled(false);
	}

	public void flipVert()
	{
		int index = contentManager.getSelectedIndex();

		if (index != -1)
		{
			Editor e = (Editor) contentManager.getComponent(index);

			Vector flipped = new Vector();
			Vector selection = e.m_window.selection;

			if (selection != null)
			{
				for (int x = 0; x < selection.size(); ++x)
				{
					Vector cur = (Vector) selection.get(x);
					Vector row = new Vector();

					for (int y = cur.size() - 1; y >= 0; --y)
					{
						row.add(cur.get(y));
					}

					flipped.add(row);
				}

				e.m_window.selection = flipped;

				e.m_window.repaint();
				e.m_window.m_radar.repaintRadar();

				e.m_window.undoer.snapShot(e.m_window);
			}
		}
	}

	public void rotateByAngle()
	{
		int index = contentManager.getSelectedIndex();

		if (index != -1)
		{
			Editor e = (Editor) contentManager.getComponent(index);

			Vector selection = e.m_window.selection;

			if (selection != null)
			{
				String s = JOptionPane.showInputDialog(null,
						"How many degrees clockwise should we rotate?", "90.0");

				if (s != null)
				{
					try
					{
						double d = Double.parseDouble(s);

						// rotate!
						AngleRotator.doRotate(selection, -d);

						// fix selection bounds
						e.m_window.width = 0;
						e.m_window.height = 0;

						e.m_window.width = selection.size();

						if (e.m_window.width > 0)
						{
							e.m_window.height = ((Vector) selection.get(0))
									.size();
						}

						e.m_window.undoer.snapShot(e.m_window);

						e.m_window.repaint();
						e.m_window.m_radar.repaintRadar();
					}
					catch (NumberFormatException ee)
					{
						JOptionPane.showMessageDialog(null,
								"Error parsing string into number: " + s);
					}
				}
			}
		}
	}

	public void retile()
	{
		int index = contentManager.getSelectedIndex();

		if (index != -1)
		{
			Editor e = (Editor) contentManager.getComponent(index);

			Vector selection = e.m_window.selection;

			if (selection != null)
			{

				Object[] options = { "Yes, Retile", "No, cancel" };

				int n = JOptionPane
						.showOptionDialog(
								this,
								"This will change all occurances in the selection of the secondary tile(blue) \n"
										+ "with the primary tile(red). If you can't see the tileset, go to windows, tileset.\n\n"
										+ "Are you sure this is what you want?",
								"Confirm Retiling", JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[1]);

				if (n == JOptionPane.YES_OPTION)
				{
					int to = e.m_window.m_tileset.getTile(MouseEvent.BUTTON1);
					int from = e.m_window.m_tileset.getTile(MouseEvent.BUTTON2);

					if (to == TilesetWindow.TILE_EZ
							|| from == TilesetWindow.TILE_EZ)
					{
						JOptionPane
								.showMessageDialog(null,
										"You can't swap tiles using the EZ tile. Sorry.");
					}
					else
					{

						for (int x = 0; x < selection.size(); ++x)
						{
							Vector col = (Vector) selection.get(x);

							for (int y = 0; y < col.size(); ++y)
							{
								int tile = ((Integer) col.get(y)).intValue();
								if (tile == from)
									col.setElementAt(new Integer(to), y);
							}
						}

						e.m_window.undoer.snapShot(e.m_window);
					}
				}
			}
		}
	}

	/**
	 * Gets an integer from a vector array, or 0 if we're out of bounds
	 *
	 * @param x
	 *            the x position in the vector array
	 * @param y
	 *            the y position
	 * @param v
	 *            the vector array
	 * @return the integer stored in the Integer in this position in the vector
	 *         array
	 */
	int getIntFromVecArray(int x, int y, Vector v)
	{
		int rv;

		if (x == -1 || y == -1 || v.size() == 0)
			rv = 0;
		else if (x >= v.size() || y >= ((Vector) v.get(0)).size())
			rv = 0;
		else
		{
			Vector v2 = (Vector) v.get(x);

			rv = ((Integer) v2.get(y)).intValue();
		}

		return rv;
	}

	public void applyEzTile()
	{
		int index = contentManager.getSelectedIndex();

		if (index != -1)
		{
			Editor e = (Editor) contentManager.getComponent(index);

			Vector selection = e.m_window.selection;

			if (selection != null)
			{
				short[][] autoSet = e.m_window.m_autotool.getAutoTileset();

				for (int x = 0; x < selection.size(); ++x)
				{
					Vector col = (Vector) selection.get(x);

					for (int y = 0; y < col.size(); ++y)
					{
						int tile = ((Integer) col.get(y)).intValue();

						boolean apply = false;

						for (int nx = 0; nx < 4 && !apply; ++nx)
						{
							for (int ny = 0; ny < 4; ++ny)
							{
								if (tile != 0 && tile == autoSet[nx][ny])
								{
									apply = true;
									break;
								}
							}
						}

						if (apply)
						{
							// construct neighbors
							int nebs[][] = new int[3][3];

							for (int nx = -1; nx < 2; ++nx)
							{
								for (int ny = -1; ny < 2; ++ny)
								{
									nebs[nx + 1][ny + 1] = getIntFromVecArray(x
											+ nx, y + ny, selection);
								}
							}

							int should = Tools.getWhatAutoTileShouldBe(nebs,
									autoSet);

							col.setElementAt(new Integer(should), y);
						}
					}
				}

				e.m_window.undoer.snapShot(e.m_window);

			}
		}
	}

	/**
	 * Add a menu item before the Exit clause, this
	 *
	 */
	private void addMenuItemToFile(JMenuItem i)
	{
		int count = file.getItemCount();

		file.add(i, count - 2);
	}

	public void actionPerformed(ActionEvent ae)
	{
		if (ae.getSource() == fileExit)
			close();
		else if (ae.getSource() == fileSaveAs)
			cmdSaveAs();
		else if (ae.getSource() == fileSaveAll
				|| ae.getSource() == managerSaveAll)
			cmdSaveAll();
		else if (ae.getSource() == fileSave || ae.getSource() == managerSave)
			cmdSave();
		else if (ae.getSource() == fileNew || ae.getSource() == managerNew)
			cmdNewFile();
		else if (ae.getSource() == fileOpen || ae.getSource() == managerOpen)
			cmdOpenFile();
		else if (ae.getSource() == fileClose)
			cmdCloseFile();
		else if (ae.getSource() == editTileset)
			cmdEditTileset();
		else if (ae.getSource() == viewZoomIn)
			cmdZoomIn();
		else if (ae.getSource() == viewZoomOut)
			cmdZoomOut();
		else if (ae.getSource() == buttonPen)
			setButtons(buttonPen, Tools.PEN);
		else if (ae.getSource() == buttonSelect)
			setButtons(buttonSelect, Tools.SELECT);
		else if (ae.getSource() == buttonEyedrop)
			setButtons(buttonEyedrop, Tools.EYEDROP);
		else if (ae.getSource() == buttonAutoPen)
			setButtons(buttonAutoPen, Tools.AUTO_PEN);
		else if (ae.getSource() == buttonLvz)
			setButtons(buttonLvz, Tools.LVZ);
		else if (ae.getSource() == buttonLvzSelection)
			setButtons(buttonLvzSelection, Tools.LVZ_SELECTION);
		else if (ae.getSource() == buttonZoom)
			setButtons(buttonZoom, Tools.ZOOM);
		else if (ae.getSource() == buttonRgn)
			setButtons(buttonRgn, Tools.RGN);
		else if (ae.getSource() == windowRadar)
			showWindow(RADAR_WINDOW);
		else if (ae.getSource() == windowTileset)
			showWindow(TILESET_WINDOW);
		else if (ae.getSource() == windowAutoTile)
			showWindow(AUTOTILE_WINDOW);
		else if (ae.getSource() == windowLvz)
			showWindow(LVZ_WINDOW);
		else if (ae.getSource() == windowAsssRegions)
			showWindow(REGION_WINDOW);
		else if (ae.getSource() == style1)
			setStyle(1);
		else if (ae.getSource() == style2)
			setStyle(2);
		else if (ae.getSource() == style3)
			setStyle(3);
		else if (ae.getSource() == style4)
			setStyle(4);
		else if (ae.getSource() == style5)
			setStyle(5);
		else if (ae.getSource() == style6)
			setStyle(6);
		else if (ae.getSource() == editCopy)
			copy();
		else if (ae.getSource() == editCut)
			cut();
		else if (ae.getSource() == editDelete)
			deleteSelection();
		else if (ae.getSource() == editPaste)
			paste();
		else if (ae.getSource() == helpTools)
			help_tools();
		else if (ae.getSource() == helpLvz)
			help_lvz();
		else if (ae.getSource() == helpManual)
			help_manual();
		else if (ae.getSource() == helpAbout)
			help_about();
		else if (ae.getSource() == buttonLine)
			setButtons(buttonLine, Tools.LINE);
		else if (ae.getSource() == buttonSquare)
			setButtons(buttonSquare, Tools.SQUARE);
		else if (ae.getSource() == buttonSquareFill)
			setButtons(buttonSquareFill, Tools.SQUAREFILL);
		else if (ae.getSource() == buttonEllipse)
			setButtons(buttonEllipse, Tools.ELLIPSE);
		else if (ae.getSource() == buttonEllipseFill)
			setButtons(buttonEllipseFill, Tools.ELLIPSEFILL);
		else if (ae.getSource() == buttonFill)
			setButtons(buttonFill, Tools.FILL);
		else if (ae.getSource() == lastMI[0])
			loadFileFromPath(lastMaps[0]);
		else if (ae.getSource() == lastMI[1])
			loadFileFromPath(lastMaps[1]);
		else if (ae.getSource() == lastMI[2])
			loadFileFromPath(lastMaps[2]);
		else if (ae.getSource() == showGrid)
		{
			gridOn = showGrid.getState();
			repaint();
		}
		else if (ae.getSource() == eLvlAttrs)
			cmdELvlAttrs();
		else if (ae.getSource() == flipHoriz)
			flipHoiz();
		else if (ae.getSource() == flipVert)
			flipVert();
		else if (ae.getSource() == rotate)
			rotateByAngle();
		else if (ae.getSource() == retile)
			retile();
		else if (ae.getSource() == applyEzTile)
			applyEzTile();
		else if (ae.getSource() == randomFill)
			randomFill();
		else if (ae.getSource() == generateScript)
			doGenerateScript();
		else if (ae.getSource() == editUndo)
			undo();
		else if (ae.getSource() == editRedo)
			redo();
		else if (ae.getSource() == altHotKey)
		{
			setHotKeys(altHotKey.getState());
		}

	}

	public void eLVL_confirm()
	{
		if (elvlIcon == null)
			elvlIcon = new ImageIcon(Main.rootDirectory + File.separator + "include" + File.separator
					+ "eLVLConfirm.png");

		Object[] options = { "Take me to the eLVL Editor",
				"No, I hate puppies", "No, and don't EVER ask me again" };
		int n = JOptionPane.showOptionDialog(null, "", "Save With eLVL Data?",
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				elvlIcon, options, options[0]);

		if (n == JOptionPane.YES_OPTION)
		{
			cmdELvlAttrs();
		}
		else if (n == JOptionPane.CANCEL_OPTION)
		{
			confirmELVL = false;
		}

	}

	public void help_tools()
	{
		File toolsHelpFile = new File(Main.rootDirectory + File.separator + "include" + File.separator
				+ "toolsHelp.png");
		if (toolsHelpIcon == null)
		{
			toolsHelpIcon = new ImageIcon(toolsHelpFile.getAbsolutePath());
		}

		JOptionPane.showMessageDialog(null, "", "Tools Help",
				JOptionPane.INFORMATION_MESSAGE, toolsHelpIcon);
	}

	private void help_manual()
	{
		ManualDialog.showHelp();
	}

	public void help_lvz()
	{
		JOptionPane
				.showMessageDialog(
						null,
						"Your lvz files will be in a folder called \""
								+ File.separator
								+ "(lvl name) files"
								+ File.separator
								+ "\" which will be in\n"
								+ "the directory of your lvl file. Be careful to copy these .lvz files when you want to upload \n"
								+ "to a server, and not cut, or the program will not load them next time you open the map.");
	}

	public void help_about()
	{
		JOptionPane.showMessageDialog(null, "Continuum Level / Lvz Tool "
				+ VERSION + " by Team 12!");
	}

	public void copy()
	{
		Editor e = (Editor) contentManager.getComponent(contentManager
				.getSelectedIndex());

		if (e.m_window.selection != null)
		{
			copied = new Vector();
			copied.addAll(e.m_window.selection);
			editPaste.setEnabled(true);
		}
		if (e.m_window.m_lvzImages.selectedMO.size() != 0)
		{
			copied_lvz = new HashMap();
			copied_lvz.putAll(e.m_window.m_lvzImages.selectedMO);
			editPaste.setEnabled(true);
		}
	}

	public void deleteSelection()
	{
		int index = contentManager.getSelectedIndex();

		if (index != -1)
		{
			Editor e = (Editor) contentManager.getComponent(index);
			if (e.m_window.currentTool == Tools.SELECT)
			{
				e.m_window.selection = null;
				e.m_window.width = 0; // erase selection
				e.m_window.height = 0;
				e.m_window.repaint();
				e.m_window.m_radar.repaintRadar();
			}
			else if (e.m_window.currentTool == Tools.LVZ_SELECTION)
			{
				Component c = contentManager.getSelectedComponent();
				if (c != null)
				{
					Editor ed = (Editor) c;
					ed.m_window.deletePressed();
				}
			}
		}

	}

	/**
	 * The cut button was pressed.
	 *
	 */
	public void cut()
	{
		copy();

		Editor e = (Editor) contentManager.getComponent(contentManager
				.getSelectedIndex());
		e.m_window.selection = null;
		e.m_window.m_lvzImages.selectedMO.clear();
		e.m_window.width = 0; // erase selection
		e.m_window.height = 0;
		e.m_window.repaint();
		e.m_window.m_radar.repaintRadar();
	}

	public void paste()
	{
		Editor e = (Editor) contentManager.getComponent(contentManager
				.getSelectedIndex());

		buttonSelect.doClick();

		if (copied_lvz != null)
		{
			Set k = copied_lvz.keySet();
			Iterator i = k.iterator();
			while (i.hasNext())
			{
				MapObject mo = (MapObject) i.next();
				Rectangle r = (Rectangle) copied_lvz.get(mo);
				MapObject n = mo.getCopy();
				n.x = 32;
				n.y = 32;

				Rectangle z = new Rectangle(32, 32, r.height, r.width);
				e.m_window.m_lvzImages.selectedMO.put(n, z);
			}
		}

		e.m_window.selection = new Vector();
		e.m_window.selection.addAll(copied);
		e.m_window.putSelectionOnScreen();
		e.m_window.repaint();
	}

	public void setStyle(int num)
	{
		style1.setSelected(false);
		style2.setSelected(false);
		style3.setSelected(false);
		style4.setSelected(false);
		style5.setSelected(false);
		style6.setSelected(false);

		try
		{
			if (num == 1)
			{
				style1.setSelected(true);
				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
				SwingUtilities.updateComponentTreeUI(this);
			}
			else if (num == 2)
			{
				style2.setSelected(true);
				UIManager.setLookAndFeel(UIManager
						.getCrossPlatformLookAndFeelClassName());
				SwingUtilities.updateComponentTreeUI(this);
			}
			else if (num == 3)
			{
				style3.setSelected(true);
				UIManager
						.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
				SwingUtilities.updateComponentTreeUI(this);
			}
			else if (num == 4)
			{
				style4.setSelected(true);
				UIManager.setLookAndFeel(new MetalLookAndFeel());

				SwingUtilities.updateComponentTreeUI(this);
			}
			else if (num == 5)
			{
				style5.setSelected(true);
				UIManager
						.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
				SwingUtilities.updateComponentTreeUI(this);
			}
			else if (num == 6)
			{
				style6.setSelected(true);
				UIManager
						.setLookAndFeel("com.sap.sce.front.plaf.SceLookAndFeel");
				SwingUtilities.updateComponentTreeUI(this);
			}
		}
		catch (Exception ea)
		{
			JOptionPane.showMessageDialog(null,
					"You do not have that style installed.\n" + ea.toString());
		}
	}

	public void windowClosing(WindowEvent e)
	{
		close();
	}

	private void saveProjectState(ProjectState ps)
	{ // saves preferences for next time
		XMLNode document = new XMLNode();
		XMLNode pref = document.addChild("Preferences");

		pref.addChild("loc_asssRegions_x", "" + ps.pos_AsssRegions.x);
		pref.addChild("loc_asssRegions_y", "" + ps.pos_AsssRegions.y);

		pref.addChild("loc_autoTile_x", "" + ps.pos_Autotool.x);
		pref.addChild("loc_autoTile_y", "" + ps.pos_Autotool.y);

		pref.addChild("loc_lvzImages_x", "" + ps.pos_LvzImages.x);
		pref.addChild("loc_lvzImages_y", "" + ps.pos_LvzImages.y);

		pref.addChild("loc_radar_x", "" + ps.pos_Radar.x);
		pref.addChild("loc_radar_y", "" + ps.pos_Radar.y);

		pref.addChild("loc_tileset_x", "" + ps.pos_Tileset.x);
		pref.addChild("loc_tileset_y", "" + ps.pos_Tileset.y);

		pref.addChild("show_radar", ps.showRadar ? "1" : "0");

		pref.addChild("lvzDrag", dragLvz.isSelected() ? "1" : "0");

		pref.addChild("lvzSelectSingle", singleSelectionLvz.isSelected() ? "1" : "0");

		pref.addChild("useAlt", ps.useAlt ? "1" : "0");

		if (isVisible())
		{
			pref.addChild("window_x", "" + getX());
			pref.addChild("window_y", "" + getY());
			pref.addChild("window_w", "" + getWidth());
			pref.addChild("window_h", "" + getHeight());
		}
		else
		{
			int inset = 50;
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			Rectangle bounds = new Rectangle(inset, inset, screenSize.width
					- inset * 2, screenSize.height - inset * 2);

			pref.addChild("window_x", "" + bounds.x);
			pref.addChild("window_y", "" + bounds.y);
			pref.addChild("window_w", "" + bounds.width);
			pref.addChild("window_h", "" + bounds.height);

		}

		pref.addChild("confirm_elvl", confirmELVL ? "1" : "0");

		if (lastMaps[0] != null)
		{
			pref.addChild("last_map_0", "" + lastMaps[0].replace(' ', '?'));
			if (lastMaps[1] != null)
			{
				pref.addChild("last_map_1", "" + lastMaps[1].replace(' ', '?'));
				if (lastMaps[2] != null)
				{
					pref.addChild("last_map_2", ""
							+ lastMaps[2].replace(' ', '?'));
				}
			}
		}

		XMLNode.saveDocument(document, Main.rootDirectory + File.separator + "include" + File.separator
				+ "preferences.xml");
	}

	private void close()
	{
		ProjectState ps = null;

		for (int x = 0; x < contentManager.getComponentCount(); ++x)
		{
			int s = 1;
			Editor e = (Editor) contentManager.getComponent(x);
			ps = e.m_window.getProjectState();

			if (e.m_window.modified)
			{
				String dialog = "Save changes to " + e.m_fileName + "?";
				String title = "Map Editor";
				String options[] = { "Yes", "No" };

				s = (int) JOptionPane.showOptionDialog(this, dialog, title,
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
						null, options, options[0]);
			}

			// 0 - yes, 1 - no, 2 - cancel, -1 - cancel
			switch (s)
			{
			case JOptionPane.YES_OPTION:
			{
				if (e.m_fileName.equals(untitledMapName))
					cmdSaveAs();
				else
					e.save();
			}
			}
		}

		// save preferences
		if (ps != null)
		{
			saveProjectState(ps);
		}

		System.exit(0);
	}

	public boolean isSingleLVZSelectionMode()
	{
		return singleSelectionLvz.isSelected();
	}

	public boolean isLVZdragEnabled()
	{
		return dragLvz.isSelected();
	}

	public void windowClosed(WindowEvent e)
	{
	}

	public void windowOpened(WindowEvent e)
	{
	}

	public void windowIconified(WindowEvent e)
	{
	}

	public void windowDeiconified(WindowEvent e)
	{
	}

	public void windowActivated(WindowEvent e)
	{
		update(getGraphics());
	}

	public void windowDeactivated(WindowEvent e)
	{
	}

	/**
	 * A button was selected on the toolbar, update everything that needs to
	 * know about it
	 *
	 * @param b
	 *            the button that was pressed
	 * @param button
	 *            the integer representing that button
	 */
	public void setButtons(JToggleButton b, int button)
	{
		buttonPen.setSelected(false);
		buttonSelect.setSelected(false);
		buttonEyedrop.setSelected(false);
		buttonAutoPen.setSelected(false);
		buttonLvz.setSelected(false);
		buttonLvzSelection.setSelected(false);
		buttonZoom.setSelected(false);
		buttonRgn.setSelected(false);
		buttonLine.setSelected(false);
		buttonSquare.setSelected(false);
		buttonSquareFill.setSelected(false);
		buttonEllipse.setSelected(false);
		buttonEllipseFill.setSelected(false);
		buttonFill.setSelected(false);

		b.setSelected(true);

		// Propogate the current tool.
		int ct = contentManager.getTabCount();
		for (int j = 0; j < ct; j++)
			((Editor) contentManager.getComponentAt(j)).m_window
					.setTool(button);
	}

	private void showWindow(int window)
	{

		Editor e = (Editor) contentManager.getSelectedComponent();

		if (window == RADAR_WINDOW)
			windowRadar.setEnabled(false);
		else if (window == TILESET_WINDOW)
			windowTileset.setEnabled(false);
		else if (window == AUTOTILE_WINDOW)
			windowAutoTile.setEnabled(false);
		else if (window == LVZ_WINDOW)
			windowLvz.setEnabled(false);
		else if (window == REGION_WINDOW)
			windowAsssRegions.setEnabled(false);

		e.m_window.showWindow(window);
	}

	private void cmdSaveAll()
	{
		for (int x = 0; x < contentManager.getComponentCount(); ++x)
		{
			Editor e = (Editor) contentManager.getComponent(x);

			if (e != null)
			{
				if (e.m_lvlFile.m_file != null)
					e.save();
				else
				{
					m_fileChooser.setMode(FileDialog.SAVE);
					m_fileChooser.setTitle("Save As...");
					m_fileChooser.setVisible(true);
					String filename = m_fileChooser.getFile();
					String dir = m_fileChooser.getDirectory();

					if (filename == null) // user pressed cancel
						return;

					if (filename.length() < 4
							|| !filename.substring(filename.length() - 4)
									.equals(".lvl"))
						filename += ".lvl";

					File file = new File(dir + filename);

					contentManager.remove(e);
					contentManager.add(e, filename, x);
					e.saveAs(file);
				}
			}
		}
	}

	public void cmdSave()
	{
		Editor e = (Editor) contentManager.getSelectedComponent();

		if (e != null)
		{
			if (e.m_lvlFile.m_file != null)
			{
				addToLatestMapsQueue(e.m_lvlFile.m_file);
				e.save();
			}
			else
			{
				if (confirmELVL
						&& e.m_lvlFile.eLvlAttrs.size() == LevelFile.DEFAULT_TAG_COUNT
						&& ((String) ((Vector) e.m_lvlFile.eLvlAttrs.get(0))
								.get(1)).equals("Unnamed"))
				{ // if the user hasn't modified their eLVL tags for their new
					// map
					eLVL_confirm();
				}

				m_fileChooser.setMode(FileDialog.SAVE);
				m_fileChooser.setTitle("Save As...");
				m_fileChooser.setVisible(true);
				String filename = m_fileChooser.getFile();
				String dir = m_fileChooser.getDirectory();

				if (filename != null)
				{

					if (filename.length() < 4
							|| !filename.substring(filename.length() - 4)
									.equals(".lvl"))
						filename += ".lvl";

					if (filename == null) // user pressed cancel
						return;

					File file = new File(dir + filename);

					int index = contentManager.getSelectedIndex();
					contentManager.remove(e);
					contentManager.add(e, filename, index);

					addToLatestMapsQueue(file);
					e.saveAs(file);
				}
			}
		}
	}

	private void cmdSaveAs()
	{
		Editor e = (Editor) contentManager.getSelectedComponent();

		if (e != null)
		{
			m_fileChooser.setMode(FileDialog.SAVE);
			m_fileChooser.setTitle("Save As...");
			m_fileChooser.setVisible(true);
			String filename = m_fileChooser.getFile();
			String dir = m_fileChooser.getDirectory();

			if (filename == null) // user pressed cancel
				return;

			String path = dir + filename;

			if (path.length() < 4
					|| !path.substring(path.length() - 4).equals(".lvl"))
				path += ".lvl";

			File file = new File(path);

			int index = contentManager.getSelectedIndex();
			contentManager.remove(e);
			contentManager.add(e, filename, index);

			addToLatestMapsQueue(file);
			e.saveAs(file);

			buttonPen.doClick();
		}
	}

	/**
	 * Creates a new lvl project in the tabbed pane
	 */
	private void cmdNewFile()
	{

		Editor e = new Editor(this);

		e.m_lvlFile.addDefaultELvLTags(); // add default eLVL tags and values

		contentManager.add(untitledMapName, e);
		contentManager.setSelectedComponent(e);
		buttonPen.doClick();
		viewZoomIn.setEnabled(true);
		viewZoomOut.setEnabled(true);

		fileSave.setEnabled(true);
		fileSaveAs.setEnabled(true);
		fileSaveAll.setEnabled(true);

		managerSave.setEnabled(true);
		managerSaveAll.setEnabled(true);
		fileClose.setEnabled(true);

		// Document open so enable File > Close

		ProjectState ps = e.m_window.getProjectState();

		windowRadar.setEnabled(!ps.m_radar);
		windowTileset.setEnabled(!ps.m_tileset);
		windowLvz.setEnabled(!ps.m_lvzImages);
		windowAutoTile.setEnabled(!ps.m_autotool);
		windowAsssRegions.setEnabled(!ps.m_asssRegions);

		editTileset.setEnabled(true);
		eLvlAttrs.setEnabled(true);
	}

	/**
	 * Open file was pressed or selected from the menu
	 *
	 */
	private void cmdOpenFile()
	{

		m_fileChooser.setMode(FileDialog.LOAD);
		m_fileChooser.setTitle("Open LVL");
		m_fileChooser.setVisible(true);
		String filename = m_fileChooser.getFile();
		String dir = m_fileChooser.getDirectory();

		if (filename == null) // user pressed cancel
			return;

		if (filename.length() < 4
				|| !filename.substring(filename.length() - 4).equals(".lvl"))
		{
			JOptionPane
					.showMessageDialog(null,
							"The only types of files that you can open are .lvl files.");
			return;
		}

		loadFileFromPath(dir + filename);
	}

	/**
	 * Add this file path to the lastest maps queue
	 *
	 * @param f
	 *            the file to add to the queue
	 */
	private void addToLatestMapsQueue(File f)
	{
		String path = f.getAbsolutePath();

		if (path.equals(lastMaps[1]))
		{
			lastMaps[1] = lastMaps[0];
			lastMI[1].setText(lastMI[0].getText());

			lastMaps[0] = path;
			lastMI[0].setText(f.getName());
		}
		else if (path.equals(lastMaps[2]))
		{
			lastMaps[2] = lastMaps[1];
			lastMI[2].setText(lastMI[1].getText());

			lastMaps[1] = lastMaps[0];
			lastMI[1].setText(lastMI[0].getText());

			lastMaps[0] = path;
			lastMI[0].setText(f.getName());
		}
		else if (!path.equals(lastMaps[0]))
		{
			for (int x = 2; x > 0; --x)
			{
				if (lastMI[x - 1] == null)
					continue;

				if (lastMI[x] == null)
				{
					lastMI[x] = new JMenuItem("tempName");
					addMenuItemToFile(lastMI[x]);
				}

				lastMaps[x] = lastMaps[x - 1];
				lastMI[x].setText(lastMI[x - 1].getText());
			}

			if (lastMI[0] == null)
			{
				lastMI[0] = new JMenuItem("tempName");
				addMenuItemToFile(lastMI[0]);
			}

			lastMaps[0] = path;
			lastMI[0].setText(f.getName());

		}
	}

	/**
	 * Load a lvl file from a path
	 *
	 * @param path
	 *            the path to load from
	 */
	private void loadFileFromPath(String path)
	{
		File file = new File(path);
		if (file.exists())
		{
			Cursor o = getCursor();

			try
			{
				// Change the cursor to WAIT

				Cursor c = new Cursor(Cursor.WAIT_CURSOR);
				setCursor(c);

				// Create a new tab for the lvl
				Editor e = new Editor(file, this);
				e.load();

				addToLatestMapsQueue(file);
				contentManager.add(file.getName(), e);
				contentManager.setSelectedComponent(e);
				buttonPen.doClick();
				viewZoomIn.setEnabled(true);
				viewZoomOut.setEnabled(true);

				// Document open so enable File > Close
				fileSave.setEnabled(true);
				fileSaveAs.setEnabled(true);
				fileSaveAll.setEnabled(true);

				managerSave.setEnabled(true);
				managerSaveAll.setEnabled(true);
				fileClose.setEnabled(true);
				editTileset.setEnabled(true);
				eLvlAttrs.setEnabled(true);

				ProjectState ps = e.m_window.getProjectState();

				windowRadar.setEnabled(!ps.m_radar);
				windowTileset.setEnabled(!ps.m_tileset);
				windowLvz.setEnabled(!ps.m_lvzImages);
				windowAutoTile.setEnabled(!ps.m_autotool);
				windowAsssRegions.setEnabled(!ps.m_asssRegions);
			}
			catch (Exception e)
			{
			}

			setCursor(o);
		}
		else
		{
			JOptionPane.showMessageDialog(null, "That file does not exist: "
					+ file.getAbsolutePath());
		}
	}

	/**
	 * Closes the current working lvl file, not available if no files are opened
	 */
	private void cmdCloseFile()
	{
		int s = 1;

		// Get our current working level
		Editor e = (Editor) contentManager.getSelectedComponent();
		if (e.m_window.modified)
		{
			String dialog = "Save changes to " + e.m_fileName + "?";
			String title = "Map Editor";
			String options[] = { "Yes", "No", "Cancel" };

			s = (int) JOptionPane.showOptionDialog(this, dialog, title,
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		}

		// 0 - yes, 1 - no, 2 - cancel, -1 - cancel
		switch (s)
		{
		case 0:
			e.save();
		case 1:
			e.hideWindows();
			contentManager.remove(e);
			if (contentManager.getTabCount() == 0)
			{
				fileClose.setEnabled(false);
				fileSave.setEnabled(false);
				fileSaveAs.setEnabled(false);
				fileSaveAll.setEnabled(false);

				managerSave.setEnabled(false);
				managerSaveAll.setEnabled(false);

				windowRadar.setEnabled(false);
				windowTileset.setEnabled(false);
				editTileset.setEnabled(false);
				eLvlAttrs.setEnabled(false);
				windowLvz.setEnabled(false);
				windowAutoTile.setEnabled(false);
				windowAsssRegions.setEnabled(false);
			}
		break;
		}
	}

	private void cmdELvlAttrs()
	{
		Editor e = (Editor) contentManager.getSelectedComponent();
		ELvlAttrEditor.editAttrs(e.m_lvlFile.eLvlAttrs, this);
	}

	private void cmdEditTileset()
	{
		Editor e = (Editor) contentManager.getSelectedComponent();
		e.m_tileset = TilesetDialog.getTileset(this, e.m_tileset);

		// Image[] tiles = new Image[190];
		for (int x = 0; x < 19; ++x)
		{
			for (int y = 0; y < 10; ++y)
			{
				e.m_tiles[x + 19 * y] = this.createImage(16, 16);
				Graphics g = e.m_tiles[x + 19 * y].getGraphics();

				g.drawImage(e.m_tileset, 0, 0, 16, 16, 16 * x, 16 * y,
						16 * x + 16, 16 * y + 16, null);
			}
		}

		e.repaint();
	}

	private void cmdZoomIn()
	{
		Editor e = (Editor) contentManager.getSelectedComponent();

		if (e != null)
		{
			LevelWindow lw = e.m_window;

			int startx = lw.getStartX();
			int starty = lw.getStartY();
			int endx = lw.getEndX();
			int endy = lw.getEndY();

			lw.centerZoomAboutTile((startx + endx) / 2, (starty + endy) / 2);

			viewZoomIn.setEnabled(e.m_window.zoomIn());
			viewZoomOut.setEnabled(true);
		}
	}

	private void cmdZoomOut()
	{
		Editor e = (Editor) contentManager.getSelectedComponent();

		if (e != null)
		{
			LevelWindow lw = e.m_window;

			int startx = lw.getStartX();
			int starty = lw.getStartY();
			int endx = lw.getEndX();
			int endy = lw.getEndY();

			lw.centerZoomAboutTile((startx + endx) / 2, (starty + endy) / 2);

			viewZoomOut.setEnabled(e.m_window.zoomOut());
			viewZoomIn.setEnabled(true);
		}
	}

	public int getSelectedTool()
	{
		if (buttonPen.isSelected())
			return Tools.PEN;
		else if (buttonSelect.isSelected())
			return Tools.SELECT;
		else if (buttonEyedrop.isSelected())
			return Tools.EYEDROP;
		else if (buttonAutoPen.isSelected())
			return Tools.AUTO_PEN;
		else if (buttonLvz.isSelected())
			return Tools.LVZ;
		else if (buttonLvzSelection.isSelected())
			return Tools.LVZ_SELECTION;
		else if (buttonZoom.isSelected())
			return Tools.ZOOM;

		return -1;
	}

	public void addComponent(Container c)
	{
		getLayeredPane().add(c);
	}

	public void stateChanged(ChangeEvent ce)
	{
		int ct = contentManager.getTabCount();
		for (int j = 0; j < ct; j++)
			if (j != contentManager.getSelectedIndex())
			{

			}
			else
			{
				Editor e = (Editor) contentManager.getComponentAt(j);

				ProjectState p = e.m_window.getProjectState();
				windowRadar.setEnabled(!p.m_radar);
				windowTileset.setEnabled(!p.m_tileset);
				windowLvz.setEnabled(!p.m_lvzImages);
				editTileset.setEnabled(true);
				eLvlAttrs.setEnabled(true);
			}
	}

	// map a tileId to an image
	public static Image TileIdToImage(int tile)
	{
		if (tile == 191)
			return newTiles[0];
		else if ((tile >= 192 && tile <= 215) || (tile >= 221 && tile <= 240)
				|| (tile >= 243 && tile <= 251))
		{
			return newTiles[1];
		}
		else if (tile == 241)
		{
			return newTiles[2];
		}
		else if (tile == 242)
		{
			return newTiles[3];
		}
		else if (tile == 252)
		{
			return newTiles[4];
		}
		else if (tile == 253)
		{
			return newTiles[5];
		}
		else if (tile == 254)
		{
			return newTiles[6];
		}
		else if (tile == 255)
		{
			return newTiles[7];
		}

		return null;
	}

	/**
	 * Starting point.
	 */
	public static void main(String args[])
	{

		// Set default look and feel to the system it is running on
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}

		// Create our editor
		Main m = new Main();

		if (args.length > 0)
		{
			m.loadFileFromPath(args[0]);
		}
	}

	class ListDialog extends JDialog implements ActionListener
	{
		private JTextArea textArea = null;

		public ListDialog(JFrame parent)
		{
			super(parent,true);

			setTitle("Result");

			textArea = new JTextArea(10, 20);
			JScrollPane scrollPane = new JScrollPane(textArea,
			                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
			                    JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			textArea.setEditable(false);

			setLayout(new BorderLayout());

			add(scrollPane,BorderLayout.CENTER);

			JButton ok = new JButton("Close");
			ok.addActionListener(this);
			add(ok,BorderLayout.SOUTH);

			pack();
		}

		public void setText(String s)
		{
			textArea.setText(s);
		}

		public void actionPerformed(ActionEvent e)
		{
			setVisible(false);
		}
	}
}