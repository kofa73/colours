package kofa.colours.model;

import kofa.maths.Vector3;
import kofa.maths.Vector3Constructor;

import static java.lang.Math.*;

/**
 * Expresses that a colour space can be converted into LCh
 *
 * @param <S> the concrete scalar subtype
 * @param <L> the concrete LCh subtype
 */
public abstract class ConvertibleToLch<S extends ConvertibleToLch<S, L>, L extends Lch<L, S>> extends Vector3 {
    private final Vector3Constructor<L> lchConstructor;

    protected ConvertibleToLch(double coordinate1, double coordinate2, double coordinate3, Vector3Constructor<L> lchConstructor) {
        super(coordinate1, coordinate2, coordinate3);
        this.lchConstructor = lchConstructor;
    }

    public double l() {
        return coordinate1;
    }

    public final L toLch() {
        var c = sqrt(coordinate2 * coordinate2 + coordinate3 * coordinate3);
        var h = atan2(coordinate3, coordinate2);
        return lchConstructor.createFrom(coordinate1, c, h < 0 ? h + 2 * PI : h);
    }


}
