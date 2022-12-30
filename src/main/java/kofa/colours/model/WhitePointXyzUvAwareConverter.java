package kofa.colours.model;

import static kofa.colours.model.CIEXYZ.D65_WHITE_IEC_61966_2_1;
import static kofa.colours.model.UV.D65_WHITE_UV_IEC_61966_2_1;

interface WhitePointXyzUvAwareConverter<T> extends WhitePointXyzAwareConverter<T> {
    default T usingD65_IEC_61966_2_1() {
        return usingWhitePoint(D65_WHITE_IEC_61966_2_1, D65_WHITE_UV_IEC_61966_2_1);
    }

    @Override
    default T usingWhitePoint(CIEXYZ referenceXyz) {
        return usingWhitePoint(referenceXyz, UV.from(referenceXyz));
    }

    T usingWhitePoint(CIEXYZ referenceXyz, UV referenceUv);
}
