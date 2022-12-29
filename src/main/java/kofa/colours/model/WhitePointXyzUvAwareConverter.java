package kofa.colours.model;

import static kofa.colours.model.ConversionHelper.D65_WHITE_UV_IEC_61966_2_1;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ_IEC_61966_2_1;

interface WhitePointXyzUvAwareConverter<T> extends WhitePointXyzAwareConverter<T> {
    default T usingD65_IEC_61966_2_1() {
        return usingWhitePoint(D65_WHITE_XYZ_IEC_61966_2_1, D65_WHITE_UV_IEC_61966_2_1);
    }

    @Override
    default T usingWhitePoint(Xyz referenceXyz) {
        return usingWhitePoint(referenceXyz, Uv.from(referenceXyz));
    }

    T usingWhitePoint(Xyz referenceXyz, Uv referenceUv);
}
