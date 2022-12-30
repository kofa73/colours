package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.*;

public class CIELAB extends LAB<CIELAB, CIELCh_ab> {
    public CIELAB(double L, double a, double b) {
        super(L, a, b, CIELCh_ab::new);
    }

    public static XyzLabConverter from(CIEXYZ xyz) {
        return new XyzLabConverter(xyz);
    }

    public LabXyzConverter toXyz() {
        return new LabXyzConverter();
    }

    public class LabXyzConverter implements WhitePointXyzAwareConverter<CIEXYZ> {
        @Override
        public CIEXYZ usingWhitePoint(CIEXYZ reference) {
            var fy = fy();
            var fx = a / 500 + fy;
            var fz = fy - b / 200;
            var xr = fxz(fx);
            var yr = L > KAPPA_EPSILON ?
                    cubeOf(fy) :
                    L / KAPPA;
            var zr = fxz(fz);
            return new CIEXYZ(xr * reference.X, yr * reference.Y, zr * reference.Z);
        }

        private double fxz(double value) {
            return value > DELTA ?
                    cubeOf(value) :
                    (116 * value - 16) / KAPPA;
        }

        private double fy() {
            return (L + 16) / 116;
        }
    }

    public static class XyzLabConverter implements WhitePointXyzAwareConverter<CIELAB> {
        private final CIEXYZ xyz;

        public XyzLabConverter(CIEXYZ xyz) {
            this.xyz = xyz;
        }

        @Override
        public CIELAB usingWhitePoint(CIEXYZ referenceXyz) {
            double fx = f(xyz.X / referenceXyz.X);
            double fy = f(xyz.Y / referenceXyz.Y);
            double fz = f(xyz.Z / referenceXyz.Z);
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
