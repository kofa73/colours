package kofa.colours.transformer;

import kofa.colours.SRGB;

public class DesaturatingTransformer extends Transformer {

    @Override
    public double[] getInsideGamut(SRGB srgb) {
        return srgb.values();
    }
}
