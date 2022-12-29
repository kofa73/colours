package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ_IEC_61966_2_1;
import static kofa.colours.model.ConverterTest.CIE_LUV_663399;
import static kofa.colours.model.ConverterTest.XYZ_663399;
import static org.assertj.core.api.Assertions.assertThat;

class CieLuvTest {
    @Test
    void values() {
        assertThat(
                CIE_LUV_663399.coordinates()
        ).containsExactly(32.90281, 12.9804, -67.75974);
    }

    @Test
    void toLch() {
        // from https://ajalt.github.io/colormath/converter/
        // converting degrees to radians, then getting it in the range returned by atan2
        double hRadians = toRadians(280.84448);
        assertIsCloseTo(
                CIE_LUV_663399.toLch(),
                new CieLchUv(32.90281, 68.99183, hRadians),
                PRECISE
        );
    }

    @Test
    void toXyz() {
        assertIsCloseTo(
                CIE_LUV_663399.toXyz().usingD65_IEC_61966_2_1(),
                XYZ_663399,
                PRECISE
        );
    }

    @Test
    void toXyz_white() {
        Xyz whiteXyz = new CieLuv(100, 0, 0).toXyz().usingD65_IEC_61966_2_1();
        assertIsCloseTo(whiteXyz, D65_WHITE_XYZ_IEC_61966_2_1, PRECISE);
    }

    @Test
    void toXyz_black() {
        assertIsCloseTo(
                new CieLuv(0, 0, 0).toXyz().usingD65_IEC_61966_2_1(),
                new Xyz(0, 0, 0),
                PRECISE
        );
        assertThat(new CieLuv(0, 100, -100).toXyz().usingD65_IEC_61966_2_1().y()).isEqualTo(0);
    }

    @Test
    void toXyz_L_below_kappa() {
        assertIsCloseTo(
                new CieLuv(7.99999, 1, 1).toXyz().usingD65_IEC_61966_2_1(),
                new Xyz(0.00865, 0.00886, 0.00843),
                PRECISE
        );
    }

    @Test
    void fromXyz() {
        // other branch of Y conditional
        assertIsCloseTo(
                CieLuv.from(XYZ_663399).usingD65_IEC_61966_2_1(),
                CIE_LUV_663399,
                PRECISE
        );
    }

    @Test
    void fromXyz_white() {
        assertIsCloseTo(
                CieLuv.from(D65_WHITE_XYZ_IEC_61966_2_1).usingD65_IEC_61966_2_1(),
                new CieLuv(100, 0, 0),
                PRECISE
        );
    }


    @Test
    void fromXyz_black() {
        assertIsCloseTo(
                CieLuv.from(new Xyz(100, 0, -100)).usingD65_IEC_61966_2_1(),
                new CieLuv(0, 0, 0),
                PRECISE
        );
    }

    @Test
    void fromXyz_yr_below_epsilon() {
        assertIsCloseTo(
                CieLuv.from(new Xyz(0.0086492080771262, 0.00885644060847103, 0.00842659724738038)).usingD65_IEC_61966_2_1(),
                new CieLuv(7.99999, 1, 1),
                PRECISE
        );
    }
}