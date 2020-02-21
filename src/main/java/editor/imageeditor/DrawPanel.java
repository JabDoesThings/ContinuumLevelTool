// Stan Bak
// Draw Panel class file
// 5-8-04

package editor.imageeditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.util.LinkedList;

public class DrawPanel extends JPanel implements KeyListener {

  Image theImage = null;

  Image startDrawingWithThisOne = null;

  Image initialImage = null;

  Image buffer = null;

  Image selectedImage = null;

  Image copiedImage = null;

  ImageDialog parent;

  Dimension myDimensions = null;

  boolean skipZoom = false;

  Color offGray = new Color(96, 96, 96);

  Color babyBlue = new Color(170, 189, 245);

  JButton ok;

  JButton cancel;

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

  int selectedTool = PENCIL;

  // boolean pressed = false; // is mouse pressed
  boolean leftButton = false;

  // zoom variables
  int zoomX = 0;

  int zoomY = 0;

  // int zoomWidth = 512;
  // int zoomHeight = 512;

  Image[] tools = null;

  Image dropperImage =
      new ImageIcon("include" + File.separator + "Images" + File.separator + "droppermouse.gif")
          .getImage();

  // Variables used for drawing
  Point dragStart = null;

  Point curMousePoint = null;

  private UndoVector drawingVector = null;

  Color foregroundColor = Color.white;

  Color backgroundColor = Color.black;

  float floater1[] = new float[] {(float) (5.0), (float) (10.0)};

  float floater2[] = new float[] {(float) (1.0), (float) (1.0)};

