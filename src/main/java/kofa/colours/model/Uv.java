package kofa.colours.model;

public record Uv(double u, double v) {
    public static Uv from(Xyz xyz) {
        var denominator = denominatorXyzForUv(xyz);
        return new Uv(
                uPrime(xyz.x(), denominator),
                vPrime(xyz.y(), denominator)
        );
    }

    private static double denominatorXyzForUv(Xyz xyz) {
        var denominator = xyz.x() + 15 * xyz.y() + 3 * xyz.z();
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

    @Override
    public String toString() {
        return "%s(%f, %f)".formatted(this.getClass().getSimpleName(), u(), v());
    }
}
