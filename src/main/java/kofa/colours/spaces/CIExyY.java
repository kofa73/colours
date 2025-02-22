package kofa.colours.spaces;

import static java.lang.Math.abs;

public class CIExyY {
    public static final double BLACK_Y_THRESHOLD = 1E-9;
    public static final double WHITE_Y_THRESHOLD = 1 - 1E-9;

    public static final double D65_WHITE_2DEG_x = 0.31271;
    public static final double D65_WHITE_2DEG_y = 0.32902;
    // https://en.wikipedia.org/wiki/Illuminant_D65#Definition
    public static final double[] D65_WHITE_2DEGREE_STANDARD_OBSERVER = new double[] {D65_WHITE_2DEG_x, D65_WHITE_2DEG_y, 1};

    public static final double D65_WHITE_10DEG_x = 0.31382;
    public static final double D65_WHITE_10DEG_y = 0.33100;
    public static final double[] D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER = new double[] {D65_WHITE_10DEG_x, D65_WHITE_10DEG_y, 1};
    
    // According to http://www.brucelindbloom.com/Eqn_XYZ_to_xyY.html, should use xy of reference white
    public static final double[] D65_BLACK_2DEGREE_STANDARD_OBSERVER = new double[] {D65_WHITE_2DEG_x, D65_WHITE_2DEG_y, 0};
    public static final double[] D65_BLACK_10DEGREE_SUPPLEMENTARY_OBSERVER = new double[] {D65_WHITE_10DEG_x, D65_WHITE_10DEG_y, 0};

    public static void xyY_to_XYZ(double[] xyY, double[] XYZ) {
        // y is derived from XYZ.Y = xyY.Y, and is only 0 if that is 0.
        // So, by checking Y we avoid division by 0, and can use the same black level as in XYZ
        if (xyY[2] <= BLACK_Y_THRESHOLD) {
            XYZ[0] = XYZ[1] = XYZ[2] = 0;
        } else {
            double yScale = xyY[2] / xyY[1];
            double xyzX = xyY[0] * yScale;
            double xyzZ = (1 - xyY[0] - xyY[1]) * yScale;

            XYZ[0] = xyzX;
            XYZ[1] = xyY[2];
            XYZ[2] = xyzZ;
        }
    }

    public static void XZY_to_xyY(double[] XYZ, double[] xyY) {
        if (XYZ[1] <= BLACK_Y_THRESHOLD) {
            setToBlack(xyY);
        } else {
            double denominator = XYZ[0] + XYZ[1] + XYZ[2];
            if (denominator == 0) {
                setToBlack(xyY);
            }

            double x = XYZ[0] / denominator;
            double Y = XYZ[1];
            double y = Y / denominator;

            xyY[0] = x;
            xyY[1] = y;
            xyY[2] = Y;
        }
    }

    private static void setToBlack(double[] xyY) {
        xyY[0] = D65_WHITE_2DEG_x;
        xyY[1] = D65_WHITE_2DEG_y;
        xyY[2] = 0;
    }

    public boolean isBlack(double[] xyY) {
        return xyY[2] <= BLACK_Y_THRESHOLD;
    }

    public boolean isWhite(double[] xyY) {
        return xyY[2] >= WHITE_Y_THRESHOLD;
    }
}
