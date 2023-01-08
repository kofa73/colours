package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.PI;
import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.*;
import static kofa.Vector3Assert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

class CIELChabTest {
    private static final double H_RADIANS = toRadians(312.28943);
    private final CIELCh_ab lch = new CIELCh_ab(32.90281, 63.73612, H_RADIANS);

    @Test
    void white() {
        assertThat(new CIELCh_ab(100, 0, 0).toLab().toXyz().usingD65_2DEGREE_STANDARD_OBSERVER())
                .isCloseTo(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, EXACT);
    }

    @Test
    void values() {
        assertThat(
                lch.coordinates()
        ).containsExactly(32.90281, 63.73612, H_RADIANS);
    }

    @Test
    void toLab() {
        assertThat(lch.toLab()).isCloseTo(new CIELAB(32.90281, 42.88651, -47.14914), PRECISE);
    }

    @Test
    void withHueInDegrees() {
        assertDegreesAreClose(new CIELCh_ab(10, 10, -2 * PI).hueInDegrees(), 0);
        assertDegreesAreClose(new CIELCh_ab(10, 10, -1.5 * PI).hueInDegrees(), 90);
        assertDegreesAreClose(new CIELCh_ab(10, 10, -1 * PI).hueInDegrees(), 180);
        assertDegreesAreClose(new CIELCh_ab(10, 10, -0.5 * PI).hueInDegrees(), 270);
        assertDegreesAreClose(new CIELCh_ab(10, 10, 0 * PI).hueInDegrees(), 360);

        assertDegreesAreClose(new CIELCh_ab(10, 10, 0.5 * PI).hueInDegrees(), 90);
        assertDegreesAreClose(new CIELCh_ab(10, 10, 1 * PI).hueInDegrees(), 180);
        assertDegreesAreClose(new CIELCh_ab(10, 10, 1.5 * PI).hueInDegrees(), 270);
        assertDegreesAreClose(new CIELCh_ab(10, 10, 2 * PI).hueInDegrees(), 360);

        assertDegreesAreClose(new CIELCh_ab(10, 10, 2.5 * PI).hueInDegrees(), 90);
        assertDegreesAreClose(new CIELCh_ab(10, 10, 3 * PI).hueInDegrees(), 180);
        assertDegreesAreClose(new CIELCh_ab(10, 10, 3.5 * PI).hueInDegrees(), 270);
        assertDegreesAreClose(new CIELCh_ab(10, 10, 4 * PI).hueInDegrees(), 360);
    }
}