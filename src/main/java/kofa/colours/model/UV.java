package kofa.colours.model;

import static kofa.colours.model.CIEXYZ.D65_WHITE_ASTM_E308_01;
import static kofa.colours.model.CIEXYZ.D65_WHITE_IEC_61966_2_1;

public record UV(double u, double v) {
    public static final UV D65_WHITE_UV_ASTM_E308_01 = UV.from(D65_WHITE_ASTM_E308_01);
    public static final UV D65_WHITE_UV_IEC_61966_2_1 = UV.from(D65_WHITE_IEC_61966_2_1);

    public static final UV D65_2DEGREE_STANDARD_OBSERVER = UV.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
    public static final UV D65_10DEGREE_SUPPLEMENTARY_OBSERVER = UV.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);

    public static UV from(CIEXYZ xyz) {
        var denominator = denominatorXyzForUv(xyz);
        return new UV(
                uPrime(xyz.X(), denominator),
                vPrime(xyz.Y(), denominator)
        );
    }

    private static double denominatorXyzForUv(CIEXYZ xyz) {
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

    @Override
    public String toString() {
        return "%s(%f, %f)".formatted(this.getClass().getSimpleName(), u(), v());
    }
}
