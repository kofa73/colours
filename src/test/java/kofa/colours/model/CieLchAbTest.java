package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.PI;
import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class CieLchAbTest {
    private static final double H_RADIANS = toRadians(312.28943);
    private final CieLchAb lch = new CieLchAb(32.90281, 63.73612, H_RADIANS);

    @Test
    void values() {
        assertThat(
                lch.coordinates()
        ).containsExactly(32.90281, 63.73612, H_RADIANS);
    }

    @Test
    void toLab() {
        assertIsCloseTo(
                lch.toLab(),
                new CieLab(32.90281, 42.88651, -47.14914),
                PRECISE
        );
    }

    @Test
    void withHueInDegrees() {
        assertDegreesAreClose(new CieLchAb(10, 10, -2 * PI).withHueInDegrees()[2], 0);
        assertDegreesAreClose(new CieLchAb(10, 10, -1.5 * PI).withHueInDegrees()[2], 90);
        assertDegreesAreClose(new CieLchAb(10, 10, -1 * PI).withHueInDegrees()[2], 180);
        assertDegreesAreClose(new CieLchAb(10, 10, -0.5 * PI).withHueInDegrees()[2], 270);
        assertDegreesAreClose(new CieLchAb(10, 10, 0 * PI).withHueInDegrees()[2], 360);

        assertDegreesAreClose(new CieLchAb(10, 10, 0.5 * PI).withHueInDegrees()[2], 90);
        assertDegreesAreClose(new CieLchAb(10, 10, 1 * PI).withHueInDegrees()[2], 180);
        assertDegreesAreClose(new CieLchAb(10, 10, 1.5 * PI).withHueInDegrees()[2], 270);
        assertDegreesAreClose(new CieLchAb(10, 10, 2 * PI).withHueInDegrees()[2], 360);

        assertDegreesAreClose(new CieLchAb(10, 10, 2.5 * PI).withHueInDegrees()[2], 90);
        assertDegreesAreClose(new CieLchAb(10, 10, 3 * PI).withHueInDegrees()[2], 180);
        assertDegreesAreClose(new CieLchAb(10, 10, 3.5 * PI).withHueInDegrees()[2], 270);
        assertDegreesAreClose(new CieLchAb(10, 10, 4 * PI).withHueInDegrees()[2], 360);
    }
}