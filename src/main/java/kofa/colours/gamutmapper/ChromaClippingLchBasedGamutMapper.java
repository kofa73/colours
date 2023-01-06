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
public class ChromaClippingLchBasedGamutMapper<P extends LCh<P, S>, S extends ConvertibleToLch<S, P>> extends GamutMapper {
    private final ToDoubleFunction<P> maxCFinder;
    private final String name;
    private final Vector3Constructor<P> lchConstructor;
    private final Function<Srgb, P> sRgbToLch;
    private final Function<P, Srgb> lchToSrgb;

    public static ChromaClippingLchBasedGamutMapper<CIELCh_ab, CIELAB> forLchAb(RgbImage image) {
        return new ChromaClippingLchBasedGamutMapper<>(
                GamutBoundarySearchParams.FOR_CIELAB,
                SimpleCurveBasedToneMapper.forCieLab(image),
                image
        );
    }

    public static ChromaClippingLchBasedGamutMapper<CIELCh_uv, CIELUV> forLchUv(RgbImage image) {
        return new ChromaClippingLchBasedGamutMapper<>(
                GamutBoundarySearchParams.FOR_CIELUV,
                SimpleCurveBasedToneMapper.forCieLuv(image),
                image
        );
    }

    public static ChromaClippingLchBasedGamutMapper<OkLCh, OkLAB> forOkLch(RgbImage image) {
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
        super(toneMapper);
        requireNonNull(searchParams);
        this.name = searchParams.type().getSimpleName();
        this.sRgbToLch = requireNonNull(searchParams.sRgbToLch());
        this.lchToSrgb = requireNonNull(searchParams.lchToSrgb());
        this.lchConstructor = requireNonNull(searchParams.lchConstructor());
        var solver = GamutBoundaryMaxCSolver.createFor(searchParams, image);
        this.maxCFinder = lch -> solver.maxCFor(lch);
    }


    @Override
    public Srgb getInsideGamut(Srgb sRgb) {
        P lchFromInput = sRgbToLch.apply(sRgb);
        var cAtGamutBoundary = maxCFinder.applyAsDouble(lchFromInput);
        P lchWithChromaAtGamutBoundary = lchConstructor.createFrom(lchFromInput.L(), cAtGamutBoundary, lchFromInput.h());
        return lchToSrgb.apply(lchWithChromaAtGamutBoundary);
    }

    @Override
    public String name() {
        return super.name() + name;
    }
}
