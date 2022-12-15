package kofa.colours;

import kofa.maths.Vector3D;

import static kofa.colours.Converter.*;

public record Luv(double L, double u, double v) implements Vector3D {
    public Luv(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    @Override
    public double[] values() {
        return new double[]{L, u, v};
    }

    public LCh_uv toLch() {
        return new LCh_uv(LchHelper.toPolar(L, u, v));
    }

    public static XYZLuvConverter fromXYZ(XYZ xyz) {
        return new XYZLuvConverter(xyz);
    }

    public static class XYZLuvConverter {
        private XYZ xyz;

        XYZLuvConverter(XYZ xyz) {
            this.xyz = xyz;
        }

        public Luv usingD65() {
            return usingWhitePoint(D65_WHITE_XYZ, D65_WHITE_uvPrime);
        }

        public Luv usingWhitePoint(XYZ referenceXYZ) {
            double referenceX = referenceXYZ.X();
            double referenceY = referenceXYZ.Y();

            double referenceDenominator = denominator_XYZ_for_UV(referenceXYZ);
            double referenceU = uPrime(referenceX, referenceDenominator);
            double referenceV = vPrime(referenceY, referenceDenominator);

            return usingWhitePoint(referenceXYZ, new UV(referenceU, referenceV));
        }

        public Luv usingWhitePoint(XYZ referenceXYZ, UV referenceUV) {
            // http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Luv.html
            double yr = xyz.Y() / referenceXYZ.Y();

            double reference_uPrime = referenceUV.u();
            double reference_vPrime = referenceUV.v();

            double denominator = denominator_XYZ_for_UV(xyz);
            double uPrime = uPrime(xyz.X(), denominator);
            double vPrime = vPrime(xyz.Y(), denominator);

            double L = yr > EPSILON ?
                    (116 * cubeRoot(yr)) - 16 :
                    KAPPA * yr;
            double L13 = 13 * L;
            double u = L13 * (uPrime - reference_uPrime);
            double v = L13 * (vPrime - reference_vPrime);

            return new Luv(L, u, v);
        }
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), L, u, v);
    }

    private double length(double u, double v) {
        return Math.sqrt(u * u + v * v);
    }
}
