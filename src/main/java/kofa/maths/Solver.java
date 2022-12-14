package kofa.maths;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.abs;

public class Solver {
    private final Function<Float, Float> errorFunction;
    private float low;
    private float high;
    private float current;
    private float threshold;

    /**
     * @param errorFunction the function to evaluate the current guess. Must return < 0 if the guess is too low,
     *                      0 if the guess is perfect, > 0 if too high
     */
    public Solver(Function<Float, Float> errorFunction) {
        this.errorFunction = errorFunction;
    }

    Optional<Float> solve(float lowerBound, float upperBound, float startingPoint, float threshold) {
        initParams(lowerBound, upperBound, startingPoint, threshold);
        return doSolve();
    }

    private Optional<Float> doSolve() {
        Optional<Float> solution = Optional.empty();
        // for safety, if we're oscillating somehow
        int iterations = 0;
        do {
            iterations++;
            float error = errorFunction.apply(current);
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
        } while (solution.isEmpty() && current != high && current != low && iterations < 100);
        return solution;
    }

    private void initParams(float lowerBound, float upperBound, float startingPoint, float threshold) {
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

    public float lastValue() {
        return current;
    }
}
