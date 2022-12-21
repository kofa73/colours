package kofa.colours.transformer;

import kofa.colours.model.*;
import kofa.io.RgbImage;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import static java.lang.Math.max;

/**
 * A transformer type that desaturates all colours by scaling LCh's C such that all colours fit inside sRGB.
 *
 * @param <S> the base colour space that has an LCh representation, e.g. Lab or Luv
 * @param <P> the corresponding polar LCh type
 */
abstract class AbstractDesaturatingLchBasedTransformer<S extends ConvertibleToLch<S, P>, P extends Lch<S>> extends Transformer {
    private final Function<Xyz, P> xyzToPolarConverter;
    private final Function<double[], P> polarCoordinatesToPolarSpaceConverter;
    private final Function<P, Xyz> polarSpaceToXyzConverter;
    private double cDivisor = 0;

    AbstractDesaturatingLchBasedTransformer(
            RgbImage image,
            ToDoubleFunction<P> maxCFinder,
            Function<Rec2020, S> rec2020ToLchConverter,
            Function<Xyz, P> xyzToPolarConverter,
            Function<double[], P> polarCoordinatesToPolarSpaceConverter,
            Function<P, Xyz> polarCoordinatesToXyzConverter
    ) {
        super(true);
        this.xyzToPolarConverter = xyzToPolarConverter;
        this.polarCoordinatesToPolarSpaceConverter = polarCoordinatesToPolarSpaceConverter;
        this.polarSpaceToXyzConverter = polarCoordinatesToXyzConverter;
        var red = image.redChannel();
        var green = image.greenChannel();
        var blue = image.blueChannel();

        IntStream.range(0, image.height()).forEach(row ->
                IntStream.range(0, image.width()).forEach(column -> {
                    var rec2020 = new Rec2020(red[row][column], green[row][column], blue[row][column]);
                    if (rec2020.toSRGB().isOutOfGamut()) {
                        var lch = rec2020ToLchConverter.apply(rec2020).toLch();
                        if (lch.C() != 0) {
                            var maxC = maxCFinder.applyAsDouble(lch);
                            if (maxC != 0) {
                                cDivisor = max(cDivisor, lch.C() / maxC);
                            }
                        }
                    }
                })
        );
        cDivisor = max(cDivisor, 1);
    }

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        var lch = xyzToPolarConverter.apply(xyz);
        var reducedC = lch.C() / cDivisor;
        return Srgb.from(
                polarSpaceToXyzConverter.apply(
                        polarCoordinatesToPolarSpaceConverter.apply(
                                new double[]{lch.L(), reducedC, lch.h()}
                        )
                )
        );
    }
}
