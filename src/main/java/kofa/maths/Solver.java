package kofa.maths;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;

public class Solver {
    private final PrimitiveDoubleToDoubleFunction errorFunction;
    private double low;
    private double high;
    private double current;
    private double threshold;

    /**
     * @param errorFunction the function to evaluate the current guess. Must return < 0 if the guess is too low,
     *                      0 if the guess is perfect, > 0 if too high
     */
    public Solver(PrimitiveDoubleToDoubleFunction errorFunction) {
        this.errorFunction = errorFunction;
    }

    public Optional<Double> solve(double lowerBound, double upperBound, double threshold) {
        initParams(lowerBound, upperBound, threshold);
        return doSolve();
    }

    private Optional<Double> doSolve() {
        Optional<Double> solution = Optional.empty();
        // for safety, if we're oscillating somehow
        int iterations = 0;
        do {
            iterations++;
            double error = errorFunction.apply(current);
            if (abs(error) <= threshold) {
                solution = Optional.of(current);
            } else {
                if (error < 0) {
                    low = current;
                } else {
                    high = current;
                }
                current = (high + low) / 2;
            }
        } while (solution.isEmpty() && current != high && current != low && iterations < 200);
        return solution;
    }

    private void initParams(double lowerBound, double upperBound, double threshold) {
        checkArgument(
                lowerBound < upperBound,
                "lowerBound %s must be < upperBound %s",
                lowerBound, upperBound
        );
        checkArgument(threshold >= 0, "threshold %s must be >= 0, but was %s", threshold);
        this.low = lowerBound;
        this.high = upperBound;
        this.threshold = threshold;
    }

    public double lastValue() {
        return current;
    }
}
