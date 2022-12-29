package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.*;

public class CieLab extends ConvertibleToLch<CieLab, CieLchAb> {
    public CieLab(double l, double a, double b) {
        super(l, a, b, CieLchAb::new);
    }

    public double l() {
        return coordinate1;
    }

    public double a() {
        return coordinate2;
    }

    public double b() {
        return coordinate3;
    }

    public static XyzLabConverter from(Xyz xyz) {
        return new XyzLabConverter(xyz);
    }

    public LabXyzConverter toXyz() {
        return new LabXyzConverter();
    }

    public class LabXyzConverter implements WhitePointXyzAwareConverter<Xyz> {
        @Override
        public Xyz usingWhitePoint(Xyz reference) {
            var fy = fy();
            var fx = a() / 500 + fy;
            var fz = fy - b() / 200;
            var xr = fxz(fx);
            var yr = l() > KAPPA_EPSILON ?
                    cubeOf(fy) :
                    l() / KAPPA;
            var zr = fxz(fz);
            return new Xyz(xr * reference.x(), yr * reference.y(), zr * reference.z());
        }

        private double fxz(double value) {
            return value > DELTA ?
                    cubeOf(value) :
                    (116 * value - 16) / KAPPA;
        }

        private double fy() {
            return (l() + 16) / 116;
        }
    }

    public static class XyzLabConverter implements WhitePointXyzAwareConverter<CieLab> {
        private final Xyz xyz;

        public XyzLabConverter(Xyz xyz) {
            this.xyz = xyz;
        }

        @Override
        public CieLab usingWhitePoint(Xyz referenceXyz) {
            double fx = f(xyz.x() / referenceXyz.x());
            double fy = f(xyz.y() / referenceXyz.y());
            double fz = f(xyz.z() / referenceXyz.z());
            double L = 116 * fy - 16;
            double a = 500 * (fx - fy);
            double b = 200 * (fy - fz);
            return new CieLab(L, a, b);
        }

        private double f(double componentRatio) {
            return componentRatio > EPSILON ?
                    cubeRootOf(componentRatio) :
                    (KAPPA * componentRatio + 16) / 116;
        }
    }

    @Override
    public String toString() {
        return "%s(%f, %f, %f)".formatted(this.getClass().getSimpleName(), l(), a(), b());
    }
}
