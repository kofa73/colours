package kofa.colours;

import static java.lang.Math.*;

class LchHelper {
    private LchHelper() {
        throw new RuntimeException("Don't instantiate this utility class");
    }

    static double[] fromPolar(double L, double C, double h) {
        return new double[]{L, C * cos(h), C * sin(h)};
    }

    static double[] toPolar(double L, double abscissa, double ordinate) {
        var C = sqrt(abscissa * abscissa + ordinate * ordinate);
        var h = atan2(ordinate, abscissa);
        return new double[]{L, C, h};
    }
}
