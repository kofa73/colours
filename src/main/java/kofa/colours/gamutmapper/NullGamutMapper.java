package kofa.colours.gamutmapper;

import kofa.colours.model.Srgb;

public class NullGamutMapper extends GamutMapper {
    public NullGamutMapper() {
        super(ignoredImageWontMap -> {
        });
    }

    @Override
    public Srgb getInsideGamut(Srgb sRgb) {
        return sRgb;
    }
}
