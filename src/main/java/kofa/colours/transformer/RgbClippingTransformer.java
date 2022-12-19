package kofa.colours.transformer;

import kofa.colours.SRGB;
import kofa.colours.XYZ;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class RgbClippingTransformer extends Transformer {

    @Override
    public double[] getInsideGamut(XYZ xzy) {
        var rgb = SRGB.from(xzy).values();
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = max(0, min(rgb[i], 1));
        }
        return rgb;
    }
}
