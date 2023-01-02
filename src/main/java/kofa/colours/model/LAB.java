package kofa.colours.model;

import kofa.maths.Vector3Constructor;

abstract class LAB<V extends LAB<V, P>, P extends LCh<P, V>> extends ConvertibleToLch<V, P> {
    public LAB(double L, double a, double b, Vector3Constructor<P> lchConstructor) {
        super(L, a, b, lchConstructor);
    }

    public final double a() {
        return coordinate2;
    }

    public final double b() {
        return coordinate3;
    }
}
