package kofa.colours.transformer;

import kofa.colours.SRGB;
import kofa.colours.XYZ;

public class NullTransformer extends Transformer {

    @Override
    public double[] getInsideGamut(XYZ xyz) {
        return SRGB.from(xyz).values();
    }
}
