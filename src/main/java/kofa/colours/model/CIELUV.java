package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.*;

public class CIELUV extends ConvertibleToLch<CIELUV, CIELCh_uv> {
    public static final CIELUV BLACK = new CIELUV(0, 0, 0);

    public CIELUV(double l, double u, double v) {
        super(l, u, v, CIELCh_uv::new);
    }

    public double L() {
        return coordinate1;
    }

    public double u() {
        return coordinate2;
    }

    public double v() {
        return coordinate3;
    }

    public static XyzLuvConverter from(CIEXYZ xyz) {
        return new XyzLuvConverter(xyz);
    }

    public LuvXyzConverter toXyz() {
        return new LuvXyzConverter();
    }

    public static class XyzLuvConverter implements WhitePointXyzUvAwareConverter<CIELUV> {
        private final CIEXYZ xyz;

        XyzLuvConverter(CIEXYZ xyz) {
            this.xyz = xyz;
        }

        public CIELUV usingWhitePoint(CIEXYZ referenceXyz, UV referenceUv) {
            if (xyz.isBlack()) {
                return BLACK;
            }
            // http://www.brucelindbloom.com/index.html?Eqn_XYZ_to_Luv.html
            double yr = xyz.Y() / referenceXyz.Y();

            var uv = UV.from(xyz);

            double L = yr > EPSILON ?
                    (116 * cubeRootOf(yr)) - 16 :
                    KAPPA * yr;
            double L13 = 13 * L;
            double u = L13 * (uv.u() - referenceUv.u());
            double v = L13 * (uv.v() - referenceUv.v());

            return new CIELUV(L, u, v);
        }
    }

    public class LuvXyzConverter implements WhitePointXyzUvAwareConverter<CIEXYZ> {

        @Override
        public CIEXYZ usingWhitePoint(CIEXYZ referenceXYZ, UV referenceUV) {
            if (L() == 0) {
                return CIEXYZ.BLACK;
            }

            double referenceY = referenceXYZ.Y();

            double L13 = 13 * L();

            double uPrime = u() / L13 + referenceUV.u();
            double vPrime = v() / L13 + referenceUV.v();

            double Y = L() > KAPPA_EPSILON ?
                    referenceY * cubeOf((L() + 16) / 116) :
                    referenceY * L() / KAPPA;

            double denominator = 4 * vPrime;
            double X = Y * 9 * uPrime / denominator;
            double Z = Y * (12 - 3 * uPrime - 20 * vPrime) / denominator;

            return new CIEXYZ(X, Y, Z);
        }
    }

    @Override
    public final String toString() {
        return "%s(%f, %f, %f)".formatted(getClass().getSimpleName(), L(), u(), v());
    }
}

