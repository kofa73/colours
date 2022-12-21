package kofa.colours.gamutmapper;

import kofa.colours.model.Srgb;
import kofa.colours.model.Xyz;

public class NullGamutMapper extends GamutMapper {

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        return Srgb.from(xyz);
    }
}
