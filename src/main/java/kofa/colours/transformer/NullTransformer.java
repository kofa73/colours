package kofa.colours.transformer;

import kofa.colours.model.SRGB;
import kofa.colours.model.XYZ;

public class NullTransformer extends Transformer {

    @Override
    public double[] getInsideGamut(XYZ xyz) {
        return SRGB.from(xyz).values();
    }
}
