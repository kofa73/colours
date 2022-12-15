package kofa.maths;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static org.assertj.core.api.Assertions.assertThat;

class MatrixTest {
    @Test
    void multiply_vector() {
        // given
        var matrix = new Matrix<>(2, 3)
                .row(1.0, -1.0, 2.0)
                .row(0, -3, 1);

        // when
        double[] result = matrix.multipliedBy(2, 1, 0);

        // then
        assertThat(result).containsExactly(1, -3);
    }

    void multiply_matrix() {
        // given
        var matrix = new Matrix<>(2, 3)
                .row(1, 2, 3)
                .row(4, 5, 6);
        var multiplier = new Matrix<>(3, 2)
                .row(7, 8)
                .row(9, 10)
                .row(11, 12);

        // when
        Matrix<?> result = matrix.multipliedBy(multiplier);

        // then
        var expectedResult = new Matrix<>(2, 2)
                .row(58, 64)
                .row(139, 154);
        assertIsCloseTo(result, expectedResult, PRECISE);
    }
}