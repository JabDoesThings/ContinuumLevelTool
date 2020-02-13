package editor.lvz;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class ProgressFrame
{
	public ProgressFrame(String title)
	{
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		theFrame = new JFrame(title);
		theFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		theFrame.setLocation((screenSize.width - WIDTH) / 2,
				(screenSize.height) / 2);
		theFrame.setSize(WIDTH, HEIGHT);

		theFrame.getContentPane().setLayout(null);

		progressLabel = new JLabel("", SwingConstants.CENTER);
		progressLabel.setBounds(10, HEIGHT / 2 - 35, WIDTH - 30, 25);
		theFrame.getContentPane().add(progressLabel);

		theFrame.show();
	}

	public void hide()
	{
		theFrame.hide();
	}

	public void setProgress(String s)
	{
		if (s.length() > 50)
			s = "..." + s.substring(s.length() - 50);

		Color bk = progressLabel.getBackground();
		Graphics g = progressLabel.getGraphics();

		progressLabel.setForeground(bk);
		progressLabel.update(g);

		progressLabel.setForeground(Color.black);
		progressLabel.setText(s);
		progressLabel.update(g);
	}

	public JFrame theFrame;

	public JLabel progressLabel;

	private final int HEIGHT = 80;

	private final int WIDTH = 400;
}