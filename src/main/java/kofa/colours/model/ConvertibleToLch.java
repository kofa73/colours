package kofa.colours.model;

import static java.lang.Math.*;

/**
 * Expresses that a colour space can be converted into LCh
 *
 * @param <L> the concrete LCh subtype
 */
public interface ConvertibleToLch<L extends Lch> {
    static double[] toPolar(double L, double abscissa, double ordinate) {
        var C = sqrt(abscissa * abscissa + ordinate * ordinate);
        var h = atan2(ordinate, abscissa);
        return new double[]{L, C, h < 0 ? h + 2 * PI : h};
    }

    L toLch();
}
