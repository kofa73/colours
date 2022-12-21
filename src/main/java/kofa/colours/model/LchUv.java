package kofa.colours.model;

import static kofa.colours.model.Lch.fromPolar;

public record LchUv(double L, double C, double h) implements Lch<Luv> {
    public LchUv(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    @Override
    public double[] values() {
        return new double[]{L, C, h};
    }

    public Luv toLuv() {
        return new Luv(fromPolar(L, C, h));
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f] [%f, %f, %f]".formatted(getClass().getSimpleName(), L, C, h, L, C, withHueInDegrees()[2]);
    }
}
