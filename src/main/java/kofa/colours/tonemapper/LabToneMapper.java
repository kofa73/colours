package kofa.colours.tonemapper;

import kofa.colours.model.CieLab;
import kofa.colours.model.Rec2020;
import kofa.io.RgbImage;
import kofa.maths.Solver;
import kofa.maths.ThanatomanicCurve6;

import java.util.Optional;

public class LabToneMapper implements ToneMapper<CieLab> {
    private final ThanatomanicCurve6 curve;

    public LabToneMapper() {
        this(0.7);
    }

    public LabToneMapper(RgbImage image) {
        this(findOptimalShoulderStart(image));
    }

    public LabToneMapper(double shoulderStart) {
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
                shoulder = shoulderSolver.solve(shoulderSearchLow, shoulderSearchLow + 0.01, (shoulderSearchLow + shoulderSearchLow + 0.01) / 2, 0);
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
    public CieLab toneMap(CieLab input) {
        double mappedL;
        if (curve == null) {
            mappedL = input.L();
        } else {
            double normalisedL = input.L() / 100;
            mappedL = applyCurve(curve, normalisedL) * 100;
        }
        if (mappedL < 0) {
            mappedL = 0;
        }
        return new CieLab(mappedL, input.a(), input.b());
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
            var lab = CieLab.from(rec2020.toXyz()).usingD65();
            var mappedLab = toneMap(lab);
            var mappedRec2020 = Rec2020.from(mappedLab.toXyz().usingD65());
            return new double[]{mappedRec2020.r(), mappedRec2020.g(), mappedRec2020.b()};
        });
    }

    private static double maxL(RgbImage image) {
        return image.pixelStream().mapToDouble(rgb -> {
            var rec2020 = new Rec2020(rgb[0], rgb[1], rgb[2]);
            return CieLab.from(rec2020.toXyz()).usingD65().L();
        }).max().orElse(0.0);
    }
}
