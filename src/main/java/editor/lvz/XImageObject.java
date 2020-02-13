package editor.lvz;

/**
 * 
 * @author baks
 */
public class XImageObject
{
	int num;

	int x;

	int y;

	int time;

	String name;

	public XImageObject()
	{
	}

	public String toString()
	{
		// IMAGE<number>=<filename>,<x tiles>,<y tiles>,<anim period>
		String s = "IMAGE" + num + "=" + name + "," + x + "," + y + "," + time;

		return s;
	}
}
