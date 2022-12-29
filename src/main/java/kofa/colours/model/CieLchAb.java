package kofa.colours.model;

public class CieLchAb extends Lch<CieLchAb, CieLab> {
    public CieLchAb(double l, double c, double h) {
        super(l, c, h, CieLab::new);
    }

    public CieLab toLab() {
        return toScalar();
    }
}
