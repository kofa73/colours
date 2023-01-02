package kofa.colours.model;

public class CIELCh_ab extends LCh<CIELCh_ab, CIELAB> {
    public static final double WHITE_L = 100;
    public static final CIELCh_ab BLACK = new CIELCh_ab(0, 0, 0);

    public CIELCh_ab(double l, double c, double h) {
        super(l, c, h, CIELAB::new, WHITE_L);
    }

    public CIELAB toLab() {
        return toVector();
    }

    public boolean isBlack() {
        return L() < CIELAB.BLACK_L_LEVEL;
    }
}
