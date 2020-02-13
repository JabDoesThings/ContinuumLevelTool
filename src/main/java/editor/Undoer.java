/*
 * Created on Jul 22, 2005
 *
 */
package editor;

import java.util.LinkedList;
import java.util.Vector;

import editor.lvz.MapObject;

/**
 * Undoer project
 * 
 * @author baks
 */
public class Undoer
{
	final static int MAX_UNDO = 100;

	LinkedList undo = new LinkedList();

	LinkedList redo = new LinkedList();

	Undoer(LevelWindow lw)
	{
		setState(lw);
	}

	short[][] map = new short[1024][1024];

	Vector selection = new Vector();

	void setState(LevelWindow lw)
	{
		undo.clear();
		redo.clear();

		for (int y = 0; y < 1024; ++y)
			for (int x = 0; x < 1024; ++x)
			{
				map[y][x] = lw.m_parent.m_map[y][x];
			}

		selection = copyVecArray(lw.selection);

		lw.m_parent.m_main.editRedo.setEnabled(false);
		lw.m_parent.m_main.editUndo.setEnabled(false);
	}

	public void deleteMapObject(LevelWindow lw, MapObject o)
	{
		Step s = new Step();

		s.type = Step.TYPE_REMOVELVZ;
		s.data = new Vector();
		s.data.add(o);

		redo.clear();
		undo.add(s);

		showUndoHideRedo(lw);

		if (undo.size() > MAX_UNDO)
			undo.removeFirst();
	}

	public void addMapObject(LevelWindow lw, MapObject o)
	{
		Step s = new Step();

		s.type = Step.TYPE_ADDLVZ;
		s.data = new Vector();
		s.data.add(o);

		redo.clear();
		undo.add(s);

		showUndoHideRedo(lw);

		if (undo.size() > MAX_UNDO)
			undo.removeFirst();
	}

	public void setLvzProperties(LevelWindow lw, MapObject before, MapObject m)
	{
		Step s = new Step();
		s.type = Step.TYPE_SETLVZPROPERTIES;

		s.data = new Vector();
		s.data.add(before);
		s.data.add(m);

		redo.clear();
		undo.add(s);

		showUndoHideRedo(lw);

		if (undo.size() > MAX_UNDO)
			undo.removeFirst();
	}

	void setButtons(LevelWindow lw)
	{
		lw.m_parent.m_main.editRedo.setEnabled(redo.size() > 0);
		lw.m_parent.m_main.editUndo.setEnabled(undo.size() > 0);
	}

	void doUndo(LevelWindow lw)
	{
		if (undo.size() > 0)
		{
			Step s = (Step) undo.removeLast();

			s.undo(lw);

			redo.add(s);
			lw.m_parent.m_main.editRedo.setEnabled(true);

			if (undo.size() == 0)
				lw.m_parent.m_main.editUndo.setEnabled(false);
		}
		else
		{
			lw.m_parent.m_main.editUndo.setEnabled(false);
		}
	}

	void doRedo(LevelWindow lw)
	{
		if (redo.size() > 0)
		{
			Step s = (Step) redo.removeLast();

			s.redo(lw);

			undo.add(s);
			lw.m_parent.m_main.editUndo.setEnabled(true);

			if (redo.size() == 0)
				lw.m_parent.m_main.editRedo.setEnabled(false);

		}
		else
		{
			lw.m_parent.m_main.editRedo.setEnabled(false);
		}
	}

	void showUndoHideRedo(LevelWindow lw)
	{
		lw.m_parent.m_main.editRedo.setEnabled(false);
		lw.m_parent.m_main.editUndo.setEnabled(true);
	}

	void snapShot(LevelWindow lw)
	{
		// check for changes in the map
		Step s = null;
		Step t = null;

		Vector changes = new Vector();
		for (short y = 0; y < 1024; ++y)
			for (short x = 0; x < 1024; ++x)
			{
				if (map[y][x] != lw.m_parent.m_map[y][x])
				{
					TileChange tc = new TileChange();

					tc.newTile = lw.m_parent.m_map[y][x];
					tc.oldTile = map[y][x];
					tc.x = x;
					tc.y = y;

					changes.add(tc);

					// update the state
					map[y][x] = lw.m_parent.m_map[y][x];
				}
			}

		if (changes.size() > 0)
		{
			s = new Step();

			s.type = Step.TYPE_SETTILES;
			s.data = changes;
		}

		// check for changes in the selection
		if (!isSameVecArray(lw.selection, selection))
		{
			if (s == null)
			{
				s = new Step();
				s.type = Step.TYPE_SETSELECTION;
				s.data = copyVecArray(selection);
			}
			else
			{
				t = new Step();
				t.type = Step.TYPE_SETSELECTION;
				t.data = copyVecArray(selection);
			}

			// update the state
			selection = copyVecArray(lw.selection);
		}

		if (s != null && t == null)
		{
			redo.clear();
			undo.add(s);

			if (undo.size() > MAX_UNDO)
				undo.removeFirst();

			showUndoHideRedo(lw);
		}
		else if (s != null && t != null)
		{ // dual state save
			Vector v = new Vector();

			v.add(s);
			v.add(t);

			Step r = new Step();
			r.type = Step.TYPE_MULTIPLE;
			r.data = v;

			redo.clear();
			undo.add(r);

			if (undo.size() > MAX_UNDO)
				undo.removeFirst();

			showUndoHideRedo(lw);
		}
	}

