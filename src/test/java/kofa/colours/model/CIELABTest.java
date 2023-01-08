package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.EXACT;
import static kofa.NumericAssertions.PRECISE;
import static kofa.Vector3Assert.assertThat;
import static kofa.colours.model.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;

class CIELABTest {
    @Test
    void values() {
        assertThat(new CIELAB(1, 2, 3).coordinates()).containsExactly(1.0, 2.0, 3.0);
    }

    @Test
    void fromXyz() {
        assertThat(CIELAB.from(XYZ_663399).usingD65_2DEGREE_STANDARD_OBSERVER())
                .isCloseTo(LAB_663399, PRECISE);
    }

    @Test
    void fromXyz_white() {
        assertThat(CIELAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DEGREE_STANDARD_OBSERVER())
                .isCloseTo(new CIELAB(100, 0, 0), EXACT);
        assertThat(CIELAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DEGREE_STANDARD_OBSERVER())
                .isCloseTo(new CIELAB(100, 0, 0), EXACT);
    }

    @Test
    void fromXyz_black() {
        assertThat(CIELAB.from(new CIEXYZ(0, 0, 0)).usingD65_2DEGREE_STANDARD_OBSERVER())
                .isCloseTo(new CIELAB(0, 0, 0), EXACT);
    }

    @Test
    void toXyz() {
        assertThat(LAB_663399.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER()).isCloseTo(XYZ_663399, PRECISE);

        // other branch of conditional
        assertThat(
                CIELAB.from(ConverterTest.TINY_XYZ).usingD65_2DEGREE_STANDARD_OBSERVER().toXyz().usingD65_2DEGREE_STANDARD_OBSERVER())
                .isCloseTo(TINY_XYZ, EXACT);
    }

    @Test
    void toXyz_white() {
        assertThat(
                new CIELAB(100, 0, 0).toXyz().usingD65_2DEGREE_STANDARD_OBSERVER())
                .isCloseTo(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, EXACT);
    }

    @Test
    void toXyz_black() {
        assertThat(new CIELAB(0, 0, 0).toXyz().usingD65_2DEGREE_STANDARD_OBSERVER())
                .isCloseTo(new CIEXYZ(0, 0, 0), EXACT);
        assertThat(new CIELAB(0, 100, -100).toXyz().usingD65_2DEGREE_STANDARD_OBSERVER().Y()).isEqualTo(0);
    }

    @Test
    void toLch() {
        assertThat(LAB_663399.toLch()).isCloseTo(
                // values from https://ajalt.github.io/colormath/converter/ RGB #663399
                new CIELCh_ab(32.90281, 63.73612, toRadians(312.28943)),
                PRECISE
        );
    }
}
