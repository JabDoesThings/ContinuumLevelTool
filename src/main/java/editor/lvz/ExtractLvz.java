package editor.lvz;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/*
 * Created on Jan 5, 2005
 * by Bak-
 * 
 */

/**
 * Extact Lvz, extracts LVZ files like debuildlevel
 * 
 * @author bak-
 */
public class ExtractLvz
{
	final static FileDialog fc = new FileDialog(new Frame());

	final static String VERSION = "CLLT 1.0";

	final static String INI_HEADER = "; Created by an ExtractLvz modification by BaK- version "
			+ VERSION;

	// error codes
	final static int ERROR_NONE = 0;

	final static int ERROR_NOPATH = 1;

	final static int ERROR_IOERROR = 2;

	final static int ERROR_HEADER = 3;

	final static int ERROR_COMPRESSEDHEADER = 4;

	final static int ERROR_DATAFORMATERROR = 5;

	final static int ERROR_OBJHEADER = 6;

	final static int ERROR_INI_DATA_NOT_LAST = 7;

	final static int ERROR_TOO_SHORT_INI_SECTION = 8;

	final static int ERROR_INVALID_SCREEN_POSITION = 9;

	final static int OBJTYPE_CLV1 = 0;

	final static int OBJTYPE_CLV2 = 1;

	final static int OBJTYPE_ERROR = 2;

	// error strings
	public final static String[] errors = {
			null,
			null,
			null,
			"No .lvz header detected.",
			"Incorrect compressed header.",
			null,
			"Unsupported Object Section header.",
			"Ini data came before File data was done.",
			"Ini data didn't contain enough bytes to describe itself completely.",
			"There is an invalid screen mode encoded in the .lvz file."

	};

	public final static String[] layers = { "BelowAll        ",
			"AfterBackground ", "AfterTiles      ", "AfterWeapons    ",
			"AfterShips      ", "AfterGauges     ", "AfterChat       ",
			"TopMost         " };

	public final static String[] modes = { "ShowAlways      ",
			"EnterZone       ", "EnterArena      ", "Kill            ",
			"Death           ", "ServerControlled" };

	public final static String[] screenObjOffsets = { "", "C", "B", "S", "G",
			"F", "E", "T", "R", "O", "W", "V" };

	final static int MAX_SCREEN_OBJ_OFFSET = 11; // anything > 11 = error

	private static String destDir = null;

	private static ArrayList restOfIni = null; // after outfile and file=
												// sections

	public static boolean DoExtractLvz(String path)
	{
		destDir = null;
		restOfIni = new ArrayList();

		int rv = ERROR_NONE;

		byte[] dword = new byte[4];
		boolean ok;

		try
		{
			File f = new File(path);

			BufferedInputStream bi = new BufferedInputStream(
					new FileInputStream(f));

			bi.read(dword, 0, 4);

			ok = checkHeader(dword);
			if (!ok)
			{
				rv = ERROR_HEADER;
			}
			else
			{
				int count, decompressedSize, compressedSize, nextLetter;
				long fileTime;
				String name;
				byte[] data, result;
				boolean aFile;
				String lvzName = getLvzName(f);

				// make the directory
				makeDir(f);

				// prefix a ! to make it first in alphabetical order in the
				// directory
				String iniLocation = destDir + lvzName + ".ini";

				BufferedOutputStream iniOut = new BufferedOutputStream(
						new FileOutputStream(new File(iniLocation)));

				writeLine(iniOut, INI_HEADER);
				writeLine(iniOut, "; Created on "
						+ new GregorianCalendar().getTime());
				writeLine(iniOut, "");
				writeLine(iniOut, "Outfile=" + lvzName + ".lvz");
				writeLine(iniOut, "");

				Inflater decompresser = new Inflater();

				bi.read(dword, 0, 4);
				count = toInt(dword);

				for (int x = 0; x < count && rv == ERROR_NONE; ++x)
				{ // for each compressed section
					bi.read(dword, 0, 4);
					ok = checkHeader(dword);

					if (!ok)
						rv = ERROR_COMPRESSEDHEADER;

					bi.read(dword, 0, 4);
					decompressedSize = toInt(dword);

					bi.read(dword, 0, 4);
					fileTime = toInt(dword);

					bi.read(dword, 0, 4);
					compressedSize = toInt(dword);

					name = "";
					nextLetter = bi.read();
					while (nextLetter > 0)
					{
						name += (char) nextLetter;
						nextLetter = bi.read();
					}

					aFile = !(name.length() == 0 && fileTime == 0);

					data = new byte[compressedSize];
					bi.read(data, 0, compressedSize);

					if (compressedSize != decompressedSize)
					{ // is it compressed?
						decompresser.setInput(data, 0, compressedSize);
						result = new byte[decompressedSize];
						decompresser.inflate(result);
						decompresser.reset();
					}
					else
						result = data;

					if (aFile)
					{
						// add it to the INI
						writeLine(iniOut, "File=" + name);

						File out = new File(destDir + name);

						BufferedOutputStream bos = new BufferedOutputStream(
								new FileOutputStream(out));

						bos.write(result);
						bos.close();

						if (fileTime > 0)
							out.setLastModified(fileTime * 1000);
					}
					else
					// ini part
					{
						rv = doIni(result);
					}

				}

				for (int x = 0; x < restOfIni.size(); ++x)
				{
					String line = (String) restOfIni.get(x);

					writeLine(iniOut, line);
				}

				decompresser.end();
				iniOut.close();
			}

			bi.close();
		}
		catch (IOException e)
		{
			rv = ERROR_IOERROR;
		}
		catch (DataFormatException e)
		{
			rv = ERROR_DATAFORMATERROR;
		}

		return (rv == ERROR_NONE);
	}

