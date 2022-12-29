package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;

import java.util.function.DoublePredicate;

import static java.lang.Math.abs;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;
import static org.apache.commons.math3.linear.MatrixUtils.inverse;

/**
 * Models any RGB space.
 *
 * @param <S> the subtype
 */
public abstract class Rgb<S extends Rgb<S>> extends Vector3 {
    Rgb(double r, double g, double b) {
        super(r, g, b);
    }

    protected abstract SpaceConversionMatrix<S, Xyz> toXyzMatrix();

    public Xyz toXyz() {
        return toXyzMatrix().multiply((S) this);
    }

    public boolean anyCoordinateMatches(DoublePredicate predicate) {
        return coordinates().anyMatch(predicate);
    }

    @Override
    public String toString() {
        return "%s(%f, %f, %f)".formatted(getClass().getSimpleName(), r(), g(), b());
    }

    public boolean isOutOfGamut() {
        return r() < 0 || r() > 1 || g() < 0 || g() > 1 || b() < 0 || b() > 1;
    }

    public static double[][] calculateToXyzMatrix(
            double xr, double yr,
            double xg, double yg,
            double xb, double yb,
            Xyz referenceWhite
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
        )).operate(new double[]{referenceWhite.x(), referenceWhite.y(), referenceWhite.z()});

        double Sr = S[0];
        double Sg = S[1];
        double Sb = S[2];

        return new double[][]{
                {Sr * Xr, Sg * Xg, Sb * Xb},
                {Sr * Yr, Sg * Yg, Sb * Yb},
                {Sr * Zr, Sg * Zg, Sb * Zb}
        };
    }

    public double r() {
        return coordinate1;
    }

    public double g() {
        return coordinate2;
    }

    public double b() {
        return coordinate3;
    }

    public boolean isBlack() {
        return abs(r()) < 1E-6 && abs(g()) < 1E-6 && abs(b()) < 1E-6;
    }

    public boolean isWhite() {
        return abs(1 - r()) < 1E-6 && abs(1 - g()) < 1E-6 && abs(1 - b()) < 1E-6;
    }
}
