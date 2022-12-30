package kofa.colours.model;

import kofa.maths.Vector3;
import kofa.maths.Vector3Constructor;

abstract class LAB<V extends LAB<V, P>, P extends LCh<P, V>> extends ConvertibleToLch<V, P> {
    protected final double a;
    protected final double b;

    public LAB(double L, double a, double b, Vector3Constructor<P> lchConstructor) {
        super(L, a, b, lchConstructor);
        this.a = a;
        this.b = b;
    }

    public final double a() {
        return a;
    }

    public final double b() {
        return b;
    }

    @Override
    public final String toString() {
        return Vector3.format(this, L, a, b);
    }
}
