// Stan Bak
// Image dialog class
// 5-8-04

package editor.imageeditor;

import editor.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class ImageDialog extends JDialog
    implements MouseListener, MouseMotionListener, ActionListener {
  final int WIDTH = 600;

  final int HEIGHT = 650;

  public DrawPanel dp;

  public JScrollPane scrollPane = null;

  private JButton ok;

  private JButton cancel;

  final FileDialog fileDialog = new FileDialog(new Frame());

  Image rv = null;

  Dimension initialDimensions;

  final int ZOOM = 0;

  final int SELECTION = 1;

  final int TEXT = 2;

  final int DROPPER = 3;

  final int FILL = 4;

  final int LINE = 5;

  final int RECTANGLE = 6;

  final int OVAL = 7;

  final int ERASER = 8;

  final int PENCIL = 9;

  private Color[] colors =
      new Color[] {
        Color.white,
        Color.black,
        Color.lightGray,
        Color.gray,
        Color.darkGray,
        Color.pink,
        Color.red,
        new Color(255, 123, 0), // orange
        Color.yellow,
        Color.green,
        Color.blue,
        Color.cyan,
        Color.magenta,
        new Color(100, 25, 0) // brown
      };

  private int numColors = 14;

  public Color foregroundColor = Color.white;

  public Color backgroundColor = Color.black;

  private Image[] tools =
      new Image[] {
        new ImageIcon(
                Main.rootDirectory
                    + File.separator
                    + "include"
                    + File.separator
                    + "Images"
                    + File.separator
                    + "magnifyingglass.gif")
            .getImage(),
        new ImageIcon(
                Main.rootDirectory
                    + File.separator
                    + "include"
                    + File.separator
                    + "Images"
                    + File.separator
                    + "selection.gif")
            .getImage(),
        new ImageIcon(
                Main.rootDirectory
                    + File.separator
                    + "include"
                    + File.separator
                    + "Images"
                    + File.separator
                    + "letters.gif")
            .getImage(),
        new ImageIcon(
                Main.rootDirectory
                    + File.separator
                    + "include"
                    + File.separator
                    + "Images"
                    + File.separator
                    + "dropper.gif")
            .getImage(),
        new ImageIcon(
                Main.rootDirectory
                    + File.separator
                    + "include"
                    + File.separator
                    + "Images"
                    + File.separator
                    + "fill.gif")
            .getImage(),
        new ImageIcon(
                Main.rootDirectory
                    + File.separator
                    + "include"
                    + File.separator
                    + "Images"
                    + File.separator
                    + "line.gif")
            .getImage(),
        new ImageIcon(
                Main.rootDirectory
                    + File.separator
                    + "include"
                    + File.separator
                    + "Images"
                    + File.separator
                    + "rectangle.gif")
            .getImage(),
        new ImageIcon(
                Main.rootDirectory
                    + File.separator
                    + "include"
                    + File.separator
                    + "Images"
                    + File.separator
                    + "oval.gif")
            .getImage(),
        new ImageIcon(
                Main.rootDirectory
                    + File.separator
                    + "include"
                    + File.separator
                    + "Images"
                    + File.separator
                    + "eraser.gif")
            .getImage(),
        new ImageIcon(
                Main.rootDirectory
                    + File.separator
                    + "include"
                    + File.separator
                    + "Images"
                    + File.separator
                    + "pencil.gif")
            .getImage(),
      };

  private int numTools = 10;

  public int selectedTool = 9;

  private Image arrowSide =
      new ImageIcon(
              Main.rootDirectory
                  + File.separator
                  + "include"
                  + File.separator
                  + "Images"
                  + File.separator
                  + "arrowside.gif")
          .getImage();

  private JMenuBar menuBar;

  private JMenu file, edit, view;
  private JMenuItem importFromImage,
      revert,
      copy,
      paste,
      undo,
      redo,
      cut,
      fontEditor,
      imageSize,
      fitImage;

  private JLabel thicknessLabel;

  public JSpinner thicknessSpinner;

  public JCheckBox fillShape;

  public JLabel imageSizeLabel;

  private JLabel positionLabel;

  FontChooser fc;

  // parent is the parent frame, curImage is the current image or null if this
  // is a new image
  // the width and height are only used if the curImage is null
  public ImageDialog(
      Frame parent, Image curImage, int newImageWidth, int newImageHeight, boolean lockDimesnions) {
    super(parent, true);
    setLocation(
        parent.getX() + (parent.getWidth() - WIDTH) / 2,
        parent.getY() + (parent.getHeight() - HEIGHT) / 2);

    init(curImage, newImageWidth, newImageHeight, lockDimesnions);
  }

  // parent is the parent dialog, curImage is the current image or null if
  // this is a new image
  // the width and height are only used if the curImage is null
  public ImageDialog(
      JDialog parent,
      Image curImage,
      int newImageWidth,
      int newImageHeight,
      boolean lockDimesnions) {
    super(parent, true);
    setLocation(
        parent.getX() + (parent.getWidth() - WIDTH) / 2,
        parent.getY() + (parent.getHeight() - HEIGHT) / 2);

    init(curImage, newImageWidth, newImageHeight, lockDimesnions);
  }

  public void init(Image curImage, int newImageWidth, int newImageHeight, boolean lockDimesnions) {
    Font smallFont = new Font("my small", Font.PLAIN, 10);

    setSize(WIDTH, HEIGHT);
    setTitle("Image Editor v6-1");
    addMouseListener(this);

    getContentPane().setLayout(null);

    createMenu();

    if (lockDimesnions) {
      imageSize.setEnabled(false);
      fitImage.setEnabled(false);
    }

    if (curImage != null) {
      initialDimensions = new Dimension(curImage.getWidth(null), curImage.getHeight(null));
    } else {
      initialDimensions = new Dimension(newImageWidth, newImageHeight);
    }

    setJMenuBar(menuBar);

    scrollPane = new JScrollPane();

    dp =
        new DrawPanel(
            this, curImage, newImageWidth, newImageHeight, WIDTH - 60, HEIGHT - 60, tools);
    dp.addMouseMotionListener(this);
    dp.addMouseListener(this);

    scrollPane.setViewportView(dp);
    scrollPane.setBounds(65, 10, 512 + 3, 512 + 3);
    getContentPane().add(scrollPane);

    thicknessLabel = new JLabel("Thickness");
    thicknessLabel.setFont(smallFont);
    thicknessLabel.setBounds(10, 375, 50, 20);
    getContentPane().add(thicknessLabel);

    thicknessSpinner =
        new javax.swing.JSpinner(
            new javax.swing.SpinnerNumberModel(
                new Integer(1),
                new Integer(1),
                new Integer(512),
                new Integer(1))); // init,min,max,step
    thicknessSpinner.setBounds(10, 395, 50, 20);
    getContentPane().add(thicknessSpinner);

    ok = new JButton("Use this Image");
    ok.setBounds(50, HEIGHT - 90, 150, 25);
    ok.addActionListener(this);
    getContentPane().add(ok);
    ok.setFocusable(false);

    cancel = new JButton("Cancel");
    cancel.setBounds(WIDTH - 200, HEIGHT - 90, 150, 25);
    cancel.addActionListener(this);
    getContentPane().add(cancel);
    cancel.setFocusable(false);

    fillShape = new JCheckBox("Fill");
    fillShape.setFont(smallFont);
    fillShape.setBounds(10, 420, 50, 20);
    fillShape.setVisible(false);
    getContentPane().add(fillShape);

    // 200, -> WIDTH - 200

    imageSizeLabel = new JLabel("Image Size:", SwingConstants.CENTER);
    imageSizeLabel.setBounds(200, HEIGHT - 100, WIDTH - 400, 20);
    getContentPane().add(imageSizeLabel);

    positionLabel = new JLabel("(50, 200)", SwingConstants.CENTER);
    positionLabel.setBounds(200, HEIGHT - 80, WIDTH - 400, 20);
    getContentPane().add(positionLabel);

    fc = new FontChooser(this, null, dp);
    // fc.setBounds(50, 50, 425, 330);
    // getContentPane().add(fc);
    imageSizeLabel.setText(
        "Image Size: " + initialDimensions.width + " x " + initialDimensions.height);
  }

  public void createMenu() {
    menuBar = new JMenuBar();

    file = new JMenu("Image Options");
    file.setMnemonic(KeyEvent.VK_I);
    menuBar.add(file);

    edit = new JMenu("Edit");
    edit.setMnemonic(KeyEvent.VK_E);
    menuBar.add(edit);

    view = new JMenu("View");
    view.setMnemonic(KeyEvent.VK_V);
    menuBar.add(view);

    importFromImage = new JMenuItem("Import Image from File", KeyEvent.VK_I);
    importFromImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
    importFromImage.addActionListener(this);
    file.add(importFromImage);

    file.addSeparator();

    revert = new JMenuItem("Revert Changes", KeyEvent.VK_R);
    revert.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
    revert.addActionListener(this);
    file.add(revert);

    undo = new JMenuItem("Undo", KeyEvent.VK_U);
    undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
    undo.addActionListener(this);
    edit.add(undo);

    redo = new JMenuItem("Redo", KeyEvent.VK_R);
    redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
    redo.addActionListener(this);
    edit.add(redo);

    edit.addSeparator();

    cut = new JMenuItem("Cut Selection", KeyEvent.VK_U);
    cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
    cut.addActionListener(this);
    edit.add(cut);

    copy = new JMenuItem("Copy Selection", KeyEvent.VK_C);
    copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
    copy.addActionListener(this);
    edit.add(copy);

    paste = new JMenuItem("Paste Selection", KeyEvent.VK_P);
    paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
    paste.addActionListener(this);
    edit.add(paste);

    edit.addSeparator();

    imageSize = new JMenuItem("Change Image Size", KeyEvent.VK_S);
    imageSize.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
    imageSize.addActionListener(this);
    edit.add(imageSize);

    fitImage = new JMenuItem("Trim Black Border", KeyEvent.VK_T);
    fitImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
    fitImage.addActionListener(this);
    edit.add(fitImage);

    fontEditor = new JMenuItem("Font Editor", KeyEvent.VK_F);
    fontEditor.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));
    view.add(fontEditor);
    fontEditor.addActionListener(this);
  }

  public void paint(Graphics g) {
    super.paint(g);

    drawColors(g);
    drawSelectedColors(g);
    drawTools(g);
  }

  public void drawSelectedColors(Graphics g1) {
    Graphics2D g = (Graphics2D) g1;
    Rectangle backgroundRect = new Rectangle(15, HEIGHT - 120, 30, 25);
    Rectangle foregroundRect = new Rectangle(30, HEIGHT - 110, 30, 25);

    Rectangle borderRect =
        new Rectangle(
            backgroundRect.x - 5,
            backgroundRect.y - 5,
            foregroundRect.x + foregroundRect.width - backgroundRect.x + 10,
            foregroundRect.y + foregroundRect.height - backgroundRect.y + 10);

    g.setColor(backgroundColor);
    g.fill(backgroundRect);
    g.setColor(Color.black);
    g.draw(backgroundRect);

    g.setColor(foregroundColor);
    g.fill(foregroundRect);
    g.setColor(Color.black);
    g.draw(foregroundRect);

    g.draw(borderRect);
  }

  public void drawTools(Graphics g) {
    int x = 35;
    int y = 115;
    int distanceBetween = 30;
    g.setColor(Color.black);
    for (int i = 0; i < numTools; ++i) {
      g.drawImage(tools[i], x, y + distanceBetween * i, null);
      g.drawRect(x - 1, y + distanceBetween * i - 1, 25, 25);
    }

    g.drawImage(arrowSide, x - 30, y + distanceBetween * selectedTool, null);
  }

  public void drawColors(Graphics g) {
    int x = 65;
    int y = 585;

    int width = 30;
    int height = 20;

    int seperation = 5;

    for (int i = 0; i < numColors; ++i) {
      g.setColor(colors[i]);
      g.fillRect(x + (width + seperation) * i, y, width, height);

      g.setColor(Color.black); // draw the black border
      g.drawRect(x + (width + seperation) * i, y, width, height);
    }
  }

  private void checkTools(Point p) {
    int x = 35;
    int y = 115;
    int distanceBetween = 30;

    for (int i = 0; i < numTools; ++i) {
      Rectangle r = new Rectangle(x - 1, y + distanceBetween * i - 1, 25, 25);

      if (r.contains(p)) {
        if (i == PENCIL || i == RECTANGLE || i == OVAL || i == LINE || i == ERASER)
          showThicknessGUI();
        else hideThicknessGUI();

        if (i == TEXT) fc.setVisible(true);
        else fc.setVisible(false);

        if (i == RECTANGLE || i == OVAL) showFillGUI();
        else hideFillGUI();

        dp.toolSelected(i);
        selectedTool = i;
        repaint();

        return;
      }
    }
  }

  public void showFillGUI() {
    fillShape.show();
  }

  public void hideFillGUI() {
    fillShape.hide();
  }

  public void showThicknessGUI() {
    thicknessLabel.show();
    thicknessSpinner.show();
  }

  public void hideThicknessGUI() {
    thicknessLabel.hide();
    thicknessSpinner.hide();
  }

  private void checkColorsDoubleClicked(Point p, boolean leftClicked) {
    int x = 65;
    int y = 585;

    int width = 30;
    int height = 20;

    int seperation = 5;

    for (int i = 0; i < numColors; ++i) {
      Rectangle r = new Rectangle(x + (width + seperation) * i, y, width, height);

      if (r.contains(p)) {
        Color newColor = JColorChooser.showDialog(this, "Choose Color", colors[i]);

        if (newColor != null) {
          colors[i] = newColor;

          if (leftClicked) {
            dp.colorChanged(newColor, true);
            foregroundColor = newColor;
          } else {
            dp.colorChanged(newColor, false);
            backgroundColor = newColor;
          }
        }

        repaint();

        return;
      }
    }
  }

  private void checkColorsSingleClicked(Point p, boolean leftClick) {
    int x = 65;
    int y = 585;

    int width = 30;
    int height = 20;

    int seperation = 5;

    for (int i = 0; i < numColors; ++i) {
      Rectangle r = new Rectangle(x + (width + seperation) * i, y, width, height);

      if (r.contains(p)) {
        if (leftClick) {
          dp.colorChanged(colors[i], true);
          foregroundColor = colors[i];
        } else {
          dp.colorChanged(colors[i], false);
          backgroundColor = colors[i];
        }

        repaint();

        return;
      }
    }
  }

  // parent is the parent frame, curImage is the current image
  // if curimage is null a 512x512 image is created
  // dimensions are not locked
  public static Image getImage(Frame par, Image curI) {
    int iWidth = 512;
    int iHeight = 512;

    ImageDialog id = new ImageDialog(par, curI, iWidth, iHeight, false);
    id.show();
    return id.rv;
  }

  // parent is the parent jdialog, curImage is the current image
  // if curimage is null a 512x512 image is created
  // dimensions are not locked
  public static Image getImage(JDialog par, Image curI) {
    int iWidth = 512;
    int iHeight = 512;

    ImageDialog id = new ImageDialog(par, curI, iWidth, iHeight, false);
    id.show();
    return id.rv;
  }

  // parent is the parent frame, curImage is the current image
  // if curimage is null a 512x512 image is created
  // lockDimensions is whehter or not the dimensions should be locked
  public static Image getImage(Frame par, Image curI, boolean lockDimensions) {
    int iWidth = 512;
    int iHeight = 512;

    ImageDialog id = new ImageDialog(par, curI, iWidth, iHeight, lockDimensions);
    id.show();
    return id.rv;
  }

  // parent is the parent frame, curImage is the current image
  // if curimage is null a 512x512 image is created
  // lockDimensions is whehter or not the dimensions should be locked
  public static Image getImage(JDialog par, Image curI, boolean lockDimensions) {
    int iWidth = 512;
    int iHeight = 512;

    ImageDialog id = new ImageDialog(par, curI, iWidth, iHeight, lockDimensions);
    id.show();
    return id.rv;
  }

  // parent is the parent frame, iWidth and iHeight at the width and height to
  // create the new image
  // dimensions are not locked
  public static Image getImage(Frame par, int iWidth, int iHeight) {
    ImageDialog id = new ImageDialog(par, null, iWidth, iHeight, false);
    id.show();
    return id.rv;
  }

  // parent is the parent jdialog, iWidth and iHeight at the width and height
  // to create the new image
  // dimensions are not locked
  public static Image getImage(JDialog par, int iWidth, int iHeight) {
    ImageDialog id = new ImageDialog(par, null, iWidth, iHeight, false);
    id.show();
    return id.rv;
  }

  // parent is the parent frame, iWidth and iHeight at the width and height to
  // creast the new image
  // lockDimensions is whehter or not the dimensions should be locked
  public static Image getImage(Frame par, int iWidth, int iHeight, boolean lockDimensions) {
    ImageDialog id = new ImageDialog(par, null, iWidth, iHeight, lockDimensions);
    id.show();
    return id.rv;
  }

  // parent is the parent jdialog, iWidth and iHeight at the width and height
  // to creast the new image
  // lockDimensions is whehter or not the dimensions should be locked
  public static Image getImage(JDialog par, int iWidth, int iHeight, boolean lockDimensions) {
    ImageDialog id = new ImageDialog(par, null, iWidth, iHeight, lockDimensions);
    id.show();
    return id.rv;
  }

  // Event handling

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == fontEditor) {
      fc.setSize(425, 260);
      fc.setLocation(getX() + (getWidth()), getY() + (getHeight() - fc.getHeight()) / 2);
      fc.show();
    } else if (e.getSource() == importFromImage) {
      fileDialog.setMode(FileDialog.LOAD);
      fileDialog.setTitle("Import Image From File");
      fileDialog.show();
      String file = fileDialog.getFile();
      String dir = fileDialog.getDirectory();

      if (file == null) // user pressed cancel
      return;

      ImageIcon i = new ImageIcon(dir + file);
      dp.setSelection(i.getImage());
    } else if (e.getSource() == copy) {
      dp.copy();
    } else if (e.getSource() == paste) {
      dp.paste();
    } else if (e.getSource() == cut) {
      dp.cut();
    } else if (e.getSource() == revert) {
      dp.makeImage(dp.initialImage);
      dp.flushUndo();
      dp.setDimensions(initialDimensions);
    } else if (e.getSource() == cancel) {
      hide();
    } else if (e.getSource() == ok) {
      rv = dp.getImage();
      hide();
    } else if (e.getSource() == undo) {
      dp.undo();
    } else if (e.getSource() == redo) {
      dp.redo();
    } else if (e.getSource() == imageSize) {
      Dimension d = DimensionSetter.getNewDimensions(this, new Dimension(dp.getDimensions()));
      if (d != null) dp.setDimensions(d);
    } else if (e.getSource() == fitImage) {
      dp.fitImage();
    }
  }

  public void mouseReleased(MouseEvent e) {
    if (e.getSource() == dp) dp.mouseReleased(e.getPoint());
  }

  public void mousePressed(MouseEvent e) {
    if (e.getSource() == dp) dp.mousePressed(e.getPoint(), e.getButton() == MouseEvent.BUTTON1);
    else {
      if (e.getClickCount() == 2) { // double click
        checkColorsDoubleClicked(e.getPoint(), e.getButton() == MouseEvent.BUTTON1);
      } else {
        checkTools(e.getPoint());
        checkColorsSingleClicked(e.getPoint(), e.getButton() == MouseEvent.BUTTON1);
      }
    }
  }

  public void mouseExited(MouseEvent e) {
    if (e.getSource() == dp) {
      dp.mouseExited();
      positionLabel.setText("");
    }
  }

  public void mouseEntered(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {}

  public void mouseMoved(MouseEvent e) {
    if (dp.getDimensions() != null && e.getSource() == dp) {
      dp.mouseMoved(e.getPoint());
      Point p = dp.getImagePointFromPanelPoint(e.getPoint().x, e.getPoint().y);
      p.x = p.x - (512 - dp.getDimensions().width) / 2;
      p.y = p.y - (512 - dp.getDimensions().height) / 2;
      positionLabel.setText("(" + p.x + ", " + p.y + ")");
    }
  }

  public void mouseDragged(MouseEvent e) {
    dp.mouseDragged(e.getPoint());
    Point p = dp.getImagePointFromPanelPoint(e.getPoint().x, e.getPoint().y);
    p.x = p.x - (512 - dp.getDimensions().width) / 2;
    p.y = p.y - (512 - dp.getDimensions().height) / 2;
    positionLabel.setText("(" + p.x + ", " + p.y + ")");
  }
}
