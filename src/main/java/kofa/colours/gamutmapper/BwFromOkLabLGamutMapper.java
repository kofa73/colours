package kofa.colours.gamutmapper;

import kofa.colours.model.OkLAB;
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
        var lab = OkLAB.from(srgb);
        return new OkLAB(lab.L, 0, 0).toSrgb();
    }
}
