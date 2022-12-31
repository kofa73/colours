package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;

import java.util.stream.DoubleStream;

import static java.lang.Math.abs;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;
import static org.apache.commons.math3.linear.MatrixUtils.inverse;

/**
 * Models any RGB space.
 *
 * @param <S> the subtype
 */
public abstract class Rgb<S extends Rgb<S>> extends Vector3<S> {
    public final double r;
    public final double g;
    public final double b;

    protected Rgb(double r, double g, double b) {
        super(r, g, b);
        this.r = r;
        this.g = g;
        this.b = b;
    }

    protected abstract SpaceConversionMatrix<S, CIEXYZ> toXyzMatrix();

    public CIEXYZ toXyz() {
        return toXyzMatrix().multiply((S) this);
    }

    public boolean isOutOfGamut() {
        return r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1;
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
        )).operate(new double[]{referenceWhite.X, referenceWhite.Y, referenceWhite.Z});

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
        return abs(r) < 1E-6 && abs(g) < 1E-6 && abs(b) < 1E-6;
    }

    public boolean isWhite() {
        return abs(1 - r) < 1E-6 && abs(1 - g) < 1E-6 && abs(1 - b) < 1E-6;
    }

    @Override
    public DoubleStream coordinates() {
        return DoubleStream.of(r, g, b);
    }

    @Override
    public String toString() {
        return Vector3.format(this, r, g, b);
    }

    @Override
    public final <O extends Vector3<O>> O multiplyBy(SpaceConversionMatrix<S, O> conversionMatrix) {
        return multiplyBy(conversionMatrix, r, g, b);
    }
}
