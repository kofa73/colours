package kofa.colours;

import kofa.maths.Vector3D;

import static kofa.colours.Converter.*;

public record Lab(double L, double a, double b) implements Vector3D {
    public static XYZLabConverter from(XYZ xyz) {
        return new XYZLabConverter(xyz);
    }

    @Override
    public double[] values() {
        return new double[]{L, a, b};
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
            return new

                    Lab(L, a, b);
        }

        private double f(double componentRatio) {
            return componentRatio > EPSILON ?
                    cubeRoot(componentRatio) :
                    (KAPPA * componentRatio + 16) / 116;
        }
    }
}