  private Stroke dotted =
      new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, floater1, 0);

  private Stroke thinDotted =
      new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10, floater2, 0);

  int curPreferredWidth;

  int curPreferredHeight;

  Point lastClickLocation = null;

  String curString;

  Point selectedPoint = null;

  Point theOffset = null; // the offset for images

  Point initialSelection = null;

  public DrawPanel(
      ImageDialog myParent,
      Image img,
      int width,
      int height,
      int WIDTH,
      int HEIGHT,
      Image[] images) {
    if (img != null) initialImage = (new ImageIcon(img)).getImage();
    else {
      myDimensions = new Dimension(width, height);
    }

    tools = images;

    setCursor(
        Toolkit.getDefaultToolkit()
            .createCustomCursor(tools[selectedTool], new Point(3, 21 + 8), "pencil tool"));

    parent = myParent;
    setSize(WIDTH, HEIGHT);

    setPreferredSize(new Dimension(512, 512));
    curPreferredWidth = 512;
    curPreferredHeight = 512;

    addKeyListener(this);
    setFocusable(true);
  }

  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    Graphics2D g2d = (Graphics2D) g;

    centerZoomIfResized();
    createImageIfNecessary();

    buffer.getGraphics().drawImage(theImage, 0, 0, 512, 512, null);
    paintDrawingPreview((Graphics2D) buffer.getGraphics());
    paintMovementPreview((Graphics2D) buffer.getGraphics());
    paintFontPreview((Graphics2D) buffer.getGraphics());
    paintSelectedImage((Graphics2D) buffer.getGraphics());

    drawDimensionBlocker(buffer.getGraphics());

    g.drawImage(
        buffer,
        0, // x
        0 // y
        ,
        getWidth(), // width
        getHeight(), // height destination
        0,
        0,
        512,
        512,
        null);

    paintZoomBox(g2d);
    // parent.fc.update(parent.fc.getGraphics());
  }

  public void drawDimensionBlocker(Graphics g) {
    if (myDimensions == null) return;

    g.setColor(babyBlue);

    int top = 512 / 2 - myDimensions.height / 2;
    int left = 512 / 2 - myDimensions.width / 2;

    g.fillRect(0, 0, left, 512);
    g.fillRect(0, 0, 512, top);

    g.fillRect(left + myDimensions.width, 0, left, 512);
    g.fillRect(0, top + myDimensions.height, 512, top);
  }

  public Dimension getDimensions() {
    return myDimensions;
  }

  public void setDimensions(Dimension d) {
    parent.imageSizeLabel.setText("Image Size: " + d.width + " x " + d.height);

    int top = 512 / 2 - myDimensions.height / 2;
    int left = 512 / 2 - myDimensions.width / 2;

    Graphics2D g = (Graphics2D) theImage.getGraphics();
    g.setColor(Color.black);

    Stroke s = new BasicStroke(1);

    Rectangle r[] = new Rectangle[4];

    r[0] = new Rectangle(0, 0, left, 512);
    r[1] = new Rectangle(0, 0, 512, top);
    r[2] = new Rectangle(left + myDimensions.width, 0, left, 512);
    r[3] = new Rectangle(0, top + myDimensions.height, 512, top);

    for (int x = 0; x < 4; ++x) {
      drawingVector.add(Color.black); // color
      drawingVector.add(r[x]); // rectangle
      drawingVector.add(s); // stroke
      drawingVector.add(new Boolean(true)); // fill

      g.fill(r[x]);
    }

    myDimensions = d;

    // System.out.println("filling");

    repaint();
  }

  public void undo() {
    Image i = drawingVector.undo();
    makeImage(i);
  }

  public void redo() {
    Image i = drawingVector.redo();
    makeImage(i);
  }

  public void copy() {
    if (selectedImage == null) return;

    copiedImage = copyImage(selectedImage);
  }

  public void paste() {
    setSelection(copiedImage);
  }

  public void cut() {
    if (selectedImage == null) return;

    copiedImage = copyImage(selectedImage);

    selectedImage = null;
    selectedPoint = null;
    initialSelection = null;

    repaint();
  }

  public void setSelection(Image i) { // user opened image from file
    toolSelected(SELECTION);
    selectedImage = i;

    int top = 512 / 2 - myDimensions.height / 2;
    int left = 512 / 2 - myDimensions.width / 2;

    selectedPoint = new Point(left, top);
    repaint();
  }

  public void makeImage(Image i) {
    initialSelection = null;

    Graphics g = theImage.getGraphics();

    if (i != null) {
      g.setColor(Color.lightGray);
      g.fillRect(0, 0, 512, 512);

      int w = i.getWidth(null);
      int h = i.getHeight(null);

      g.setColor(Color.gray);
      g.fillRect((512 - w) / 2, (512 - h) / 2, w, h);

      g.drawImage(i, (512 - w) / 2, (512 - h) / 2, w, h, null);
    } else {
      g.setColor(Color.black);
      g.fillRect(0, 0, 512, 512);
    }

    repaint();
  }

  public void flushUndo() {
    drawingVector = new UndoVector(theImage, this);
  }

  public Image copyImage(Image i) {
    if (i == null) return null;

    Image newI = this.createImage(i.getWidth(null), i.getHeight(null));

    Graphics g = newI.getGraphics();
    g.drawImage(i, 0, 0, null);

    return newI;
  }

  private void drawImage() {
    if (selectedTool == SELECTION) {

      Graphics g = theImage.getGraphics();

      if (selectedPoint != null && selectedImage != null) {
        g.drawImage(
            selectedImage,
            selectedPoint.x,
            selectedPoint.y,
            selectedImage.getWidth(null),
            selectedImage.getHeight(null),
            null);

        drawingVector.add(selectedImage);
        drawingVector.add(selectedPoint);
      }
    }
  }

  // paints the current selection
  private void paintSelectedImage(Graphics2D g) {
    if (selectedTool == SELECTION) {
      if (selectedPoint != null && selectedImage != null) {
        g.drawImage(
            selectedImage,
            selectedPoint.x,
            selectedPoint.y,
            selectedImage.getWidth(null),
            selectedImage.getHeight(null),
            null);

        g.setColor(offGray);
        g.setStroke(thinDotted);
        g.drawRect(
            selectedPoint.x,
            selectedPoint.y,
            selectedImage.getWidth(null) - 1,
            selectedImage.getHeight(null) - 1);
      }
    }
  }

  // draws the preview of the current font, this gets applied when they change
  // tools or press Ok
  private void paintFontPreview(Graphics2D g) {
    if (selectedTool == TEXT && lastClickLocation != null) {
      Point click = getImagePointFromPanelPoint(lastClickLocation.x, lastClickLocation.y);
      // System.out.println("click = " + click);

      g.setColor(offGray);
      g.drawLine(click.x, click.y, click.x, click.y - 12);

      g.drawLine(click.x, click.y, click.x + 12, click.y);

      g.setColor(foregroundColor);
      g.setFont(parent.fc.getSelectedFont());

      g.drawString(curString, click.x + 2, click.y - 2);
    }
  }

  private void drawText() {
    if (selectedTool == TEXT && lastClickLocation != null && theImage != null) {
      Graphics g = theImage.getGraphics();
      Point click = getImagePointFromPanelPoint(lastClickLocation.x, lastClickLocation.y);
      // System.out.println("click = " + click);

      g.setColor(foregroundColor);
      g.setFont(parent.fc.getSelectedFont());

      g.drawString(curString, click.x + 2, click.y - 2);

      drawingVector.add(foregroundColor);
      drawingVector.add(curString);
      drawingVector.add(parent.fc.getSelectedFont());
      drawingVector.add(new Point(click.x + 2, click.y - 2));
    }
  }

  // draws anything that should happen when the user moves the mouse (not just
  // drags)
  private void paintMovementPreview(Graphics2D g) {
    if (curMousePoint == null) return;

    if (selectedTool == ERASER) {
      Color cur = backgroundColor;
      int thickness = ((Integer) parent.thicknessSpinner.getValue()).intValue();

      Point from = getImagePointFromPanelPoint(curMousePoint.x, curMousePoint.y);
      Point to = new Point(from.x - thickness, from.y + thickness);

      Rectangle r = getRect(from, to);

      r.x += 1;
      r.width -= 1;
      r.height -= 1;

      g.drawRect(r.x, r.y, r.width, r.height);
    }
  }

  // draw the lines, squares, ect as the user drags
  private void paintDrawingPreview(Graphics2D g) {
    if (dragStart == null || curMousePoint == null) return;

    if (selectedTool == LINE) {
      Color cur = getDrawingColor();
      g.setColor(cur);

      int thickness = ((Integer) parent.thicknessSpinner.getValue()).intValue();
      Stroke s = new BasicStroke(thickness);
      g.setStroke(s);

      Point from = getImagePointFromPanelPoint(dragStart.x, dragStart.y);
      Point to = getImagePointFromPanelPoint(curMousePoint.x, curMousePoint.y);

      g.drawLine(from.x, from.y, to.x, to.y);
    } else if (selectedTool == RECTANGLE) {
      Color cur = getDrawingColor();
      g.setColor(cur);

      int thickness = ((Integer) parent.thicknessSpinner.getValue()).intValue();
      Stroke s = new BasicStroke(thickness);
      g.setStroke(s);

      Point from = getImagePointFromPanelPoint(dragStart.x, dragStart.y);
      Point to = getImagePointFromPanelPoint(curMousePoint.x, curMousePoint.y);

      Rectangle r = getRect(from, to);

      if (parent.fillShape.isSelected()) g.fillRect(r.x, r.y, r.width, r.height);
      else g.drawRect(r.x, r.y, r.width, r.height);
    } else if (selectedTool == OVAL) {
      Color cur = getDrawingColor();
      g.setColor(cur);

      int thickness = ((Integer) parent.thicknessSpinner.getValue()).intValue();
      Stroke s = new BasicStroke(thickness);
      g.setStroke(s);

      Point from = getImagePointFromPanelPoint(dragStart.x, dragStart.y);
      Point to = getImagePointFromPanelPoint(curMousePoint.x, curMousePoint.y);

      Rectangle r = getRect(from, to);

      if (parent.fillShape.isSelected()) g.fillOval(r.x, r.y, r.width, r.height);
      else g.drawOval(r.x, r.y, r.width, r.height);
    } else if (selectedTool == SELECTION) {
      g.setColor(offGray);

      g.setStroke(thinDotted);

      Point from = getImagePointFromPanelPoint(dragStart.x, dragStart.y);
      Point to = getImagePointFromPanelPoint(curMousePoint.x, curMousePoint.y);

      Rectangle r = getRect(from, to);

      g.drawRect(r.x, r.y, r.width, r.height);
    }
  }

  // get the rectangle with these two points as corners
  private Rectangle getRect(Point one, Point two) {
    int x = one.x;
    if (two.x < x) x = two.x;

    int y = one.y;
    if (two.y < y) y = two.y;

    int width = one.x - two.x;
    if (width < 0) width *= -1;

    int height = one.y - two.y;
    if (height < 0) height *= -1;

    return new Rectangle(x, y, width, height);
  }

  // if theImage is equal to null this method will create it.
  private void createImageIfNecessary() {
    if (theImage == null) {

      buffer = this.createImage(512, 512);
      theImage = this.createImage(512, 512);
      Graphics g = theImage.getGraphics();

      if (initialImage != null
          && initialImage.getWidth(null) > 0
          && initialImage.getHeight(null) > 0) {
        g.setColor(Color.black);
        g.fillRect(0, 0, 512, 512);

        int w = initialImage.getWidth(null);
        int h = initialImage.getHeight(null);

        g.fillRect((512 - w + 1) / 2, (512 - h + 1) / 2, w, h);

        g.drawImage(initialImage, (512 - w + 1) / 2, (512 - h + 1) / 2, w, h, null);

        drawingVector = new UndoVector(theImage, this);

        myDimensions = new Dimension(w, h);
        setDimensions(new Dimension(w, h));

        // zoom in on it
        int curSide = 512;

        while (curSide > w && curSide > h) {
          curSide /= 2;
        }

        if (curSide < 512) curSide *= 2;

        setPreferredSize(new Dimension(512 * 512 / curSide, 512 * 512 / curSide));
        updateUI();

        zoomX = (512 * 512 / curSide) / 2 - 256;
        zoomY = (512 * 512 / curSide) / 2 - 256;

      } else {
        g.setColor(Color.black);
        g.fillRect(0, 0, 512, 512);
        drawingVector = new UndoVector(theImage, this);
        // myDimensions = new Dimension(512,512);
      }
    }
  }

  // x and y are in panel coords
  // returns in image coords (between 0 and 512)
  public Point getImagePointFromPanelPoint(int x, int y) {
    Point p1 = getScreenPointFromPanelPoint(x, y);

    return getImagePointFromScreenPoint(p1.x, p1.y);
  }

  // x and y are in panel coords
  // returns in screen coords (between 0 and 512)
  private Point getScreenPointFromPanelPoint(int x, int y) {
    int x1 = x - parent.scrollPane.getHorizontalScrollBar().getValue();
    int y1 = y - parent.scrollPane.getVerticalScrollBar().getValue();

    return new Point(x1, y1);
  }

  // x and y are in screen coords
  // returns in image coordinate system (between 0 and 512)
  private Point getImagePointFromScreenPoint(int x, int y) {
    double xScale = 512.0 / (double) curPreferredWidth;
    double yScale = 512.0 / (double) curPreferredHeight;

    int x1 = (int) (xScale * (parent.scrollPane.getHorizontalScrollBar().getValue() + x));
    int y1 = (int) (yScale * (parent.scrollPane.getVerticalScrollBar().getValue() + y));

    return new Point(x1, y1);
  }

  // x and y are in screen coords between 0-512
  // returns the coords on the current panel
  private Point getPanelPointFromScreenPoint(int x, int y) {
    int realX = (int) (parent.scrollPane.getHorizontalScrollBar().getValue() + x);

    int realY = (int) (parent.scrollPane.getVerticalScrollBar().getValue() + y);

    return new Point(realX, realY);
  }

  private void centerZoomIfResized() {
    int preferredWidth = getPreferredSize().width;
    int preferredHeight = getPreferredSize().height;

    if (curPreferredWidth != preferredWidth) {
      JScrollBar hor = parent.scrollPane.getHorizontalScrollBar();
      hor.setValue(zoomX);
      curPreferredWidth = preferredWidth;
      // repaint();
      // }

      // if (curPreferredHeight != preferredHeight)
      // {
      JScrollBar vert = parent.scrollPane.getVerticalScrollBar();
      curPreferredHeight = preferredHeight;
      vert.setValue(zoomY);

      // System.out.println("centering to " + zoomX + ", " + zoomY);

      parent.scrollPane.revalidate();

      repaint();
      parent.repaint();
    }
  }

  private void paintZoomBox(Graphics2D g) {
    if (selectedTool == ZOOM && curMousePoint != null) {
      Rectangle zoomBox = getZoomBoxBounds(curMousePoint);
      g.setStroke(dotted);
      g.setColor(offGray);

      g.draw(zoomBox);
    }
  }

  public void colorChanged(Color newColor, boolean foreground) {
    if (foreground) foregroundColor = newColor;
    else backgroundColor = newColor;
  }

  public void toolSelected(int whichTool) {
    drawText();
    drawImage();
    curString = "";
    selectedPoint = null;
    initialSelection = null;

    selectedTool = whichTool;
    lastClickLocation = null;

    if (selectedTool == ZOOM) {
      setCursor(
          Toolkit.getDefaultToolkit()
              .createCustomCursor(tools[selectedTool], new Point(7, 7 + 8), "zoom tool"));
    } else if (selectedTool == PENCIL) {
      setCursor(
          Toolkit.getDefaultToolkit()
              .createCustomCursor(tools[selectedTool], new Point(3, 21 + 8), "pencil tool"));
    } else if (selectedTool == FILL) {
      setCursor(
          Toolkit.getDefaultToolkit()
              .createCustomCursor(tools[selectedTool], new Point(1, 23 + 8), "fill tool"));
    } else if (selectedTool == DROPPER) {
      setCursor(
          Toolkit.getDefaultToolkit()
              .createCustomCursor(dropperImage, new Point(3, 21 + 8), "dropper tool"));
    } else if (selectedTool == ERASER) {
      setCursor(
          Toolkit.getDefaultToolkit()
              .createCustomCursor(tools[selectedTool], new Point(1, 19 + 8), "eraser tool"));
    } else if (selectedTool == LINE
        || selectedTool == RECTANGLE
        || selectedTool == OVAL
        || selectedTool == SELECTION
        || selectedTool == TEXT) {
      setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      curString = "";
    }
  }

  private Color getDrawingColor() {
    if (leftButton == true) return foregroundColor;

    return backgroundColor;
  }

  public void pencilDragged(Point p) {
    Color cur = getDrawingColor();

    Point to = getImagePointFromPanelPoint(p.x, p.y);
    Point from = getImagePointFromPanelPoint(curMousePoint.x, curMousePoint.y);

    int thickness = ((Integer) parent.thicknessSpinner.getValue()).intValue();
    Stroke s = new BasicStroke(thickness);

    drawingVector.add(cur); // color

    Line2D.Float line = new Line2D.Float(from, to);
    drawingVector.add(line); // line
    drawingVector.add(s); // stroke

    Graphics2D g = (Graphics2D) theImage.getGraphics();
    g.setColor(cur);
    g.setStroke(s);
    g.draw(line);

    repaint();
  }

  public Image getImage() {
    drawText();
    drawImage();
    curString = "";

    Image rv = this.createImage(myDimensions.width, myDimensions.height);
    int top = 512 / 2 - myDimensions.height / 2;
    int left = 512 / 2 - myDimensions.width / 2;
    rv.getGraphics()
        .drawImage(
            theImage,
            0,
            0,
            myDimensions.width,
            myDimensions.height, // desintantion
            left,
            top,
            left + myDimensions.width,
            top + myDimensions.height,
            null); // source

    return rv;
  }

  // gets the Image cooridate bounds of the current screen
  private Rectangle getRealBounds() {
    Point one = getImagePointFromScreenPoint(0, 0);
    Point two = getImagePointFromScreenPoint(512, 512);

    return new Rectangle(one.x, one.y, two.x - one.x, two.y - one.y);
  }

  private Rectangle getPanelBounds() {
    // return new Rectangle(zoomX,zoomY,zoomX + zoomWidth, zoomY +
    // zoomHeight);

    Point one = getPanelPointFromScreenPoint(0, 0);
    Point two = getPanelPointFromScreenPoint(512, 512);

    return new Rectangle(one.x, one.y, two.x - one.x, two.y - one.y);
  }

  private void zoomIn(Point pressed) {
    Rectangle zoomBounds = getZoomBoxBounds(pressed);

    setPreferredSize(new Dimension(getPreferredSize().width * 2, getPreferredSize().height * 2));
    updateUI();

    zoomX = 2 * zoomBounds.x;
    zoomY = 2 * zoomBounds.y;

    Point screen = getScreenPointFromPanelPoint(curMousePoint.x, curMousePoint.y);

    curMousePoint.x = zoomX + screen.x;
    curMousePoint.y = zoomY + screen.y;

    repaint();
  }

  private void zoomOut(Point pressed) {
    if (curPreferredWidth == 512) return;
    Rectangle zoomBounds = getZoomBoxBounds(pressed);

    Rectangle r = getPanelBounds();

    Point pressedPoint =
        new Point( // PANEL COORDS
            zoomBounds.x + zoomBounds.width / 2, zoomBounds.y + zoomBounds.height / 2);

    setPreferredSize(new Dimension(getPreferredSize().width / 2, getPreferredSize().height / 2));

    zoomX = (pressedPoint.x - r.width) / 2;
    zoomY = (pressedPoint.y - r.height) / 2;

    Point screen = getScreenPointFromPanelPoint(curMousePoint.x, curMousePoint.y);

    curMousePoint.x = zoomX + screen.x;
    curMousePoint.y = zoomY + screen.y;

    repaint();
  }

  public Rectangle getZoomBoxBounds(Point curPoint) {
    int WIDTH = 512;
    int HEIGHT = 512;

    int xOffset = parent.scrollPane.getHorizontalScrollBar().getValue();
    int yOffset = parent.scrollPane.getVerticalScrollBar().getValue();

    int zoomBoxWidth = (WIDTH / 2);
    int zoomBoxHeight = (HEIGHT / 2);

    Point p = new Point(curPoint);

    p.x -= xOffset;
    p.y -= yOffset;

    if (p.x < zoomBoxWidth / 2) p.x = zoomBoxWidth / 2;

    if (p.y < zoomBoxHeight / 2) p.y = zoomBoxHeight / 2;

    if (p.x > WIDTH - zoomBoxWidth / 2) p.x = WIDTH - zoomBoxWidth / 2;

    if (p.y > HEIGHT - zoomBoxHeight / 2) p.y = HEIGHT - zoomBoxHeight / 2;

    return new Rectangle(
        xOffset + p.x - zoomBoxWidth / 2,
        yOffset + p.y - zoomBoxHeight / 2,
        zoomBoxWidth,
        zoomBoxHeight);
  }

  private void createLine(Point end) {
    Color cur = getDrawingColor();

    Point to = getImagePointFromPanelPoint(end.x, end.y);
    Point from = getImagePointFromPanelPoint(dragStart.x, dragStart.y);

    int thickness = ((Integer) parent.thicknessSpinner.getValue()).intValue();
    Stroke s = new BasicStroke(thickness);

    drawingVector.add(cur);

    Line2D.Float line = new Line2D.Float(from, to);
    drawingVector.add(line);

    drawingVector.add(s);

    Graphics2D g = (Graphics2D) theImage.getGraphics();
    g.setColor(cur);
    g.setStroke(s);
    g.draw(line);

    repaint();
  }

  private void createRectangle(Rectangle r, Color cur) {
    int thickness = 1;
    Stroke s = new BasicStroke(thickness);

    drawingVector.add(cur); // color
    drawingVector.add(r); // rectangle
    drawingVector.add(s); // stroke
    drawingVector.add(new Boolean(true)); // fill

    Graphics2D g = (Graphics2D) theImage.getGraphics();
    g.setColor(cur);
    g.setStroke(s);

    g.fill(r);

    repaint();
  }

  private void createRectangle(Point end) {
    Color cur = getDrawingColor();

    Point to = getImagePointFromPanelPoint(end.x, end.y);
    Point from = getImagePointFromPanelPoint(dragStart.x, dragStart.y);

    int thickness = ((Integer) parent.thicknessSpinner.getValue()).intValue();
    Stroke s = new BasicStroke(thickness);

    Rectangle r = getRect(from, to);

    drawingVector.add(cur); // color
    drawingVector.add(r); // retangle
    drawingVector.add(s); // stroke
    drawingVector.add(new Boolean(parent.fillShape.isSelected())); // fill

    Graphics2D g = (Graphics2D) theImage.getGraphics();
    g.setColor(cur);
    g.setStroke(s);

    if (parent.fillShape.isSelected()) g.fill(r);
    else g.draw(r);

    repaint();
  }

  private void selectImage(Rectangle r) {
    selectedImage = this.createImage(r.width, r.height);
    selectedImage
        .getGraphics()
        .drawImage(
            theImage,
            0,
            0,
            r.width,
            r.height, // destination
            r.x,
            r.y,
            r.x + r.width,
            r.y + r.height // source
            ,
            null);
  }

  private void selectRegion(Point end) {
    Point to = getImagePointFromPanelPoint(end.x, end.y);
    Point from = getImagePointFromPanelPoint(dragStart.x, dragStart.y);

    Rectangle r = getRect(from, to);

    if (r.width == 0 || r.height == 0) {
      selectedPoint = null;
      initialSelection = null;
      drawImage();
      selectedImage = null;
      return;
    }

    r.width += 1;
    r.height += 1;

    selectedPoint = new Point(r.x, r.y);
    initialSelection = new Point(r.x, r.y);

    Stroke s = new BasicStroke(1);

    selectImage(r);

    drawingVector.add(backgroundColor); // fill with background color
    drawingVector.add(r);
    drawingVector.add(s);
    drawingVector.add(new Boolean(true));

    Graphics2D g = (Graphics2D) theImage.getGraphics();
    g.setColor(backgroundColor);
    g.setStroke(s);

    g.fill(r);

    repaint();
  }

  private void createOval(Point end) {
    Color cur = getDrawingColor();

    Point to = getImagePointFromPanelPoint(end.x, end.y);
    Point from = getImagePointFromPanelPoint(dragStart.x, dragStart.y);

    int thickness = ((Integer) parent.thicknessSpinner.getValue()).intValue();
    Stroke s = new BasicStroke(thickness);

    Rectangle r = getRect(from, to);

    Ellipse2D.Float e = new Ellipse2D.Float(r.x, r.y, r.width, r.height);

    drawingVector.add(cur);
    drawingVector.add(e);
    drawingVector.add(s);
    drawingVector.add(new Boolean(parent.fillShape.isSelected()));

    Graphics2D g = (Graphics2D) theImage.getGraphics();
    g.setColor(cur);
    g.setStroke(s);

    if (parent.fillShape.isSelected()) g.fill(e);
    else g.draw(e);

    repaint();
  }

  private int[] getAllColors() {
    int[] storeHere = new int[512 * 512];
    PixelGrabber pg = new PixelGrabber(theImage, 0, 0, 512, 512, storeHere, 0, 512);
    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
      JOptionPane.showMessageDialog(null, "interrupted waiting for pixels!");
      storeHere = null;
      return null;
    }
    if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
      JOptionPane.showMessageDialog(null, "image fetch aborted or errored");
      storeHere = null;
      return null;
    }

    return storeHere;
  }

  private int[] getRGB(int pixel) {
    int[] rgb = new int[3];

    rgb[0] = (pixel >> 16) & 0xff;
    rgb[1] = (pixel >> 8) & 0xff;
    rgb[2] = (pixel) & 0xff;

    return rgb;
  }

  // p is in image coordinated
  private Color getColorAt(Point p) {
    int[] pixels = new int[1];
    PixelGrabber pg = new PixelGrabber(theImage, p.x, p.y, 1, 1, pixels, 0, 1);

    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
      JOptionPane.showMessageDialog(null, "interrupted waiting for pixels!");
      return null;
    }
    if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
      JOptionPane.showMessageDialog(null, "image fetch aborted or errored");
      return null;
    }

    // int alpha = (pixels[0] >> 24) & 0xff;
    int[] rgb = getRGB(pixels[0]);

    // System.out.println("a = " + alpha + ", r = " + red + ", g = " + green
    // + ", b = " + blue);

    return new Color(rgb[0], rgb[1], rgb[2]);
  }

  private void fillAtPoint(Point start, Graphics g, int sourcePixel, int[] pixels) {
    LinkedList myList = new LinkedList();

    int notCurrentColor = sourcePixel;
    if (notCurrentColor == 500) notCurrentColor = 501;
    else notCurrentColor = 500;

    myList.add(start);

    Rectangle r =
        new Rectangle(
            512 / 2 - myDimensions.width / 2,
            512 / 2 - myDimensions.height / 2,
            myDimensions.width,
            myDimensions.height);

    while (!myList.isEmpty()) {
      Point where = (Point) myList.removeFirst();

      if (pixels[where.x + where.y * 512] == sourcePixel) { // color is a match!
        pixels[where.x + where.y * 512] = notCurrentColor;

        g.fillRect(where.x, where.y, 1, 1);

        if (where.x > r.x) // left case
        myList.add(new Point(where.x - 1, where.y));

        if (where.x < r.x + r.width - 1) // right case
        myList.add(new Point(where.x + 1, where.y));

        if (where.y > r.y) // up case
        myList.add(new Point(where.x, where.y - 1));

        if (where.y < r.y + r.height - 1) // down case
        myList.add(new Point(where.x, where.y + 1));
      }
    }
  }

  private void fillAtPoint(Point imageP) {
    int top = 512 / 2 - myDimensions.height / 2;
    int left = 512 / 2 - myDimensions.width / 2;

    Rectangle r = new Rectangle(left, top, myDimensions.width, myDimensions.height);
    if (!r.contains(imageP)) return;

    Color cur = getDrawingColor();
    Graphics g = theImage.getGraphics();
    g.setColor(cur);

    drawingVector.add(cur);
    drawingVector.add(imageP);
    drawingVector.add(myDimensions);

    int[] allPixels = null;
    allPixels = getAllColors();

    if (allPixels != null) fillAtPoint(imageP, g, allPixels[imageP.x + imageP.y * 512], allPixels);

    repaint();
  }

  public void setCurrentImageAsDefault() {
    startDrawingWithThisOne = this.createImage(512, 512);
    startDrawingWithThisOne.getGraphics().drawImage(theImage, 0, 0, 512, 512, null);
  }

  public void fitImage() {
    int[] allPixels = null;
    allPixels = getAllColors();

    if (allPixels == null) return;

    int top = 512 / 2 - myDimensions.height / 2;
    int left = 512 / 2 - myDimensions.width / 2;
    int right = left + myDimensions.width - 1;
    int bottom = top + myDimensions.height - 1;

    boolean ok = true;
    for (; left < right; ++left) // MOVE LEFT towards right
    {
      for (int y = top; y < bottom; ++y) {
        int rgb[] = getRGB(allPixels[left + 512 * y]);

        if (!(rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0)) {
          ok = false;
          break;
        }
      }

      if (!ok) break;
    }

    ok = true;
    for (; right > left; --right) // MOVE RIGHT towards left
    {
      for (int y = top; y < bottom; ++y) {
        int rgb[] = getRGB(allPixels[right + 512 * y]);

        if (!(rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0)) {
          ok = false;
          break;
        }
      }

      if (!ok) break;
    }

    ok = true;
    for (; top < bottom; ++top) // MOVE TOP towards bottom
    {
      for (int x = left; x < right; ++x) {
        int rgb[] = getRGB(allPixels[x + 512 * top]);

        if (!(rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0)) {
          ok = false;
          break;
        }
      }

      if (!ok) break;
    }

    ok = true;
    for (; bottom > top; --bottom) // MOVE TOP towards bottom
    {
      for (int x = left; x < right; ++x) {
        int rgb[] = getRGB(allPixels[x + 512 * bottom]);

        if (!(rgb[0] == 0 && rgb[1] == 0 && rgb[2] == 0)) {
          ok = false;
          break;
        }
      }

      if (!ok) break;
    }

    int w = right - left;
    int h = bottom - top;
    if (w < 3) {
      JOptionPane.showMessageDialog(null, "Your image is too narrow for this to work.");
      return;
    }
    if (h < 3) {
      JOptionPane.showMessageDialog(null, "Your image is too short for this to work.");
      return;
    }

    w++;
    h++;

    Dimension d = new Dimension(w, h);

    int TOP = 512 / 2 - d.height / 2;
    int LEFT = 512 / 2 - d.width / 2;

    theImage
        .getGraphics()
        .drawImage(
            copyImage(theImage),
            LEFT,
            TOP,
            LEFT + w,
            TOP + h, // destination
            left,
            top,
            left + w,
            top + h,
            null); // source

    // zoom in on it
    int curSide = 512;

    while (curSide > w + 1 && curSide > h + 1) {
      curSide /= 2;
    }

    if (curSide < 512) curSide *= 2;

    setPreferredSize(new Dimension(512 * 512 / curSide, 512 * 512 / curSide));
    updateUI();

    zoomX = (512 * 512 / curSide) / 2 - 256;
    zoomY = (512 * 512 / curSide) / 2 - 256;

    setDimensions(d);
    flushUndo();
  }

  // pseudo event handling

  public void keyPressed(KeyEvent e) {}

  public void keyReleased(KeyEvent e) {}

  public void keyTyped(KeyEvent e) {
    if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
      if (curString.length() > 0) curString = curString.substring(0, curString.length() - 1);
    } else curString += e.getKeyChar();

    repaint();
  }

  public void mouseExited() {
    curMousePoint = null;
    repaint();
  }

  public void mousePressed(Point p, boolean left) {
    drawText();

    // pressed = true;
    dragStart = p;
    leftButton = left;
    curMousePoint = p;
    lastClickLocation = p;
    curString = "";

    if (selectedTool == ZOOM) {
      if (left) zoomIn(p);
      else zoomOut(p);
    } else if (selectedTool == PENCIL) {
      pencilDragged(p);
    } else if (selectedTool == ERASER) {
      int thickness = ((Integer) parent.thicknessSpinner.getValue()).intValue();
      Point from = getImagePointFromPanelPoint(curMousePoint.x, curMousePoint.y);
      Point to = new Point(from.x - thickness, from.y + thickness);

      Rectangle r = getRect(from, to);

      r.x += 1;
      createRectangle(r, backgroundColor);
    } else if (selectedTool == FILL) {
      Point imageP = getImagePointFromPanelPoint(p.x, p.y);
      fillAtPoint(imageP);
    } else if (selectedTool == DROPPER) {
      Color c = getColorAt(getImagePointFromPanelPoint(curMousePoint.x, curMousePoint.y));

      if (leftButton == true) {
        foregroundColor = c;
        parent.foregroundColor = c;
      } else {
        backgroundColor = c;
        parent.backgroundColor = c;
      }
      parent.drawSelectedColors(parent.getGraphics());
    } else if (selectedTool == TEXT) {
      repaint();
    }

    if (selectedTool == SELECTION) {
      if (selectedImage != null && selectedPoint != null) // there is a
      // selection
      {
        Rectangle r =
            new Rectangle(
                selectedPoint.x,
                selectedPoint.y,
                selectedImage.getWidth(null),
                selectedImage.getHeight(null));

        Point mouseP = getImagePointFromPanelPoint(p.x, p.y);

        if (r.contains(mouseP)) {
          initialSelection = new Point(selectedPoint.x, selectedPoint.y);
          theOffset = new Point(mouseP.x, mouseP.y);

          dragStart = null;
        } else if (!r.contains(p)) {
          drawImage();
          selectedImage = null;
          selectedPoint = null;
          theOffset = null;
          initialSelection = null;
        }
      } else {
        drawImage();
        selectedPoint = null;
        theOffset = null;
        initialSelection = null;
      }
    } else {
      drawImage();
      selectedPoint = null;
      initialSelection = null;
      theOffset = null;
    }
  }

  public void mouseReleased(Point p) {
    if (selectedTool == LINE) {
      createLine(p);
    } else if (selectedTool == RECTANGLE) {
      createRectangle(p);
    } else if (selectedTool == OVAL) {
      createOval(p);
    } else if (selectedTool == DROPPER) {
      parent.selectedTool = PENCIL;
      parent.repaint();
      parent.showThicknessGUI();

      toolSelected(PENCIL);
    } else if (selectedTool == SELECTION) {
      if (dragStart != null) // we're not moving... we're making a
      // selection
      {
        selectRegion(p);
        repaint();
      }
    }

    // pressed = false;
    dragStart = null;
  }

  public void mouseMoved(Point p) {
    if (p == curMousePoint) return;

    curMousePoint = p;

    if (selectedTool == ZOOM || selectedTool == ERASER) {
      repaint();
    }

    if (selectedTool == SELECTION) {
      if (selectedImage != null && selectedPoint != null) // there is a
      // selection
      {
        Point mouse = getImagePointFromPanelPoint(p.x, p.y);

        Rectangle r =
            new Rectangle(
                selectedPoint.x,
                selectedPoint.y,
                selectedImage.getWidth(null),
                selectedImage.getHeight(null));

        if (r.contains(mouse) && getCursor().getType() != Cursor.MOVE_CURSOR)
          setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        else if (!r.contains(mouse) && getCursor().getType() != Cursor.CROSSHAIR_CURSOR)
          setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
      }
    }
  }

  public void mouseDragged(Point p) {
    if (p == curMousePoint) return;

    if (curMousePoint == null) curMousePoint = p;

    // if (pressed)
    // { // dragged, should always be true
    if (selectedTool == PENCIL) {
      pencilDragged(p);
    } else if (selectedTool == ERASER) {
      int thickness = ((Integer) parent.thicknessSpinner.getValue()).intValue();
      Point from = getImagePointFromPanelPoint(p.x, p.y);
      Point to = new Point(from.x - thickness, from.y + thickness);

      Rectangle r = getRect(from, to);

      r.x += 1;
      createRectangle(r, backgroundColor);
    } else if (selectedTool == LINE || selectedTool == RECTANGLE || selectedTool == OVAL) {
      repaint();
    } else if (selectedTool == DROPPER) {
      Color c = getColorAt(getImagePointFromPanelPoint(curMousePoint.x, curMousePoint.y));

      boolean change = false;

      if (leftButton == true) {
        if (foregroundColor != c) change = true;

        foregroundColor = c;
        parent.foregroundColor = c;
      } else {
        if (backgroundColor != c) change = true;

        backgroundColor = c;
        parent.backgroundColor = c;
      }

      if (change) parent.drawSelectedColors(parent.getGraphics());
    } else if (selectedTool == SELECTION) {
      if (theOffset != null) {
        Point mouse = getImagePointFromPanelPoint(p.x, p.y);

        selectedPoint.x = initialSelection.x + mouse.x - theOffset.x;
        selectedPoint.y = initialSelection.y + mouse.y - theOffset.y;

        // debug("initial point = " + initialSelection);
        // debug("selected point = " + selectedPoint);
      }

      repaint();
    }

    // }

    curMousePoint = p;
  }
}
