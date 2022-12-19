package kofa.colours;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.ConverterTest.Luv_663399;
import static kofa.colours.ConverterTest.XYZ_663399;
import static org.assertj.core.api.Assertions.assertThat;

class LuvTest {
    @Test
    void values() {
        assertThat(
                Luv_663399.values()
        ).containsExactly(32.90281, 12.9804, -67.75974);
    }

    @Test
    void toLCh() {
        // from https://ajalt.github.io/colormath/converter/
        // converting degrees to radians, then getting it in the range returned by atan2
        double hRadians = toRadians(280.84448);
        assertIsCloseTo(
                Luv_663399.toLCh(),
                new LCh_uv(32.90281, 68.99183, hRadians),
                PRECISE
        );
    }

    @Test
    void toXYZ() {
        assertIsCloseTo(
                Luv_663399.toXYZ().usingD65(),
                XYZ_663399,
                PRECISE
        );
    }

    @Test
    void fromXYZ() {
        // other branch of Y conditional
        assertIsCloseTo(
                Luv.from(XYZ_663399).usingD65(),
                Luv_663399,
                PRECISE
        );
    }
}