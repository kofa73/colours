package kofa.colours.tonemapper;

import kofa.colours.model.CieLab;
import kofa.colours.model.OkLab;
import kofa.colours.model.OkLch;
import kofa.colours.model.Rec2020;
import kofa.io.RgbImage;
import kofa.maths.Solver;
import kofa.maths.ThanatomanicCurve6;

import java.util.Optional;

public class OkLabToneMapper implements ToneMapper<OkLab> {
    private final ThanatomanicCurve6 curve;

    public OkLabToneMapper() {
        this(0.7);
    }

    public OkLabToneMapper(RgbImage image) {
        this(findOptimalShoulderStart(image));
    }

    public OkLabToneMapper(double shoulderStart) {
        if (shoulderStart == 1) {
            curve = null;
        } else {
            curve = ThanatomanicCurve6.linearUntil(shoulderStart);
        }
    }

    private static double findOptimalShoulderStart(RgbImage image) {
        double shoulderStart;
        double maxL = maxL(image);
        if (maxL <= 1) {
            System.out.println("Tone mapping is not needed, maxL = " + maxL);
            shoulderStart = 1;
        } else {
            var shoulderSearchLow = 0.99;
            Optional<Double> shoulder;
            do {
                var shoulderSolver = new Solver(currentShoulder -> {
                    var curve = ThanatomanicCurve6.linearUntil(currentShoulder);
                    double mappedValue = applyCurve(curve, maxL);
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
            System.out.println("Will use shoulderStart = " + shoulderStart + " for tone mapping, maxL = " + maxL);
        }
        return shoulderStart;
    }

    private static double applyCurve(ThanatomanicCurve6 curve, double x) {
        return curve.mappedValueOf(x);
    }

    @Override
    public OkLab toneMap(OkLab input) {
        double mappedL;
        if (curve == null) {
            mappedL = input.l();
        } else {
            double normalisedL = input.l();
            mappedL = applyCurve(curve, normalisedL);
        }
        if (mappedL < 0) {
            mappedL = 0;
        }

        var mappedLab = new OkLab(mappedL, input.a(), input.b());
        var mappedRec2020 = Rec2020.from(mappedLab.toXyz());
        OkLch lch = mappedLab.toLch();
        while (mappedRec2020.anyCoordinateMatches(coordinate -> coordinate < 0)) {
            lch = new OkLch(lch.l(), lch.c() * 0.99, lch.h());
            mappedLab = lch.toLab();
            mappedRec2020 = Rec2020.from(mappedLab.toXyz());
        }
        CieLab cieLab = CieLab.from(mappedRec2020.toXyz()).usingD65_IEC_61966_2_1();
        if (cieLab.l() < 0) {
            System.out.println("%s, %s".formatted(input, cieLab));
        }

        return mappedLab;
    }

    @Override
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
            var lab = OkLab.from(rec2020.toXyz());
            var mappedLab = toneMap(lab);
            var mappedRec2020 = Rec2020.from(mappedLab.toXyz());
            CieLab cieLab = CieLab.from(mappedRec2020.toXyz()).usingD65_IEC_61966_2_1();
            if (cieLab.l() < 0) {
                System.out.println("%s, %s".formatted(rec2020, cieLab));
            }
            return new double[]{mappedRec2020.r(), mappedRec2020.g(), mappedRec2020.b()};
        });
    }

    private static double maxL(RgbImage image) {
        return image.pixelStream().mapToDouble(rgb -> {
            var rec2020 = new Rec2020(rgb[0], rgb[1], rgb[2]);
            return OkLab.from(rec2020.toXyz()).l();
        }).max().orElse(0.0);
    }
}
