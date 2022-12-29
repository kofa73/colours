package kofa.colours.gamutmapper;

import kofa.colours.model.*;
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
 * @param <L> the polar LCh type
 */
public class ChromaClippingLchBasedGamutMapper<L extends Lch<L, ?>> extends GamutMapper {
    private final ToDoubleFunction<Srgb> maxCFinder;
    private final String name;
    private final Vector3Constructor<L> lchConstructor;
    private final Function<Srgb, L> sRgbToLch;
    private final Function<L, Srgb> lchToSrgb;

    public static ChromaClippingLchBasedGamutMapper<CieLchAb> forLchAb() {
        return new ChromaClippingLchBasedGamutMapper<>(GamutBoundarySearchParams.FOR_CIELAB);
    }

    public static ChromaClippingLchBasedGamutMapper<CieLchUv> forLchUv() {
        return new ChromaClippingLchBasedGamutMapper<>(GamutBoundarySearchParams.FOR_CIELUV);
    }

    public static ChromaClippingLchBasedGamutMapper<OkLch> forOkLch() {
        return new ChromaClippingLchBasedGamutMapper<>(GamutBoundarySearchParams.FOR_OKLAB);
    }

    private ChromaClippingLchBasedGamutMapper(
            GamutBoundarySearchParams<L> searchParams
    ) {
        requireNonNull(searchParams);
        this.name = searchParams.type().getSimpleName();
        this.sRgbToLch = requireNonNull(searchParams.sRgbToLch());
        this.lchToSrgb = requireNonNull(searchParams.lchToSrgb());
        this.lchConstructor = requireNonNull(searchParams.lchConstructor());
        this.maxCFinder = sRgb -> new MaxCLabLuvSolver<L>(searchParams).solveMaxCForLch(sRgb);
    }

    @Override
    public Srgb getInsideGamut(Srgb sRgb) {
        var cAtGamutBoundary = maxCFinder.applyAsDouble(sRgb);
        L lchFromInput = sRgbToLch.apply(sRgb);
        L lchWithChromaAtGamutBoundary = lchConstructor.createFrom(lchFromInput.l(), cAtGamutBoundary, lchFromInput.h());
        return lchToSrgb.apply(lchWithChromaAtGamutBoundary);
    }

    @Override
    public String name() {
        return super.name() + name;
    }
}

