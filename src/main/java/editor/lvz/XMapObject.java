package editor.lvz;

/**
 * 
 * @author baks
 */
public class XMapObject
{
	int id;

	int x;

	int y;

	int imageNum;

	int layer;

	int time;

	int mode;

	public XMapObject()
	{
	}

	public String toString()
	{
		// <x coord>,<y coord>,<image>,<layer>,<mode>,<display time>,<object id>

		String imageString;
		if (imageNum < 10)
			imageString = "IMAGE" + imageNum + "  ";
		else if (imageNum < 100)
			imageString = "IMAGE" + imageNum + " ";
		else
			imageString = "IMAGE" + imageNum;

		return x + ",\t" + y + ",\t" + imageString + ",\t"
				+ ExtractLvz.layers[layer] + ",\t" + ExtractLvz.modes[mode]
				+ ",\t" + time + ",\t" + id;
	}
}
