package kofa.colours.gamutmapper;

import kofa.colours.model.CieLab;
import kofa.colours.model.Srgb;
import kofa.colours.model.Xyz;

/**
 * Not really a gamut mapper, as all colour info is discarded.
 */
public class BwFromLGamutMapper extends GamutMapper {
    public BwFromLGamutMapper() {
        super(true);
    }

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        var lab = CieLab.from(xyz).usingD65().coordinates();
        lab[1] = 0;
        lab[2] = 0;
        return Srgb.from(new CieLab(lab).toXyz().usingD65());
    }
}
