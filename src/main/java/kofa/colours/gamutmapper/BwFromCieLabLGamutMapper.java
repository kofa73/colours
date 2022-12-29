package kofa.colours.gamutmapper;

import kofa.colours.model.CieLab;
import kofa.colours.model.Srgb;

/**
 * Not really a gamut mapper, as all colour info is discarded.
 */
public class BwFromCieLabLGamutMapper extends GamutMapper {
    public BwFromCieLabLGamutMapper() {
        super(true);
    }

    @Override
    public Srgb getInsideGamut(Srgb srgb) {
        var lab = CieLab.from(srgb.toXyz()).usingD65_IEC_61966_2_1();
        return Srgb.from(new CieLab(lab.l(), 0, 0).toXyz().usingD65_IEC_61966_2_1());
    }
}
