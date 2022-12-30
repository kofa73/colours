package kofa.colours.model;

import kofa.maths.Vector3;
import kofa.maths.Vector3Constructor;

import static java.lang.Math.*;

/**
 * Expresses that a colour space can be converted into LCh
 *
 * @param <V> the concrete vector subtype (e.g., some LAB or LUV)
 * @param <P> the concrete polar LCh subtype
 */
public abstract class ConvertibleToLch<V extends ConvertibleToLch<V, P>, P extends LCh<P, V>> extends Vector3 {
    private final Vector3Constructor<P> lchConstructor;

    protected ConvertibleToLch(double coordinate1, double coordinate2, double coordinate3, Vector3Constructor<P> lchConstructor) {
        super(coordinate1, coordinate2, coordinate3);
        this.lchConstructor = lchConstructor;
    }

    public double L() {
        return coordinate1;
    }

    public final P toLch() {
        var c = sqrt(coordinate2 * coordinate2 + coordinate3 * coordinate3);
        var h = atan2(coordinate3, coordinate2);
        return lchConstructor.createFrom(coordinate1, c, h < 0 ? h + 2 * PI : h);
    }
}
