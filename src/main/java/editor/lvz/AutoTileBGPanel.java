package editor.lvz;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

public class AutoTileBGPanel extends JPanel implements ActionListener
{

	LvzImageWindow parent;

	// Autotile Panel
	JLabel l_xstart;

	JLabel l_ystart;

	JLabel l_xend;

	JLabel l_yend;

	JSpinner s_xstart;

	JSpinner s_ystart;

	JSpinner s_xend;

	JSpinner s_yend;

	JButton tileImages;

	JProgressBar bar;

	// END autotile

	private static final long serialVersionUID = -7185925193496392968L;

	AutoTileBGPanel(LvzImageWindow window)
	{
		parent = window;

		setLayout(null);

		// X Start
		l_xstart = new JLabel("X Start:", SwingConstants.LEFT);
		l_xstart.setBounds(5, 120, 50, 20); // x start, y start, x end, y end?
		add(l_xstart);

		s_xstart = new JSpinner(new SpinnerNumberModel(0, 0, 1023, 1));
		s_xstart.setBounds(45, 120, 50, 20);
		add(s_xstart);

		// Y Start
		l_ystart = new JLabel("Y Start:", SwingConstants.LEFT);
		l_ystart.setBounds(5, 140, 50, 20); // x start, y start, x end, y end?
		add(l_ystart);

		s_ystart = new JSpinner(new SpinnerNumberModel(0, 0, 1023, 1));
		s_ystart.setBounds(45, 140, 50, 20);
		add(s_ystart);

		// X END
		l_xend = new JLabel("X End:", SwingConstants.LEFT);
		l_xend.setBounds(105, 120, 50, 20); // x start, y start, x end, y end?
		add(l_xend);

		s_xend = new JSpinner(new SpinnerNumberModel(1023, 0, 1023, 1));
		s_xend.setBounds(140, 120, 50, 20);
		add(s_xend);

		// Y End
		l_yend = new JLabel("Y End:", SwingConstants.LEFT);
		l_yend.setBounds(105, 140, 50, 20); // x start, y start, x end, y end?
		add(l_yend);

		s_yend = new JSpinner(new SpinnerNumberModel(1023, 0, 1023, 1));
		s_yend.setBounds(140, 140, 50, 20);
		add(s_yend);

		tileImages = new JButton("Tile Image");
		tileImages.setBounds(230, 115, 120, 25);
		tileImages.addActionListener(this);
		add(tileImages);

		bar = new JProgressBar(0, 100);
		bar.setBounds(230, 150, 120, 25);
		bar.setVisible(false);
		add(bar);
	}

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		// draw selected image
		Image i = parent.getSelectedImage();

		if (i != null)
		{
			g.drawImage(i, 150, 10, 250, 110, 0, 0, i.getWidth(null), i
					.getHeight(null), null);
		}
		else
		{
			g.drawString("No Image Selected!", 100, 50);
		}

	}

	public void actionPerformed(ActionEvent e)
	{
		// check for user error
		int startX = ((Number) s_xstart.getValue()).intValue() * 16;
		int startY = ((Number) s_ystart.getValue()).intValue() * 16;
		int endX = ((Number) s_xend.getValue()).intValue() * 16;
		int endY = ((Number) s_yend.getValue()).intValue() * 16;
		Image i = parent.getSelectedImage();

		if (startX > endX || startY > endY)
		{
			JOptionPane.showMessageDialog(null,
					"Your start x/y is more than your end x/y.");
		}
		else if (i == null)
		{
			JOptionPane.showMessageDialog(null,
					"You must select and image in the Image tab.");
		}
		else
		{
			int w = i.getWidth(null);
			int h = i.getHeight(null);
			int countX = startX;
			int initStartY = startY;

			bar.setVisible(true);

			for (; startY <= endY; startY += h)
			{
				countX = startX;
				int bottom = (endY - initStartY);
				
				
				bar.setValue(100 * (startY - initStartY) / (bottom != 0 ? bottom : 1));
				bar.update(bar.getGraphics());

				for (; countX <= endX; countX += w)
				{
					parent.addMapObject(countX, startY);
				}
			}
		}
		bar.setVisible(false);

	}
}
