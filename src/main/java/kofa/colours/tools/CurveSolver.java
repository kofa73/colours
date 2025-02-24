package kofa.colours.tools;

import kofa.maths.Solver;
import kofa.maths.ThanatomanicCurve6;

import java.util.Optional;

public class CurveSolver {

    public static final double MIN_SHOULDER_START = 0.8;

    public static <S> double findOptimalShoulderStart(double maxValue) {
        if (maxValue > 1000) {
            System.out.println("ouch");
        }
        double shoulderStart;
        if (maxValue <= 1) {
            System.out.println("Compression is not needed, maxValue = " + maxValue);
            shoulderStart = 1;
        } else {
            var shoulderSearchLow = 0.9999;
            Optional<Double> shoulder;
            do {
                var shoulderSolver = new Solver(currentShoulder -> {
                    var curve = ThanatomanicCurve6.linearUntil(currentShoulder);
                    double mappedValue = curve.mappedValueOf(maxValue);
                    return mappedValue < 0.99 ?
                            -1.0 :
                            mappedValue > 0.9999 ?
                                    1 :
                                    0;
                });
                shoulder = shoulderSolver.solve(shoulderSearchLow, 1 - 1e-6, 0);
                if (shoulder.isEmpty()) {
                    shoulderSearchLow -= 0.01;
                }
            } while (shoulder.isEmpty() && shoulderSearchLow > MIN_SHOULDER_START);
            shoulderStart = shoulder.orElse(MIN_SHOULDER_START);
            System.out.println("Will use shoulderStart = " + shoulderStart + " for compression, maxValue = " + maxValue);
        }
        return shoulderStart;
    }
}
