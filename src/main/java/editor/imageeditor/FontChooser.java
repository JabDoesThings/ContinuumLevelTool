package editor.imageeditor;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

//// FontChooser  by Noah w.//

public class FontChooser extends JDialog {
  String[] styleList = new String[] {"Plain", "Bold", "Italic"};

  String[] sizeList =
      new String[] {
        "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18",
        "19", "20", "22", "24", "27", "30", "34", "39", "45", "51", "60", "72"
      };

  NwList StyleList;

  NwList FontList;

  NwList SizeList;

  static JLabel Sample = new JLabel();

  boolean ob = false;

  DrawPanel drawingPanel;

  public FontChooser(JDialog owner, Font font, DrawPanel dp) {
    super(owner);
    setTitle("Font Choosr - by  Noah w.");
    initAll();

    if (font == null) font = Sample.getFont();

    FontList.setSelectedItem(font.getName());
    SizeList.setSelectedItem(font.getSize() + "");
    StyleList.setSelectedItem(styleList[font.getStyle()]);

    setSize(425, 260);
    setLocation(
        owner.getX() + owner.getWidth() + 20, owner.getY() + (owner.getHeight() - getHeight()) / 2);
    setVisible(false);
    drawingPanel = dp;
  }

  public Font getSelectedFont() {
    Font fo = null;

    fo = Sample.getFont();

    return (fo);
  }

  private void initAll() {
    getContentPane().setLayout(null);
    addLists();
    Sample.setBounds(10, 170, 415, 45);
    Sample.setForeground(Color.black);
    getContentPane().add(Sample);
  }

  private void addLists() {
    FontList =
        new NwList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
    StyleList = new NwList(styleList);
    SizeList = new NwList(sizeList);
    FontList.setBounds(10, 10, 260, 145);
    StyleList.setBounds(280, 10, 80, 145);
    SizeList.setBounds(370, 10, 40, 145);

    getContentPane().add(FontList);
    getContentPane().add(StyleList);
    getContentPane().add(SizeList);
  }

  private void showSample() {
    int g = 0;

    try {
      g = Integer.parseInt(SizeList.getSelectedValue());
    } catch (NumberFormatException nfe) {
    }

    String st = StyleList.getSelectedValue();
    int s = Font.PLAIN;

    if (st.equalsIgnoreCase("Bold")) s = Font.BOLD;

    if (st.equalsIgnoreCase("Italic")) s = Font.ITALIC;

    Sample.setFont(new Font(FontList.getSelectedValue(), s, g));
    Sample.setText("The quick brown fox jumped over the lazy dog.");
    if (drawingPanel != null) drawingPanel.repaint();
  } // ////////////////////////////////////////////////////////////////////

  public class NwList extends JPanel {
    JList jl;

    JScrollPane sp;

    JLabel jt;

    String si = " ";

    public NwList(String[] values) {
      setLayout(null);
      jl = new JList(values);
      sp = new JScrollPane(jl);
      jt = new JLabel();
      // jt.setBackground(Color.white);
      jt.setForeground(Color.black);
      jt.setOpaque(true);
      jt.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
      jt.setFont(getFont());
      jl.setBounds(0, 0, 100, 1000);
      jl.setBackground(Color.white);
      jl.addListSelectionListener(
          new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
              jt.setText(" " + (String) jl.getSelectedValue());
              si = (String) jl.getSelectedValue();
              showSample();
            }
          });
      add(sp);
      add(jt);
    }

    public String getSelectedValue() {
      return (si);
    }

    public void setSelectedItem(String s) {
      jl.setSelectedValue(s, true);
    }

    public void setBounds(int x, int y, int w, int h) {
      super.setBounds(x, y, w, h);
      sp.setBounds(0, y + 12, w, h - 23);
      sp.revalidate();
      jt.setBounds(0, 0, w, 20);
    }
  }
}
