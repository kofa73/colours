package kofa.colours.tonemapper;

import kofa.colours.model.*;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

record ToneMappingParams<S>(
        double brightnessScaling,
        Function<Rec2020, S> rec2020ToMappingSpace,
        Function<S, Rec2020> mappingSpaceToRec2020,
        BiFunction<S, Double, S> constructorWithMappedBrightness,
        ToDoubleFunction<S> brightnessFunction
) {

    static final ToneMappingParams<CIELAB> FOR_CIELAB = new ToneMappingParams<>(
            CIELCh_ab.WHITE_L,
            rec2020 -> CIELAB.from(rec2020.toXyz()).usingD65_2DegreeStandardObserver(),
            cieLab -> Rec2020.from(cieLab.toXyz().usingD65_2DegreeStandardObserver()),
            (cieLab, mappedL) -> new CIELAB(mappedL, cieLab.a(), cieLab.b()),
            CIELAB::L
    );

    static final ToneMappingParams<CIELUV> FOR_CIELUV = new ToneMappingParams<>(
            CIELCh_uv.WHITE_L,
            rec2020 -> CIELUV.from(rec2020.toXyz()).usingD65_2DegreeStandardObserver(),
            cieLuv -> Rec2020.from(cieLuv.toXyz().usingD65_2DegreeStandardObserver()),
            (cieLuv, mappedL) -> new CIELUV(mappedL, cieLuv.u(), cieLuv.v()),
            CIELUV::L
    );

    static final ToneMappingParams<OkLAB> FOR_OKLAB = new ToneMappingParams<>(
            OkLCh.WHITE_L,
            rec2020 -> OkLAB.from(rec2020.toSRGB()),
            okLab -> okLab.toSrgb().toRec2020(),
            (okLab, mappedL) -> new OkLAB(mappedL, okLab.a(), okLab.b()),
            OkLAB::L
    );
}
