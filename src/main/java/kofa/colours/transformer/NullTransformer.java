package kofa.colours.transformer;

import kofa.colours.model.Srgb;
import kofa.colours.model.XYZ;

public class NullTransformer extends Transformer {

    @Override
    public Srgb getInsideGamut(XYZ xyz) {
        return Srgb.from(xyz);
    }
}
