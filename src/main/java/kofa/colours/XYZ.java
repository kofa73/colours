package kofa.colours;

import kofa.maths.Vector3D;

import static java.lang.Math.pow;
import static kofa.colours.Converter.*;

public record XYZ(float X, float Y, float Z) implements Vector3D {
    public XYZ(float[] floats) {
        this(floats[0], floats[1], floats[2]);
    }

    @Override
    public float[] toFloats() {
        return new float[]{X, Y, Z};
    }

    public Luv toLuvUsingWhitePoint(XYZ referenceXYZ) {
        float referenceX = referenceXYZ.X();
        float referenceY = referenceXYZ.Y();
        float referenceZ = referenceXYZ.Z();

        float referenceDenominator = denominator_XYZ_for_u_v(referenceX, referenceY, referenceZ);
        float reference_uPrime = uPrime(referenceX, referenceDenominator);
        float reference_vPrime = vPrime(referenceY, referenceDenominator);

        return toLuvUsingWhitePoint(referenceXYZ, new UV(reference_uPrime, reference_vPrime));
    }

    public Luv toLuvUsingWhitePoint(XYZ referenceXYZ, UV reference_uvPrime) {
        float referenceY = referenceXYZ.Y();
        return toLuvUsingWhitePoint(reference_uvPrime, Y / referenceY);
    }

    public Luv toLuvUsingWhitePoint_Y1(XYZ referenceXYZ, UV reference_uvPrime) {
        float referenceY = referenceXYZ.Y();
        if (referenceY != 1.0f) {
            throw new IllegalArgumentException("Must be called with referenceY=1, but was " + referenceY);
        }
        return toLuvUsingWhitePoint(reference_uvPrime, Y);
    }

    // http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Luv.html
    public Luv toLuvUsingWhitePoint(UV reference_uv, float yr) {
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

    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), X, Y, Z);
    }
}
