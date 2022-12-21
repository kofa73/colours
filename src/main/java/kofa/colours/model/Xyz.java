package kofa.colours.model;

import kofa.maths.Vector3D;

public record Xyz(double X, double Y, double Z) implements Vector3D {
    public Xyz(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    @Override
    public double[] values() {
        return new double[]{X, Y, Z};
    }


    @Override
    public String toString() {
        return "%s[%f, %f, %f]".formatted(getClass().getSimpleName(), X, Y, Z);
    }
}
