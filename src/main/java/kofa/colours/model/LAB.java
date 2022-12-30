package kofa.colours.model;

import kofa.maths.Vector3;
import kofa.maths.Vector3Constructor;

abstract class LAB<V extends LAB<V, P>, P extends LCh<P, V>> extends ConvertibleToLch<V, P> {
    public final double a;
    public final double b;

    public LAB(double L, double a, double b, Vector3Constructor<P> lchConstructor) {
        super(L, a, b, lchConstructor);
        this.a = a;
        this.b = b;
    }

    @Override
    public final String toString() {
        return Vector3.format(this, L, a, b);
    }
}
