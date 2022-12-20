package kofa.colours.model;

import kofa.maths.DoubleVector;

public record UV(double u, double v) implements DoubleVector {
    @Override
    public double[] values() {
        return new double[]{u, v};
    }

    @Override
    public String toString() {
        return "%s[%f, %f]".formatted(getClass().getSimpleName(), u, v);
    }

    public static UV from(XYZ xyz) {
        var denominator = denominator_XYZ_for_UV(xyz);
        return new UV(
                uPrime(xyz.X(), denominator),
                vPrime(xyz.Y(), denominator)
        );
    }

    private static double denominator_XYZ_for_UV(XYZ xyz) {
        var denominator = xyz.X() + 15 * xyz.Y() + 3 * xyz.Z();
        if (denominator == 0.0) {
            denominator = 1E-9;
        }
        return denominator;
    }

    private static double uPrime(double X, double denominator) {
        return 4 * X / denominator;
    }

    private static double vPrime(double Y, double denominator) {
        return 9 * Y / denominator;
    }
}
