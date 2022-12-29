package kofa.colours.model;

import kofa.maths.Vector3;

import static java.lang.Math.abs;

public class Xyz extends Vector3 {
    public Xyz(double x, double y, double z) {
        super(x, y, z);
    }

    public double x() {
        return coordinate1;
    }

    public double y() {
        return coordinate2;
    }

    public double z() {
        return coordinate3;
    }

    public boolean isBlack() {
        return abs(y()) < 1E-6;
    }

    public boolean isWhite() {
        return abs(1 - y()) < 1E-6;
    }


    @Override
    public String toString() {
        return "%s(%f, %f, %f)".formatted(this.getClass().getSimpleName(), x(), y(), z());
    }
}
