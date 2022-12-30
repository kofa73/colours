package kofa.colours.model;

public class OkLCh extends LCh<OkLCh, OkLAB> {
    public OkLCh(double l, double c, double h) {
        super(l, c, h, OkLAB::new);
    }

    public OkLAB toLab() {
        return toVector();
    }
}
