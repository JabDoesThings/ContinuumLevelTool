/*
 * Created on Jul 3, 2005
 *
 */
package editor;

import java.util.Vector;

/** @author baks */
public class AngleRotator {
  public static void doRotate(Vector vals, double degangle) {
    int h = vals.size();
    double angle = -Math.toRadians(degangle);

    if (h > 0) {
      int w = ((Vector) vals.get(0)).size();
      int nw = Math.max(2 * w + 1, 2 * h + 1);
      int nh = nw;
      int cx = w / 2;
      int cy = h / 2;
      int cBx = nw / 2;
      int cBy = nh / 2;

      Vector v = new Vector();
      for (int x = 0; x < nw; ++x) {
        Vector c = new Vector();

        for (int y = 0; y < nh; ++y) {
          c.add(new Integer(0));
        }
        v.add(c);
      }

      for (int y = 0; y < nh; ++y) {
        for (int x = 0; x < nw; ++x) {
          // do a little bit of voodoo
          int dx = x - cBx;
          int dy = y - cBy;

          double theta = Math.atan2(dy, dx);

          double d = Math.sqrt(dx * dx + dy * dy);

          double A2 = theta + angle;

          double a = (d * Math.cos(A2));
          double o = (d * Math.sin(A2));

          int nx = (int) Math.round(cx + a);
          int ny = (int) Math.round(cy + o);

          /*
           * if (value == 1) { System.out.println("dx = " + dx);
           * System.out.println("dy = " + dy);
           * System.out.println("theta = " + theta);
           * System.out.println("d = " + d);
           * System.out.println("ntheta = " + ntheta);
           * System.out.println("ndx = " + ndx);
           * System.out.println("ndy = " + ndy);
           * System.out.println("nx = " + nx); System.out.println("ny = " +
           * ny); }
           */

          int value = getVectorValue(nx, ny, vals);

          // set the new spot to the value

          setVec(v, x, y, value);
        }
      }

      vals.clear();
      vals.addAll(v);

      trimVector(vals);
    }
  }

  private static int getVectorValue(int y, int x, Vector v) {
    int rv;

    if (x < 0 || y < 0 || v.size() == 0) rv = 0;
    else if (x >= v.size() || y >= ((Vector) v.get(0)).size()) rv = 0;
    else rv = ((Integer) ((Vector) v.get(x)).get(y)).intValue();

    return rv;
  }

  // trim 0's from all edges
  private static void trimVector(Vector v) {
    // trim left
    while (v.size() > 0) {
      boolean allZeros = true;
      Vector w = (Vector) v.get(0);

      for (int y = 0; y < w.size(); ++y) {
        int a = ((Integer) w.get(y)).intValue();

        if (a != 0) {
          allZeros = false;
          break;
        }
      }

      if (allZeros) {
        v.remove(0);
      } else {
        break;
      }
    }

    // trim right
    while (v.size() > 0) {
      boolean allZeros = true;
      Vector w = (Vector) v.get(v.size() - 1);

      for (int y = 0; y < w.size(); ++y) {
        int a = ((Integer) w.get(y)).intValue();

        if (a != 0) {
          allZeros = false;
          break;
        }
      }

      if (allZeros) {
        v.remove(v.size() - 1);
      } else {
        break;
      }
    }

    if (v.size() > 0) {
      // trim top
      while (true) {
        int size = ((Vector) v.get(0)).size();

        if (size == 0) break;

        boolean allZeros = true;

        for (int x = 0; x < v.size(); ++x) {
          Vector w = (Vector) v.get(x);

          if (((Integer) w.get(0)).intValue() != 0) {
            allZeros = false;
            break;
          }
        }

        if (allZeros) {
          for (int x = 0; x < v.size(); ++x) {
            Vector w = (Vector) v.get(x);

            w.remove(0);
          }
        } else {
          break;
        }
      }
    }

    if (v.size() > 0) {
      // trim bottom
      while (true) {
        int size = ((Vector) v.get(0)).size();

        if (size == 0) break;

        boolean allZeros = true;

        for (int x = 0; x < v.size(); ++x) {
          Vector w = (Vector) v.get(x);

          if (((Integer) w.get(w.size() - 1)).intValue() != 0) {
            allZeros = false;
            break;
          }
        }

        if (allZeros) {
          for (int x = 0; x < v.size(); ++x) {
            Vector w = (Vector) v.get(x);

            w.remove(w.size() - 1);
          }
        } else {
          break;
        }
      }
    }
  }

  private static void setVec(Vector v, int x, int y, int value) {
    Vector col = (Vector) v.get(y);

    col.set(x, new Integer(value));
  }
}
