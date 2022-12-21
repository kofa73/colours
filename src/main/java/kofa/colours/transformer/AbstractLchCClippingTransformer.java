package kofa.colours.transformer;

import kofa.colours.model.ConvertibleToLch;
import kofa.colours.model.Lch;
import kofa.colours.model.Srgb;
import kofa.colours.model.Xyz;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.min;

/**
 * A transformer type that clips C to the maximum value
 *
 * @param <S> the base colour space that has an LCh representation, e.g. Lab or Luv
 * @param <P> the corresponding polar LCh type
 */
abstract class AbstractLchCClippingTransformer<S extends ConvertibleToLch<S, P>, P extends Lch<S>> extends Transformer {
    private final Function<Xyz, P> xyzToLchConverter;
    private final Function<double[], P> lchCoordinatesToLchConverter;
    private final Function<P, Xyz> lchToXyzConverter;
    private final ToDoubleFunction<P> maxCFinder;

    AbstractLchCClippingTransformer(
            Function<Xyz, P> xyzToLchConverter,
            Function<double[], P> lchCoordinatesToLchConverter,
            Function<P, Xyz> lchToXyzConverter,
            ToDoubleFunction<P> maxCFinder
    ) {
        this.xyzToLchConverter = xyzToLchConverter;
        this.lchCoordinatesToLchConverter = lchCoordinatesToLchConverter;
        this.lchToXyzConverter = lchToXyzConverter;
        this.maxCFinder = maxCFinder;
    }

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        var lch = xyzToLchConverter.apply(xyz);
        var maxC = maxCFinder.applyAsDouble(lch);
        var reducedC = min(lch.C(), maxC);
        return Srgb.from(
                lchToXyzConverter.apply(
                        lchCoordinatesToLchConverter.apply(
                                new double[]{lch.L(), reducedC, lch.h()}
                        )
                )
        );
    }
}
