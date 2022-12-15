package kofa.colours;

import kofa.maths.Vector3D;

public record LCh_uv(double L, double C, double h) implements Vector3D {
    public LCh_uv(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    @Override
    public double[] values() {
        return new double[]{L, C, h};
    }

    public Luv toLuv() {
        double u = (C * Math.cos(h));
        double v = (C * Math.sin(h));
        return new Luv(L, u, v);
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), L, C, h);
    }
}
