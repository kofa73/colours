package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.PI;
import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.*;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ_IEC_61966_2_1;
import static org.assertj.core.api.Assertions.assertThat;

class CieLchUvTest {
    private static final double H_RADIANS = toRadians(280.84448);
    private final CieLchUv lch = new CieLchUv(32.90281, 68.99183, H_RADIANS);

    @Test
    void white() {
        assertIsCloseTo(new CieLchUv(100, 0, 0).toLuv().toXyz().usingD65_IEC_61966_2_1(), D65_WHITE_XYZ_IEC_61966_2_1, PRECISE);
    }

    @Test
    void values() {
        assertThat(
                lch.coordinates()
        ).containsExactly(32.90281, 68.99183, H_RADIANS);
    }

    @Test
    void toLuv() {
        assertIsCloseTo(
                lch.toLuv(),
                new CieLuv(32.90281, 12.9804, -67.75974),
                PRECISE
        );
    }

    @Test
    void withHueInDegrees() {
        assertDegreesAreClose(new CieLchUv(10, 10, -2 * PI).hueInDegrees(), 0);
        assertDegreesAreClose(new CieLchUv(10, 10, -1.5 * PI).hueInDegrees(), 90);
        assertDegreesAreClose(new CieLchUv(10, 10, -1 * PI).hueInDegrees(), 180);
        assertDegreesAreClose(new CieLchUv(10, 10, -0.5 * PI).hueInDegrees(), 270);
        assertDegreesAreClose(new CieLchUv(10, 10, 0 * PI).hueInDegrees(), 360);
        assertDegreesAreClose(new CieLchUv(10, 10, 0.5 * PI).hueInDegrees(), 90);
        assertDegreesAreClose(new CieLchUv(10, 10, 1 * PI).hueInDegrees(), 180);
        assertDegreesAreClose(new CieLchUv(10, 10, 1.5 * PI).hueInDegrees(), 270);
        assertDegreesAreClose(new CieLchUv(10, 10, 2 * PI).hueInDegrees(), 360);
    }
}