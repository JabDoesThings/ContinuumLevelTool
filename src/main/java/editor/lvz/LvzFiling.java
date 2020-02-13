// Stan Bak
// 5-31-04
// Filing class... static methods mostly for loading

package editor.lvz;

import java.util.*;
import java.io.*;
import javax.swing.*;

public class LvzFiling
{
	/**
	 * Extract a lvz file at the specified path
	 * 
	 * @param path
	 *            the path to extract the lvz from
	 * @return true iff sucessful
	 */
	static boolean debuildLvz(String path)
	{
		return ExtractLvz.DoExtractLvz(path);
	}

	/**
	 * Create a lvz file from an ini file
	 * 
	 * @param iniFile
	 *            the path to the ini file
	 * @return the iff sucressful
	 */
	public static int buildLvz(String iniFile)
	{ // this builds it to the local buildlevel directory
		return CreateLvz.DoCreateLvz(iniFile);
	}

	/**
	 * Copy a file from one place to another
	 * 
	 * @param from
	 *            the source to copy from
	 * @param to
	 *            the destination to copy to
	 * @return true iff the copy was successful (no io errors)
	 */
	static boolean copyFile(String from, String to)
	{
		boolean rv = true;

		if (from != to)
		{
			try
			{
				File f = new File(to).getParentFile();

				if (!f.exists())
				{
					if (!f.mkdirs())
						throw new IOException("mkdirs failed on "
								+ f.getAbsolutePath());
				}

				FileInputStream fis = new FileInputStream(from);
				FileOutputStream fos = new FileOutputStream(to);

				byte[] buffer = new byte[2048]; // read 2k at a time

				for (int read = fis.read(buffer); read != -1; read = fis
						.read(buffer))
				{
					fos.write(buffer);
				}

				fos.close();
				fis.close();
			}
			catch (IOException e)
			{
				System.out.println(e);
				rv = false;
			}
		}

		return rv;
	}

	static Vector loadImages(String iniFile) // returns a vector of LvzImages
												// or NULL if it fails
	{
		Vector v = new Vector();
		File f = new File(iniFile);
		String parent = f.getParent() + File.separator;

		try
		{
			FileReader fr = new FileReader(iniFile);
			BufferedReader br = new BufferedReader(fr);

			String s[];

			while (br.ready())
			{
				String line = br.readLine();
				if (line == null)
					break;

				if (line.equals("[mapobjects]"))
					break;

				// System.out.println("Line = " + line);
				if (line.length() > 5 && line.substring(0, 5).equals("IMAGE"))
				{
					// System.out.println("Line starting with IMAGE ->" + line);
					LvzImage l = new LvzImage();
					s = line.split("IMAGE");

					s = s[1].split("=");
					l.index = Integer.parseInt(s[0]);

					s = s[1].split(",");

					l.image = ImageLoader.loadImage(parent + s[0]);

					l.xFrames = Integer.parseInt(s[1]);
					l.yFrames = Integer.parseInt(s[2]);
					l.animationTime = Integer.parseInt(s[3]);

					v.add(l);
				}
			}
			br.close();
			return v;
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, e.toString());
			return null;
		}

	}

	static Vector loadMapObjects(String iniFile) // returns a vector of
													// MapObjects or NULL if it
													// fails
	{
		Vector v = new Vector();

		try
		{
			FileReader fr = new FileReader(iniFile);
			BufferedReader br = new BufferedReader(fr);

			String s[];
			boolean readHere = false;

			while (true)
			{

				String line = br.readLine();
				if (line == null)
					break;

				if (line.equals("[mapobjects]"))
				{
					readHere = true;
					continue;
				}

				if (!readHere)
					continue;

				if (line.equals("[screenobjects]"))
					break;

				// System.out.println("Line = " + line);
				if (line.length() > 1 && !line.substring(0, 1).equals(";"))
				{
					// System.out.println("Line starting with IMAGE ->" + line);
					MapObject m = new MapObject();

					s = line.split(",");

					if (s.length > 6)
					{

						m.x = Integer.parseInt(s[0].trim());

						m.y = Integer.parseInt(s[1].trim());

						m.imageIndex = Integer.parseInt(s[2].trim().substring(
								(5)));

						String layer = s[3].trim();
						for (int x = 0; x < MapObject.NUMLAYERS; ++x)
						{
							if (layer.equals(MapObject.layers[x]))
							{
								m.layer = x;
								break;
							}
						}

						String mode = s[4].trim();
						for (int x = 0; x < MapObject.NUMMODES; ++x)
						{
							if (mode.equals(MapObject.modes[x]))
							{
								m.mode = x;
								break;
							}
						}

						m.displayTime = Integer.parseInt(s[5].trim());

						m.id = Integer.parseInt(s[6].trim());

						v.add(m);
					}
					else
					{
						System.err
								.println("error, split resulted in < 7 substrings line ="
										+ line);
						System.exit(0);
					}
				}
			}

			br.close();

			return v;
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, e.toString());
			return null;
		}
	}
}