package kofa.colours.model;

public class CIELCh_uv extends LCh<CIELCh_uv, CIELUV> {
    public static final double WHITE_L = 100;
    public static final CIELCh_uv BLACK = new CIELCh_uv(0, 0, 0);

    public CIELCh_uv(double l, double c, double h) {
        super(l, c, h, CIELUV::new, WHITE_L);
    }

    public CIELUV toLuv() {
        return toVector();
    }

    public boolean isBlack() {
        return L() < CIELUV.BLACK_L_LEVEL;
    }
}
