package kofa.colours.model;

import static java.lang.Math.pow;

public class ConversionHelper {
    private ConversionHelper() {
        throw new RuntimeException("Don't instantiate this utility class");
    }

    public static double cubeOf(double number) {
        return number * number * number;
    }

    public static double cubeRootOf(double number) {
        return pow(number, 1.0 / 3.0);
    }

    // http://www.brucelindbloom.com/LContinuity.html
    // http://www.brucelindbloom.com/Eqn_XYZ_to_Luv.html
    public static final double DELTA = 6d / 29;
    public static final double EPSILON = 216d / 24389;
    public static final double KAPPA = 24389d / 27;
    // KAPPA * EPSILON
    public static final double KAPPA_EPSILON = 8;

    public static final UV D65_WHITE_uvPrime = new UV(0.19783, 0.46832);
    public static final XYZ D65_WHITE_XYZ = new XYZ(0.9504559, 1, 1.0890578);
}
