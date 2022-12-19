package kofa.colours.transformer;

import kofa.colours.model.LCh;
import kofa.colours.model.LChable;
import kofa.colours.model.Srgb;
import kofa.colours.model.XYZ;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.min;

/**
 * A transformer type that clips C to the maximum value
 *
 * @param <S> the base colour space that has an LCh representation, e.g. Lab or Luv
 * @param <P> the corresponding polar LCh type
 */
abstract class AbstractLchCClippingTransformer<S extends LChable<S, P>, P extends LCh<S>> extends Transformer {
    private final Function<XYZ, P> xyzToLchConverter;
    private final Function<double[], P> lchCoordinatesToLchConverter;
    private final Function<P, XYZ> lchToXyzConverter;
    private final ToDoubleFunction<P> solverFunction;

    AbstractLchCClippingTransformer(
            Function<XYZ, P> xyzToLchConverter,
            Function<double[], P> lchCoordinatesToLchConverter,
            Function<P, XYZ> lchToXyzConverter,
            ToDoubleFunction<P> solverFunction
    ) {
        this.xyzToLchConverter = xyzToLchConverter;
        this.lchCoordinatesToLchConverter = lchCoordinatesToLchConverter;
        this.lchToXyzConverter = lchToXyzConverter;
        this.solverFunction = solverFunction;
    }

    @Override
    public Srgb getInsideGamut(XYZ xyz) {
        var lch = xyzToLchConverter.apply(xyz);
        var maxC = solverFunction.applyAsDouble(lch);
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
