package kofa.maths;

import org.assertj.core.data.Offset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static kofa.maths.Curve.linearUntil;
import static org.assertj.core.api.Assertions.assertThat;

class CurveTest {
    public static final double SHOULDER_START = 0.78;
    private final Curve curve = linearUntil(SHOULDER_START);

    @ParameterizedTest
    @MethodSource("mappings")
    void testRolloff(double x, double expectedMappedValue) {
        assertThat(curve.mappedValueOf(x)).isEqualTo(expectedMappedValue, Offset.offset(0.0001));
    }

    private static Stream<Arguments> mappings() {
        return Stream.of(
                Arguments.of(0, 0),
                Arguments.of(SHOULDER_START, SHOULDER_START),
                /*
                 * read from the chart plotted at https://www.desmos.com/calculator
                 *
                 * 1+\left(a\cdot t-1\right)\cdot e^{\frac{\left(ax-at\right)}{\left(a\cdot t-1\right)}}\left\{t\le x\right\}
                 *
                 */
                Arguments.of(1.0, 0.9191),
                Arguments.of(1.5, 0.9917),
                Arguments.of(2.0, 0.9991)
        );
    }
}
