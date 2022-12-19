package kofa.colours.transformer;

import kofa.colours.Lab;
import kofa.colours.SRGB;
import kofa.colours.XYZ;

public class L_As_BwTransformer extends Transformer {
    public L_As_BwTransformer() {
        super(true);
    }

    @Override
    public double[] getInsideGamut(XYZ xyz) {
        var lab = Lab.from(xyz).usingD65().values();
        lab[1] = 0;
        lab[2] = 0;
        return SRGB.from(new Lab(lab).toXYZ().usingD65()).values();
    }
}
