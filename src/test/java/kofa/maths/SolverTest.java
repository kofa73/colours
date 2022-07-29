package kofa.maths;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static java.lang.Math.PI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SolverTest {
    private static final float PI_FLOAT = (float) PI;
    private static final float SQUARE_OF_PI = (float) (PI * PI);
    private static final Function<Float, Float> SQUARE_ERROR = guess -> guess * guess - SQUARE_OF_PI;
    private final Solver solver = new Solver(SQUARE_ERROR);

    @Test
    void solveSuccess() {
        float threshold = 0.0001f;
        Optional<Float> solution = solver.solve(0, 10, 1, threshold);

        assertThat(solution).isPresent()
                .get()
                .satisfies(number -> assertThat(number).isCloseTo(PI_FLOAT, within(threshold)))
                .satisfies(number -> assertThat(SQUARE_ERROR.apply(number)).isCloseTo(0, within(threshold)));
    }

    @Test
    void solveFailure_solutionOutsideRange() {
        Optional<Float> solution = solver.solve(0, 1, 1, 0.001f);

        assertThat(solution).isEmpty();
    }

    @Test
    void solveFailure_solutionNotPreciseEnough() {
        Optional<Float> solution = solver.solve(0, 10, 1, 0f);

        assertThat(solution).isEmpty();
        assertThat(solver.lastValue()).isCloseTo(PI_FLOAT, within(0.000_001f));
    }
}