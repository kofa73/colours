package kofa.colours.model;

interface WhitePointAwareConverter<T> {
    T usingD65_IEC_61966_2_1();

    T usingD65_ASTM_E308_01();

    T usingD65_2DEGREE_STANDARD_OBSERVER();

    T usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER();
}
