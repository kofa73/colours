package kofa.colours;

import kofa.maths.Vector3D;

import static java.lang.Math.pow;
import static kofa.colours.Converter.*;
import static kofa.colours.LChable.toPolar;

public record Luv(double L, double u, double v) implements Vector3D, LChable<Luv, LCh_uv> {
    public Luv(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    @Override
    public double[] values() {
        return new double[]{L, u, v};
    }

    @Override
    public LCh_uv toLCh() {
        return new LCh_uv(toPolar(L, u, v));
    }

    public static XYZLuvConverter from(XYZ xyz) {
        return new XYZLuvConverter(xyz);
    }

    public LuvXYZConverter toXYZ() {
        return new LuvXYZConverter();
    }

    public static class XYZLuvConverter implements WhitePointXyzUvAwareConverter<Luv> {
        private final XYZ xyz;

        XYZLuvConverter(XYZ xyz) {
            this.xyz = xyz;
        }

        @Override
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
                    (116 * cubeRootOf(yr)) - 16 :
                    KAPPA * yr;
            double L13 = 13 * L;
            double u = L13 * (uPrime - reference_uPrime);
            double v = L13 * (vPrime - reference_vPrime);

            return new Luv(L, u, v);
        }
    }

    public class LuvXYZConverter implements WhitePointXyzUvAwareConverter<XYZ> {

        @Override
        public XYZ usingWhitePoint(XYZ referenceXYZ, UV referenceUV) {
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
    }
}
