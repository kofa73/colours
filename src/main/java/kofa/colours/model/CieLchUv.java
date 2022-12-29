package kofa.colours.model;

public class CieLchUv extends Lch<CieLchUv, CieLuv> {
    public CieLchUv(double l, double c, double h) {
        super(l, c, h, CieLuv::new);
    }

    public CieLuv toLuv() {
        return toScalar();
    }
}
