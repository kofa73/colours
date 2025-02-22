package kofa.colours.tools;

import kofa.maths.Solver;
import kofa.maths.ThanatomanicCurve6;

import java.util.Optional;

public class CurveSolver {
    public static <S> double findOptimalShoulderStart(double maxValue) {
        double shoulderStart;
        if (maxValue <= 1) {
            System.out.println("Compression is not needed, maxValue = " + maxValue);
            shoulderStart = 1;
        } else {
            var shoulderSearchLow = 0.99;
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
                shoulder = shoulderSolver.solve(shoulderSearchLow, shoulderSearchLow + 0.01, 0);
                if (shoulder.isEmpty()) {
                    shoulderSearchLow -= 0.01;
                }
            } while (shoulder.isEmpty() && shoulderSearchLow > 0.5);
            shoulderStart = shoulder.orElse(0.01);
            System.out.println("Will use shoulderStart = " + shoulderStart + " for compression, maxValue = " + maxValue);
        }
        return shoulderStart;
    }
}
