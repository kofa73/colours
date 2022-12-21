package kofa.colours.model;

import kofa.maths.Vector3D;

import static kofa.colours.model.ConversionHelper.*;
import static kofa.colours.model.ConvertibleToLch.toPolar;

public record Lab(double L, double a, double b) implements Vector3D, ConvertibleToLch<LchAb> {
    public Lab(double[] values) {
        this(values[0], values[1], values[2]);
    }

    public static XyzLabConverter from(Xyz xyz) {
        return new XyzLabConverter(xyz);
    }

    @Override
    public LchAb toLch() {
        return new LchAb(toPolar(L, a, b));
    }

    @Override
    public double[] coordinates() {
        return new double[]{L, a, b};
    }

    public LabXyzConverter toXyz() {
        return new LabXyzConverter();
    }

    public class LabXyzConverter implements WhitePointXyzAwareConverter<Xyz> {
        @Override
        public Xyz usingWhitePoint(Xyz reference) {
            var fy = fy();
            var fx = a / 500 + fy;
            var fz = fy - b / 200;
            var xr = fxz(fx);
            var yr = L > KAPPA_EPSILON ?
                    cubeOf(fy) :
                    L / KAPPA;
            var zr = fxz(fz);
            return new Xyz(xr * reference.X(), yr * reference.Y(), zr * reference.Z());
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

    public static class XyzLabConverter implements WhitePointXyzAwareConverter<Lab> {
        private final Xyz xyz;

        public XyzLabConverter(Xyz xyz) {
            this.xyz = xyz;
        }

        @Override
        public Lab usingWhitePoint(Xyz referenceXyz) {
            double fx = f(xyz.X() / referenceXyz.X());
            double fy = f(xyz.Y() / referenceXyz.Y());
            double fz = f(xyz.Z() / referenceXyz.Z());
            double L = 116 * fy - 16;
            double a = 500 * (fx - fy);
            double b = 200 * (fy - fz);
            return new Lab(L, a, b);
        }

        private double f(double componentRatio) {
            return componentRatio > EPSILON ?
                    cubeRootOf(componentRatio) :
                    (KAPPA * componentRatio + 16) / 116;
        }
    }
}
