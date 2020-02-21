package editor.lvz;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.Deflater;

/*
 * Created on Jan 6, 2005
 * By BaK-
 */

/**
 * Create Lvz, makes an lvz file like buildlevel
 *
 * @author bak-
 */
public class CreateLvz {
  static final String VERSION = "1.02";

  static final FileDialog fc = new FileDialog(new Frame());

  // error codes
  static final int ERROR_NONE = 0;

  static final int ERROR_NOPATH = 1;

  static final int ERROR_IOERROR = 2;

  static final int ERROR_OUTFILE_NOT_ONE_EQUALS_SIGN = 3;

  static final int ERROR_MULTIPLE_OUTFILES = 4;

  static final int ERROR_OUTFILE_NOT_FIRST = 5;

  static final int ERROR_FILE_BAD_HEAD = 6;

  static final int ERROR_FILE_MUST_HAVE_ONE_EQUALS = 7;

  static final int ERROR_FILE_DOESNT_EXIST = 8;

  static final int ERROR_IMAGE_BAD_START = 9;

  static final int ERROR_IMAGE_NEED_ONE_EQUALS = 10;

  static final int ERROR_IMAGE_TOO_MANY_COMMAS = 11;

  static final int ERROR_PARSE_INT = 12;

  static final int ERROR_RANGE = 13;

  static final int ERROR_PRECISION = 14;

  static final int ERROR_MAPOBJECT_NOT_ENOUGH_SECTIONS = 15;

  static final int ERROR_MAPOBJECT_TOO_MANY_SECTIONS = 16;

  static final int ERROR_UNKNOWN_LAYER = 17;

  static final int ERROR_UNKNOWN_MODE = 18;

  static final int ERROR_SCREENOBJECT_NOT_ENOUGH_SECTIONS = 19;

  static final int ERROR_SCREENOBJECT_TOO_MANY_SECTIONS = 20;

  static final int ERROR_TOO_BIG_LVZ = 21;

  static final int ERROR_BADIMAGENUM = 22;

  // error strings
  public static final String[] errors = {
    null,
    null,
    null,
    "Your OutFile line does not contain exactly one equals(=) sign.",
    "Your ini file contains multiple OutFile lines.",
    "Your ini file contains a noncomment before the OutFile line.",
    "A line was expected to start with File=, but it didn't",
    "Your File line must have exactly one = sign",
    "A file in your File= section coudln't be found in the same directory as the ini file",
    "Your IMAGE# definition must start with 'IMAGE'",
    "Your IMAGE= line must have exactly one = sign",
    "Your IMAGE= line has too many commas in it to be a legal entry",
    "Error parsing your numbers into integers",
    "One or more of your values is out of the allowed range",
    "One or more of your values can not be stored with the desired precision",
    "An entry in your mapobjects section must have at least three (x,y,imagenum) sections",
    "An entry in your mapobjects section has too many sections",
    "Your layer is not a valid one",
    "Your mode is not a valid one",
    "An entry in your screenobjects section must have at least three (x,y,imagenum) sections",
    "An entry in your screenobjects section has too many sections",
    "Your LVZ file was created but the LVZ file is > 4 megs in size, Continuum only downloads lvz that"
        + " are less than 4 megabytes.\n"
        + "Perhaps you should split up your lvz file if you intend Continuum clients to download it",
    "You have an IMAGE# line where # doesnt' coorespond to an image in your [objectimages] section"
  };

  public static final String[] layers = {
    "belowall",
    "afterbackground",
    "aftertiles",
    "afterweapons",
    "afterships",
    "aftergauges",
    "afterchat",
    "topmost"
  };

  public static final String[] modes = {
    "showalways", "enterzone", "enterarena", "kill", "death", "servercontrolled"
  };

  public static final String[] screenObjOffsets = {
    "", "c", "b", "s", "g", "f", "e", "t", "r", "o", "w", "v"
  };

  static final int MAX_SCREEN_OBJ_OFFSET = 11; // anything > 11 = error