	/**
	 * print the hex for the requested part
	 * 
	 * @param data
	 *            the byte[] to print
	 * @param offset
	 *            the offset to start at
	 * @param len
	 *            the length to print
	 */
	private static void printHex(byte[] data, int offset, int len)
	{
		int last = offset + len;
		for (int x = offset; x < last; ++x)
		{
			String hex = Integer.toHexString(0x0FF & data[x]);
			if (hex.length() < 2)
				hex = "0" + hex;
			System.out.print(hex + " ");
		}

		System.out.println();
	}

	/**
	 * Write the main part of the ini, after all the file='s have been written
	 * 
	 * @param data
	 *            the uncompressed data for this section
	 * @return an error code from this operation
	 */
	private static int doIni(byte[] data)
	{
		int rv = ERROR_NONE;
		int size = data.length;

		if (size < 12)
			rv = ERROR_TOO_SHORT_INI_SECTION;
		else
		{
			int format = checkObjectSectionHeader(data);

			int objectCount = toInt(data, 4);
			int imageCount = toInt(data, 8);

			int offset = 12;

			if (format == OBJTYPE_ERROR)
				rv = ERROR_OBJHEADER;
			else
			{
				ArrayList images = new ArrayList();
				ArrayList mapobjs = new ArrayList();
				ArrayList screenobjs = new ArrayList();

				// read level objects
				for (int x = 0; x < objectCount && rv == ERROR_NONE; ++x)
				{
					if (offset + 10 > size)
					{
						rv = ERROR_TOO_SHORT_INI_SECTION;
						break;
					}

					boolean mapObject = (data[offset] & 0x01) == 1;

					if (mapObject || format == OBJTYPE_CLV1)
					{
						/*
						 * 0: [low 7 bits id] [1 bit mapobject] 1: [high 8 bits
						 * id] 2: [low 8 bits x] 3: [high 8 bits x] 4: [low 8
						 * bits y] 5: [high 8 bits y] 6: [8 bits image num] 7:
						 * [8 bits layer] 8: [low 8 bits display time] 9: [4
						 * bits mode] [high 4 bits display time]
						 * 
						 * display time is stored as 1/10 seconds... but written
						 * in the ini file as 1/100th ... dumb
						 * 
						 */
						XMapObject mo = new XMapObject();

						mo.id = (toInt(data[offset + 1]) << 7)
								| (toInt(data[offset]) >> 1);

						mo.x = readShort(data, offset + 2);
						mo.y = readShort(data, offset + 4);

						mo.imageNum = toInt(data[offset + 6]);
						mo.layer = toInt(data[offset + 7]);

						mo.time = 10 * (toInt(data[offset + 8]) | ((data[offset + 9] & 0x0F) << 8));
						mo.mode = (data[offset + 9] & 0x0F0) >> 4;

						if (mapObject)
							mapobjs.add(mo);
						else
							screenobjs.add(mo);
					}
					else
					{
						XScreenObject so = new XScreenObject();
						/*
						 * 0: [low 7 bits id] [1 bit mapobject] 1: [high 8 bits
						 * id] 2: [low 4 bits x_coord] [4 bits x_type] 3: [high
						 * 8 bits x_coord] 4: [low 4 bits x_coord] [ 4 bits
						 * x_type] 5: [high 8 bits y_coord] 6: [8 bits image
						 * num] 7: [8 bits layer] 8: [low 8 bits display time]
						 * 9: [4 bits mode] [high 4 bits display time]
						 * 
						 * display time is stored as 1/10 seconds... but written
						 * in the ini file as 1/100th ... dumb
						 */

						so.id = (toInt(data[offset + 1]) << 7)
								| (toInt(data[offset]) >> 1);

						so.x_type = (data[offset + 2] & 0x0F);
						so.x_coord = ((data[offset + 2] & 0x0F0) >> 4)
								| (data[offset + 3] << 4);

						so.y_type = (data[offset + 4] & 0x0F);
						so.y_coord = ((data[offset + 4] & 0x0F0) >> 4)
								| (data[offset + 5] << 4);

						so.imageNum = toInt(data[offset + 6]);
						so.layer = toInt(data[offset + 7]);

						so.time = 10 * (toInt(data[offset + 8]) | ((data[offset + 9] & 0x0F) << 8));
						so.mode = (data[offset + 9] & 0x0F0) >> 4;

						if (so.x_type > MAX_SCREEN_OBJ_OFFSET
								|| so.y_type > MAX_SCREEN_OBJ_OFFSET)
						{
							rv = ERROR_INVALID_SCREEN_POSITION;
							break;
						}

						screenobjs.add(so);
					}

					offset += 10;
				}

				// read image objects
				for (int x = 0; x < imageCount && rv == ERROR_NONE; ++x)
				{
					/*
					 * i16 X Count i16 Y Count i16 Display Time Null-end str
					 * File Name
					 */

					XImageObject io = new XImageObject();

					io.num = x;

					if (offset + 6 > size)
					{
						rv = ERROR_TOO_SHORT_INI_SECTION;
						break;
					}

					io.x = readShort(data, offset);
					io.y = readShort(data, offset + 2);
					io.time = (readShort(data, offset + 4) & 0x0000FFFF);
					offset += 6;
					io.name = readNullTerminatedString(data, offset);

					offset += io.name.length() + 1; // +1 cause it's null
													// terminated

					images.add(io);
				}

				if (images.size() > 0)
				{
					restOfIni.add("");
					restOfIni.add("[objectimages]");
					restOfIni
							.add("; IMAGE<number>=<filename>, <x tiles>, <y tiles>, <anim period>");
					for (int x = 0; x < images.size(); ++x)
					{
						restOfIni.add(images.get(x).toString());
					}
				}

				if (mapobjs.size() > 0)
				{
					restOfIni.add("");
					restOfIni.add("[mapobjects]");
					restOfIni
							.add(";<X>, <Y>, <Image Id>, <Layer>, <Mode>, <Display Time (1/100th seconds)>, <Object Id>");
					for (int x = 0; x < mapobjs.size(); ++x)
					{
						restOfIni.add(mapobjs.get(x).toString());
					}
				}

				if (screenobjs.size() > 0)
				{
					restOfIni.add("");
					restOfIni.add("[screenobjects]");
					restOfIni
							.add(";<X>, <Y>, <Image Id>, <Layer>, <Mode>, <Display Time (1/100th seconds)>, <Object Id>");
					for (int x = 0; x < screenobjs.size(); ++x)
					{
						restOfIni.add(screenobjs.get(x).toString());
					}
				}
			}
		}

		return rv;
	}

