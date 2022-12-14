package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericHelper.assertApproximatelyEqual;
import static org.assertj.core.api.Assertions.assertThat;

class LuvTest {
    private final Luv luv = new Luv(32.90281f, 12.9804f, -67.75974f);

    @Test
    void x() {
        assertThat(
                luv.toFloats()
        ).containsExactly(32.90281f, 12.9804f, -67.75974f);
    }

    @Test
    void toLCh() {
        // from https://ajalt.github.io/colormath/converter/
        float hRadians = (float) (2 * Math.PI * 280.84448f / 360f - 2 * Math.PI);
        assertApproximatelyEqual(
                luv.toLch_uv(),
                // converting degrees to radians, then getting it in the range returned by atan2
                new LCh_uv(32.90281f, 68.99183f, hRadians),
                1E-5f
        );
    }
}