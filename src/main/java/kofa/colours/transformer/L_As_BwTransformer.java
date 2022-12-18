package kofa.colours.transformer;

import kofa.colours.SRGB;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class L_As_BwTransformer extends Transformer {

    @Override
    public double[] getInsideGamut(SRGB srgb) {
        var rgb = srgb.values();
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = max(0, min(rgb[i], 1));
        }
        return rgb;
    }
}
