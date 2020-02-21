package editor.imageeditor;

// double buffered canvas
import java.awt.*;

public class DBCanvas extends Canvas {

  public Image bottomImage = null;

  public Image topImage = null;

  public int selectedTile = -1;

  private Image offScreenBuffer;

  public void update(Graphics g) {
    Graphics gr;
    // Will hold the graphics context from the offScreenBuffer.
    // We need to make sure we keep our offscreen buffer the same size
    // as the graphics context we're working with.
    if (offScreenBuffer == null
        || (!(offScreenBuffer.getWidth(this) == this.getSize().width
            && offScreenBuffer.getHeight(this) == this.getSize().height))) {
      offScreenBuffer = this.createImage(getSize().width, getSize().height);
    }

    // We need to use our buffer Image as a Graphics object:
    gr = offScreenBuffer.getGraphics();

    Color lastColor = gr.getColor();

    gr.setColor(Color.white);
    gr.fillRect(0, 0, getSize().width, getSize().height);
    gr.setColor(lastColor);

    paint(gr); // Passes our off-screen buffer to our paint method, which,
    // unsuspecting, paints on it just as it would on the Graphics
    // passed by the browser or applet viewer.
    if (g != null) g.drawImage(offScreenBuffer, 0, 0, this);
    // And now we transfer the info in the buffer onto the
    // graphics context we got from the browser in one smooth motion.
  }

  public void paint(Graphics g) {
    if (bottomImage != null) {
      g.drawImage(bottomImage, 0, 0, getWidth(), getHeight(), null);

      if (topImage != null) {
        g.drawImage(topImage, 0, 0, getWidth(), getHeight(), null);
      }
    } else {
      g.setColor(Color.white);
      g.fillRect(0, 0, getWidth(), getHeight());
    }

    if (selectedTile != -1) {
      int x = selectedTile % 19;
      int y = selectedTile / 19;

      g.setColor(Color.white);
      g.drawRect(x * 16, y * 16, 16, 16);
      g.drawRect(x * 16 + 1, y * 16 + 1, 14, 14);
    }
  }
}