  // contants
  private static final int MODE_FILES = 0;

  private static final int MODE_IMAGEOBJECTS = 1;

  private static final int MODE_MAPOBJECTS = 2;

  private static final int MODE_SCREENOBJECTS = 3;

  private static final int NOT_AN_INT = Integer.MIN_VALUE;

  private static final int FOUR_MEGS = 1 << 22;

  // error helping string
  private static String errorString = null;

  // variable to store ini data in
  private static String directory = null;

  // variables to store ini data in
  private static ArrayList imageObjects = null;

  private static ArrayList mapObjects = null;

  private static ArrayList screenObjects = null;

  private static ArrayList files = null;

  private static String outputFile = null;

  public static int DoCreateLvz(String path) {
    // init per build variables
    errorString = null;
    directory = null;
    imageObjects = new ArrayList();
    mapObjects = new ArrayList();
    screenObjects = new ArrayList();
    files = new ArrayList();
    outputFile = null;

    int rv = ERROR_NONE;

    try {
      BufferedReader in = new BufferedReader(new FileReader(path));
      directory = new File(path).getParent() + File.separator;

      rv = readIni(in);

      in.close();

      if (rv == ERROR_NONE) { // no errors reading ini, make the lvz file
        File outFile = new File(directory + outputFile);

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));

        byte[] dword = new byte[4];
        dword[0] = 'C';
        dword[1] = 'O';
        dword[2] = 'N';
        dword[3] = 'T';
        bos.write(dword);
        int sections = files.size() + 1;
        bos.write(toDWORD(sections));

        // do files
        Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION);

