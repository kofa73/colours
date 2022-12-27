package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ;
import static kofa.colours.model.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;

class CieLabTest {
    @Test
    void values() {
        assertThat(new CieLab(1, 2, 3).coordinates()).containsExactly(1, 2, 3);
    }

    @Test
    void fromXyz() {
        assertIsCloseTo(
                CieLab.from(XYZ_663399).usingD65(),
                LAB_663399,
                PRECISE
        );
    }

    @Test
    void fromXyz_white() {
        assertIsCloseTo(
                CieLab.from(D65_WHITE_XYZ).usingD65(),
                new CieLab(100, 0, 0),
                PRECISE
        );
    }

    @Test
    void fromXyz_black() {
        assertIsCloseTo(
                CieLab.from(new Xyz(0, 0, 0)).usingD65(),
                new CieLab(0, 0, 0),
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
                CieLab.from(ConverterTest.TINY_XYZ).usingD65().toXyz().usingD65(),
                TINY_XYZ,
                PRECISE
        );
    }

    @Test
    void toXyz_white() {
        assertIsCloseTo(
                new CieLab(100, 0, 0).toXyz().usingD65(),
                D65_WHITE_XYZ,
                PRECISE
        );
    }

    @Test
    void toXyz_black() {
        assertIsCloseTo(
                new CieLab(0, 0, 0).toXyz().usingD65(),
                new Xyz(0, 0, 0),
                PRECISE
        );
        assertThat(new CieLab(0, 100, -100).toXyz().usingD65().Y()).isEqualTo(0);
    }

    @Test
    void toLch() {
        assertIsCloseTo(
                LAB_663399.toLch(),
                // values from https://ajalt.github.io/colormath/converter/ RGB #663399
                new CieLchAb(32.90281, 63.73612, toRadians(312.28943)),
                PRECISE
        );
    }
}
