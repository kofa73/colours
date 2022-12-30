package kofa.colours.tonemapper;

import kofa.colours.model.CieLab;
import kofa.colours.model.CieLuv;
import kofa.colours.model.OkLab;
import kofa.colours.model.Rec2020;

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

    static final ToneMappingParams<CieLab> FOR_CIELAB = new ToneMappingParams<>(
            100,
            rec2020 -> CieLab.from(rec2020.toXyz()).usingD65_IEC_61966_2_1(),
            cieLab -> Rec2020.from(cieLab.toXyz().usingD65_IEC_61966_2_1()),
            (cieLab, mappedL) -> new CieLab(mappedL, cieLab.a(), cieLab.b()),
            CieLab::l
    );

    static final ToneMappingParams<CieLuv> FOR_CIELUV = new ToneMappingParams<>(
            100,
            rec2020 -> CieLuv.from(rec2020.toXyz()).usingD65_IEC_61966_2_1(),
            cieLuv -> Rec2020.from(cieLuv.toXyz().usingD65_IEC_61966_2_1()),
            (cieLuv, mappedL) -> new CieLuv(mappedL, cieLuv.u(), cieLuv.v()),
            CieLuv::l
    );

    static final ToneMappingParams<OkLab> FOR_OKLAB = new ToneMappingParams<>(
            1,
            rec2020 -> OkLab.from(rec2020.toSRGB()),
            okLab -> okLab.toSrgb().toRec2020(),
            (okLab, mappedL) -> new OkLab(mappedL, okLab.a(), okLab.b()),
            OkLab::l
    );

}
