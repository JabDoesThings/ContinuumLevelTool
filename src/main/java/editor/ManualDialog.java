package editor;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ManualDialog extends JFrame implements HyperlinkListener {
  // private static final String MANUAL_PAGE = Main.rootDirectory + File.separator + "include" +
  // File.separator +
  // "manual"
  // + File.separator + "continuum.html";

  private static final String MANUAL_PAGE =
      "file:///" + Main.rootDirectory + File.separator + "/include/manual/continuum.html";

  private JEditorPane jep;

  private static ManualDialog md = null;

  private static ManualDialog getInstance() {
    if (md == null) {
      md = new ManualDialog();
    }

    return md;
  }

  private ManualDialog() {
    setTitle("Manual");

    jep = new JEditorPane();
    jep.setEditable(false);
    jep.addHyperlinkListener(this);
    // jep.setEditorKit(new HTMLEditorKit());

    try {
      jep.setPage(MANUAL_PAGE);
    } catch (IOException e) {
      System.out.println(e);
    }

    JScrollPane sp = new JScrollPane(jep);

    add(sp);

    setSize(1024, 768);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
  }

  public static void showHelp() {
    ManualDialog md = getInstance();

    md.setVisible(true);
  }

  public void hyperlinkUpdate(HyperlinkEvent e) {
    if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
      JEditorPane src = (JEditorPane) e.getSource();
      String desc = e.getDescription();

      int index = desc.lastIndexOf("#");
      String ref = e.getDescription().substring(index + 1);

      // src.scrollToReference(ref);
      src.scrollToReference(ref);
      src.revalidate();
    }
  }
}
