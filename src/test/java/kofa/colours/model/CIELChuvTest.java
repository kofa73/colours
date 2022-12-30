package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.PI;
import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class CIELChuvTest {
    private static final double H_RADIANS = toRadians(280.84448);
    private final CIELCh_uv lch = new CIELCh_uv(32.90281, 68.99183, H_RADIANS);

    @Test
    void white() {
        assertIsCloseTo(new CIELCh_uv(100, 0, 0).toLuv().toXyz().usingD65_2DegreeStandardObserver(), CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, EXACT);
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
                new CIELUV(32.90281, 12.9804, -67.75974),
                PRECISE
        );
    }

    @Test
    void withHueInDegrees() {
        assertDegreesAreClose(new CIELCh_uv(10, 10, -2 * PI).hueInDegrees(), 0);
        assertDegreesAreClose(new CIELCh_uv(10, 10, -1.5 * PI).hueInDegrees(), 90);
        assertDegreesAreClose(new CIELCh_uv(10, 10, -1 * PI).hueInDegrees(), 180);
        assertDegreesAreClose(new CIELCh_uv(10, 10, -0.5 * PI).hueInDegrees(), 270);
        assertDegreesAreClose(new CIELCh_uv(10, 10, 0 * PI).hueInDegrees(), 360);
        assertDegreesAreClose(new CIELCh_uv(10, 10, 0.5 * PI).hueInDegrees(), 90);
        assertDegreesAreClose(new CIELCh_uv(10, 10, 1 * PI).hueInDegrees(), 180);
        assertDegreesAreClose(new CIELCh_uv(10, 10, 1.5 * PI).hueInDegrees(), 270);
        assertDegreesAreClose(new CIELCh_uv(10, 10, 2 * PI).hueInDegrees(), 360);
    }
}