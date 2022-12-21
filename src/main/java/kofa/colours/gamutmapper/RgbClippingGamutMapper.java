package kofa.colours.gamutmapper;

import kofa.colours.model.Srgb;
import kofa.colours.model.Xyz;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class RgbClippingGamutMapper extends GamutMapper {

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        var rgb = Srgb.from(xyz).coordinates();
        for (int i = 0; i < rgb.length; i++) {
            rgb[i] = max(0, min(rgb[i], 1));
        }
        return new Srgb(rgb);
    }
}
