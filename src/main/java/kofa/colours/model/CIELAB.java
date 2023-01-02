package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.*;

public class CIELAB extends LAB<CIELAB, CIELCh_ab> {
    // less than L from Rec2020(0.0001 / 65535, 0.0001 / 65535, 0.0001 / 65535) ~1.38E-6
    public static final double BLACK_L_THRESHOLD = 1E-6;
    public static final CIELAB BLACK = new CIELAB(0, 0, 0);
    public static final int WHITE_L = 100;
    public static final double WHITE_L_THRESHOLD = WHITE_L - BLACK_L_THRESHOLD;
    public static final CIELAB WHITE = new CIELAB(WHITE_L, 0, 0);

    public CIELAB(double L, double a, double b) {
        super(L, a, b, CIELCh_ab::new);
    }

    public static XyzLabConverter from(CIEXYZ xyz) {
        return new XyzLabConverter(xyz);
    }

    public LabXyzConverter toXyz() {
        return new LabXyzConverter();
    }

    public boolean isWhite() {
        return L() >= WHITE_L_THRESHOLD;
    }

    public class LabXyzConverter implements WhitePointXyzAwareConverter<CIEXYZ> {
        @Override
        public CIEXYZ usingWhitePoint(CIEXYZ reference) {
            if (CIELAB.this.isBlack()) {
                return CIEXYZ.BLACK;
            }
            var fy = fy();
            var fx = a() / 500 + fy;
            var fz = fy - b() / 200;
            var xr = fxz(fx);
            var yr = L() > KAPPA_EPSILON ?
                    cubeOf(fy) :
                    L() / KAPPA;
            var zr = fxz(fz);
            return new CIEXYZ(xr * reference.X(), yr * reference.Y(), zr * reference.Z());
        }

        private double fxz(double value) {
            return value > DELTA ?
                    cubeOf(value) :
                    (116 * value - 16) / KAPPA;
        }

        private double fy() {
            return (L() + 16) / 116;
        }
    }

    public boolean isBlack() {
        return L() < BLACK_L_THRESHOLD;
    }

    public static class XyzLabConverter implements WhitePointXyzAwareConverter<CIELAB> {
        private final CIEXYZ xyz;

        public XyzLabConverter(CIEXYZ xyz) {
            this.xyz = xyz;
        }

        @Override
        public CIELAB usingWhitePoint(CIEXYZ referenceXyz) {
            if (xyz.isBlack()) {
                return CIELAB.BLACK;
            }
            double fx = f(xyz.X() / referenceXyz.X());
            double fy = f(xyz.Y() / referenceXyz.Y());
            double fz = f(xyz.Z() / referenceXyz.Z());
            double L = 116 * fy - 16;
            double a = 500 * (fx - fy);
            double b = 200 * (fy - fz);
            return new CIELAB(L, a, b);
        }

        private double f(double componentRatio) {
            return componentRatio > EPSILON ?
                    cubeRootOf(componentRatio) :
                    (KAPPA * componentRatio + 16) / 116;
        }
    }
}
