package kofa.colours.model;

public class CIELCh_ab extends LCh<CIELCh_ab, CIELAB> {
    public static final double WHITE_L = CIELAB.WHITE_L;
    public static final CIELCh_ab BLACK = new CIELCh_ab(0, 0, 0);
    public static final CIELCh_ab WHITE = new CIELCh_ab(WHITE_L, 0, 0);
    public static final double BLACK_L_THRESHOLD = CIELAB.BLACK_L_THRESHOLD;

    public CIELCh_ab(double l, double c, double h) {
        super(l, c, h, CIELAB::new);
    }

    public CIELAB toLab() {
        return toVector();
    }

    public boolean isBlack() {
        return L() < BLACK_L_THRESHOLD;
    }

    public boolean isWhite() {
        return L() >= CIELAB.WHITE_L_THRESHOLD;
    }
}
