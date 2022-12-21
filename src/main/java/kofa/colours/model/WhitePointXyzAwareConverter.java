package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ;

interface WhitePointXyzAwareConverter<T> {
    default T usingD65() {
        return usingWhitePoint(D65_WHITE_XYZ);
    }

    T usingWhitePoint(Xyz referenceXyz);
}
