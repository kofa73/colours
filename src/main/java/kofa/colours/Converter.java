package kofa.colours;

import kofa.maths.Matrix3x3;
import kofa.maths.Vector3D;

import static java.lang.Math.pow;

public class Converter {
    private Converter() {
        throw new RuntimeException("Don't instantiate this utility class");
    }

    public static double cubeOf(double number) {
        return number * number * number;
    }

    public static double divideSafely(double a, double b) {
        if (b >= 0 && b < 1E-9) {
            b = 1E-9;
        } else if (b > -1E-9) {
            b = -1E-9;
        }
        return a / b;
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


    public static <I extends Vector3D, O extends Vector3D> O convert(I values, Matrix3x3<I, O> conversionMatrix) {
        return conversionMatrix.multiply(values);
    }

    public static XYZ convert_Luv_to_XYZ_D65(Luv valuesLuv) {
        return convert_Luv_to_XYZ(valuesLuv, D65_WHITE_XYZ, D65_WHITE_uvPrime);
    }

    public static XYZ convert_Luv_to_XYZ(Luv valuesLuv, XYZ referenceXYZ) {
        double referenceX = referenceXYZ.X();
        double referenceY = referenceXYZ.Y();

        double referenceDenominator = denominator_XYZ_for_UV(referenceXYZ);
        double referenceU = uPrime(referenceX, referenceDenominator);
        double referenceV = vPrime(referenceY, referenceDenominator);

        return convert_Luv_to_XYZ(valuesLuv, referenceXYZ, new UV(referenceU, referenceV));
    }

    public static XYZ convert_Luv_to_XYZ(Luv valuesLuv, XYZ referenceXYZ, UV referenceUV) {
        double L = valuesLuv.L();
        double u = valuesLuv.u();
        double v = valuesLuv.v();

        double referenceY = referenceXYZ.Y();

        double reference_uPrime = referenceUV.u();
        double reference_vPrime = referenceUV.v();

        double L13 = 13 * L;

        double uPrime = u / L13 + reference_uPrime;
        double vPrime = v / L13 + reference_vPrime;

        double Y = L > KAPPA_EPSILON ?
                (referenceY * pow(((L + 16) / 116), 3)) :
                referenceY * L / KAPPA;

        double denominator = 4 * vPrime;
        double X = Y * 9 * uPrime / denominator;
        double Z = Y * (12 - 3 * uPrime - 20 * vPrime) / denominator;

        return new XYZ(X, Y, Z);
    }

    public static LCh_uv convert_Luv_to_LCH_uv(Luv inputLuv) {
        double L = inputLuv.L();
        double u = inputLuv.u();
        double v = inputLuv.v();
        double C = length(u, v);
        double H = Math.atan2(v, u);
        return new LCh_uv(L, C, H);
    }

    public static Luv convert_LCH_uv_to_Luv(LCh_uv inputLCH) {
        double C = inputLCH.C();
        double h = inputLCH.h();
        double u = (C * Math.cos(h));
        double v = (C * Math.sin(h));
        return new Luv(inputLCH.L(), u, v);
    }

    private static double length(double u, double v) {
        return Math.sqrt(u * u + v * v);
    }

    static double vPrime(double Y, double denominator) {
        return 9 * Y / denominator;
    }

    static double uPrime(double X, double denominator) {
        return 4 * X / denominator;
    }

    static double denominator_XYZ_for_UV(XYZ xyz) {
        var denominator = xyz.X() + 15 * xyz.Y() + 3 * xyz.Z();
        if (denominator == 0.0) {
            denominator = 1E-9;
        }
        return denominator;
    }
}
