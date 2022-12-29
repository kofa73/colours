package kofa.colours.gamutmapper;

import kofa.colours.model.Srgb;

public class NullGamutMapper extends GamutMapper {

    @Override
    public Srgb getInsideGamut(Srgb sRgb) {
        return sRgb;
    }
}
