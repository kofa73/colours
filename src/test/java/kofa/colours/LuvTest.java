package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static org.assertj.core.api.Assertions.assertThat;

class LuvTest {
    private final Luv luv = new Luv(32.90281, 12.9804, -67.75974);

    @Test
    void values() {
        assertThat(
                luv.values()
        ).containsExactly(32.90281, 12.9804, -67.75974);
    }

    @Test
    void toLCh() {
        // from https://ajalt.github.io/colormath/converter/
        double hRadians = (2 * Math.PI * 280.84448 / 360 - 2 * Math.PI);
        assertIsCloseTo(
                luv.toLch_uv(),
                // converting degrees to radians, then getting it in the range returned by atan2
                new LCh_uv(32.90281, 68.99183, hRadians),
                PRECISE
        );
    }
}