package kofa.maths;

public class MathHelpers {
    private static final double ln2 = Math.log(2);

    public static double dot(double[] one, double[] other) {
        return one[0] * other[0]
                + one[1] * other[1]
                + one[2] * other[2];
    }

    public static double[] mul(double[] one, double[] other) {
        return new double[]{one[0] * other[0], one[1] * other[1], one[2] * other[2]};
    }

    public static double[] mul(double scalar, double[] vector) {
        return new double[]{scalar * vector[0], scalar * vector[1], scalar * vector[2]};
    }

    public static double[] mul(double[] vector, double scalar) {
        return mul(scalar, vector);
    }

    public static double[] div(double[] vector, double scalar) {
        return new double[]{vector[0] / scalar, vector[1] / scalar, vector[2] / scalar};
    }

    public static double[] add(double[] one, double[] other) {
        return new double[]{one[0] + other[0], one[1] + other[1], one[2] + other[2]};
    }

    public static double[] add(double[] vector, double scalar) {
        return new double[]{vector[0] + scalar, vector[1] + scalar, vector[2] + scalar};
    }

    public static double[] add(double scalar, double[] vector) {
        return add(vector, scalar);
    }

    public static double[] sub(double[] one, double[] other) {
        return new double[]{one[0] - other[0], one[1] - other[1], one[2] - other[2]};
    }

    public static double[] sub(double[] vector, double scalar) {
        return new double[]{vector[0] - scalar, vector[1] - scalar, vector[2] - scalar};
    }

    public static double[] pow(double[] vector, double[] exponents) {
        return new double[]{Math.pow(vector[0], exponents[0]), Math.pow(vector[1], exponents[1]), Math.pow(vector[2], exponents[2])};
    }

    public static double maxComponentValue(double[] vector) {
        return Math.max(vector[0], Math.max(vector[1], vector[2]));
    }

    public static double[] max(double[] vector, double minimumComponentValue) {
        return new double[]{
                Math.max(vector[0], minimumComponentValue),
                Math.max(vector[1], minimumComponentValue),
                Math.max(vector[2], minimumComponentValue)
        };
    }

    public static void ensureMinimum(double[] vector, double minimumComponentValue) {
        vector[0] = Math.max(vector[0], minimumComponentValue);
        vector[1] = Math.max(vector[1], minimumComponentValue);
        vector[2] = Math.max(vector[2], minimumComponentValue);
    }

    public static double[] min(double[] vector, double maximumComponentValue) {
        return new double[]{
                Math.min(vector[0], maximumComponentValue),
                Math.min(vector[1], maximumComponentValue),
                Math.min(vector[2], maximumComponentValue)
        };
    }

    public static double smoothstep(double edge0, double edge1, double value) {
        double t = Math.clamp((value - edge0) / (edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    public static double step(double edge, double value) {
        return value < edge ? 0 : 1;
    }

    public static double[] clamp(double[] vector, double min, double max) {
        return new double[]{
                Math.clamp(vector[0], min, max),
                Math.clamp(vector[1], min, max),
                Math.clamp(vector[2], min, max)
        };
    }

    public static void clampInPlace(double[] vector, double min, double max) {
        vector[0] = Math.clamp(vector[0], min, max);
        vector[1] = Math.clamp(vector[1], min, max);
        vector[2] = Math.clamp(vector[2], min, max);
    }

    public static double[] log2(double[] vector) {
        return new double[] {
                Math.log(vector[0]) / ln2,
                Math.log(vector[1]) / ln2,
                Math.log(vector[2]) / ln2
        };
    }

    public static void log2InPlace(double[] vector) {
        vector[0] = Math.log(vector[0]) / ln2;
        vector[1] = Math.log(vector[1]) / ln2;
        vector[2] = Math.log(vector[2]) / ln2;
    }

    public static double[] vec3(double value) {
        return new double[] {value, value, value};
    }

    public static double[] vec3(double x, double y, double z) {
        return new double[] {x, y, z};
    }

    public static double[] vec3() {
        return new double[3];
    }

    public static double[] mix(double[] x, double[] y, double a) {
        return add(mul(x, 1 - a), mul(y, a));
    }

}
