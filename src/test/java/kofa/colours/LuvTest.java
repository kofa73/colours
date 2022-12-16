package kofa.colours;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
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
        // converting degrees to radians, then getting it in the range returned by atan2
        double hRadians = toRadians(280.84448);
        assertIsCloseTo(
                luv.toLch(),
                new LCh_uv(32.90281, 68.99183, hRadians),
                PRECISE
        );
    }
}