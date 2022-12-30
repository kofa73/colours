package kofa.colours.model;

import kofa.maths.Vector3;
import kofa.maths.Vector3Constructor;

import java.util.stream.DoubleStream;

import static java.lang.Math.*;

/**
 * LCh representation
 *
 * @param <P> the polar Lch subtype
 * @param <V> the vector subtype
 */
public abstract class LCh<P extends LCh<P, V>, V extends ConvertibleToLch<V, P>> extends Vector3 {
    private final Vector3Constructor<V> vectorConstructor;
    private final double L;
    private final double C;
    private final double h;

    protected LCh(double L, double C, double h, Vector3Constructor<V> vectorConstructor) {
        super(L, C, h);
        this.L = L;
        this.C = C;
        this.h = h;
        this.vectorConstructor = vectorConstructor;
    }

    public final double L() {
        return L;
    }

    public final double C() {
        return C;
    }

    public final double h() {
        return h;
    }

    public final double hueInDegrees() {
        var hueDegrees = toDegrees(h);
        return hueDegrees > 360 ? hueDegrees - 360 : (hueDegrees < 0 ? hueDegrees + 360 : hueDegrees);
    }

    protected final V toVector() {
        return vectorConstructor.createFrom(L, C * cos(h), C * sin(h));
    }

    @Override
    public final String toString() {
        return "%s (%.8f, %.8f, %.8f)".formatted(Vector3.format(this, L, C, h), L, C, hueInDegrees());
    }

    @Override
    public final DoubleStream coordinates() {
        return DoubleStream.of(L, C, h);
    }
}
