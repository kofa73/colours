package kofa.colours;

import kofa.maths.Vector3D;

import static kofa.colours.Converter.*;

public record Lab(double L, double a, double b) implements Vector3D {
    public Lab(double[] values) {
        this(values[0], values[1], values[2]);
    }

    public static XYZLabConverter from(XYZ xyz) {
        return new XYZLabConverter(xyz);
    }

    public LCh_ab toLCh() {
        return new LCh_ab(LchHelper.toPolar(L, a, b));
    }


    @Override
    public double[] values() {
        return new double[]{L, a, b};
    }

    public XYZ toXyz(XYZ reference) {
        var fy = fy();
        var fx = a / 500 + fy;
        var fz = fy - b / 200;
        var xr = fxz(fx);
        var yr = L > KAPPA * EPSILON ?
                cubeOf(fy) :
                L / KAPPA;
        var zr = fxz(fz);
        return new XYZ(xr * reference.X(), yr * reference.Y(), zr * reference.Z());
    }

    private double fxz(double value) {
        return value > DELTA ?
                cubeOf(value) :
                (116 * value - 16) / KAPPA;
    }

    private double fy() {
        return (L + 16) / 116;
    }

    public static class XYZLabConverter {
        private final XYZ xyz;

        public XYZLabConverter(XYZ xyz) {
            this.xyz = xyz;
        }

        public Lab usingD65() {
            return usingWhitePoint(D65_WHITE_XYZ);
        }

        public Lab usingWhitePoint(XYZ referenceXYZ) {
            double fx = f(xyz.X() / referenceXYZ.X());
            double fy = f(xyz.Y() / referenceXYZ.Y());
            double fz = f(xyz.Z() / referenceXYZ.Z());
            double L = 116 * fy - 16;
            double a = 500 * (fx - fy);
            double b = 200 * (fy - fz);
            return new Lab(L, a, b);
        }

        private double f(double componentRatio) {
            return componentRatio > EPSILON ?
                    cubeRoot(componentRatio) :
                    (KAPPA * componentRatio + 16) / 116;
        }
    }
}
