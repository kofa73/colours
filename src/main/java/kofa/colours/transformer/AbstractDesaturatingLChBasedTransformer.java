package kofa.colours.transformer;

import kofa.colours.*;
import kofa.io.RgbImage;

import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static kofa.colours.tools.MaxCLabLuv.hToIndex;
import static kofa.colours.tools.MaxCLabLuv.lToIndex;

/**
 * A transformer type that desaturates all colours by scaling LCh's C such that all colours fit inside sRGB.
 *
 * @param <S> the base colour space that has an LCh representation, e.g. Lab or Luv
 * @param <P> the corresponding polar LCh type
 */
abstract class AbstractDesaturatingLChBasedTransformer<S extends LChable<S, P>, P extends LCh<S>> extends Transformer {
    private final Function<XYZ, P> xyzToPolarConverter;
    private final Function<double[], P> polarCoordinatesToPolarSpaceConverter;
    private final Function<P, XYZ> polarSpaceToXyzConverter;
    private double cDivisor = 0;

    AbstractDesaturatingLChBasedTransformer(
            RgbImage image,
            double[][] maxC,
            Function<Rec2020, S> rec2020ToLChConverter,
            Function<XYZ, P> xyzToPolarConverter,
            Function<double[], P> polarCoordinatesToPolarSpaceConverter,
            Function<P, XYZ> polarCoordinatesToXyzConverter
    ) {
        this.xyzToPolarConverter = xyzToPolarConverter;
        this.polarCoordinatesToPolarSpaceConverter = polarCoordinatesToPolarSpaceConverter;
        this.polarSpaceToXyzConverter = polarCoordinatesToXyzConverter;
        var red = image.redChannel();
        var green = image.greenChannel();
        var blue = image.blueChannel();

        IntStream.range(0, image.height()).forEach(row ->
                IntStream.range(0, image.width()).forEach(column -> {
                    var lch = rec2020ToLChConverter.apply(
                            new Rec2020(red[row][column], green[row][column], blue[row][column])
                    ).toLCh();
                    int lIndex = lToIndex(lch.L());
                    int hIndex = hToIndex(lch.h());
                    cDivisor = max(cDivisor, lch.C() / maxC[lIndex][hIndex]);
                })
        );
        cDivisor = max(cDivisor/* + 0.01*/, 1);
    }

    @Override
    public double[] getInsideGamut(XYZ xyz) {
        var lch = xyzToPolarConverter.apply(xyz);
        var reducedC = lch.C() / cDivisor;
        return SRGB.from(
                polarSpaceToXyzConverter.apply(
                        polarCoordinatesToPolarSpaceConverter.apply(
                                new double[]{lch.L(), reducedC, lch.h()}
                        )
                )
        ).values();
    }
}
