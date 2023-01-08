package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.*;
import static kofa.colours.model.ConverterTest.CIE_LUV_663399;
import static kofa.colours.model.ConverterTest.XYZ_663399;
import static org.assertj.core.api.Assertions.assertThat;

class CIELUVTest {
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
                new CIELCh_uv(32.90281, 68.99183, hRadians),
                EXACT
        );
    }

    @Test
    void toXyz() {
        assertIsCloseTo(
                CIE_LUV_663399.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER(),
                XYZ_663399,
                PRECISE
        );
    }

    @Test
    void toXyz_white() {
        CIEXYZ whiteXyz = new CIELUV(100, 0, 0).toXyz().usingD65_2DEGREE_STANDARD_OBSERVER();
        assertIsCloseTo(whiteXyz, CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, EXACT);
    }

    @Test
    void toXyz_black() {
        assertIsCloseTo(
                new CIELUV(0, 0, 0).toXyz().usingD65_2DEGREE_STANDARD_OBSERVER(),
                new CIEXYZ(0, 0, 0),
                EXACT
        );
        assertThat(new CIELUV(0, 100, -100).toXyz().usingD65_2DEGREE_STANDARD_OBSERVER().Y()).isEqualTo(0);
    }

    @Test
    void toXyz_L_below_kappa() {
        assertIsCloseTo(
                new CIELUV(7.99999, 1, 1).toXyz().usingD65_2DEGREE_STANDARD_OBSERVER(),
                new CIEXYZ(0.00864896, 0.00885644, 0.00842526),
                PRECISE
        );
    }

    @Test
    void fromXyz() {
        // other branch of Y conditional
        assertIsCloseTo(
                CIELUV.from(XYZ_663399).usingD65_2DEGREE_STANDARD_OBSERVER(),
                CIE_LUV_663399,
                PRECISE
        );
    }

    @Test
    void fromXyz_white() {
        assertIsCloseTo(
                CIELUV.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DEGREE_STANDARD_OBSERVER(),
                new CIELUV(100, 0, 0),
                EXACT
        );
    }


    @Test
    void fromXyz_black() {
        assertIsCloseTo(
                CIELUV.from(new CIEXYZ(100, 0, -100)).usingD65_2DEGREE_STANDARD_OBSERVER(),
                new CIELUV(0, 0, 0),
                EXACT
        );
    }

    @Test
    void fromXyz_yr_below_epsilon() {
        assertIsCloseTo(
                CIELUV.from(new CIEXYZ(0.00864896, 0.00885644, 0.00842526)).usingD65_2DEGREE_STANDARD_OBSERVER(),
                new CIELUV(7.99999, 1, 1),
                PRECISE
        );
    }

    @Test
    void convert_LCH_uv_to_Luv() {
        // given
        // RGB #663399 -> LCh_uv(32.90281, 68.99183, -280.84448 degrees -> 4.90166086204 radians)
        var lchUv = new CIELCh_uv(32.90281, 68.99183, 4.90166086204);

        // when
        var luv = lchUv.toLuv();

        // then
        var expectedLuv = new CIELUV(32.90281, 12.9804, -67.75974);

        assertIsCloseTo(luv, expectedLuv, PRECISE);
    }
}
