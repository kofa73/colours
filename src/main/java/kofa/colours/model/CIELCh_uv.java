package kofa.colours.model;

public class CIELCh_uv extends LCh<CIELCh_uv, CIELUV> {
    public CIELCh_uv(double l, double c, double h) {
        super(l, c, h, CIELUV::new);
    }

    public CIELUV toLuv() {
        return toVector();
    }
}
