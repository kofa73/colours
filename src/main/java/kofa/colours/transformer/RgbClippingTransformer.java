package kofa.colours.transformer;

import kofa.colours.model.Srgb;
import kofa.colours.model.Xyz;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class RgbClippingTransformer extends Transformer {

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        var rgb = Srgb.from(xyz).values();
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = max(0, min(rgb[i], 1));
        }
        return new Srgb(rgb);
    }
}
