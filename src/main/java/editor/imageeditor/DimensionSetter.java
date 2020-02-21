// Stan Bak
// 5-27-04
// DimensionSetterDialog

package editor.imageeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DimensionSetter extends JDialog implements ActionListener {
  JSpinner x =
      new javax.swing.JSpinner(
          new SpinnerNumberModel(
              new Integer(512), new Integer(1), new Integer(512), new Integer(1)));

  JSpinner y =
      new javax.swing.JSpinner(
          new SpinnerNumberModel(
              new Integer(512), new Integer(1), new Integer(512), new Integer(1)));

  JLabel xLabel = new JLabel("Width:");

  JLabel yLabel = new JLabel("Height:");

  JButton ok = new JButton("Ok");

  JButton cancel = new JButton("Cancel");

  boolean cancelPressed = true;

  public DimensionSetter(JDialog parent, Dimension init) {
    super(parent, true);

    int WIDTH = 200;
    int HEIGHT = 120;

    getContentPane().setLayout(null);

    xLabel.setBounds(50, 10, 60, 20);
    getContentPane().add(xLabel);

    x.setBounds(110, 10, 50, 20);
    getContentPane().add(x);

    yLabel.setBounds(50, 30, 60, 20);
    getContentPane().add(yLabel);

    y.setBounds(110, 30, 50, 20);
    getContentPane().add(y);

    ok.setBounds(5, 60, 80, 20);
    getContentPane().add(ok);
    ok.addActionListener(this);

    cancel.setBounds(105, 60, 80, 20);
    getContentPane().add(cancel);
    cancel.addActionListener(this);

    setLocation(
        parent.getX() + (parent.getWidth() - WIDTH) / 2,
        parent.getY() + (parent.getHeight() - HEIGHT) / 2);
    setSize(WIDTH, HEIGHT);
    setTitle("Dimension Setter");

    if (init != null) {
      y.setValue(new Integer(init.height));
      x.setValue(new Integer(init.width));
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == ok) {
      cancelPressed = false;
      setVisible(false);
    } else if (e.getSource() == cancel) setVisible(false);
  }

  public Dimension getDimensions() {
    Dimension d =
        new Dimension(((Integer) x.getValue()).intValue(), ((Integer) y.getValue()).intValue());

    return d;
  }

  public static Dimension getNewDimensions(JDialog parent, Dimension init) {
    DimensionSetter ds = new DimensionSetter(parent, init);
    ds.show();

    if (!ds.cancelPressed) return ds.getDimensions();
    else return null;
  }
}
