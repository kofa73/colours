package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.io.RgbImage;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import static java.lang.Math.max;

/**
 * A gamut mapper type that desaturates all colours by scaling LCh's C using the same value such that all colours fit inside sRGB.
 * It analyses the image, finds the maximum 'actual C' to 'C at gamut boundary' ratio, and uses that to desaturate
 * the image. Therefore, saturation differences are maintained, at the cost of severe desaturation.
 *
 * @param <S> the base colour space that has an LCh representation, e.g. Lab or Luv
 * @param <L> the corresponding polar LCh type
 */
abstract class AbstractDesaturatingLchBasedGamutMapper<S extends ConvertibleToLch<L>, L extends Lch> extends GamutMapper {
    private final Function<Xyz, L> xyzToLchConverter;
    private final Function<double[], L> polarCoordinatesToLchConverter;
    private final Function<L, Xyz> lchToXyzConverter;
    private double cDivisor = 0;

    AbstractDesaturatingLchBasedGamutMapper(
            RgbImage image,
            ToDoubleFunction<L> maxCFinder,
            Function<Rec2020, S> rec2020ToLchConverter,
            Function<Xyz, L> xyzToLchConverter,
            Function<double[], L> polarCoordinatesToLchConverter,
            Function<L, Xyz> polarCoordinatesToXyzConverter
    ) {
        super(true);
        this.xyzToLchConverter = xyzToLchConverter;
        this.polarCoordinatesToLchConverter = polarCoordinatesToLchConverter;
        this.lchToXyzConverter = polarCoordinatesToXyzConverter;
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
        var lch = xyzToLchConverter.apply(xyz);
        var reducedC = lch.C() / cDivisor;
        return Srgb.from(
                lchToXyzConverter.apply(
                        polarCoordinatesToLchConverter.apply(
                                new double[]{lch.L(), reducedC, lch.h()}
                        )
                )
        );
    }
}
