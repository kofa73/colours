package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.colours.tonemapper.SimpleCurveBasedToneMapper;
import kofa.colours.tonemapper.ToneMapper;
import kofa.io.RgbImage;
import kofa.maths.Vector3Constructor;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static java.util.Objects.requireNonNull;

/**
 * A gamut mapper type that clips LCh chroma to the maximum value, independently for each pixel.
 * That is, for each out-of-gamut pixel the LCh representation is computed, and 'C at gamut boundary for L and h' is
 * determined. Then C is replaced by that maximum (this mapper's getInsideGamut is only invoked for out-of-gamut
 * pixels).
 *
 * @param <S> the base colour space
 * @param <P> the corresponding polar LCh type
 */
public class ChromaClippingLchBasedGamutMapper<P extends Lch<P, S>, S extends ConvertibleToLch<S, P>> extends GamutMapper {
    private final ToDoubleFunction<Srgb> maxCFinder;
    private final String name;
    private final Vector3Constructor<P> lchConstructor;
    private final Function<Srgb, P> sRgbToLch;
    private final Function<P, Srgb> lchToSrgb;

    public static ChromaClippingLchBasedGamutMapper<CieLchAb, CieLab> forLchAb(RgbImage image) {
        return new ChromaClippingLchBasedGamutMapper<>(
                GamutBoundarySearchParams.FOR_CIELAB,
                SimpleCurveBasedToneMapper.forCieLab(image),
                image
        );
    }

    public static ChromaClippingLchBasedGamutMapper<CieLchUv, CieLuv> forLchUv(RgbImage image) {
        return new ChromaClippingLchBasedGamutMapper<>(
                GamutBoundarySearchParams.FOR_CIELUV,
                SimpleCurveBasedToneMapper.forCieLuv(image),
                image
        );
    }

    public static ChromaClippingLchBasedGamutMapper<OkLch, OkLab> forOkLch(RgbImage image) {
        return new ChromaClippingLchBasedGamutMapper<>(
                GamutBoundarySearchParams.FOR_OKLAB,
                SimpleCurveBasedToneMapper.forOkLab(image),
                image
        );
    }

    private ChromaClippingLchBasedGamutMapper(
            GamutBoundarySearchParams<P> searchParams,
            ToneMapper<S> toneMapper,
            RgbImage image
    ) {
        super(toneMapper, image);
        requireNonNull(searchParams);
        this.name = searchParams.type().getSimpleName();
        this.sRgbToLch = requireNonNull(searchParams.sRgbToLch());
        this.lchToSrgb = requireNonNull(searchParams.lchToSrgb());
        this.lchConstructor = requireNonNull(searchParams.lchConstructor());
        this.maxCFinder = sRgb -> new MaxCLabLuvSolver<P>(searchParams).solveMaxCForLch(sRgb);
    }


    @Override
    public Srgb getInsideGamut(Srgb sRgb) {
        var cAtGamutBoundary = maxCFinder.applyAsDouble(sRgb);
        P lchFromInput = sRgbToLch.apply(sRgb);
        P lchWithChromaAtGamutBoundary = lchConstructor.createFrom(lchFromInput.l(), cAtGamutBoundary, lchFromInput.h());
        return lchToSrgb.apply(lchWithChromaAtGamutBoundary);
    }

    @Override
    public String name() {
        return super.name() + name;
    }
}

