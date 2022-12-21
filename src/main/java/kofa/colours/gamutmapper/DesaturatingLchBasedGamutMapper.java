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
 * @param <L> the LCh subtype
 */
public class DesaturatingLchBasedGamutMapper<L extends Lch> extends GamutMapper {
    private final String name;
    private final Function<Xyz, L> xyzToLchConverter;
    private final Function<double[], L> polarCoordinatesToLchConverter;
    private final Function<L, Xyz> lchToXyzConverter;
    private double cDivisor = 0;

    public static DesaturatingLchBasedGamutMapper<LchAb> forLchAb(RgbImage image) {
        return new DesaturatingLchBasedGamutMapper<>(
                image,
                LchAb.class,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchAb(lch),
                xyz -> Lab.from(xyz).usingD65().toLch(),
                LchAb::new,
                lch -> lch
                        .toLab()
                        .toXyz().usingD65()
        );
    }

    public static DesaturatingLchBasedGamutMapper<LchUv> forLchUv(RgbImage image) {
        return new DesaturatingLchBasedGamutMapper<>(
                image,
                LchUv.class,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchUv(lch),
                xyz -> Luv.from(xyz).usingD65().toLch(),
                LchUv::new,
                lch -> lch
                        .toLuv()
                        .toXyz().usingD65()
        );
    }

    private DesaturatingLchBasedGamutMapper(
            RgbImage image,
            Class<L> type,
            ToDoubleFunction<L> maxCFinder,
            Function<Xyz, L> xyzToLchConverter,
            Function<double[], L> polarCoordinatesToLchConverter,
            Function<L, Xyz> polarCoordinatesToXyzConverter
    ) {
        super(true);
        this.name = type.getSimpleName();
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
                        var lch = xyzToLchConverter.apply(rec2020.toXyz());
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

    @Override
    public String name() {
        return super.name() + name;
    }
}