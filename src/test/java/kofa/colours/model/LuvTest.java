package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ;
import static kofa.colours.model.ConverterTest.LUV_663399;
import static kofa.colours.model.ConverterTest.XYZ_663399;
import static org.assertj.core.api.Assertions.assertThat;

class LuvTest {
    @Test
    void values() {
        assertThat(
                LUV_663399.coordinates()
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
    void toXyz_white() {
        assertIsCloseTo(
                new Luv(100, 0, 0).toXyz().usingD65(),
                D65_WHITE_XYZ,
                PRECISE
        );
    }

    @Test
    void toXyz_black() {
        assertIsCloseTo(
                new Luv(0, 0, 0).toXyz().usingD65(),
                new Xyz(0, 0, 0),
                PRECISE
        );
        assertThat(new Luv(0, 100, -100).toXyz().usingD65().Y()).isEqualTo(0);
    }

    @Test
    void toXyz_L_below_kappa() {
        assertIsCloseTo(
                new Luv(7.99999, 1, 1).toXyz().usingD65(),
                new Xyz(0.00865, 0.00886, 0.00843),
                PRECISE
        );
    }

    @Test
    void fromXyz() {
        // other branch of Y conditional
        assertIsCloseTo(
                Luv.from(XYZ_663399).usingD65(),
                LUV_663399,
                PRECISE
        );
    }

    @Test
    void fromXyz_white() {
        assertIsCloseTo(
                Luv.from(D65_WHITE_XYZ).usingD65(),
                new Luv(100, 0, 0),
                PRECISE
        );
    }


    @Test
    void fromXyz_black() {
        assertIsCloseTo(
                Luv.from(new Xyz(100, 0, -100)).usingD65(),
                new Luv(0, 0, 0),
                PRECISE
        );
    }

    @Test
    void fromXyz_yr_below_epsilon() {
        assertIsCloseTo(
                Luv.from(new Xyz(0.0086492080771262, 0.00885644060847103, 0.00842659724738038)).usingD65(),
                new Luv(7.99999, 1, 1),
                PRECISE
        );
    }
}