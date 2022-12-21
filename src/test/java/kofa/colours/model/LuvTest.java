package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.model.ConverterTest.LUV_663399;
import static kofa.colours.model.ConverterTest.XYZ_663399;
import static org.assertj.core.api.Assertions.assertThat;

class LuvTest {
    @Test
    void values() {
        assertThat(
                LUV_663399.values()
        ).containsExactly(32.90281, 12.9804, -67.75974);
    }

    @Test
    void toLch() {
        // from https://ajalt.github.io/colormath/converter/
        // converting degrees to radians, then getting it in the range returned by atan2
        double hRadians = toRadians(280.84448);
        assertIsCloseTo(
                LUV_663399.toLch(),
                new LchUv(32.90281, 68.99183, hRadians),
                PRECISE
        );
    }

    @Test
    void toXyz() {
        assertIsCloseTo(
                LUV_663399.toXyz().usingD65(),
                XYZ_663399,
                PRECISE
        );
    }

    @Test
    void fromXYZ() {
        // other branch of Y conditional
        assertIsCloseTo(
                Luv.from(XYZ_663399).usingD65(),
                LUV_663399,
                PRECISE
        );
    }
}