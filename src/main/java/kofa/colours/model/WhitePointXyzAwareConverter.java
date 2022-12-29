package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ_IEC_61966_2_1;

interface WhitePointXyzAwareConverter<T> {
    default T usingD65_IEC_61966_2_1() {
        return usingWhitePoint(D65_WHITE_XYZ_IEC_61966_2_1);
    }

    T usingWhitePoint(Xyz referenceXyz);
}
