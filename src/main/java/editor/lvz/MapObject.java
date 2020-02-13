//Stan Bak
//5-31-04
//lvzimage.java
//stores information for each instance of a map object

package editor.lvz;

public class MapObject
{
	public static int BELOWALL = 0;

	public static int AFTERBACKGROUND = 1;

	public static int AFTERTILES = 2;

	public static int AFTERWEAPONS = 3;

	public static int AFTERSHIPS = 4;

	public static int AFTERGAUGES = 5;

	public static int AFTERCHAT = 6;

	public static int TOPMOST = 7;

	public static String layers[] = new String[] { "BelowAll",
			"AfterBackground", "AfterTiles", "AfterWeapons", "AfterShips",
			"AfterGauges", "AfterChat", "TopMost" };

	public static int NUMLAYERS = 8;

	public static int SHOWALWAYS = 0;

	public static int ENTERZONE = 1;

	public static int ENTERARENA = 2;

	public static int KILL = 3;

	public static int DEATH = 4;

	public static int SERVERCONTROLLED = 5;

	public static String modes[] = new String[] { "ShowAlways", "EnterZone",
			"EnterArena", "Kill", "Death", "ServerControlled" };

	public static int NUMMODES = 6;

	public int x;

	public int y;

	public int imageIndex;

	public int layer;

	public int mode;

	public int displayTime; // centiseconds

	public int id;

	public MapObject getCopy()
	{
		MapObject m = new MapObject();

		m.displayTime = displayTime;
		m.id = id;
		m.imageIndex = imageIndex;
		m.layer = layer;
		m.mode = mode;
		m.x = x;
		m.y = y;

		return m;
	}

	public boolean equals(MapObject m)
	{
		return (m.displayTime == displayTime && m.id == id
				&& m.imageIndex == imageIndex && m.layer == layer
				&& m.mode == mode && m.x == x && m.y == y);
	}
}
