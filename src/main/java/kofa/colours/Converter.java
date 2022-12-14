package kofa.colours;

import kofa.maths.Matrix3;
import kofa.maths.Vector3D;

import static java.lang.Math.pow;

public class Converter {
    // http://www.brucelindbloom.com/LContinuity.html
    // http://www.brucelindbloom.com/Eqn_XYZ_to_Luv.html
    private static final float EPSILON = 216f / 24389f;
    private static final float KAPPA = 24389f / 27f;

    public static final UV D65_WHITE_uvPrime = new UV(0.19783f, 0.46832f);
    public static final XYZ D65_WHITE_XYZ = new XYZ(0.9504559f, 1, 1.0890578f);

    // http://www.russellcottrell.com/photo/matrixCalculator.htm
    public static final Matrix3<RGB, XYZ> REC2020_TO_XYZ = new Matrix3<>(XYZ::new)
            .row(0.6369580f, 0.1446169f, 0.1688810f)
            .row(0.2627002f, 0.6779981f, 0.0593017f)
            .row(0.0000000f, 0.0280727f, 1.0609851f);

    public static final Matrix3<XYZ, RGB> XYZ_TO_REC2020 = new Matrix3<>(RGB::new)
            .row(1.7166512f, -0.3556708f, -0.2533663f)
            .row(-0.6666844f, 1.6164812f, 0.0157685f)
            .row(0.0176399f, -0.0427706f, 0.9421031f);

    // http://www.brucelindbloom.com/Eqn_RGB_XYZ_Matrix.html - sRGB D65
    public static final Matrix3<RGB, XYZ> LINEAR_SRGB_TO_XYZ = new Matrix3<>(XYZ::new)
            .row(0.4124564f, 0.3575761f, 0.1804375f)
            .row(0.2126729f, 0.7151522f, 0.0721750f)
            .row(0.0193339f, 0.1191920f, 0.9503041f);

    public static final Matrix3<XYZ, RGB> XYZ_TO_LINEAR_SRGB = new Matrix3<>(RGB::new)
            .row(3.2404542f, -1.5371385f, -0.4985314f)
            .row(-0.9692660f, 1.8760108f, 0.0415560f)
            .row(0.0556434f, -0.2040259f, 1.0572252f);
    private static final double ONE_THIRD = 1d / 3d;

    public <I extends Vector3D, O extends Vector3D> O convert(I values, Matrix3<I, O> conversionMatrix) {
        return conversionMatrix.multipliedBy(values);
    }

    public Luv convert_XYZ_to_Luv_D65(XYZ valuesXYZ) {
        return convert_XYZ_to_Luv_Y1(valuesXYZ, D65_WHITE_XYZ, D65_WHITE_uvPrime);
    }

    public Luv convert_XYZ_to_Luv(XYZ valuesXYZ, XYZ referenceXYZ) {
        float referenceX = referenceXYZ.X();
        float referenceY = referenceXYZ.Y();
        float referenceZ = referenceXYZ.Z();

        float referenceDenominator = denominator_XYZ_for_u_v(referenceX, referenceY, referenceZ);
        float reference_uPrime = uPrime(referenceX, referenceDenominator);
        float reference_vPrime = vPrime(referenceY, referenceDenominator);

        return convert_XYZ_to_Luv(valuesXYZ, referenceXYZ, new UV(reference_uPrime, reference_vPrime));
    }

    public Luv convert_XYZ_to_Luv(XYZ valuesXYZ, XYZ referenceXYZ, UV reference_uvPrime) {
        float Y = valuesXYZ.Y();
        float referenceY = referenceXYZ.Y();
        return convert_XYZ_to_Luv(valuesXYZ, reference_uvPrime, Y / referenceY);
    }

    public Luv convert_XYZ_to_Luv_Y1(XYZ valuesXYZ, XYZ referenceXYZ, UV reference_uvPrime) {
        float referenceY = referenceXYZ.Y();
        if (referenceY != 1.0f) {
            throw new IllegalArgumentException("Must be called with referenceY=1, but was " + referenceY);
        }
        return convert_XYZ_to_Luv(valuesXYZ, reference_uvPrime, valuesXYZ.Y());
    }

    // http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Luv.html
    public Luv convert_XYZ_to_Luv(XYZ valuesXYZ, UV reference_uv, float yr) {
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

    public XYZ convert_Luv_to_XYZ_D65(Luv valuesLuv) {
        return convert_Luv_to_XYZ(valuesLuv, D65_WHITE_XYZ, D65_WHITE_uvPrime);
    }

    public XYZ convert_Luv_to_XYZ(Luv valuesLuv, XYZ referenceXYZ) {
        float referenceX = referenceXYZ.X();
        float referenceY = referenceXYZ.Y();
        float referenceZ = referenceXYZ.Z();

        float referenceDenominator = denominator_XYZ_for_u_v(referenceX, referenceY, referenceZ);
        float reference_uPrime = uPrime(referenceX, referenceDenominator);
        float reference_vPrime = vPrime(referenceY, referenceDenominator);

        return convert_Luv_to_XYZ(valuesLuv, referenceXYZ, new UV(reference_uPrime, reference_vPrime));
    }

    public XYZ convert_Luv_to_XYZ(Luv valuesLuv, XYZ referenceXYZ, UV reference_uvPrime) {
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

    public LCh convert_Luv_to_LCH_uv(Luv inputLuv) {
        float L = inputLuv.L();
        float u = inputLuv.u();
        float v = inputLuv.v();
        float C = length(u, v);
        float H = (float) Math.atan2(v, u);
        return new LCh(L, C, H);
    }

    public Luv convert_LCH_uv_to_Luv(LCh inputLCH) {
        float C = inputLCH.C();
        float h = inputLCH.h();
        float u = (float) (C * Math.cos(h));
        float v = (float) (C * Math.sin(h));
        return new Luv(inputLCH.L(), u, v);
    }

    private float length(float u, float v) {
        return (float) Math.sqrt(u * u + v * v);
    }

    private static float vPrime(float Y, float denominator) {
        return 9 * Y / denominator;
    }

    private static float uPrime(float X, float denominator) {
        return 4 * X / denominator;
    }

    private static float denominator_XYZ_for_u_v(float X, float Y, float Z) {
        return X + 15 * Y + 3 * Z;
    }
}
