package kofa.colours.model;

import kofa.maths.Vector3;
import kofa.maths.Vector3Constructor;

import static java.lang.Math.*;

/**
 * LCh representation
 *
 * @param <L> the polar Lch subtype
 * @param <S> the scalar subtype
 */
public abstract class Lch<L extends Lch<L, S>, S extends ConvertibleToLch<S, L>> extends Vector3 {
    private final Vector3Constructor<S> scalarConstructor;

    protected Lch(double l, double c, double h, Vector3Constructor<S> scalarConstructor) {
        super(l, c, h);
        this.scalarConstructor = scalarConstructor;
    }

    public final double l() {
        return coordinate1;
    }

    public final double c() {
        return coordinate2;
    }

    public final double h() {
        return coordinate3;
    }

    public final double hueInDegrees() {
        var hueDegrees = toDegrees(h());
        return hueDegrees > 360 ? hueDegrees - 360 : (hueDegrees < 0 ? hueDegrees + 360 : hueDegrees);
    }

    protected final S toScalar() {
        return scalarConstructor.createFrom(l(), c() * cos(h()), c() * sin(h()));
    }

    @Override
    public final String toString() {
        return "%s(%f, %f, %f) (%f, %f, %f)".formatted(getClass().getSimpleName(), l(), c(), h(), l(), c(), hueInDegrees());
    }
}
