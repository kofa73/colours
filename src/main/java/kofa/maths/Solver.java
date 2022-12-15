package kofa.maths;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;

public class Solver {
    private final Function<Double, Double> errorFunction;
    private double low;
    private double high;
    private double current;
    private double threshold;

    /**
     * @param errorFunction the function to evaluate the current guess. Must return < 0 if the guess is too low,
     *                      0 if the guess is perfect, > 0 if too high
     */
    public Solver(Function<Double, Double> errorFunction) {
        this.errorFunction = errorFunction;
    }

    Optional<Double> solve(double lowerBound, double upperBound, double startingPoint, double threshold) {
        initParams(lowerBound, upperBound, startingPoint, threshold);
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

    private void initParams(double lowerBound, double upperBound, double startingPoint, double threshold) {
        checkArgument(
                lowerBound < upperBound,
                "lowerBound %s must be < upperBound %s",
                lowerBound, upperBound
        );
        checkArgument(
                lowerBound <= startingPoint && upperBound >= startingPoint,
                "startingPoint %s must be in [%s, %s]",
                startingPoint, lowerBound, upperBound
        );
        checkArgument(threshold >= 0, "threshold %s must be >= 0", threshold);
        this.low = lowerBound;
        this.high = upperBound;
        this.current = startingPoint;
        this.threshold = threshold;
    }

    public double lastValue() {
        return current;
    }
}
