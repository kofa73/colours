package kofa.colours.tonemapper;

import kofa.colours.model.CieLab;
import kofa.colours.model.CieLuv;
import kofa.colours.model.OkLab;
import kofa.colours.model.Rec2020;
import kofa.io.RgbImage;
import kofa.maths.Solver;
import kofa.maths.ThanatomanicCurve6;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * @param <S> the space used for tone mapping
 */
public class SimpleCurveBasedToneMapper<S> implements ToneMapper<S> {
    private final ThanatomanicCurve6 curve;
    private final double brightnessScaling;
    private final Function<Rec2020, S> rec2020ToMappingSpace;
    private final Function<S, Rec2020> mappingSpaceToRec2020;
    private final BiFunction<S, Double, S> constructorWithMappedBrightness;
    private final ToDoubleFunction<S> brightnessFunction;

    public static SimpleCurveBasedToneMapper<CieLab> forCieLab(RgbImage image) {
        return new SimpleCurveBasedToneMapper<>(image, ToneMappingParams.FOR_CIELAB);
    }

    public static SimpleCurveBasedToneMapper<CieLuv> forCieLuv(RgbImage image) {
        return new SimpleCurveBasedToneMapper<>(image, ToneMappingParams.FOR_CIELUV);
    }

    public static SimpleCurveBasedToneMapper<OkLab> forOkLab(RgbImage image) {
        return new SimpleCurveBasedToneMapper<>(image, ToneMappingParams.FOR_OKLAB);
    }

    public SimpleCurveBasedToneMapper(
            ToneMappingParams<S> params
    ) {
        this(0.7, params);
    }

    public SimpleCurveBasedToneMapper(
            RgbImage image,
            ToneMappingParams<S> params
    ) {
        this(findOptimalShoulderStart(image, params), params);
    }

    public SimpleCurveBasedToneMapper(
            double shoulderStart,
            ToneMappingParams<S> params
    ) {
        this.brightnessScaling = params.brightnessScaling();
        checkArgument(brightnessScaling > 0, "brightnessScaling=%s", brightnessScaling);
        this.rec2020ToMappingSpace = requireNonNull(params.rec2020ToMappingSpace());
        this.mappingSpaceToRec2020 = requireNonNull(params.mappingSpaceToRec2020());
        this.constructorWithMappedBrightness = requireNonNull(params.constructorWithMappedBrightness());
        this.brightnessFunction = requireNonNull(params.brightnessFunction());
        if (shoulderStart == 1) {
            curve = null;
        } else {
            curve = ThanatomanicCurve6.linearUntil(shoulderStart);
        }
    }

    private static <S> double findOptimalShoulderStart(
            RgbImage image,
            ToneMappingParams<S> params
    ) {
        double brightnessScaling = params.brightnessScaling();
        checkArgument(brightnessScaling > 0, "brightnessScaling=%s", brightnessScaling);
        Function<Rec2020, S> rec2020ToLSpace = requireNonNull(params.rec2020ToMappingSpace());
        ToDoubleFunction<S> brightnessFunction = requireNonNull(params.brightnessFunction());

        double shoulderStart;
        double maxBrightness = maxBrightness(image, rec2020ToLSpace, brightnessFunction);
        if (maxBrightness <= brightnessScaling) {
            System.out.println("Tone mapping is not needed, maxBrightness = " + maxBrightness);
            shoulderStart = 1;
        } else {
            var shoulderSearchLow = 0.99;
            Optional<Double> shoulder;
            do {
                var shoulderSolver = new Solver(currentShoulder -> {
                    var curve = ThanatomanicCurve6.linearUntil(currentShoulder);
                    double mappedValue = applyCurve(curve, maxBrightness);
                    return mappedValue < 0.99 ?
                            -1.0 :
                            mappedValue > 0.9999 ?
                                    1 :
                                    0;
                });
                shoulder = shoulderSolver.solve(shoulderSearchLow, shoulderSearchLow + 0.01, 0);
                if (shoulder.isEmpty()) {
                    shoulderSearchLow -= 0.01;
                }
            } while (shoulder.isEmpty() && shoulderSearchLow >= 0.5);
            shoulderStart = shoulder.orElse(0.5);
            System.out.println("Will use shoulderStart = " + shoulderStart + " for tone mapping, maxBrightness = " + maxBrightness);
        }
        return shoulderStart;
    }

    public void toneMap(RgbImage image) {
        if (curve == null) {
            return;
        }
        image.transformAllPixels((row, column, red, green, blue) -> {
            var rec2020 = new Rec2020(
                    red,
                    green,
                    blue
            );
            var lSpaceRepresentation = rec2020ToMappingSpace.apply(rec2020);
            var mappedLSpaceRepresentation = toneMap(lSpaceRepresentation);
            var mappedRec2020 = mappingSpaceToRec2020.apply(mappedLSpaceRepresentation);
            return new double[]{mappedRec2020.r(), mappedRec2020.g(), mappedRec2020.b()};
        });
    }

    private S toneMap(S input) {
        double mappedL;
        if (curve == null) {
            mappedL = brightnessFunction.applyAsDouble(input);
        } else {
            double normalisedL = brightnessFunction.applyAsDouble(input) / brightnessScaling;
            mappedL = applyCurve(curve, normalisedL) * brightnessScaling;
        }
        if (mappedL < 0) {
            mappedL = 0;
        }
        return constructorWithMappedBrightness.apply(input, mappedL);
    }

    private static double applyCurve(ThanatomanicCurve6 curve, double x) {
        return curve.mappedValueOf(x);
    }

    private static <S> double maxBrightness(RgbImage image, Function<Rec2020, S> rec2020ToLSpace, ToDoubleFunction<S> brightnessFunction) {
        return image.pixelStream().mapToDouble(rgb -> {
            var rec2020 = new Rec2020(rgb[0], rgb[1], rgb[2]);
            return brightnessFunction.applyAsDouble(rec2020ToLSpace.apply(rec2020));
        }).max().orElse(0.0);
    }

}
