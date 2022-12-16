package kofa.colours;

public interface WhitePointXyzAwareConverter<T> {
    default T usingD65() {
        return usingWhitePoint(Converter.D65_WHITE_XYZ);
    }

    T usingWhitePoint(XYZ referenceXYZ);
}
