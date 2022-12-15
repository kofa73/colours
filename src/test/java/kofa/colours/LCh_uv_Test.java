package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static org.assertj.core.api.Assertions.assertThat;

class LCh_uv_Test {
    private static final double H_RADIANS = (280.84448 / 360 * 2 * Math.PI);
    private final LCh_uv lch = new LCh_uv(32.90281, 68.99183, H_RADIANS);

    @Test
    void toDoubles() {
        assertThat(
                lch.values()
        ).containsExactly(32.90281, 68.99183, H_RADIANS);
    }

    @Test
    void toLuv() {
        assertIsCloseTo(
                lch.toLuv(),
                new Luv(32.90281, 12.9804, -67.75974),
                PRECISE
        );
    }
}