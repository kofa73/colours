package kofa.colours.model;

interface WhitePointXyzAwareConverter<T> extends WhitePointAwareConverter<T> {
    @Override
    default T usingD65_IEC_61966_2_1() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_IEC_61966_2_1);
    }

    @Override
    default T usingD65_ASTM_E308_01() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_ASTM_E308_01);
    }

    @Override
    default T usingD65_2DEGREE_STANDARD_OBSERVER() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
    }

    @Override
    default T usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER);
    }

    T usingWhitePoint(CIEXYZ referenceXyz);
}
