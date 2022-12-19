package kofa.colours.model;

import kofa.maths.Matrix3x3;
import kofa.maths.Vector3D;

public abstract class Rgb<S extends Rgb<S>> implements Vector3D {
    public final double r;
    public final double g;
    public final double b;

    Rgb(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    Rgb(double r, double g, double b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    protected abstract Matrix3x3<S, XYZ> toXyzMatrix();

    public XYZ toXYZ() {
        return toXyzMatrix().multiply((S) this);
    }

    @Override
    public double[] values() {
        return new double[]{r, g, b};
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), r, g, b);
    }

    public boolean isOutOfGamut() {
        return r < 0 || r > 1 || g < 0 || g > 1 || b < 0 || b > 1;
    }
}
