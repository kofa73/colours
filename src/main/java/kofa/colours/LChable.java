package kofa.colours;

import static java.lang.Math.*;

/**
 * Expresses that a colour can be converted into LCh
 *
 * @param <S> the concrete space that can be converted into LCh
 * @param <T> the concrete LCh subtype
 */
public interface LChable<S extends LChable<S, T>, T extends LCh<S>> {
    static double[] toPolar(double L, double abscissa, double ordinate) {
        var C = sqrt(abscissa * abscissa + ordinate * ordinate);
        var h = atan2(ordinate, abscissa);
        return new double[]{L, C, h < 0 ? h + 2 * PI : h};
    }

    public abstract T toLCh();
}
