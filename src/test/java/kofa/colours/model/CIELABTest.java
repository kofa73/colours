package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.model.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;

class CIELABTest {
    @Test
    void values() {
        assertThat(new CIELAB(1, 2, 3).coordinates()).containsExactly(1.0, 2.0, 3.0);
    }

    @Test
    void fromXyz() {
        assertIsCloseTo(
                CIELAB.from(XYZ_663399).usingD65_2DegreeStandardObserver(),
                LAB_663399,
                PRECISE
        );
    }

    @Test
    void fromXyz_white() {
        assertIsCloseTo(
                CIELAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DegreeStandardObserver(),
                new CIELAB(100, 0, 0),
                PRECISE
        );
        assertIsCloseTo(
                CIELAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DegreeStandardObserver(),
                new CIELAB(100, 0, 0),
                PRECISE
        );
    }

    @Test
    void fromXyz_black() {
        assertIsCloseTo(
                CIELAB.from(new CIEXYZ(0, 0, 0)).usingD65_2DegreeStandardObserver(),
                new CIELAB(0, 0, 0),
                PRECISE
        );
    }

    @Test
    void toXyz() {
        assertIsCloseTo(
                LAB_663399.toXyz().usingD65_2DegreeStandardObserver(),
                XYZ_663399,
                PRECISE
        );

        // other branch of conditional
        assertIsCloseTo(
                CIELAB.from(ConverterTest.TINY_XYZ).usingD65_2DegreeStandardObserver().toXyz().usingD65_2DegreeStandardObserver(),
                TINY_XYZ,
                PRECISE
        );
    }

    @Test
    void toXyz_white() {
        assertIsCloseTo(
                new CIELAB(100, 0, 0).toXyz().usingD65_2DegreeStandardObserver(),
                CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER,
                PRECISE
        );
    }

    @Test
    void toXyz_black() {
        assertIsCloseTo(
                new CIELAB(0, 0, 0).toXyz().usingD65_2DegreeStandardObserver(),
                new CIEXYZ(0, 0, 0),
                PRECISE
        );
        assertThat(new CIELAB(0, 100, -100).toXyz().usingD65_2DegreeStandardObserver().Y()).isEqualTo(0);
    }

    @Test
    void toLch() {
        assertIsCloseTo(
                LAB_663399.toLch(),
                // values from https://ajalt.github.io/colormath/converter/ RGB #663399
                new CIELCh_ab(32.90281, 63.73612, toRadians(312.28943)),
                PRECISE
        );
    }
}
