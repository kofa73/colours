package kofa.colours.model;

import kofa.maths.Vector3;
import kofa.maths.Vector3Constructor;

import static java.lang.Math.*;

/**
 * LCh representation
 *
 * @param <P> the polar Lch subtype
 * @param <V> the vector subtype
 */
public abstract class LCh<P extends LCh<P, V>, V extends ConvertibleToLch<V, P>> extends Vector3 {
    private final Vector3Constructor<V> vectorConstructor;

    protected LCh(double L, double C, double h, Vector3Constructor<V> vectorConstructor) {
        super(L, C, h);
        this.vectorConstructor = vectorConstructor;
    }

    public final double L() {
        return coordinate1;
    }

    public final double C() {
        return coordinate2;
    }

    public final double h() {
        return coordinate3;
    }

    public final double hueInDegrees() {
        var hueDegrees = toDegrees(h());
        return hueDegrees > 360 ? hueDegrees - 360 : (hueDegrees < 0 ? hueDegrees + 360 : hueDegrees);
    }

    protected final V toVector() {
        return vectorConstructor.createFrom(L(), C() * cos(h()), C() * sin(h()));
    }

    @Override
    public final String toString() {
        return "%s (%.8f, %.8f, %.8f)".formatted(super.toString(), L(), C(), hueInDegrees());
    }
}
