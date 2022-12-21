package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.D65_WHITE_UV;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ;

interface WhitePointXyzUvAwareConverter<T> extends WhitePointXyzAwareConverter<T> {
    default T usingD65() {
        return usingWhitePoint(D65_WHITE_XYZ, D65_WHITE_UV);
    }

    @Override
    default T usingWhitePoint(Xyz referenceXyz) {
        return usingWhitePoint(referenceXyz, Uv.from(referenceXyz));
    }

    T usingWhitePoint(Xyz referenceXyz, Uv referenceUv);
}
