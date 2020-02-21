// Stan Bak
// 5-30-04
// TilesetDialog.java

package editor.imageeditor;

import editor.Main;
import editor.loaders.BitMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public class TilesetDialog extends JDialog implements ActionListener, ItemListener, MouseListener {
  DBCanvas tilesetCanvas, tilePreviewCanvas;
  JPanel tilesetBorder, tilePreviewBorder;
  JButton importTileset, editTile, ok, cancel;

  JCheckBox showTypes;

  JLabel typeLabel[] = new JLabel[10];

  private int selectedTile = 0;

  JFrame parent = null;

  Image filter =
      new ImageIcon(
              Main.rootDirectory
                  + File.separator
                  + "include"
                  + File.separator
                  + "Images"
                  + File.separator
                  + "filter.gif")
          .getImage();

  final FileDialog fileDialog = new FileDialog(new Frame());

  ImageIcon icons[] =
      new ImageIcon[] {
        new ImageIcon(
            Main.rootDirectory
                + File.separator
                + "include"
                + File.separator
                + "Images"
                + File.separator
                + "filterBlue.gif"),
        new ImageIcon(
            Main.rootDirectory
                + File.separator
                + "include"
                + File.separator
                + "Images"
                + File.separator
                + "filterBrown.gif"),
        new ImageIcon(
            Main.rootDirectory
                + File.separator
                + "include"
                + File.separator
                + "Images"
                + File.separator
                + "filterGray.gif"),
        new ImageIcon(
            Main.rootDirectory
                + File.separator
                + "include"
                + File.separator
                + "Images"
                + File.separator
                + "filterGreen.gif"),
        new ImageIcon(
            Main.rootDirectory
                + File.separator
                + "include"
                + File.separator
                + "Images"
                + File.separator
                + "filterOrange.gif"),
        new ImageIcon(
            Main.rootDirectory
                + File.separator
                + "include"
                + File.separator
                + "Images"
                + File.separator
                + "filterPurple.gif"),
        new ImageIcon(
            Main.rootDirectory
                + File.separator
                + "include"
                + File.separator
                + "Images"
                + File.separator
                + "filterRed.gif"),
        new ImageIcon(
            Main.rootDirectory
                + File.separator
                + "include"
                + File.separator
                + "Images"
                + File.separator
                + "filterTeal.gif"),
        new ImageIcon(
            Main.rootDirectory
                + File.separator
                + "include"
                + File.separator
                + "Images"
                + File.separator
                + "filterYellow.gif")
      };

  String types[] =
      new String[] {
        new String("Border Tile"),
        new String("Soccer Goal"),
        new String("Fly Over"),
        new String("Fly Under (opaque)"),
        new String("Fly Under (transparent)"),
        new String("Safe Zone"),
        new String("Flag"),
        new String("Horizontal Door"),
        new String("Vertical Door")
      };

  Image rv = null; // return value

  public TilesetDialog(JFrame parent) {
    super(parent, true);
    this.parent = parent;

    setTitle("Tileset Editor v5-30");
    setSize(675, 330);
    setLocation(
        parent.getX() + (parent.getWidth() - getWidth()) / 2,
        parent.getY() + (parent.getHeight() - getHeight()) / 2);

    getContentPane().setLayout(null);

    tilesetBorder = new JPanel();
    tilesetBorder.setBounds(25 - 1, 25 - 1, 304 + 2, 160 + 2);
    tilesetBorder.setBorder(new javax.swing.border.EtchedBorder());
    tilesetBorder.setFocusable(false);
    getContentPane().add(tilesetBorder);

    tilesetCanvas = new DBCanvas();
    tilesetCanvas.addMouseListener(this);
    tilesetCanvas.setBounds(25, 25, 304, 160);
    getContentPane().add(tilesetCanvas);

    tilePreviewBorder = new JPanel();
    tilePreviewBorder.setBounds(450 - 1, 25 - 1, 100 + 2, 100 + 2);
    tilePreviewBorder.setBorder(new javax.swing.border.EtchedBorder());
    tilePreviewBorder.setFocusable(false);
    getContentPane().add(tilePreviewBorder);

    tilePreviewCanvas = new DBCanvas();
    tilePreviewCanvas.setBounds(450, 25, 100, 100);
    getContentPane().add(tilePreviewCanvas);

    editTile = new JButton("Edit Tile");
    editTile.addActionListener(this);
    editTile.setBounds(460, 140, 80, 20);
    getContentPane().add(editTile);

    importTileset = new JButton("Import Tileset");
    importTileset.addActionListener(this);
    importTileset.setBounds(102, 200, 150, 25);
    getContentPane().add(importTileset);

    ok = new JButton("Ok");
    ok.addActionListener(this);
    ok.setBounds(27, 250, 100, 25);
    getContentPane().add(ok);

    cancel = new JButton("Cancel");
    cancel.addActionListener(this);
    cancel.setBounds(227, 250, 100, 25);
    getContentPane().add(cancel);

    showTypes = new JCheckBox("Show Tile Types");
    showTypes.addItemListener(this);
    showTypes.setBounds(425, 170, 150, 20);
    getContentPane().add(showTypes);

    makeJLabels(350, 190);

    tilesetCanvas.selectedTile = selectedTile;
    showTypes.setSelected(true);
  }

  private void makeJLabels(int x, int y) {
    for (int c = 0; c < 5; ++c) {
      typeLabel[c] = new JLabel(types[c], icons[c], SwingConstants.LEFT);
      typeLabel[c].setBounds(x, y + 20 * c, 150, 20);
      getContentPane().add(typeLabel[c]);
    }

    for (int c = 5; c < 9; ++c) {
      typeLabel[c] = new JLabel(types[c], icons[c], SwingConstants.LEFT);
      typeLabel[c].setBounds(x + 150, y + 20 * (c - 5), 150, 20);
      getContentPane().add(typeLabel[c]);
    }
  }

  public void show(Image tileset) {
    tilesetCanvas.bottomImage = copyImage(tileset);
    tilePreviewCanvas.bottomImage = copyImage(tileset, 0, 0, 16, 16);

    super.show();
  }

  public Image copyImage(Image i) {
    if (i == null) return null;

    Image newI = parent.createImage(i.getWidth(null), i.getHeight(null));

    Graphics g = newI.getGraphics();
    g.drawImage(i, 0, 0, null);

    return newI;
  }

  public Image copyImage(Image i, int x1, int y1, int x2, int y2) {
    if (i == null) return null;

    int w = x2 - x1;
    int h = y2 - y1;

    Image newI = parent.createImage(w, h);

    Graphics g = newI.getGraphics();
    g.drawImage(
        i, 0, 0, w, h, // destination
        x1, y1, x2, y2, // source
        null);

    return newI;
  }

  public void editSelectedTile() {
    Image i = tilePreviewCanvas.bottomImage;

    Image newImage = ImageDialog.getImage(this, i, true);

    if (newImage == null) return;

    tilePreviewCanvas.bottomImage = newImage;
    tilePreviewCanvas.repaint();

    Graphics g = tilesetCanvas.bottomImage.getGraphics();

    int x = (selectedTile % 19) * 16;
    int y = (selectedTile / 19) * 16;

    g.drawImage(newImage, x, y, null);

    tilesetCanvas.repaint();
  }

  public static Image getTileset(JFrame parent, Image tileset) {
    TilesetDialog td = new TilesetDialog(parent);
    td.show(tileset);

    if (td.rv != null) return td.rv;

    return tileset; // cancel pressed
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == cancel) {
      hide();
    } else if (e.getSource() == ok) {
      rv = tilesetCanvas.bottomImage;
      hide();
    } else if (e.getSource() == editTile) {
      editSelectedTile();
    } else if (e.getSource() == importTileset) {
      fileDialog.setMode(FileDialog.LOAD);
      fileDialog.setTitle("Import Tileset");
      fileDialog.show();
      String file = fileDialog.getFile();
      String dir = fileDialog.getDirectory();

      if (file == null) // user pressed cancel
      return;

      Image i = new ImageIcon().getImage();

      File m_file = new File(dir + file);

      try {

        BufferedInputStream bs = new BufferedInputStream(new FileInputStream(m_file));
        BitMap bmp = new BitMap(bs);
        bmp.readBitMap(false);

        if (bmp.isBitMap()) {
          i = bmp.getImage();
        } else {
          i = new ImageIcon(dir + file).getImage();
        }
      } catch (Exception er) {
        JOptionPane.showMessageDialog(null, er.toString());
      }

      tilesetCanvas.bottomImage.getGraphics().drawImage(i, 0, 0, null);
      tilesetCanvas.repaint();

      int x = selectedTile % 19;
      int y = selectedTile / 19;

      tilePreviewCanvas.bottomImage =
          copyImage(tilesetCanvas.bottomImage, x * 16, y * 16, x * 16 + 16, y * 16 + 16);
      tilePreviewCanvas.repaint();
    }
  }

  public void itemStateChanged(ItemEvent e) {
    if (showTypes.isSelected()) {
      tilesetCanvas.topImage = filter;
    } else {
      tilesetCanvas.topImage = null;
    }

    tilesetCanvas.selectedTile = selectedTile;
    tilesetCanvas.repaint();
  }

  public void mousePressed(MouseEvent e) {
    if (e.getClickCount() == 1) {
      int x = e.getPoint().x / 16;
      int y = e.getPoint().y / 16;

      selectedTile = x + y * 19;
      tilesetCanvas.selectedTile = selectedTile;
      tilesetCanvas.repaint();

      tilePreviewCanvas.bottomImage =
          copyImage(tilesetCanvas.bottomImage, x * 16, y * 16, x * 16 + 16, y * 16 + 16);
      tilePreviewCanvas.repaint();
    } else if (e.getClickCount() == 2) {
      editSelectedTile();
    }
  }

  public void mouseReleased(MouseEvent e) {}

  public void mouseClicked(MouseEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}
}
