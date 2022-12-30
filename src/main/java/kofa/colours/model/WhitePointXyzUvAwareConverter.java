package kofa.colours.model;

interface WhitePointXyzUvAwareConverter<T> extends WhitePointXyzAwareConverter<T> {
    default T usingD65_IEC_61966_2_1() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_IEC_61966_2_1, UV.D65_IEC_61966_2_1);
    }

    default T usingD65_2DEGREE_STANDARD_OBSERVER() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, UV.D65_2DEGREE_STANDARD_OBSERVER);
    }

    @Override
    default T usingWhitePoint(CIEXYZ referenceXyz) {
        return usingWhitePoint(referenceXyz, UV.from(referenceXyz));
    }

    T usingWhitePoint(CIEXYZ referenceXyz, UV referenceUv);
}
