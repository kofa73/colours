package kofa.colours.gamutmapper;

import kofa.colours.model.CieLab;
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
        var lab = CieLab.from(srgb.toXyz()).usingD65_IEC_61966_2_1();
        return Srgb.from(new CieLab(lab.l(), 0, 0).toXyz().usingD65_IEC_61966_2_1());
    }
}
