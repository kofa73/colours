package kofa.colours;

import kofa.maths.DoubleVector;

public record UV(double u, double v) implements DoubleVector {
    public UV(double[] doubles) {
        this(doubles[0], doubles[1]);
    }

    @Override
    public double[] values() {
        return new double[]{u, v};
    }

    @Override
    public String toString() {
        return "%s[%f, %f]".formatted(getClass().getSimpleName(), u, v);
    }

}
