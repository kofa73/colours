package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.*;

public class CIELUV extends ConvertibleToLch<CIELUV, CIELCh_uv> {
    // less than L from Rec2020(0.0001 / 65535, 0.0001 / 65535, 0.0001 / 65535) ~1.38E-6
    public static final double BLACK_L_LEVEL = 1E-6;
    public static final CIELUV BLACK = new CIELUV(0, 0, 0);

    public CIELUV(double L, double u, double v) {
        super(L, u, v, CIELCh_uv::new);
    }

    public final double u() {
        return coordinate2;
    }

    public final double v() {
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
            if (CIELUV.this.isBlack()) {
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

    public boolean isBlack() {
        return L() < BLACK_L_LEVEL;
    }
}
