package kofa.maths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Function;

import static java.lang.Math.PI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class SolverTest {
    private static final float SQUARE_OF_PI = (float) (PI * PI);
    private static final Function<Float, Float> SQUARE_ROOT_EVALUATOR = guess -> SQUARE_OF_PI - guess * guess;
    private final Solver solver = new Solver(SQUARE_ROOT_EVALUATOR);

    @Test
    void solveSuccess() {
        float threshold = 0.000_001f;
        Optional<Float> solution = solver.solve(0, 10, 1, threshold);

        assertThat(solution).isPresent();
        assertThat(solution.get()).isEqualTo((float) PI, offset(threshold));
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
    }

    @AfterEach
    void log() {
        System.out.println("Last value = " + solver.lastValue() + ", iterations = " + solver.iterations());
    }

}