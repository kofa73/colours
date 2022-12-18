package kofa.colours;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;

class LabTest {
    @Test
    void values() {
        assertThat(new Lab(1, 2, 3).values()).containsExactly(1, 2, 3);
    }

    @Test
    void fromXYZ() {
        assertIsCloseTo(
                Lab.from(XYZ_663399).usingD65(),
                LAB_663399,
                PRECISE
        );
    }

    @Test
    void toXYZ() {
        assertIsCloseTo(
                LAB_663399.toXYZ().usingD65(),
                XYZ_663399,
                PRECISE
        );

        // other branch of conditional
        assertIsCloseTo(
                Lab.from(ConverterTest.TINY_XYZ).usingD65().toXYZ().usingD65(),
                TINY_XYZ,
                PRECISE
        );
    }

    @Test
    void toLCh_ab() {
        assertIsCloseTo(
                LAB_663399.toLCh(),
                // values from https://ajalt.github.io/colormath/converter/ RGB #663399
                new LCh_ab(32.90281, 63.73612, toRadians(312.28943)),
                PRECISE
        );
    }
}
