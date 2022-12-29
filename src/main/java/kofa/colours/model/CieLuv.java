package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.*;

public class CieLuv extends ConvertibleToLch<CieLuv, CieLchUv> {
    public static final CieLuv BLACK = new CieLuv(0, 0, 0);

    public CieLuv(double l, double u, double v) {
        super(l, u, v, CieLchUv::new);
    }

    public static XyzLuvConverter from(Xyz xyz) {
        return new XyzLuvConverter(xyz);
    }

    public LuvXyzConverter toXyz() {
        return new LuvXyzConverter();
    }

    public static class XyzLuvConverter implements WhitePointXyzUvAwareConverter<CieLuv> {
        private final Xyz xyz;

        XyzLuvConverter(Xyz xyz) {
            this.xyz = xyz;
        }

        public CieLuv usingWhitePoint(Xyz referenceXyz, Uv referenceUv) {
            if (xyz.y() == 0) {
                return BLACK;
            }
            // http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Luv.html
            double yr = xyz.y() / referenceXyz.y();

            var uv = Uv.from(xyz);

            double L = yr > EPSILON ?
                    (116 * cubeRootOf(yr)) - 16 :
                    KAPPA * yr;
            double L13 = 13 * L;
            double u = L13 * (uv.u() - referenceUv.u());
            double v = L13 * (uv.v() - referenceUv.v());

            return new CieLuv(L, u, v);
        }
    }

    public class LuvXyzConverter implements WhitePointXyzUvAwareConverter<Xyz> {

        @Override
        public Xyz usingWhitePoint(Xyz referenceXyz, Uv referenceUv) {
            double referenceY = referenceXyz.y();

            double L13 = 13 * l();

            double uPrime = u() / L13 + referenceUv.u();
            double vPrime = v() / L13 + referenceUv.v();

            double Y = l() > KAPPA_EPSILON ?
                    referenceY * cubeOf((l() + 16) / 116) :
                    referenceY * l() / KAPPA;

            double denominator = 4 * vPrime;
            double X = Y * 9 * uPrime / denominator;
            double Z = Y * (12 - 3 * uPrime - 20 * vPrime) / denominator;

            return new Xyz(X, Y, Z);
        }
    }

    public double l() {
        return coordinate1;
    }

    public double u() {
        return coordinate2;
    }

    public double v() {
        return coordinate3;
    }

    @Override
    public final String toString() {
        return "%s(%f, %f, %f)".formatted(getClass().getSimpleName(), l(), u(), v());
    }
}