	/**
	 * Write this string to this buffedoutputstream
	 * 
	 * @param out
	 *            the stream to write to
	 * @param s
	 *            the string to write
	 * @throws IOException
	 *             if there's an io error
	 */
	private static void writeLine(BufferedOutputStream out, String s)
			throws IOException
	{
		if (s.length() > 0)
		{
			byte[] writeMe = s.getBytes("UTF-8");
			out.write(writeMe);
		}

		// use windows newlines
		out.write(13);
		out.write(10);
	}

	/**
	 * Get the name of this Lvz File: so like if f points to "c:\mylvz.lvz" we'd
	 * return "mylvz"
	 * 
	 * @param f
	 *            the file of the .lvz file
	 * @return the name of the .lvz file
	 */
	private static String getLvzName(File f)
	{
		String name = f.getName();

		int substringLength = name.length() - 4;
		if (substringLength < 0)
			substringLength += 4;

		return name.substring(0, substringLength);
	}

	/**
	 * Make a destination directory for this lvz file and set destDir
	 * 
	 * Thx Dr. Brain for a bug fix in this function.
	 * 
	 * @param f
	 *            the lvz file
	 */
	private static void makeDir(File f)
	{
		String name = getLvzName(f);
		String parent = f.getParent();

		if (parent != null)
			destDir = parent + File.separator;

		destDir += name + File.separator;

		new File(destDir).mkdir();
	}

