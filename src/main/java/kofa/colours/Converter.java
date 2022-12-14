package kofa.colours;

import kofa.maths.Matrix3;
import kofa.maths.Vector3D;

import static java.lang.Math.pow;

public class Converter {
    private Converter() {
        throw new RuntimeException("Don't instantiate this utility class");
    }

    // http://www.brucelindbloom.com/LContinuity.html
    // http://www.brucelindbloom.com/Eqn_XYZ_to_Luv.html
    public static final float EPSILON = 216f / 24389f;
    public static final float KAPPA = 24389f / 27f;

    public static final UV D65_WHITE_uvPrime = new UV(0.19783f, 0.46832f);
    public static final XYZ D65_WHITE_XYZ = new XYZ(0.9504559f, 1, 1.0890578f);

    public static final double ONE_THIRD = 1.0 / 3.0;

    public static <I extends Vector3D, O extends Vector3D> O convert(I values, Matrix3<I, O> conversionMatrix) {
        return conversionMatrix.multipliedBy(values);
    }

    public static Luv convert_XYZ_to_Luv_D65(XYZ valuesXYZ) {
        return convert_XYZ_to_Luv_Y1(valuesXYZ, D65_WHITE_XYZ, D65_WHITE_uvPrime);
    }

    public static Luv convert_XYZ_to_Luv(XYZ valuesXYZ, XYZ referenceXYZ) {
        float referenceX = referenceXYZ.X();
        float referenceY = referenceXYZ.Y();
        float referenceZ = referenceXYZ.Z();

        float referenceDenominator = denominator_XYZ_for_u_v(referenceX, referenceY, referenceZ);
        float reference_uPrime = uPrime(referenceX, referenceDenominator);
        float reference_vPrime = vPrime(referenceY, referenceDenominator);

        return convert_XYZ_to_Luv(valuesXYZ, referenceXYZ, new UV(reference_uPrime, reference_vPrime));
    }

    public static Luv convert_XYZ_to_Luv(XYZ valuesXYZ, XYZ referenceXYZ, UV reference_uvPrime) {
        float Y = valuesXYZ.Y();
        float referenceY = referenceXYZ.Y();
        return convert_XYZ_to_Luv(valuesXYZ, reference_uvPrime, Y / referenceY);
    }

    public static Luv convert_XYZ_to_Luv_Y1(XYZ valuesXYZ, XYZ referenceXYZ, UV reference_uvPrime) {
        float referenceY = referenceXYZ.Y();
        if (referenceY != 1.0f) {
            throw new IllegalArgumentException("Must be called with referenceY=1, but was " + referenceY);
        }
        return convert_XYZ_to_Luv(valuesXYZ, reference_uvPrime, valuesXYZ.Y());
    }

    // http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Luv.html
    public static Luv convert_XYZ_to_Luv(XYZ valuesXYZ, UV reference_uv, float yr) {
        float X = valuesXYZ.X();
        float Y = valuesXYZ.Y();
        float Z = valuesXYZ.Z();

        float reference_uPrime = reference_uv.u();
        float reference_vPrime = reference_uv.v();

        float denominator = denominator_XYZ_for_u_v(X, Y, Z);
        float uPrime = uPrime(X, denominator);
        float vPrime = vPrime(Y, denominator);

        float L = yr > EPSILON ?
                (float) (116 * pow(yr, ONE_THIRD)) - 16 :
                KAPPA * yr;
        float L13 = 13 * L;
        float u = L13 * (uPrime - reference_uPrime);
        float v = L13 * (vPrime - reference_vPrime);

        return new Luv(L, u, v);
    }

    public static XYZ convert_Luv_to_XYZ_D65(Luv valuesLuv) {
        return convert_Luv_to_XYZ(valuesLuv, D65_WHITE_XYZ, D65_WHITE_uvPrime);
    }

    public static XYZ convert_Luv_to_XYZ(Luv valuesLuv, XYZ referenceXYZ) {
        float referenceX = referenceXYZ.X();
        float referenceY = referenceXYZ.Y();
        float referenceZ = referenceXYZ.Z();

        float referenceDenominator = denominator_XYZ_for_u_v(referenceX, referenceY, referenceZ);
        float reference_uPrime = uPrime(referenceX, referenceDenominator);
        float reference_vPrime = vPrime(referenceY, referenceDenominator);

        return convert_Luv_to_XYZ(valuesLuv, referenceXYZ, new UV(reference_uPrime, reference_vPrime));
    }

    public static XYZ convert_Luv_to_XYZ(Luv valuesLuv, XYZ referenceXYZ, UV reference_uvPrime) {
        float L = valuesLuv.L();
        float u = valuesLuv.u();
        float v = valuesLuv.v();

        float referenceY = referenceXYZ.Y();

        float reference_uPrime = reference_uvPrime.u();
        float reference_vPrime = reference_uvPrime.v();

        float L13 = 13 * L;

        float uPrime = u / L13 + reference_uPrime;
        float vPrime = v / L13 + reference_vPrime;

        float Y = L > 8 ?
                (float) (referenceY * pow(((L + 16) / 116), 3)) :
                referenceY * L / KAPPA;

        float denominator = 4 * vPrime;
        float X = Y * 9 * uPrime / denominator;
        float Z = Y * (12 - 3 * uPrime - 20 * vPrime) / denominator;

        return new XYZ(X, Y, Z);
    }

    public static LCh_uv convert_Luv_to_LCH_uv(Luv inputLuv) {
        float L = inputLuv.L();
        float u = inputLuv.u();
        float v = inputLuv.v();
        float C = length(u, v);
        float H = (float) Math.atan2(v, u);
        return new LCh_uv(L, C, H);
    }

    public static Luv convert_LCH_uv_to_Luv(LCh_uv inputLCH) {
        float C = inputLCH.C();
        float h = inputLCH.h();
        float u = (float) (C * Math.cos(h));
        float v = (float) (C * Math.sin(h));
        return new Luv(inputLCH.L(), u, v);
    }

    private static float length(float u, float v) {
        return (float) Math.sqrt(u * u + v * v);
    }

    static float vPrime(float Y, float denominator) {
        return 9 * Y / denominator;
    }

    static float uPrime(float X, float denominator) {
        return 4 * X / denominator;
    }

    static float denominator_XYZ_for_u_v(float X, float Y, float Z) {
        return X + 15 * Y + 3 * Z;
    }
}
