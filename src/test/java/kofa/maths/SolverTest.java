package kofa.maths;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SolverTest {
    @Test
    void solveSuccess() {
        // given
        double threshold = 0;
        // requires 116 iterations in the Solver
        double evilValue = 1.7023637E38;

        PrimitiveDoubleToDoubleFunction errorFunction = squareRootError(evilValue);
        Solver solver = new Solver(errorFunction);

        // when
        Optional<Double> solution = solver.solve(0, evilValue, threshold);

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
        Optional<Double> solution = solver.solve(0, 1, 0.001);

        // then
        assertThat(solution).isEmpty();
    }

    @Test
    void solveFailure_solutionNotPreciseEnough() {
        // given
        double value = 123456789012.34567890123;

        Solver solver = new Solver(doubleBasedSquareRootError(value));
        Optional<Double> solution = solver.solve(0, value, 0);

        assertThat(solution).isEmpty();
        // we came close, but did not get a precise solution due to limited numeric precision
        assertThat(solver.lastValue() * solver.lastValue()).isCloseTo(value, within(1E-4));
    }

    private static PrimitiveDoubleToDoubleFunction squareRootError(double value) {
        return guess -> guess * guess - value;
    }

    private static PrimitiveDoubleToDoubleFunction doubleBasedSquareRootError(double value) {
        return guess -> {
            double error = guess * guess - value;
            return error == 0 ? 0d :
                    error > 0 ? 1d : -1d;
        };
    }
}
