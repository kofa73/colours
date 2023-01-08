package kofa.colours.model;

public class OkLABTunerLCh extends LCh<OkLABTunerLCh, OkLABTuner> {
    public static final double WHITE_L = 1;
    public static final double BLACK_L_THRESHOLD = OkLABTuner.BLACK_L_THRESHOLD;

    public OkLABTunerLCh(double l, double c, double h) {
        super(l, c, h, OkLABTuner::new);
    }

    public OkLABTuner toLab() {
        return toVector();
    }

    public boolean isBlack() {
        return L() < OkLABTuner.BLACK_L_THRESHOLD;
    }
}
