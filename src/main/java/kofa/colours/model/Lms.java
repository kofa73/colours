package kofa.colours.model;

import kofa.maths.Vector3;

public class Lms extends Vector3 {
    protected Lms(double l, double m, double s) {
        super(l, m, s);
    }

    double l() {
        return coordinate1;
    }

    double m() {
        return coordinate2;
    }

    double s() {
        return coordinate3;
    }

    @Override
    public String toString() {
        return "%s(%f, %f, %f)".formatted(this.getClass().getSimpleName(), l(), m(), s());
    }
}
