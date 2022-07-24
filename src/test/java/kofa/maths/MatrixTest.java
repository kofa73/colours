package kofa.maths;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MatrixTest {
    @Test
    void multiply() {
        // given
        Matrix matrix = new Matrix(2, 3)
                .row(1.0f, -1.0f, 2.0f)
                .row(0, -3, 1);

        // when
        float[] result = matrix.multiply(2, 1, 0);

        // then
        assertThat(result).containsExactly(1, -3);
    }
}