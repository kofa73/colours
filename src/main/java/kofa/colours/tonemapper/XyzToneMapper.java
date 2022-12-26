package kofa.colours.tonemapper;

import kofa.colours.model.Rec2020;
import kofa.colours.model.Xyz;
import kofa.io.RgbImage;
import kofa.maths.Solver;
import kofa.maths.ThanatomanicCurve6;

import java.util.Optional;

public class XyzToneMapper implements ToneMapper<Xyz> {
    private final ThanatomanicCurve6 curve;

    public XyzToneMapper() {
        this(0.7);
    }

    public XyzToneMapper(RgbImage image) {
        this(findOptimalShoulderStart(image));
    }

    public XyzToneMapper(double shoulderStart) {
        if (shoulderStart == 1) {
            curve = null;
        } else {
            curve = ThanatomanicCurve6.linearUntil(shoulderStart);
        }
    }

    private static double findOptimalShoulderStart(RgbImage image) {
        double shoulderStart;
        double maxY = maxY(image);
        if (maxY <= 1) {
            System.out.println("Tone mapping is not needed, maxL = " + maxY);
            shoulderStart = 1;
        } else {
            var shoulderSearchLow = 0.99;
            Optional<Double> shoulder;
            do {
                var shoulderSolver = new Solver(currentShoulder -> {
                    var curve = ThanatomanicCurve6.linearUntil(currentShoulder);
                    double mappedValue = applyCurve(curve, maxY);
                    System.out.println(currentShoulder + "," + mappedValue);
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
            } while (shoulder.isEmpty() && shoulderSearchLow >= 0.18);
            shoulderStart = shoulder.orElse(0.18);
            System.out.println("Will use shoulderStart = " + shoulderStart + " for tone mapping, maxY = " + maxY);
        }
        return shoulderStart;
    }

    private static double applyCurve(ThanatomanicCurve6 curve, double x) {
        return curve.mappedValueOf(x);
    }

    @Override
    public Xyz toneMap(Xyz input) {
        double mappedY;
        if (curve == null) {
            mappedY = input.Y();
        } else {
            mappedY = applyCurve(curve, input.Y());
        }
        if (mappedY < 0) {
            mappedY = 0;
        }
        return new Xyz(input.X(), mappedY, input.Z());
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
            var xyz = rec2020.toXyz();
            var mappedXyz = toneMap(xyz);
            var mappedRec2020 = Rec2020.from(mappedXyz);
            return new double[]{mappedRec2020.r(), mappedRec2020.g(), mappedRec2020.b()};
        });
    }

    private static double maxY(RgbImage image) {
        return image.pixelStream().mapToDouble(rgb -> {
            var rec2020 = new Rec2020(rgb[0], rgb[1], rgb[2]);
            return rec2020.toXyz().Y();
        }).max().orElse(0.0);
    }
}
