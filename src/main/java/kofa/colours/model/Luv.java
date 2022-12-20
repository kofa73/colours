package kofa.colours.model;

import kofa.maths.Vector3D;

import static java.lang.Math.pow;
import static kofa.colours.model.ConversionHelper.*;
import static kofa.colours.model.LChable.toPolar;

public record Luv(double L, double u, double v) implements Vector3D, LChable<Luv, LchUv> {
    public Luv(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    @Override
    public double[] values() {
        return new double[]{L, u, v};
    }

    @Override
    public LchUv toLCh() {
        return new LchUv(toPolar(L, u, v));
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
        public Luv usingWhitePoint(XYZ referenceXYZ, UV referenceUV) {
            // http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Luv.html
            double yr = xyz.Y() / referenceXYZ.Y();

            double reference_uPrime = referenceUV.u();
            double reference_vPrime = referenceUV.v();

            var uv = UV.from(xyz);

            double L = yr > EPSILON ?
                    (116 * cubeRootOf(yr)) - 16 :
                    KAPPA * yr;
            double L13 = 13 * L;
            double u = L13 * (uv.u() - reference_uPrime);
            double v = L13 * (uv.v() - reference_vPrime);

            return new Luv(L, u, v);
        }
    }

    public class LuvXYZConverter implements WhitePointXyzUvAwareConverter<XYZ> {

        @Override
        public XYZ usingWhitePoint(XYZ referenceXYZ, UV referenceUV) {
            double referenceY = referenceXYZ.Y();

            double L13 = 13 * L;

            double uPrime = u / L13 + referenceUV.u();
            double vPrime = v / L13 + referenceUV.v();

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

