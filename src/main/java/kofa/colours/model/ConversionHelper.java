package kofa.colours.model;

import static java.lang.Math.cbrt;

public class ConversionHelper {
    private ConversionHelper() {
        throw new RuntimeException("Don't instantiate this utility class");
    }

    public static double cubeOf(double number) {
        return number * number * number;
    }

//    public static float cubeOfFloat(double number) {
//        float f = (float) number;
//        return f * f * f;
//    }

    public static double cubeRootOf(double number) {
        return cbrt(number);
    }

//    public static float cubeRootOfFloat(double number) {
//        return (float) cbrt((float) number);
//    }

    // http://www.brucelindbloom.com/LContinuity.html
    // http://www.brucelindbloom.com/Eqn_XYZ_to_Luv.html
    public static final double DELTA = 6d / 29;
    public static final double EPSILON = 216d / 24389;
    public static final double KAPPA = 24389d / 27;
    // KAPPA * EPSILON
    public static final double KAPPA_EPSILON = 8;

    // http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
    public static final Xyz D65_WHITE_XYZ_ASTM_E308_01 = new Xyz(0.95047, 1, 1.08883);
    public static final Uv D65_WHITE_UV_ASTM_E308_01 = Uv.from(D65_WHITE_XYZ_ASTM_E308_01);
    // alternatively, IEC 61966-2-1, https://en.wikipedia.org/wiki/Illuminant_D65#Definition
    public static final Xyz D65_WHITE_XYZ_IEC_61966_2_1 = new Xyz(0.9504559, 1, 1.0890578);
    public static final Uv D65_WHITE_UV_IEC_61966_2_1 = Uv.from(D65_WHITE_XYZ_IEC_61966_2_1);
}
