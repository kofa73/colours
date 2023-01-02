package kofa.colours.gamutmapper;

import kofa.colours.model.Srgb;
import kofa.colours.tonemapper.SimpleCurveBasedToneMapper;
import kofa.io.RgbImage;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class SrgbClippingGamutMapper extends GamutMapper {
    public SrgbClippingGamutMapper(RgbImage image) {
        super(SimpleCurveBasedToneMapper.forCieLab(image));
    }

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
