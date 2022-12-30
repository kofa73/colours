package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.colours.tonemapper.SimpleCurveBasedToneMapper;
import kofa.colours.tonemapper.ToneMapper;
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
 * @param <S> the base colour space
 * @param <P> the corresponding polar LCh type
 */
public class DesaturatingLchBasedGamutMapper<P extends LCh<P, S>, S extends ConvertibleToLch<S, P>> extends GamutMapper {
    private final String name;
    private final Vector3Constructor<P> lchConstructor;
    private final Function<Srgb, P> sRgbToLch;
    private final Function<P, Srgb> lchToSrgb;
    private double cDivisor = 0;

    public static DesaturatingLchBasedGamutMapper<CIELCh_ab, CIELAB> forLchAb(RgbImage image) {
        return new DesaturatingLchBasedGamutMapper<>(
                image,
                GamutBoundarySearchParams.FOR_CIELAB,
                SimpleCurveBasedToneMapper.forCieLab(image)
        );
    }

    public static DesaturatingLchBasedGamutMapper<CIELCh_uv, CIELUV> forLchUv(RgbImage image) {
        return new DesaturatingLchBasedGamutMapper<>(
                image,
                GamutBoundarySearchParams.FOR_CIELUV,
                SimpleCurveBasedToneMapper.forCieLuv(image)
        );
    }

    public static DesaturatingLchBasedGamutMapper<OkLCh, OkLAB> forOkLch(RgbImage image) {
        return new DesaturatingLchBasedGamutMapper<>(
                image,
                GamutBoundarySearchParams.FOR_OKLAB,
                SimpleCurveBasedToneMapper.forOkLab(image)
        );
    }

    private DesaturatingLchBasedGamutMapper(
            RgbImage image,
            GamutBoundarySearchParams<P> searchParams,
            ToneMapper<S> toneMapper
    ) {
        super(true, toneMapper, image);
        this.name = searchParams.type().getSimpleName();
        this.sRgbToLch = requireNonNull(searchParams.sRgbToLch());
        this.lchToSrgb = requireNonNull(searchParams.lchToSrgb());
        this.lchConstructor = requireNonNull(searchParams.lchConstructor());

        ToDoubleFunction<Srgb> maxCFinder = sRgb -> new MaxCLabLuvSolver<>(searchParams).solveMaxCForLch(sRgb);

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
        P lchFromInput = sRgbToLch.apply(sRgb);
        double reducedC = lchFromInput.c() / cDivisor;
        P lchWithChromaAtGamutBoundary = lchConstructor.createFrom(lchFromInput.l(), reducedC, lchFromInput.h());
        return lchToSrgb.apply(lchWithChromaAtGamutBoundary);
    }

    @Override
    public String name() {
        return super.name() + name;
    }
}
