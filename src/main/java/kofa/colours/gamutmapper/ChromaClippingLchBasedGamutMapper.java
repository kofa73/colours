package kofa.colours.gamutmapper;

import kofa.colours.model.*;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * A gamut mapper type that clips LCh chroma to the maximum value, independently for each pixel.
 * That is, for each out-of-gamut pixel the LCh representation is computed, and 'C at gamut boundary for L and h' is
 * determined. Then C is replaced by that maximum (this mapper's getInsideGamut is only invoked for out-of-gamut
 * pixels).
 *
 * @param <L> the polar LCh type
 */
public class ChromaClippingLchBasedGamutMapper<L extends Lch> extends GamutMapper {
    private final Function<Xyz, L> xyzToLch;
    private final Function<double[], L> lchCoordinatesToLch;
    private final Function<L, Xyz> lchToXyz;
    private final ToDoubleFunction<L> maxCFinder;
    private final String name;

    public static ChromaClippingLchBasedGamutMapper<CieLchAb> forLchAb() {
        return new ChromaClippingLchBasedGamutMapper<>(
                CieLchAb.class,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchAb(lch), xyz -> CieLab.from(xyz).usingD65().toLch(),
                CieLchAb::new,
                lch -> lch
                        .toLab()
                        .toXyz().usingD65()
        );
    }

    public static ChromaClippingLchBasedGamutMapper<CieLchUv> forLchUv() {
        return new ChromaClippingLchBasedGamutMapper<>(
                CieLchUv.class,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchUv(lch), xyz -> CieLuv.from(xyz).usingD65().toLch(),
                CieLchUv::new,
                lch -> lch
                        .toLuv()
                        .toXyz().usingD65()
        );
    }

    private ChromaClippingLchBasedGamutMapper(
            Class<L> type,
            ToDoubleFunction<L> maxCFinder,
            Function<Xyz, L> xyzToLch,
            Function<double[], L> lchCoordinatesToLch,
            Function<L, Xyz> lchToXyz
    ) {
        this.name = type.getSimpleName();
        this.maxCFinder = maxCFinder;
        this.xyzToLch = xyzToLch;
        this.lchCoordinatesToLch = lchCoordinatesToLch;
        this.lchToXyz = lchToXyz;
    }

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        var lch = xyzToLch.apply(xyz);
        var cAtGamutBoundary = maxCFinder.applyAsDouble(lch);
        return Srgb.from(
                lchToXyz.apply(
                        lchCoordinatesToLch.apply(
                                new double[]{lch.L(), cAtGamutBoundary, lch.h()}
                        )
                )
        );
    }

    @Override
    public String name() {
        return super.name() + name;
    }
}

