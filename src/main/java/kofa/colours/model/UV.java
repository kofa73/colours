package kofa.colours.model;

import kofa.maths.DoubleVector;

public record UV(double u, double v) implements DoubleVector {
    @Override
    public double[] values() {
        return new double[]{u, v};
    }

    @Override
    public String toString() {
        return "%s[%f, %f]".formatted(getClass().getSimpleName(), u, v);
    }

}
