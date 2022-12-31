package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;

import java.util.stream.DoubleStream;

public class CIExyY extends Vector3<CIExyY> {
    // According to http://www.brucelindbloom.com/Eqn_XYZ_to_xyY.html, should return xy of reference white
    // Here, we hardcode D65, as that's what both sRGB and Rec2020 uses, but theoretically that's not OK.
    // https://en.wikipedia.org/wiki/Illuminant_D65#Definition
    public static final CIExyY D65_WHITE_2DEGREE_STANDARD_OBSERVER = new CIExyY(0.31271, 0.32902, 1);
    public static final CIExyY D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER = new CIExyY(0.31382, 0.33100, 1);
    public static final CIExyY D65_BLACK_2DEGREE_STANDARD_OBSERVER = new CIExyY(D65_WHITE_2DEGREE_STANDARD_OBSERVER.x, D65_WHITE_2DEGREE_STANDARD_OBSERVER.y, 0);
    public static final CIExyY D65_BLACK_10DEGREE_SUPPLEMENTARY_OBSERVER = new CIExyY(D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER.x, D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER.y, 0);

    public final double x;
    public final double y;
    public final double Y;

    protected CIExyY(double x, double y, double Y) {
        super(x, y, Y);
        this.x = x;
        this.y = y;
        this.Y = Y;
    }

    @Override
    public DoubleStream coordinates() {
        return DoubleStream.of(x, y, Y);
    }

    @Override
    public String toString() {
        return Vector3.format(this, x, y, Y);
    }

    public CIEXYZ toXyz() {
        if (y == 0) {
            return CIEXYZ.BLACK;
        }

        double yr = Y / y;
        double xyzX = x * yr;
        double xyzZ = (1 - x - y) * yr;

        return new CIEXYZ(xyzX, Y, xyzZ);
    }

    public static CIExyY from(CIEXYZ xyz) {
        if (xyz.isBlack()) {
            return D65_BLACK_2DEGREE_STANDARD_OBSERVER;
        }
        double denominator = xyz.X + xyz.Y + xyz.Z;
        if (denominator == 0) {
            return D65_BLACK_2DEGREE_STANDARD_OBSERVER;
        }

        double Y = xyz.Y;
        double x = xyz.X / denominator;
        double y = Y / denominator;

        return new CIExyY(x, y, Y);
    }

    @Override
    public final <O extends Vector3<O>> O multiplyBy(SpaceConversionMatrix<CIExyY, O> conversionMatrix) {
        return multiplyBy(conversionMatrix, x, y, Y);
    }
}
