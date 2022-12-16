package kofa.colours;

import static kofa.colours.Converter.*;

public interface WhitePointXyzUvAwareConverter<T> extends WhitePointXyzAwareConverter<T> {
    default T usingD65() {
        return usingWhitePoint(D65_WHITE_XYZ, D65_WHITE_uvPrime);
    }

    @Override
    default T usingWhitePoint(XYZ referenceXYZ) {
        double referenceDenominator = denominator_XYZ_for_UV(referenceXYZ);
        double referenceU = uPrime(referenceXYZ.X(), referenceDenominator);
        double referenceV = vPrime(referenceXYZ.Y(), referenceDenominator);
        return usingWhitePoint(referenceXYZ, new UV(referenceU, referenceV));
    }

    T usingWhitePoint(XYZ referenceXYZ, UV referenceUV);
}
