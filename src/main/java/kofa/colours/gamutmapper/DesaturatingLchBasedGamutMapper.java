package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.io.RgbImage;
import kofa.maths.Vector3Constructor;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.max;
import static java.util.Objects.requireNonNull;

/**
 * A gamut mapper type that desaturates all colours by scaling LCh's C using the same value such that all colours fit inside sRGB.
 * It analyses the image, finds the maximum 'actual C' to 'C at gamut boundary' ratio, and uses that to desaturate
 * the image. Therefore, saturation differences are maintained, at the cost of severe desaturation.
 *
 * @param <L> the LCh subtype
 */
public class DesaturatingLchBasedGamutMapper<L extends Lch<L, ?>> extends GamutMapper {
    private final String name;
    private final Vector3Constructor<L> lchConstructor;
    private final Function<Srgb, L> sRgbToLch;
    private final Function<L, Srgb> lchToSrgb;
    private double cDivisor = 0;

    public static DesaturatingLchBasedGamutMapper<CieLchAb> forLchAb(RgbImage image) {
        return new DesaturatingLchBasedGamutMapper<>(
                image,
                GamutBoundarySearchParams.FOR_CIELAB
        );
    }

    public static DesaturatingLchBasedGamutMapper<CieLchUv> forLchUv(RgbImage image) {
        return new DesaturatingLchBasedGamutMapper<>(
                image,
                GamutBoundarySearchParams.FOR_CIELUV
        );
    }

    public static DesaturatingLchBasedGamutMapper<OkLch> forOkLch(RgbImage image) {
        return new DesaturatingLchBasedGamutMapper<>(
                image,
                GamutBoundarySearchParams.FOR_OKLAB
        );
    }

    private DesaturatingLchBasedGamutMapper(
            RgbImage image,
            GamutBoundarySearchParams<L> searchParams) {
        super(true);
        this.name = searchParams.type().getSimpleName();
        this.sRgbToLch = requireNonNull(searchParams.sRgbToLch());
        this.lchToSrgb = requireNonNull(searchParams.lchToSrgb());
        this.lchConstructor = requireNonNull(searchParams.lchConstructor());

        ToDoubleFunction<Srgb> maxCFinder = sRgb -> new MaxCLabLuvSolver<L>(searchParams).solveMaxCForLch(sRgb);

        image.forEachPixelSequentially((row, column, red, green, blue) -> {
            var rec2020 = new Rec2020(red, green, blue);
            Srgb sRgb = rec2020.toSRGB();
            if (sRgb.isOutOfGamut()) {
                var lch = searchParams.sRgbToLch().apply(sRgb);
                if (lch.c() != 0) {
                    var maxC = maxCFinder.applyAsDouble(sRgb);
                    if (maxC != 0) {
                        cDivisor = max(cDivisor, lch.c() / maxC);
                    }
                }
            }
        });

        cDivisor = max(cDivisor, 1);
    }

    @Override
    public Srgb getInsideGamut(Srgb sRgb) {
        L lchFromInput = sRgbToLch.apply(sRgb);
        double reducedC = lchFromInput.c() / cDivisor;
        L lchWithChromaAtGamutBoundary = lchConstructor.createFrom(lchFromInput.l(), reducedC, lchFromInput.h());
        return lchToSrgb.apply(lchWithChromaAtGamutBoundary);
    }

    @Override
    public String name() {
        return super.name() + name;
    }
}
