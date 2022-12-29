package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.maths.Vector3Constructor;

import java.util.function.Function;

public record GamutBoundarySearchParams<L extends Lch<L, ?>>(
        Class<L> type,
        Function<Srgb, L> sRgbToLch,
        Function<L, Srgb> lchToSrgb,
        Vector3Constructor<L> lchConstructor,
        double roughChromaSearchStep, double solutionThreshold,
        double maxL) {

    public static final GamutBoundarySearchParams<CieLchAb> FOR_CIELAB = new GamutBoundarySearchParams<>(
            CieLchAb.class,
            sRgb -> CieLab.from(sRgb.toXyz()).usingD65_IEC_61966_2_1().toLch(),
            lch -> Srgb.from(lch.toLab().toXyz().usingD65_IEC_61966_2_1()),
            CieLchAb::new,
            1,
            1E-6,
            100
    );

    public static final GamutBoundarySearchParams<CieLchUv> FOR_CIELUV = new GamutBoundarySearchParams<>(
            CieLchUv.class,
            sRgb -> CieLuv.from(sRgb.toXyz()).usingD65_IEC_61966_2_1().toLch(),
            lch -> Srgb.from(lch.toLuv().toXyz().usingD65_IEC_61966_2_1()),
            CieLchUv::new,
            1,
            1E-6,
            100
    );

    public static final GamutBoundarySearchParams<OkLch> FOR_OKLAB = new GamutBoundarySearchParams<>(
            OkLch.class,
            sRgb -> OkLab.from(sRgb).toLch(),
            lch -> lch.toLab().toSrgb(),
            OkLch::new,
            1,
            1E-6,
            1
    );
}