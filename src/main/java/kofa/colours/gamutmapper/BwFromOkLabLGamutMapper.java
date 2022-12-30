package kofa.colours.gamutmapper;

import kofa.colours.model.OkLab;
import kofa.colours.model.Srgb;
import kofa.colours.tonemapper.SimpleCurveBasedToneMapper;
import kofa.io.RgbImage;

/**
 * Not really a gamut mapper, as all colour info is discarded.
 */
public class BwFromOkLabLGamutMapper extends GamutMapper {
    public BwFromOkLabLGamutMapper(RgbImage image) {
        super(true, SimpleCurveBasedToneMapper.forOkLab(image), image);
    }

    @Override
    public Srgb getInsideGamut(Srgb srgb) {
        var lab = OkLab.from(srgb);
        return new OkLab(lab.l(), 0, 0).toSrgb();
    }
}
