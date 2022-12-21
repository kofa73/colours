package kofa.colours.model;

import kofa.maths.Vector3D;

import static java.lang.Math.pow;
import static kofa.colours.model.ConversionHelper.*;
import static kofa.colours.model.ConvertibleToLch.toPolar;

public record Luv(double L, double u, double v) implements Vector3D, ConvertibleToLch<Luv, LchUv> {
    public Luv(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    @Override
    public double[] values() {
        return new double[]{L, u, v};
    }

    @Override
    public LchUv toLch() {
        return new LchUv(toPolar(L, u, v));
    }

    public static XyzLuvConverter from(Xyz xyz) {
        return new XyzLuvConverter(xyz);
    }

    public LuvXyzConverter toXyz() {
        return new LuvXyzConverter();
    }

    public static class XyzLuvConverter implements WhitePointXyzUvAwareConverter<Luv> {
        private final Xyz xyz;

        XyzLuvConverter(Xyz xyz) {
            this.xyz = xyz;
        }

        public Luv usingWhitePoint(Xyz referenceXyz, Uv referenceUv) {
            // http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Luv.html
            double yr = xyz.Y() / referenceXyz.Y();

            double referenceU = referenceUv.u();
            double referenceV = referenceUv.v();

            var uv = Uv.from(xyz);

            double L = yr > EPSILON ?
                    (116 * cubeRootOf(yr)) - 16 :
                    KAPPA * yr;
            double L13 = 13 * L;
            double u = L13 * (uv.u() - referenceU);
            double v = L13 * (uv.v() - referenceV);

            return new Luv(L, u, v);
        }
    }

    public class LuvXyzConverter implements WhitePointXyzUvAwareConverter<Xyz> {

        @Override
        public Xyz usingWhitePoint(Xyz referenceXyz, Uv referenceUv) {
            double referenceY = referenceXyz.Y();

            double L13 = 13 * L;

            double uPrime = u / L13 + referenceUv.u();
            double vPrime = v / L13 + referenceUv.v();

            double Y = L > KAPPA_EPSILON ?
                    (referenceY * pow(((L + 16) / 116), 3)) :
                    referenceY * L / KAPPA;

            double denominator = 4 * vPrime;
            double X = Y * 9 * uPrime / denominator;
            double Z = Y * (12 - 3 * uPrime - 20 * vPrime) / denominator;

            return new Xyz(X, Y, Z);
        }
    }
}

