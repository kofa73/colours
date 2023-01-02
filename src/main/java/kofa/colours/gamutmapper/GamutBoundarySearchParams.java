package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.maths.Vector3Constructor;

import java.util.function.Function;

public record GamutBoundarySearchParams<L extends LCh<L, ?>>(
        Class<L> type,
        Function<Srgb, L> sRgbToLch,
        Function<L, Srgb> lchToSrgb,
        Vector3Constructor<L> lchConstructor,
        double roughChromaSearchStep, double solutionThreshold,
        double maxL) {

    public static final GamutBoundarySearchParams<CIELCh_ab> FOR_CIELAB = new GamutBoundarySearchParams<>(
            CIELCh_ab.class,
            sRgb -> CIELAB.from(sRgb.toXyz()).usingD65_2DegreeStandardObserver().toLch(),
            lch -> Srgb.from(lch.toLab().toXyz().usingD65_2DegreeStandardObserver()),
            CIELCh_ab::new,
            1,
            1E-6,
            CIELCh_ab.WHITE_L
    );

    public static final GamutBoundarySearchParams<CIELCh_uv> FOR_CIELUV = new GamutBoundarySearchParams<>(
            CIELCh_uv.class,
            sRgb -> CIELUV.from(sRgb.toXyz()).usingD65_2DegreeStandardObserver().toLch(),
            lch -> Srgb.from(lch.toLuv().toXyz().usingD65_2DegreeStandardObserver()),
            CIELCh_uv::new,
            1,
            1E-6,
            CIELCh_uv.WHITE_L
    );

    public static final GamutBoundarySearchParams<OkLCh> FOR_OKLAB = new GamutBoundarySearchParams<>(
            OkLCh.class,
            sRgb -> OkLAB.from(sRgb).toLch(),
            lch -> lch.toLab().toSrgb(),
            OkLCh::new,
            1,
            1E-6,
            OkLCh.WHITE_L
    );
}