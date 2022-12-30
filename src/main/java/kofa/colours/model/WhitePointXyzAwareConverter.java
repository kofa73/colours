package kofa.colours.model;

import static kofa.colours.model.CIEXYZ.D65_WHITE_IEC_61966_2_1;

interface WhitePointXyzAwareConverter<T> {
    default T usingD65_IEC_61966_2_1() {
        return usingWhitePoint(D65_WHITE_IEC_61966_2_1);
    }

    T usingWhitePoint(CIEXYZ referenceXyz);
}