        for (int x = 0; x < files.size(); ++x) {
          File f = new File((String) files.get(x));
          String name = f.getName();

          // 4-len str Type
          bos.write(dword);

          // i32 Decompress Size

          BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));

          int size = bis.available();
          bos.write(toDWORD(size));

          // i32 File Time
          int time = (int) (f.lastModified() / 1000);
          bos.write(toDWORD(time));

          // i32 Compressed Size
          // 0.1% larger than sourceLen plus 12 bytes
          int bufferSize = (int) (size * 1.01 + 13);
          byte[] uncompressed = new byte[size];
          byte[] compressed = new byte[bufferSize];

          bis.read(uncompressed);

          compresser.setInput(uncompressed);
          compresser.finish();
          int compressedDataLength = compresser.deflate(compressed);

          bos.write(toDWORD(compressedDataLength));

          // Null-end str File Name

          byte[] string = name.getBytes("UTF-8");

          bos.write(string);
          bos.write(0); // null terminated

          // * data * Compressed data

          bos.write(compressed, 0, compressedDataLength);

          bis.close();
          compresser.reset();
        }

        // do ini
        ArrayList rawIni = getIniAsBytes();

        int bufferSize = (int) (rawIni.size() * 1.01 + 13);
        byte[] uncompressed = new byte[rawIni.size()];
        for (int c = 0; c < rawIni.size(); ++c) {
          uncompressed[c] = ((Byte) rawIni.get(c)).byteValue();
        }

        byte[] compressed = new byte[bufferSize];
        compresser.setInput(uncompressed);
        compresser.finish();

        int compressedDataLength = compresser.deflate(compressed);

        // ENCODE IT

        // 4-len str Type
        bos.write(dword);

        // i32 Decompress Size

        int size = rawIni.size();
        bos.write(toDWORD(size));

        // i32 File Time
        int time = 0;
        bos.write(toDWORD(time));

        // i32 Compressed Size
        // 0.1% larger than sourceLen plus 12 bytes

        bos.write(toDWORD(compressedDataLength));

        // Null-end str File Name
        bos.write(0); // empty String

        // * data * Compressed data

        bos.write(compressed, 0, compressedDataLength);

        // //////////

        bos.close();

        if (outFile.length() >= FOUR_MEGS) {
          rv = ERROR_TOO_BIG_LVZ;
          errorString = outFile.length() + " >= " + FOUR_MEGS;
        }
      }
    } catch (IOException e) {
      JOptionPane.showMessageDialog(null, e);
      rv = ERROR_IOERROR;
    }

    return rv;
  }

  /**
   * Get the ini file from the static imageObjects, mapObjects, and screenObjects variables as an
   * ArrayList of bytes
   *
   * @throws UnsupportedEncodingException if the OS doesn't support UTF-8 encoding
   * @return an ArrayList of bytes of the encoding of the ini data
   */
  private static ArrayList getIniAsBytes() throws UnsupportedEncodingException {
    ArrayList bytes = new ArrayList();
    byte b;

    // 4-len str Type CLV2
    byte[] dword = new byte[4];
    dword[0] = 'C';
    dword[1] = 'L';
    dword[2] = 'V';
    dword[3] = '2';
    insertByteArray(bytes, dword);

    // i32 Object Count
    int objectCount = mapObjects.size() + screenObjects.size();
    insertByteArray(bytes, toDWORD(objectCount));

    // i32 Image Count
    int imageCount = imageObjects.size();
    insertByteArray(bytes, toDWORD(imageCount));

    // MapObject:
    XMapObject mo;
    for (int x = 0; x < mapObjects.size(); ++x) {
      mo = (XMapObject) mapObjects.get(x);

      // 0: [low 7 bits id] [1 bit mapobject]
      b = (byte) (((mo.id & 0x7F) << 1) | 1);
      bytes.add(new Byte(b));

      // 1: [high 8 bits id]
      b = (byte) ((mo.id >> 7) & 0x0FF);
      bytes.add(new Byte(b));

      // 2: [low 8 bits x]
      // 3: [high 8 bits x]
      insertByteArray(bytes, toWORD(mo.x));

      // 4: [low 8 bits y]
      // 5: [high 8 bits y]
      insertByteArray(bytes, toWORD(mo.y));

      // 6: [8 bits image num]
      b = (byte) (getImageNum(mo.imageNum) & 0x0FF);
      bytes.add(new Byte(b));

      // 7: [8 bits layer]
      b = (byte) mo.layer;
      bytes.add(new Byte(b));

      // 8: [low 8 bits display time]
      b = (byte) (mo.time & 0x0FF);
      bytes.add(new Byte(b));

      // 9: [4 bits mode] [high 4 bits display time]
      b = (byte) ((mo.mode << 4) | ((mo.time >> 8) & 0x0F));
      bytes.add(new Byte(b));
    }

    // ScreenObject:
    XScreenObject so;
    for (int x = 0; x < screenObjects.size(); ++x) {
      so = (XScreenObject) screenObjects.get(x);

      // 0: [low 7 bits id] [1 bit mapobject]
      b = (byte) (((so.id & 0x7F) << 1) | 0);
      bytes.add(new Byte(b));

      // 1: [high 8 bits id]
      b = (byte) ((so.id >> 7) & 0x0FF);
      bytes.add(new Byte(b));

      // 2: [low 4 bits x_coord] [4 bits x_type]
      b = (byte) (((so.x_coord & 0x0F) << 4) | (so.x_type));
      bytes.add(new Byte(b));

      // 3: [high 8 bits x_coord]
      b = (byte) ((so.x_coord >> 4) & 0x0FF);
      bytes.add(new Byte(b));

      // 4: [low 4 bits y_coord] [4 bits y_type]
      b = (byte) (((so.y_coord & 0x0F) << 4) | (so.y_type));
      bytes.add(new Byte(b));

      // 4: [high 8 bits y_coord]
      b = (byte) ((so.y_coord >> 4) & 0x0FF);
      bytes.add(new Byte(b));

      // 6: [8 bits image num]
      b = (byte) (getImageNum(so.imageNum) & 0x0FF);
      bytes.add(new Byte(b));

      // 7: [8 bits layer]
      b = (byte) so.layer;
      bytes.add(new Byte(b));

      // 8: [low 8 bits display time]
      b = (byte) (so.time & 0x0FF);
      bytes.add(new Byte(b));

      // 9: [4 bits mode] [high 4 bits display time]
      b = (byte) ((so.mode << 4) | ((so.time >> 8) & 0x0F));
      bytes.add(new Byte(b));
    }

    // ImageObject:
    XImageObject io;
    for (int x = 0; x < imageObjects.size(); ++x) {
      io = (XImageObject) imageObjects.get(x);

      // 0: [low 8 bits x-count]
      // 1: [high 8 bits x-count]
      insertByteArray(bytes, toWORD(io.x));

      // 2: [low 8 bits y-count]
      // 3: [high 8 bits y-count]
      insertByteArray(bytes, toWORD(io.y));

      // 4: [low 8 bits displayTime]
      // 5: [high 8 bits displayTime]
      insertByteArray(bytes, toWORD(io.time));

      // 6+: [Null-ended string filepath]
      byte[] string = io.name.getBytes("UTF-8");
      insertByteArray(bytes, string);

      bytes.add(new Byte((byte) 0));
    }

    return bytes;
  }

  /**
   * We need to map the encoded image num, to the actual one, since we don't get a choice when we're
   * encoding the image objects
   *
   * @param encodeNum the encoded number in the ini file, that we'll transform into the correct one
   * @return the correct byte we should use for the image num in our lvz file
   */
  private static int getImageNum(int encodeNum) {
    int num = -1;

    for (int x = 0; x < imageObjects.size(); ++x) {
      XImageObject io = (XImageObject) imageObjects.get(x);

      if (io.num == encodeNum) {
        num = x;
        break;
      }
    }

    return num;
  }

  /**
   * Is this image number defined in our objectimages
   *
   * @param imageNum the imageNumber to check
   * @return true iff it's a defined imageNum
   */
  private static boolean isEncodedImageNum(int imageNum) {
    return getImageNum(imageNum) != -1;
  }

  /**
   * Insert add to this arraylist these bytes
   *
   * @param list the list to add to
   * @param insertThis the bytes to add
   */
  private static void insertByteArray(ArrayList list, byte[] insertThis) {
    for (int x = 0; x < insertThis.length; ++x) {
      list.add(new Byte(insertThis[x]));
    }
  }

  /**
   * return this int as a dword
   *
   * @param n the int to encode
   * @return the dword of bytes (in little endian)
   */
  private static byte[] toDWORD(int n) {
    byte[] DWORD = new byte[4];

    DWORD[0] = (byte) (n & 0xff);
    n = n >> 8;

    DWORD[1] = (byte) (n & 0xff);
    n = n >> 8;

    DWORD[2] = (byte) (n & 0xff);
    n = n >> 8;

    DWORD[3] = (byte) (n & 0xff);

    return DWORD;
  }

  /**
   * return this int s a word
   *
   * @param n the int to encode
   * @return the word of byte (in little endian)
   */
  private static byte[] toWORD(int n) {
    byte[] WORD = new byte[2];

    WORD[0] = (byte) (n & 0xff);
    n = n >> 8;

    WORD[1] = (byte) (n & 0xff);
    n = n >> 8;

    return WORD;
  }

  /**
   * read the ini file and put it the data into this classes static variables
   *
   * @param in the BufferedReader referring to the ini file, already open
   * @throws IOException when an IO error occurs
   * @return an error code for the error that occured
   */
  private static int readIni(BufferedReader in) throws IOException {
    int rv = ERROR_NONE;

    int curMode = MODE_FILES;

    for (String line = in.readLine(); line != null; line = in.readLine()) { // for each line
      String originalLine = line.trim();
      line = line.trim().toLowerCase();
      line = line.split(";")[0].trim(); // get rid of comments
      if (line.length() == 0) continue; // empty line, or comment

      // OutFile=example.lvz

      if (line.startsWith("outfile")) {
        String[] twoString = line.split("=");
        if (twoString.length != 2) {
          rv = ERROR_OUTFILE_NOT_ONE_EQUALS_SIGN;
          break;
        }
        if (outputFile != null) {
          rv = ERROR_MULTIPLE_OUTFILES;
          break;
        }

        outputFile = twoString[1].trim();
      } else if (outputFile == null) {
        rv = ERROR_OUTFILE_NOT_FIRST;
        break;
      } else {

        if (line.equals("[objectimages]")) curMode = MODE_IMAGEOBJECTS;
        else if (line.equals("[mapobjects]")) curMode = MODE_MAPOBJECTS;
        else if (line.equals("[screenobjects]")) curMode = MODE_SCREENOBJECTS;
        else {
          if (curMode == MODE_FILES) { // File=ships.bm2
            String[] s = line.split("=");

            if (!s[0].equals("file")) {
              rv = ERROR_FILE_BAD_HEAD;
              errorString = line;
              break;
            } else if (s.length != 2) {
              rv = ERROR_FILE_MUST_HAVE_ONE_EQUALS;
              errorString = line;
              break;
            }

            String pathToImport = directory + s[1];

            File f = new File(pathToImport);
            if (!f.exists()) {
              rv = ERROR_FILE_DOESNT_EXIST;
              errorString = line;
              break;
            }

            files.add(pathToImport);
          } else if (curMode == MODE_IMAGEOBJECTS) {
            // i16 X Count
            // i16 Y Count
            // i16 Display Time
            // IMAGE0=ssshield.bm2,10,1,100

            final int MAX = 1 << 16;

            if (!line.startsWith("image")) {
              rv = ERROR_IMAGE_BAD_START;
              errorString = line;
              break;
            }

            String[] s = line.split("=");
            if (s.length != 2) {
              rv = ERROR_IMAGE_NEED_ONE_EQUALS;
              errorString = line;
              break;
            }

            // assign image number

            String numString = s[0].trim().substring(5);
            int number = parseInt(numString);

            if (number == NOT_AN_INT) {
              rv = ERROR_PARSE_INT;
              errorString = originalLine + " (on symbol '" + numString + "')";
              break;
            }

            s = s[1].split(",");

            if (s.length > 4) {
              rv = ERROR_IMAGE_TOO_MANY_COMMAS;
              errorString = originalLine;
              break;
            }

            String path = s[0].trim();
            int x = 1;
            int y = 1;
            int loop = 100;

            if (s.length > 1) {
              x = parseInt(s[1]);
              if (x == NOT_AN_INT) {
                rv = ERROR_PARSE_INT;
                errorString = originalLine + " (on symbol '" + s[1].trim() + "')";
                break;
              }

              if (s.length > 2) {
                y = parseInt(s[2]);

                if (y == NOT_AN_INT) {
                  rv = ERROR_PARSE_INT;
                  errorString = originalLine + " (on symbol '" + s[2].trim() + "')";
                  break;
                }

                if (s.length > 3) {
                  loop = parseInt(s[3]);

                  if (loop == NOT_AN_INT) {
                    rv = ERROR_PARSE_INT;
                    errorString = originalLine + " (on symbol '" + s[3].trim() + "')";
                    break;
                  }
                }
              }
            }

            // check ranges
            if (x < 1 || x >= MAX || y < 1 || y >= MAX) {
              rv = ERROR_RANGE;
              errorString = line + " (legal ranges for x and y are [1," + MAX + "))";
              break;
            } else if (loop < 0 || loop >= MAX) {
              rv = ERROR_RANGE;
              errorString = line + " (legal range for looptime is [0," + MAX + "))";
              break;
            } else if (number < 0 || number > 255) {
              rv = ERROR_RANGE;
              errorString = line + " (legal range for IMAGE# is [0,255])";
              break;
            }

            // add it
            XImageObject io = new XImageObject();
            io.name = path;
            io.x = x;
            io.y = y;
            io.time = loop;
            io.num = number;

            imageObjects.add(io);
          } else if (curMode == MODE_MAPOBJECTS) {
            // <x coord>,<y coord>,<image>,<layer>,<mode>,<display
            // time>,<object id>
            /*
             * i15 Object ID i16 X Coord i16 Y Coord i8 Image Number
             * i8 Layer i12 Display Time i4 Display Mode
             */
            final int IDMAX = 1 << 15;
            final int COORDMAX = 1 << 15;
            final int COORDMIN = -COORDMAX - 1;
            final int DISPLAYTIMEMAX = (1 << 12) * 10;
            int x, y, imageNum; // must be there
            int layer = 0;
            int mode = 0;
            int displayTime = 0;
            int id = 0;

            String[] s = line.split(",");
            if (s.length < 3) {
              rv = ERROR_MAPOBJECT_NOT_ENOUGH_SECTIONS;
              errorString = originalLine;
              break;
            } else if (s.length > 7) {
              rv = ERROR_MAPOBJECT_TOO_MANY_SECTIONS;
              errorString = originalLine;
              break;
            }

            x = parseInt(s[0]);

            if (x == NOT_AN_INT) {
              rv = ERROR_PARSE_INT;
              errorString = originalLine + " (on symbol '" + s[0].trim() + "')";
              break;
            }

            y = parseInt(s[1]);

            if (y == NOT_AN_INT) {
              rv = ERROR_PARSE_INT;
              errorString = originalLine + " (on symbol '" + s[1].trim() + "')";
              break;
            }

            s[2] = s[2].trim();
            if (s[2].length() < 6 || !s[2].startsWith("image")) {
              rv = ERROR_IMAGE_BAD_START;
              errorString = originalLine;
              break;
            }

            s[2] = s[2].substring(5);
            imageNum = parseInt(s[2]);

            if (imageNum == NOT_AN_INT) {
              rv = ERROR_PARSE_INT;
              errorString = originalLine + " (on symbol '" + s[2].trim() + "')";
              break;
            }

            if (s.length > 3) {
              layer = getLayer(s[3]);
              if (layer == -1) {
                rv = ERROR_UNKNOWN_LAYER;
                errorString = originalLine + " (on symbol '" + s[3].trim() + "')";
                break;
              }
            }

            if (s.length > 4) {
              mode = getMode(s[4]);
              if (mode == -1) {
                rv = ERROR_UNKNOWN_MODE;
                errorString = originalLine + " (on symbol '" + s[4].trim() + "')";
                break;
              }
            }

            if (s.length > 5) {
              displayTime = parseInt(s[5]);
              if (displayTime == NOT_AN_INT) {
                rv = ERROR_PARSE_INT;
                errorString = originalLine + " (on symbol '" + s[5].trim() + "')";
                break;
              }
            }

            if (s.length > 6) {
              id = parseInt(s[6]);
              if (id == NOT_AN_INT) {
                rv = ERROR_PARSE_INT;
                errorString = originalLine + " (on symbol '" + s[6].trim() + "')";
                break;
              }
            }

            // check ranges
            if (x <= COORDMIN || x >= COORDMAX || y <= COORDMIN || y >= COORDMAX) {
              rv = ERROR_RANGE;
              errorString =
                  originalLine
                      + " (legal range for coords is ("
                      + COORDMIN
                      + ", "
                      + COORDMAX
                      + "))";
              break;
            } else if (imageNum < 0 || imageNum > 255) {
              rv = ERROR_RANGE;
              errorString = originalLine + " (legal range for IMAGE# is [0,255])";
              break;
            } else if (displayTime < 0 || displayTime >= DISPLAYTIMEMAX) {
              rv = ERROR_RANGE;
              errorString =
                  originalLine + " (legal range for display time is [0," + (DISPLAYTIMEMAX) + "))";
              break;
            } else if (displayTime % 10 != 0) {
              rv = ERROR_PRECISION;
              errorString = originalLine + " (display time must be divisible by 10)";
              break;
            } else if (id < 0 || id >= IDMAX) {
              rv = ERROR_RANGE;
              errorString = originalLine + " (legal range for object id is [0," + IDMAX + "))";
              break;
            }

            rv = isEncodedImageNum(imageNum) ? ERROR_NONE : ERROR_BADIMAGENUM;
            if (rv != ERROR_NONE) {
              errorString = line;
              break;
            }

            XMapObject mo = new XMapObject();
            mo.id = id;
            mo.imageNum = imageNum;
            mo.layer = layer;
            mo.mode = mode;
            mo.time = displayTime / 10;
            mo.x = x;
            mo.y = y;

            mapObjects.add(mo);
          } else if (curMode == MODE_SCREENOBJECTS) {
            // <x coord>,<y coord>,<image>,<layer>,<mode>,<display
            // time>,<object id>
            /*
             * i15 Object ID u4 X Type i12 X Coord u4 Y Type i12 Y
             * Coord i8 Image Number i8 Layer i12 Display Time i4
             * Display Mode
             */
            final int IDMAX = 1 << 15;
            final int COORDMAX = 1 << 12;
            final int COORDMIN = -COORDMAX - 1;
            final int DISPLAYTIMEMAX = (1 << 12) * 10;
            int x, y, x_type, y_type, imageNum; // must be there
            int layer = 0;
            int mode = 0;
            int displayTime = 0;
            int id = 0;

            String[] s = line.split(",");
            if (s.length < 3) {
              rv = ERROR_SCREENOBJECT_NOT_ENOUGH_SECTIONS;
              errorString = originalLine;
              break;
            } else if (s.length > 7) {
              rv = ERROR_SCREENOBJECT_TOO_MANY_SECTIONS;
              errorString = originalLine;
              break;
            }

            s[0] = s[0].trim();
            x_type = getOffset(s[0]);
            if (x_type > 0) s[0] = s[0].substring(1);

            x = parseInt(s[0]);

            if (x == NOT_AN_INT) {
              rv = ERROR_PARSE_INT;
              errorString = originalLine + " (on symbol '" + s[0].trim() + "')";
              break;
            }

            s[1] = s[1].trim();
            y_type = getOffset(s[1]);
            if (y_type > 0) {
              s[1] = s[1].substring(1);
            }

            y = parseInt(s[1]);

            if (y == NOT_AN_INT) {
              rv = ERROR_PARSE_INT;
              errorString = originalLine + " (on symbol '" + s[1].trim() + "')";
              break;
            }

            s[2] = s[2].trim();
            if (s[2].length() < 6 || !s[2].startsWith("image")) {
              rv = ERROR_IMAGE_BAD_START;
              errorString = originalLine;
              break;
            }

            s[2] = s[2].substring(5);
            imageNum = parseInt(s[2]);

            if (imageNum == NOT_AN_INT) {
              rv = ERROR_PARSE_INT;
              errorString = originalLine + " (on symbol '" + s[2].trim() + "')";
              break;
            }

            if (s.length > 3) {
              layer = getLayer(s[3]);
              if (layer == -1) {
                rv = ERROR_UNKNOWN_LAYER;
                errorString = originalLine + " (on symbol '" + s[3].trim() + "')";
                break;
              }
            }

            if (s.length > 4) {
              mode = getMode(s[4]);
              if (mode == -1) {
                rv = ERROR_UNKNOWN_MODE;
                errorString = originalLine + " (on symbol '" + s[4].trim() + "')";
                break;
              }
            }

            if (s.length > 5) {
              displayTime = parseInt(s[5]);
              if (displayTime == NOT_AN_INT) {
                rv = ERROR_PARSE_INT;
                errorString = originalLine + " (on symbol '" + s[5].trim() + "')";
                break;
              }
            }

            if (s.length > 6) {
              id = parseInt(s[6]);
              if (id == NOT_AN_INT) {
                rv = ERROR_PARSE_INT;
                errorString = originalLine + " (on symbol '" + s[6].trim() + "')";
                break;
              }
            }

            // check ranges
            if (x <= COORDMIN || x >= COORDMAX || y <= COORDMIN || y >= COORDMAX) {
              rv = ERROR_RANGE;
              errorString =
                  originalLine
                      + " (legal range for coords is ("
                      + COORDMIN
                      + ", "
                      + COORDMAX
                      + "))";
              break;
            } else if (imageNum < 0 || imageNum > 255) {
              rv = ERROR_RANGE;
              errorString = originalLine + " (legal range for IMAGE# is [0,255])";
              break;
            } else if (displayTime < 0 || displayTime >= DISPLAYTIMEMAX) {
              rv = ERROR_RANGE;
              errorString =
                  originalLine + " (legal range for display time is [0," + (DISPLAYTIMEMAX) + "))";
              break;
            } else if (displayTime % 10 != 0) {
              rv = ERROR_PRECISION;
              errorString = originalLine + " (display time must be divisible by 10)";
              break;
            } else if (id < 0 || id >= IDMAX) {
              rv = ERROR_RANGE;
              errorString = originalLine + " (legal range for object id is [0," + IDMAX + "))";
              break;
            }

            rv = isEncodedImageNum(imageNum) ? ERROR_NONE : ERROR_BADIMAGENUM;
            if (rv != ERROR_NONE) {
              errorString = line;
              break;
            }

            XScreenObject so = new XScreenObject();
            so.id = id;
            so.imageNum = imageNum;
            so.layer = layer;
            so.mode = mode;
            so.time = displayTime / 10;
            so.x_coord = x;
            so.y_coord = y;
            so.x_type = x_type;
            so.y_type = y_type;

            screenObjects.add(so);
          }
        }
      }
    }

    return rv;
  }

  /**
   * Map the mode String to an int or -1 if no good
   *
   * @param s the string to change to a mode
   * @return the in associated with this string's mode in array modes
   */
  private static int getMode(String s) {
    int rv = -1;
    s = s.trim();

    for (int x = 0; x < modes.length; ++x) {
      if (s.equals(modes[x])) {
        rv = x;
        break;
      }
    }

    return rv;
  }

  /**
   * Map the layer String to an int or -1 if no good
   *
   * @param s the string to change to a layer
   * @return the in associated with this string's layer in array layers
   */
  private static int getLayer(String s) {
    int rv = -1;
    s = s.trim();

    for (int x = 0; x < layers.length; ++x) {
      if (s.equals(layers[x])) {
        rv = x;
        break;
      }
    }

    return rv;
  }

  /**
   * Get the screenobject offset from this string s, or 0 if unknown
   *
   * @param s the string with the offset in it
   * @return the offset at the begining of this coord, defaulting to 0 if unknown
   */
  private static int getOffset(String s) {
    int rv = 0;

    for (int x = 1; x <= MAX_SCREEN_OBJ_OFFSET; ++x) {
      if (s.startsWith(screenObjOffsets[x])) {
        rv = x;
        break;
      }
    }

    return rv;
  }

  /**
   * Parse the String to an int, returning NOT_AN_INT if it's not an int
   *
   * @param s the String to parse... can start with + or -
   * @return an int that we parsed out... or NOT_AN_INT
   */
  private static int parseInt(String s) {
    boolean canParse = true;
    int rv = NOT_AN_INT;

    s = s.trim();

    if (s.startsWith("+")) s = s.substring(1);

    int len = s.length();

    if (len == 0) canParse = false;

    for (int x = 0; x < len; ++x) {
      char c = s.charAt(x);
      if (!(Character.isDigit(c) || (x == 0 && c == '-'))) {
        canParse = false;
        break;
      }
    }

    if (canParse) rv = Integer.parseInt(s);

    return rv;
  }

  /**
   * Get the path of a file the user selects
   *
   * @return a file path or null
   */
  private static String getPath() {
    fc.setMode(FileDialog.LOAD);
    fc.setTitle("Select .ini to Create Lvz From - Version " + VERSION);
    fc.show();
    String filename = fc.getFile();
    String dir = fc.getDirectory();

    return (filename == null ? null : dir + filename);
  }
}
