package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;

import static java.lang.Math.abs;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;
import static org.apache.commons.math3.linear.MatrixUtils.inverse;

/**
 * Models any RGB space.
 *
 * @param <S> the subtype
 */
public abstract class Rgb<S extends Rgb<S>> extends Vector3 {
    // corresponds to SRGB and Rec2020 components of CIELAB(CIELAB.BLACK_LEVEL, 0, 0)
    public static final double BLACK_LEVEL = 1E-9;

    protected Rgb(double r, double g, double b) {
        super(r, g, b);
    }

    public final double r() {
        return coordinate1;
    }

    public final double g() {
        return coordinate2;
    }

    public final double b() {
        return coordinate3;
    }

    protected abstract SpaceConversionMatrix<S, CIEXYZ> toXyzMatrix();

    public CIEXYZ toXyz() {
        return toXyzMatrix().multiply((S) this);
    }

    public boolean isOutOfGamut() {
        return r() < 0 || r() > 1 || g() < 0 || g() > 1 || b() < 0 || b() > 1;
    }

    public static double[][] calculateToXyzMatrix(
            double xr, double yr,
            double xg, double yg,
            double xb, double yb,
            CIEXYZ referenceWhite
    ) {
        double Xr = xr / yr;
        double Yr = 1;
        double Zr = (1 - xr - yr) / yr;
        double Xg = xg / yg;
        double Yg = 1;
        double Zg = (1 - xg - yg) / yg;
        double Xb = xb / yb;
        double Yb = 1;
        double Zb = (1 - xb - yb) / yb;

        double[] S = inverse(createRealMatrix(new double[][]
                {
                        {Xr, Xg, Xb},
                        {Yr, Yg, Yb},
                        {Zr, Zg, Zb}
                }
        )).operate(new double[]{referenceWhite.X(), referenceWhite.Y(), referenceWhite.Z()});

        double Sr = S[0];
        double Sg = S[1];
        double Sb = S[2];

        return new double[][]{
                {Sr * Xr, Sg * Xg, Sb * Xb},
                {Sr * Yr, Sg * Yg, Sb * Yb},
                {Sr * Zr, Sg * Zg, Sb * Zb}
        };
    }

    public boolean isBlack() {
        return abs(r()) < BLACK_LEVEL && abs(g()) < BLACK_LEVEL && abs(b()) < BLACK_LEVEL;
    }

//    public boolean isWhite() {
//        return abs(1 - r()) < BLACK_LEVEL && abs(1 - g()) < BLACK_LEVEL && abs(1 - b()) < BLACK_LEVEL;
//    }
}
