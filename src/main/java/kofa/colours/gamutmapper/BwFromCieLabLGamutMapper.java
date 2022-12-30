package kofa.colours.gamutmapper;

import kofa.colours.model.CIELAB;
import kofa.colours.model.Srgb;
import kofa.colours.tonemapper.SimpleCurveBasedToneMapper;
import kofa.io.RgbImage;

/**
 * Not really a gamut mapper, as all colour info is discarded.
 */
public class BwFromCieLabLGamutMapper extends GamutMapper {
    public BwFromCieLabLGamutMapper(RgbImage image) {
        super(true, SimpleCurveBasedToneMapper.forCieLab(image), image);
    }

    @Override
    public Srgb getInsideGamut(Srgb srgb) {
        var lab = CIELAB.from(srgb.toXyz()).usingD65_2DegreeStandardObserver();
        return Srgb.from(new CIELAB(lab.L(), 0, 0).toXyz().usingD65_2DegreeStandardObserver());
    }
}
