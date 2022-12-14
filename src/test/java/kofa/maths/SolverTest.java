package kofa.maths;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static java.lang.Math.sqrt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SolverTest {
    @Test
    void solveSuccess() {
        // given
        float threshold = 0;
        // requires 89 iterations in the Solver
        float evilValue = 1.7023637E38f;

        Function<Float, Float> errorFunction = squareRootError(evilValue);
        Solver solver = new Solver(errorFunction);

        // when
        Optional<Float> solution = solver.solve(0, evilValue, 0, threshold);

        // then
        assertThat(solution).isPresent()
                .get()
                .satisfies(root -> assertThat(root * root).isCloseTo(evilValue, within(threshold)))
                .satisfies(root -> assertThat(errorFunction.apply(root)).isCloseTo(0, within(threshold)));
    }

    @Test
    void solveFailure_solutionOutsideRange() {
        // given
        Solver solver = new Solver(squareRootError(10));

        // when
        Optional<Float> solution = solver.solve(0, 1, 0.5f, 0.001f);

        // then
        assertThat(solution).isEmpty();
    }

    @Test
    void solveFailure_solutionNotPreciseEnough() {
        // given
        double value = 123456789012.34567890123f;

        Solver solver = new Solver(doubleBasedSquareRootError(value));
        Optional<Float> solution = solver.solve(0, (float) value, 1, 0f);

        assertThat(solution).isEmpty();
        assertThat(solver.lastValue()).isCloseTo((float) sqrt(value), within(0.000_001f));
    }

    private static Function<Float, Float> squareRootError(float value) {
        return guess -> guess * guess - value;
    }

    private static Function<Float, Float> doubleBasedSquareRootError(double value) {
        return guess -> {
            double error = (double) guess * guess - value;
            return error == 0 ? 0f :
                    error > 0 ? 1f : -1f;
        };
    }
}
