package kofa.colours.spaces;

import static java.lang.Math.abs;

public class CIExyY {
    public static final float BLACK_Y_THRESHOLD = 1E-9f;
    public static final float WHITE_Y_THRESHOLD = 1 - 1E-9f;

    public static final float D65_WHITE_2DEG_x = 0.31271f;
    public static final float D65_WHITE_2DEG_y = 0.32902f;
    // https://en.wikipedia.org/wiki/Illuminant_D65#Definition
    public static final float[] D65_WHITE_2DEGREE_STANDARD_OBSERVER = new float[] {D65_WHITE_2DEG_x, D65_WHITE_2DEG_y, 1f};

    public static final float D65_WHITE_10DEG_x = 0.31382f;
    public static final float D65_WHITE_10DEG_y = 0.33100f;
    public static final float[] D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER = new float[] {D65_WHITE_10DEG_x, D65_WHITE_10DEG_y, 1f};
    
    // According to http://www.brucelindbloom.com/Eqn_XYZ_to_xyY.html, should use xy of reference white
    public static final float[] D65_BLACK_2DEGREE_STANDARD_OBSERVER = new float[] {D65_WHITE_2DEG_x, D65_WHITE_2DEG_y, 0f};
    public static final float[] D65_BLACK_10DEGREE_SUPPLEMENTARY_OBSERVER = new float[] {D65_WHITE_10DEG_x, D65_WHITE_10DEG_y, 0f};

    public static void xyY_to_XYZ(float[] xyY, float[] XYZ) {
        // y is derived from XYZ.Y = xyY.Y, and is only 0 if that is 0.
        // So, by checking Y we avoid division by 0, and can use the same black level as in XYZ
        if (xyY[2] <= BLACK_Y_THRESHOLD) {
            XYZ[0] = XYZ[1] = XYZ[2] = 0;
        } else {
            float yScale = xyY[2] / xyY[1];
            float xyzX = xyY[0] * yScale;
            float xyzZ = (1 - xyY[0] - xyY[1]) * yScale;

            XYZ[0] = xyzX;
            XYZ[1] = xyY[2];
            XYZ[2] = xyzZ;
        }
    }

    public static void XZY_to_xyY(float[] XYZ, float[] xyY) {
        if (XYZ[1] <= BLACK_Y_THRESHOLD) {
            setToBlack(xyY);
        } else {
            float denominator = XYZ[0] + XYZ[1] + XYZ[2];
            if (denominator == 0) {
                setToBlack(xyY);
            }

            float x = XYZ[0] / denominator;
            float Y = XYZ[1];
            float y = Y / denominator;

            xyY[0] = x;
            xyY[1] = y;
            xyY[2] = Y;
        }
    }

    private static void setToBlack(float[] xyY) {
        xyY[0] = D65_WHITE_2DEG_x;
        xyY[1] = D65_WHITE_2DEG_y;
        xyY[2] = 0f;
    }

    public boolean isBlack(float[] xyY) {
        return xyY[2] <= BLACK_Y_THRESHOLD;
    }

    public boolean isWhite(float[] xyY) {
        return xyY[2] >= WHITE_Y_THRESHOLD;
    }
}
