/*
 * Created on Dec 29, 2004
 *
 */
package editor.asss;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

/**
 * The eLVL ATTR edtior Panel
 * 
 * @author baks
 */
public class ELvlAttrEditor extends JDialog implements ActionListener
{
	private Vector d;

	private JButton add = new JButton("Add New Row");

	private JButton remove = new JButton("Remove Selected Row");

	private JButton close = new JButton("Close Window");

	private JTable table;

	private static Vector colums = new Vector();

	static
	{
		colums.add("<key>");
		colums.add("<value>");
	}

	/**
	 * Make a new Dialog
	 * 
	 * @param parent
	 *            the parent frame
	 * @param data
	 *            the data to edit as a vector of Strings
	 */
	private ELvlAttrEditor(JFrame parent, Vector data)
	{
		super(parent, true);
		d = data;
		setTitle("eLVL ATTR Editor");
		setSize(370, 250);
		setLocation(parent.getX() + parent.getWidth() / 2 - getWidth() / 2,
				parent.getY() + parent.getHeight() / 2 - getHeight() / 2);

		table = new JTable(d, colums);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setPreferredScrollableViewportSize(new Dimension(330, 110));
		table.getColumnModel().getColumn(0).setPreferredWidth(110);
		table.getColumnModel().getColumn(1).setPreferredWidth(220);
		JScrollPane scrollPane = new JScrollPane(table);

		Container c = getContentPane();
		c.setLayout(null);

		scrollPane.setBounds(20, 30, 330, 125);
		c.add(scrollPane);

		add.setBounds(20, 5, 140, 20);
		c.add(add);
		add.addActionListener(this);

		remove.setBounds(185, 5, 140, 20);
		remove.addActionListener(this);
		c.add(remove);

		JLabel j = new JLabel("Make sure you press enter after any changes.");
		j.setBounds(20, 160, 330, 20);
		c.add(j);

		close.setBounds(100, 185, 170, 25);
		c.add(close);
		close.addActionListener(this);
	}

	/**
	 * Edit the elvl attributes stored in a vector
	 * 
	 * @param editThis
	 *            the vector of strings representing ATTR tags
	 * @param parent
	 *            the parent jframe for this modal dialog
	 */
	public static void editAttrs(Vector editThis, JFrame parent)
	{
		ELvlAttrEditor e = new ELvlAttrEditor(parent, editThis);
		e.setVisible(true);
	}

	// EVENTS

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == close)
			setVisible(false);
		else if (e.getSource() == add)
		{
			Vector row = new Vector();
			row.add("TAGSTRING");
			row.add("VALUESTRING");
			d.add(row);
			table.revalidate();
		}
		else if (e.getSource() == remove)
		{
			if (table.getRowCount() > 0)
			{
				int row = table.getSelectedRow();

				if (row != -1)
				{
					d.remove(row);
					table.revalidate();
				}
			}
		}
	}
}
