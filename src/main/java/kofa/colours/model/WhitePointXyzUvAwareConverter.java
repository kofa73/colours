package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ;
import static kofa.colours.model.ConversionHelper.D65_WHITE_uvPrime;

interface WhitePointXyzUvAwareConverter<T> extends WhitePointXyzAwareConverter<T> {
    default T usingD65() {
        return usingWhitePoint(D65_WHITE_XYZ, D65_WHITE_uvPrime);
    }

    @Override
    default T usingWhitePoint(XYZ referenceXYZ) {
        return usingWhitePoint(referenceXYZ, UV.from(referenceXYZ));
    }

    T usingWhitePoint(XYZ referenceXYZ, UV referenceUV);
}
