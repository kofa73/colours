package kofa.colours.model;

interface WhitePointXyzAwareConverter<T> {
    default T usingD65_IEC_61966_2_1() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_IEC_61966_2_1);
    }

    default T usingD65_2DegreeStandardObserver() {
        return usingWhitePoint(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
    }

    T usingWhitePoint(CIEXYZ referenceXyz);
}
