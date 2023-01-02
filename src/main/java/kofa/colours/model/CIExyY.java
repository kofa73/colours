package kofa.colours.model;

import kofa.maths.Vector3;

import static java.lang.Math.abs;

public class CIExyY extends Vector3 {
    // According to http://www.brucelindbloom.com/Eqn_XYZ_to_xyY.html, should return xy of reference white
    // Here, we hardcode D65, as that's what both sRGB and Rec2020 uses, but theoretically that's not OK.
    // https://en.wikipedia.org/wiki/Illuminant_D65#Definition
    public static final CIExyY D65_WHITE_2DEGREE_STANDARD_OBSERVER = new CIExyY(0.31271, 0.32902, 1);
    public static final CIExyY D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER = new CIExyY(0.31382, 0.33100, 1);
    public static final CIExyY D65_BLACK_2DEGREE_STANDARD_OBSERVER = new CIExyY(D65_WHITE_2DEGREE_STANDARD_OBSERVER.x(), D65_WHITE_2DEGREE_STANDARD_OBSERVER.y(), 0);
    public static final CIExyY D65_BLACK_10DEGREE_SUPPLEMENTARY_OBSERVER = new CIExyY(D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER.x(), D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER.y(), 0);

    // much less than Y of new Rec2020(0.0001 / 65535, 0.0001 / 65535, 0.0001 / 65535) ~ 1.5E-9
    public static final double BLACK_Y_LEVEL = 1E-9;

    protected CIExyY(double x, double y, double Y) {
        super(x, y, Y);
    }

    public final double x() {
        return coordinate1;
    }

    public final double y() {
        return coordinate2;
    }

    public final double Y() {
        return coordinate3;
    }

    public CIEXYZ toXyz() {
        // y is derived from XYZ.Y = xyY.Y, and is only 0 if that is 0.
        // So, by checking Y we avoid division by 0, and can use the same black level as in XYZ
        if (abs(Y()) <= BLACK_Y_LEVEL) {
            return CIEXYZ.BLACK;
        }

        double yr = Y() / y();
        double xyzX = x() * yr;
        double xyzZ = (1 - x() - y()) * yr;

        return new CIEXYZ(xyzX, Y(), xyzZ);
    }

    public static CIExyY from(CIEXYZ xyz) {
//        if (xyz.isBlack()) {
//            return D65_BLACK_2DEGREE_STANDARD_OBSERVER;
//        }
        double denominator = xyz.X() + xyz.Y() + xyz.Z();
        if (denominator == 0) {
            return D65_BLACK_2DEGREE_STANDARD_OBSERVER;
        }

        double Y = xyz.Y();
        double x = xyz.X() / denominator;
        double y = Y / denominator;

        return new CIExyY(x, y, Y);
    }

    public boolean isBlack() {
        return Y() < BLACK_Y_LEVEL;
    }
}
