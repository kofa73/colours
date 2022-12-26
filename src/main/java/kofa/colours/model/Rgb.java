package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3D;

import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;
import static org.apache.commons.math3.linear.MatrixUtils.inverse;

public abstract class Rgb<S extends Rgb<S>> implements Vector3D {
    private final double r;
    private final double g;
    private final double b;

    Rgb(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    Rgb(double r, double g, double b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    protected abstract SpaceConversionMatrix<S, Xyz> toXyzMatrix();

    public Xyz toXyz() {
        return toXyzMatrix().multiply((S) this);
    }

    @Override
    public double[] coordinates() {
        return new double[]{r, g, b};
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), r, g, b);
    }

    public boolean isOutOfGamut() {
        return r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1;
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

    public double r() {
        return r;
    }

    public double g() {
        return g;
    }

    public double b() {
        return b;
    }
}
