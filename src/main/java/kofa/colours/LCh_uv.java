package kofa.colours;

public record LCh_uv(double L, double C, double h) implements LCh<Luv> {
    public LCh_uv(double[] doubles) {
        this(doubles[0], doubles[1], doubles[2]);
    }

    @Override
    public double[] values() {
        return new double[]{L, C, h};
    }

    public Luv toLuv() {
        return new Luv(LCh.fromPolar(L, C, h));
    }

    @Override
    public String toString() {
        return "%s[%f, %f, %f] [%f, %f, %f]".formatted(getClass().getSimpleName(), L, C, h, L, C, withHueInDegrees()[2]);
    }
}
