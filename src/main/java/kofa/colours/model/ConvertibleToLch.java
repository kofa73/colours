package kofa.colours.model;

import kofa.maths.Vector3;
import kofa.maths.Vector3Constructor;

import java.util.stream.DoubleStream;

import static java.lang.Math.*;

/**
 * Expresses that a colour space can be converted into LCh
 *
 * @param <V> the concrete vector subtype (e.g., some LAB or LUV)
 * @param <P> the concrete polar LCh subtype
 */
public abstract class ConvertibleToLch<V extends ConvertibleToLch<V, P>, P extends LCh<P, V>> extends Vector3 {
    protected final double L;
    private final double colour1;
    private final double colour2;

    private final Vector3Constructor<P> lchConstructor;

    protected ConvertibleToLch(double L, double colour1, double colour2, Vector3Constructor<P> lchConstructor) {
        super(L, colour1, colour2);
        this.L = L;
        this.colour1 = colour1;
        this.colour2 = colour2;
        this.lchConstructor = lchConstructor;
    }

    public final double L() {
        return L;
    }

    public final P toLch() {
        var c = sqrt(colour1 * colour1 + colour2 * colour2);
        var h = atan2(colour2, colour1);
        return lchConstructor.createFrom(L, c, h < 0 ? h + 2 * PI : h);
    }


    @Override
    public final DoubleStream coordinates() {
        return DoubleStream.of(L, colour1, colour2);
    }
}
