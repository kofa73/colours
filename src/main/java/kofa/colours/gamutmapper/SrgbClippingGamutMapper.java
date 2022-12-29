package kofa.colours.gamutmapper;

import kofa.colours.model.Srgb;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class SrgbClippingGamutMapper extends GamutMapper {

    @Override
    public Srgb getInsideGamut(Srgb sRgb) {
        return new Srgb(
                clip(sRgb.r()),
                clip(sRgb.g()),
                clip(sRgb.b())
        );
    }

    private double clip(double value) {
        return max(0, min(value, 1));
    }
}
