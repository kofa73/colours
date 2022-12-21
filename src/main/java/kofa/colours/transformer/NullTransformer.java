package kofa.colours.transformer;

import kofa.colours.model.Srgb;
import kofa.colours.model.Xyz;

public class NullTransformer extends Transformer {

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        return Srgb.from(xyz);
    }
}