	static boolean isSameVecArray(Vector one, Vector two)
	{
		boolean rv = true;

		if (one == null)
			one = new Vector();

		if (two == null)
			two = new Vector();

		if (one.size() != two.size())
		{
			rv = false;
		}
		else
		{
			for (int x = 0; x < one.size() && rv; ++x)
			{
				Vector v1 = (Vector) one.get(x);
				Vector v2 = (Vector) two.get(x);

				if (v1.size() != v2.size())
				{
					rv = false;
					break;
				}
				else
				{
					for (int y = 0; y < v1.size(); ++y)
					{
						int a = ((Integer) v1.get(y)).intValue();
						int b = ((Integer) v2.get(y)).intValue();

						if (a != b)
						{
							rv = false;
							break;
						}
					}
				}
			}
		}

		return rv;
	}

	static Vector copyVecArray(Vector from)
	{
		int w = 0;

		Vector to = new Vector();

		if (from != null)
		{
			w = from.size();

			for (int x = 0; x < w; ++x)
			{
				Vector v = (Vector) from.get(x);
				Vector nv = new Vector();

				for (int y = 0; y < v.size(); ++y)
				{
					int i = ((Integer) v.get(y)).intValue();

					nv.add(new Integer(i));
				}

				to.add(nv);
			}
		}

		return to;
	}

	class Step
	{
		int type;

		static final int TYPE_SETSELECTION = 0;

		static final int TYPE_SETTILES = 1;

		static final int TYPE_MULTIPLE = 2;

		static final int TYPE_ADDLVZ = 3;

		static final int TYPE_REMOVELVZ = 4;

		static final int TYPE_SETLVZPROPERTIES = 5;

		Vector data;

		void undo(LevelWindow lw)
		{
			if (type == TYPE_SETTILES)
			{
				for (int x = 0; x < data.size(); ++x)
				{
					TileChange tc = (TileChange) data.get(x);

					lw.m_parent.m_map[tc.y][tc.x] = tc.oldTile;

					map[tc.y][tc.x] = tc.oldTile;
				}
			}
			else if (type == TYPE_SETSELECTION)
			{
				Vector d = copyVecArray(lw.selection);
				lw.selection = copyVecArray(data);
				selection = copyVecArray(data);

				lw.width = data.size();

				if (lw.width > 0)
					lw.height = ((Vector) data.get(0)).size();
				else
					lw.height = 0;

				data = d;
			}
			else if (type == TYPE_ADDLVZ)
			{
				MapObject o = (MapObject) data.get(0);

				lw.m_lvzImages.deleteMapObject(o);
			}
			else if (type == TYPE_REMOVELVZ)
			{
				MapObject o = (MapObject) data.get(0);

				lw.m_lvzImages.addMapObject(o);
			}
			else if (type == TYPE_SETLVZPROPERTIES)
			{
				MapObject before = (MapObject) data.get(0);
				MapObject after = (MapObject) data.get(1);

				MapObject afterClone = after.getCopy();

				after.displayTime = before.displayTime;
				after.id = before.id;
				after.imageIndex = before.imageIndex;
				after.layer = before.layer;
				after.mode = before.mode;
				after.x = before.x;
				after.y = before.y;

				data.clear();

				data.add(afterClone);
				data.add(after);

				lw.m_lvzImages.selectedMO.clear();
				lw.m_lvzImages.repaint();
			}
			else if (type == TYPE_MULTIPLE)
			{
				for (int x = 0; x < data.size(); ++x)
				{
					Step s = (Step) data.get(x);
					s.undo(lw);
				}
			}

			lw.repaint();
			lw.m_radar.repaintRadar();
		}

		void redo(LevelWindow lw)
		{
			if (type == TYPE_SETTILES)
			{
				for (int x = 0; x < data.size(); ++x)
				{
					TileChange tc = (TileChange) data.get(x);

					lw.m_parent.m_map[tc.y][tc.x] = tc.newTile;

					map[tc.y][tc.x] = tc.newTile;
				}
			}
			else if (type == TYPE_ADDLVZ)
			{
				MapObject o = (MapObject) data.get(0);

				lw.m_lvzImages.addMapObject(o);
			}
			else if (type == TYPE_REMOVELVZ)
			{
				MapObject o = (MapObject) data.get(0);

				lw.m_lvzImages.deleteMapObject(o);
			}
			else if (type == TYPE_SETSELECTION)
			{
				Vector d = copyVecArray(lw.selection);

				lw.selection = copyVecArray(data);

				selection = copyVecArray(data);

				lw.width = data.size();

				if (lw.width > 0)
					lw.height = ((Vector) data.get(0)).size();
				else
					lw.height = 0;

				data = d;
			}
			else if (type == TYPE_SETLVZPROPERTIES)
			{
				MapObject before = (MapObject) data.get(0);
				MapObject after = (MapObject) data.get(1);

				MapObject afterClone = after.getCopy();

				after.displayTime = before.displayTime;
				after.id = before.id;
				after.imageIndex = before.imageIndex;
				after.layer = before.layer;
				after.mode = before.mode;
				after.x = before.x;
				after.y = before.y;

				data.clear();

				data.add(afterClone);
				data.add(after);

				lw.m_lvzImages.selectedMO.clear();
				lw.m_lvzImages.repaint();
			}
			else if (type == TYPE_MULTIPLE)
			{
				for (int x = 0; x < data.size(); ++x)
				{
					Step s = (Step) data.get(x);
					s.redo(lw);
				}
			}

			lw.repaint();
			lw.m_radar.repaintRadar();
		}
	}

	class TileChange
	{
		short x, y;

		short oldTile, newTile;
	}

}
