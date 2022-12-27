package kofa.colours.model;

import static kofa.colours.model.Lch.fromPolar;

public record CieLchAb(double L, double C, double h) implements Lch {
    public CieLchAb(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    @Override
    public double[] coordinates() {
        return new double[]{L, C, h};
    }

    public CieLab toLab() {
        return new CieLab(fromPolar(L, C, h));
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f] [%f, %f, %f]".formatted(getClass().getSimpleName(), L, C, h, L, C, withHueInDegrees()[2]);
    }
}
