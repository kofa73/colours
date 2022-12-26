package kofa.colours.tonemapper;

import kofa.colours.model.Lab;
import kofa.colours.model.Rec2020;
import kofa.io.RgbImage;
import kofa.maths.Solver;
import kofa.maths.ThanatomanicCurve6;

import java.util.Optional;

import static java.lang.Math.max;

public class LabToneMapper implements ToneMapper<Lab> {
    private final double shoulderStart;
    private final ThanatomanicCurve6 curve;

    public LabToneMapper() {
        shoulderStart = 70;
        curve = ThanatomanicCurve6.linearUntil(shoulderStart / 100);
    }

    public LabToneMapper(RgbImage image) {
        double maxL = maxL(image);
        if (maxL <= 100) {
            System.out.println("Tone mapping is not needed, maxL = " + maxL);
            shoulderStart = 100;
            curve = null;
        } else {
            var shoulderSearchLow = 0.99;
            Optional<Double> shoulder;
            var normalisedMaxL = maxL / 100;
            do {
                var shoulderSolver = new Solver(currentShoulder -> {
                    var curve = ThanatomanicCurve6.linearUntil(currentShoulder);
                    double mappedValue = applyCurveWithDampening(curve, currentShoulder, normalisedMaxL);
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
            curve = ThanatomanicCurve6.linearUntil(shoulderStart);
            System.out.println("Will use shoulderStart = " + shoulderStart + " for tone mapping, maxL = " + maxL);
        }
    }

    private double applyCurveWithDampening(ThanatomanicCurve6 curve, double shoulder, double x) {
        return curve.mappedValueOf(x);
    }

    @Override
    public Lab toneMap(Lab input) {
        double mappedL;
        if (curve == null) {
            mappedL = input.L();
        } else {
            double normalisedL = input.L() / 100;
            mappedL = applyCurveWithDampening(curve, shoulderStart, normalisedL) * 100;
        }
        if (mappedL < 0) {
            mappedL = 0;
        }
        return new Lab(mappedL, input.a(), input.b());
    }

    @Override
    public void toneMap(RgbImage image) {
        if (curve == null) {
            return;
        }
        double[][] red = image.redChannel();
        double[][] green = image.greenChannel();
        double[][] blue = image.blueChannel();
        for (int row = 0; row < image.height(); row++) {
            for (int column = 0; column < image.width(); column++) {
                var rec2020 = new Rec2020(
                        red[row][column],
                        green[row][column],
                        blue[row][column]
                );
                var lab = Lab.from(rec2020.toXyz()).usingD65();
                var mappedLab = toneMap(lab);
                var mappedRec2020 = Rec2020.from(mappedLab.toXyz().usingD65());
                red[row][column] = mappedRec2020.r();
                green[row][column] = mappedRec2020.g();
                blue[row][column] = mappedRec2020.b();
            }
        }
    }

    private static double maxL(RgbImage image) {
        double maxL = 0;
        double[][] red = image.redChannel();
        double[][] green = image.greenChannel();
        double[][] blue = image.blueChannel();
        for (int row = 0; row < image.height(); row++) {
            for (int column = 0; column < image.width(); column++) {
                var rec2020 = new Rec2020(
                        red[row][column],
                        green[row][column],
                        blue[row][column]
                );
                var l = Lab.from(rec2020.toXyz()).usingD65().L();
                maxL = max(maxL, l);
            }
        }
        return maxL;
    }
}
