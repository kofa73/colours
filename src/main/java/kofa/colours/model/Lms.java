package kofa.colours.model;

import kofa.maths.Vector3D;

public record Lms(double l, double m, double s) implements Vector3D {
    public Lms(double[] coordinates) {
        this(coordinates[0], coordinates[1], coordinates[2]);
    }

    @Override
    public double[] coordinates() {
        return new double[]{l, m, s};
    }
}
