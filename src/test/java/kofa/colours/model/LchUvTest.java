package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.PI;
import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class LchUvTest {
    private static final double H_RADIANS = toRadians(280.84448);
    private final LCh_uv lch = new LCh_uv(32.90281, 68.99183, H_RADIANS);

    @Test
    void values() {
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

    @Test
    void withHueInDegrees() {
        assertDegreesAreClose(new LCh_uv(10, 10, -2 * PI).withHueInDegrees()[2], 0);
        assertDegreesAreClose(new LCh_uv(10, 10, -1.5 * PI).withHueInDegrees()[2], 90);
        assertDegreesAreClose(new LCh_uv(10, 10, -1 * PI).withHueInDegrees()[2], 180);
        assertDegreesAreClose(new LCh_uv(10, 10, -0.5 * PI).withHueInDegrees()[2], 270);
        assertDegreesAreClose(new LCh_uv(10, 10, 0 * PI).withHueInDegrees()[2], 360);
        assertDegreesAreClose(new LCh_uv(10, 10, 0.5 * PI).withHueInDegrees()[2], 90);
        assertDegreesAreClose(new LCh_uv(10, 10, 1 * PI).withHueInDegrees()[2], 180);
        assertDegreesAreClose(new LCh_uv(10, 10, 1.5 * PI).withHueInDegrees()[2], 270);
        assertDegreesAreClose(new LCh_uv(10, 10, 2 * PI).withHueInDegrees()[2], 360);
    }
}