	/**
	 * Read a string from a byte[]
	 * 
	 * @param data
	 *            the byte[] to read from
	 * @param index
	 *            the index to start
	 * @param length
	 *            the length to use
	 * @return the String extracted
	 */
	private static String readString(byte[] data, int index, int length)
	{
		char[] charArray = new char[length];

		for (int i = 0; i < length; i++)
		{
			charArray[i] = (char) (data[index + i]);
		}

		return new String(charArray).trim();
	}

	/**
	 * Read a null terminated String in
	 * 
	 * @param index
	 *            the index to start
	 * @return a null terminated String starting at index
	 */
	private static String readNullTerminatedString(byte[] data, int index)
	{
		int i = 0;
		int len = data.length;

		while (index + i < len && data[index + i] != '\0')
		{
			i++;
		}

		return readString(data, index, i);
	}

	/**
	 * byte to unsigned int
	 * 
	 * @param b
	 *            the byte to convert
	 * @return the unisgned int representation
	 */
	private static int toInt(byte b)
	{
		return b & 0x0FF;
	}

	/**
	 * Extact a little endian int from this array at this offset
	 * 
	 * @param array
	 *            the array to extact from
	 * @param offeset
	 *            the offset where we're extracting
	 * @return the int extacted
	 */
	private static int toInt(byte[] array, int offset)
	{
		return (int) (((array[offset + 3] & 0xff) << 24)
				| ((array[offset + 2] & 0xff) << 16)
				| ((array[offset + 1] & 0xff) << 8) | ((array[offset] & 0xff)));

	}

	/**
	 * Extact a little endian int from this dword
	 * 
	 * @param dword
	 *            the encoded int
	 * @return the int extacted
	 */
	private static int toInt(byte[] dword)
	{
		return (int) (((dword[3] & 0xff) << 24) | ((dword[2] & 0xff) << 16)
				| ((dword[1] & 0xff) << 8) | ((dword[0] & 0xff)));

	}

	/**
	 * Extact a little endian short from this dword
	 * 
	 * @param word
	 *            the encoded int
	 * @param index
	 *            the index to read at
	 * @return the short extacted
	 */
	private static short readShort(byte[] word, int index)
	{
		return (short) (((word[index + 1] & 0xff) << 8) | ((word[index] & 0xff)));
	}

	/**
	 * check if the header == CONT
	 * 
	 * @param dword
	 *            the header byte[], 4 bytes long
	 * @return true iff the header is correct
	 */
	private static boolean checkHeader(byte[] dword)
	{
		boolean rv = false;
		if (dword[0] == 'C' && dword[1] == 'O' && dword[2] == 'N'
				&& dword[3] == 'T')
			rv = true;

		return rv;
	}

	/**
	 * check if the header == CLV1 or CLV2
	 * 
	 * @param dword
	 *            byte[] header is in, at least 4 bytes long
	 * @return OBJTYPE_CLV1 or OBJTYPE_CLV2 or OBJTYPE_ERROR
	 */
	private static int checkObjectSectionHeader(byte[] dword)
	{
		int rv = OBJTYPE_ERROR;

		if (dword[0] == 'C' && dword[1] == 'L' && dword[2] == 'V')
		{
			if (dword[3] == '1')
				rv = OBJTYPE_CLV1;
			else if (dword[3] == '2')
				rv = OBJTYPE_CLV2;
		}

		return rv;
	}

	/**
	 * Get the path of a file the user selects
	 * 
	 * @return a file path or null
	 */
	private static String getPath()
	{
		fc.setMode(FileDialog.LOAD);
		fc.setTitle("Select LVZ to Extract - Version " + VERSION);
		fc.show();
		String filename = fc.getFile();
		String dir = fc.getDirectory();

		return (filename == null ? null : dir + filename);
	}

	/**
	 * get the bit fragment from startIndex to endIndex
	 * 
	 * @param extractFrom
	 *            the byte to extract from
	 * @param startIndex
	 *            the inclusive leftbound index: 1234 5678
	 * @param endIndex
	 *            the inclusive rightbound index 1234 5678 and > startIndex
	 * @return the int extracted from the requested bits
	 */
	private static int getBitFragment(byte extractFrom, int startIndex,
			int endIndex)
	{
		int shift = 8 - endIndex;
		int numBits = endIndex - startIndex + 1;
		byte mask = (byte) ((0x01 << numBits) - 1);

		return (extractFrom >> shift) & mask;
	}
}
