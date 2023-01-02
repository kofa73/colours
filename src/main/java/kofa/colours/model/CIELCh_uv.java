package kofa.colours.model;

public class CIELCh_uv extends LCh<CIELCh_uv, CIELUV> {
    public static final double WHITE_L = CIELUV.WHITE_L;
    public static final CIELCh_uv BLACK = new CIELCh_uv(0, 0, 0);
    public static final CIELCh_uv WHITE = new CIELCh_uv(WHITE_L, 0, 0);
    public static final double BLACK_L_THRESHOLD = CIELUV.BLACK_L_THRESHOLD;

    public CIELCh_uv(double l, double c, double h) {
        super(l, c, h, CIELUV::new);
    }

    public CIELUV toLuv() {
        return toVector();
    }

    public boolean isBlack() {
        return L() < BLACK_L_THRESHOLD;
    }

    public boolean isWhite() {
        return L() >= CIELUV.WHITE_L_THRESHOLD;
    }
}
