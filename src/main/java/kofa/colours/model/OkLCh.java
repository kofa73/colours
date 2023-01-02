package kofa.colours.model;

public class OkLCh extends LCh<OkLCh, OkLAB> {
    public static final double WHITE_L = 1;

    public OkLCh(double l, double c, double h) {
        super(l, c, h, OkLAB::new, WHITE_L);
    }

    public OkLAB toLab() {
        return toVector();
    }

    public boolean isBlack() {
        return L() < OkLAB.BLACK_L_LEVEL;
    }
}
