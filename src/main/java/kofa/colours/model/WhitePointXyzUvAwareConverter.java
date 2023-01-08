package kofa.colours.model;

interface WhitePointXyzUvAwareConverter<T> extends WhitePointXyzAwareConverter<T> {
    @Override
    default T usingD65_IEC_61966_2_1() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_IEC_61966_2_1, UV.D65_IEC_61966_2_1);
    }

    @Override
    default T usingD65_ASTM_E308_01() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_ASTM_E308_01, UV.D65_ASTM_E308_01);
    }

    @Override
    default T usingD65_2DEGREE_STANDARD_OBSERVER() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, UV.D65_2DEGREE_STANDARD_OBSERVER);
    }

    @Override
    default T usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER, UV.D65_10DEGREE_SUPPLEMENTARY_OBSERVER);
    }

    @Override
    default T usingWhitePoint(CIEXYZ referenceXyz) {
        return usingWhitePoint(referenceXyz, UV.from(referenceXyz));
    }

    T usingWhitePoint(CIEXYZ referenceXyz, UV referenceUv);
}
