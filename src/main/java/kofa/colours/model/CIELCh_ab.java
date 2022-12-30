package kofa.colours.model;

public class CIELCh_ab extends LCh<CIELCh_ab, CIELAB> {
    public CIELCh_ab(double l, double c, double h) {
        super(l, c, h, CIELAB::new);
    }

    public CIELAB toLab() {
        return toVector();
    }
}
