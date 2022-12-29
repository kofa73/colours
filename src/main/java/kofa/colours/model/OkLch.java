package kofa.colours.model;

public class OkLch extends Lch<OkLch, OkLab> {
    public OkLch(double l, double c, double h) {
        super(l, c, h, OkLab::new);
    }

    public OkLab toLab() {
        return toScalar();
    }
}
