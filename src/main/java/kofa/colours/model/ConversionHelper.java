package kofa.colours.model;

import static java.lang.Math.cbrt;

public class ConversionHelper {
    private ConversionHelper() {
        throw new RuntimeException("Don't instantiate this utility class");
    }

    public static double cubeOf(double number) {
        return number * number * number;
    }

    public static double cubeRootOf(double number) {
        return cbrt(number);
    }

    // http://www.brucelindbloom.com/LContinuity.html
    // http://www.brucelindbloom.com/Eqn_XYZ_to_Luv.html
    public static final double DELTA = 6d / 29;
    public static final double EPSILON = 216d / 24389;
    public static final double KAPPA = 24389d / 27;
    // KAPPA * EPSILON
    public static final double KAPPA_EPSILON = 8;

    public static final Xyz D65_WHITE_XYZ = new Xyz(0.9504559, 1, 1.0890578);
    public static final Uv D65_WHITE_UV = Uv.from(D65_WHITE_XYZ);
}
