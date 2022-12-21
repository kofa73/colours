package kofa.colours.model;

import kofa.maths.DoubleVector;

public record Uv(double u, double v) implements DoubleVector {
    @Override
    public double[] coordinates() {
        return new double[]{u, v};
    }

    @Override
    public String toString() {
        return "%s[%f, %f]".formatted(getClass().getSimpleName(), u, v);
    }

    public static Uv from(Xyz xyz) {
        var denominator = denominatorXyzForUv(xyz);
        return new Uv(
                uPrime(xyz.X(), denominator),
                vPrime(xyz.Y(), denominator)
        );
    }

    private static double denominatorXyzForUv(Xyz xyz) {
        var denominator = xyz.X() + 15 * xyz.Y() + 3 * xyz.Z();
        if (denominator == 0) {
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
