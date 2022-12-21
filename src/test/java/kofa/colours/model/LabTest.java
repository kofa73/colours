package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ;
import static kofa.colours.model.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;

class LabTest {
    @Test
    void values() {
        assertThat(new Lab(1, 2, 3).coordinates()).containsExactly(1, 2, 3);
    }

    @Test
    void fromXyz() {
        assertIsCloseTo(
                Lab.from(XYZ_663399).usingD65(),
                LAB_663399,
                PRECISE
        );
    }

    @Test
    void fromXyz_white() {
        assertIsCloseTo(
                Lab.from(D65_WHITE_XYZ).usingD65(),
                new Lab(100, 0, 0),
                PRECISE
        );
    }

    @Test
    void fromXyz_black() {
        assertIsCloseTo(
                Lab.from(new Xyz(0, 0, 0)).usingD65(),
                new Lab(0, 0, 0),
                PRECISE
        );
    }

    @Test
    void toXyz() {
        assertIsCloseTo(
                LAB_663399.toXyz().usingD65(),
                XYZ_663399,
                PRECISE
        );

        // other branch of conditional
        assertIsCloseTo(
                Lab.from(ConverterTest.TINY_XYZ).usingD65().toXyz().usingD65(),
                TINY_XYZ,
                PRECISE
        );
    }

    @Test
    void toXyz_white() {
        assertIsCloseTo(
                new Lab(100, 0, 0).toXyz().usingD65(),
                D65_WHITE_XYZ,
                PRECISE
        );
    }

    @Test
    void toXyz_black() {
        assertIsCloseTo(
                new Lab(0, 0, 0).toXyz().usingD65(),
                new Xyz(0, 0, 0),
                PRECISE
        );
        assertThat(new Lab(0, 100, -100).toXyz().usingD65().Y()).isEqualTo(0);
    }

    @Test
    void toLch() {
        assertIsCloseTo(
                LAB_663399.toLch(),
                // values from https://ajalt.github.io/colormath/converter/ RGB #663399
                new LchAb(32.90281, 63.73612, toRadians(312.28943)),
                PRECISE
        );
    }
}
