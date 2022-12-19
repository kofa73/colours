package kofa.colours.transformer;

import kofa.colours.model.LCh;
import kofa.colours.model.LChable;
import kofa.colours.model.SRGB;
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
    private final Function<XYZ, P> xyzToPolarConverter;
    private final Function<double[], P> polarCoordinatesToPolarSpaceConverter;
    private final Function<P, XYZ> polarSpaceToXyzConverter;
    private final ToDoubleFunction<P> solverFunction;

    AbstractLchCClippingTransformer(
            Function<XYZ, P> xyzToPolarConverter,
            Function<double[], P> polarCoordinatesToPolarSpaceConverter,
            Function<P, XYZ> polarSpaceToXyzConverter,
            ToDoubleFunction<P> solverFunction
    ) {
        this.xyzToPolarConverter = xyzToPolarConverter;
        this.polarCoordinatesToPolarSpaceConverter = polarCoordinatesToPolarSpaceConverter;
        this.polarSpaceToXyzConverter = polarSpaceToXyzConverter;
        this.solverFunction = solverFunction;
    }

    @Override
    public double[] getInsideGamut(XYZ xyz) {
        var lch = xyzToPolarConverter.apply(xyz);
        var maxC = solverFunction.applyAsDouble(lch);
        var reducedC = min(lch.C(), maxC);
        return SRGB.from(
                polarSpaceToXyzConverter.apply(
                        polarCoordinatesToPolarSpaceConverter.apply(
                                new double[]{lch.L(), reducedC, lch.h()}
                        )
                )
        ).values();
    }
}
