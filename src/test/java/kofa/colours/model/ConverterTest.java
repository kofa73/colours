package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.EXACT;
import static kofa.NumericAssertions.ROUGH;
import static kofa.Vector3Assert.assertThat;
import static kofa.colours.model.Rec2020.applyInverseOetf;

class ConverterTest {
    // standard (RGB: #663399) XYZ for test from https://ajalt.github.io/colormath/converter/
    public static final CIEXYZ XYZ_663399 = new CIEXYZ(0.12412, 0.07493, 0.3093);
    public static final Srgb LINEAR_SRGB_663399 = new Srgb(0.13287, 0.0331, 0.31855);
    // https://ajalt.github.io/colormath/converter/ does not provide linear Rec2020
    public static final Rec2020 REC2020_663399 = new Rec2020(
            applyInverseOetf(0.30459), applyInverseOetf(0.16817), applyInverseOetf(0.53086)
    );
    public static final CIELAB LAB_663399 = new CIELAB(32.90281, 42.88651, -47.14914);
    public static final CIELUV CIE_LUV_663399 = new CIELUV(32.90281, 12.9804, -67.75974);

    public static final CIEXYZ TINY_XYZ = new CIEXYZ(0.5, 1E-4, 1E-5);

    @Test
    void rec2020_Xyz_sRgb() {
        var originalRec2020 = new Rec2020(101 / 255.0, 114 / 255.0, 170 / 255.0);

        // when
        var xyz = originalRec2020.toXyz();
        var sRGB = Srgb.from(xyz);

        // then - same area average value picked in darktable with rec2020
        var expectedSRGB = new Srgb(89 / 255.0, 115 / 255.0, 177 / 255.0);

        // all integers, so need very rough comparison
        assertThat(sRGB).isCloseTo(expectedSRGB, ROUGH);
    }

    @Test
    void convert_Luv_to_LCH_uv() {
        // values based on https://ajalt.github.io/colormath/converter/

        // given
        // RGB #663399 -> Luv(32.90281, 12.9804, -67.75974)
        var luv = new CIELUV(32.90281, 12.9804, -67.75974);

        // when
        var lchUv = luv.toLch();

        // then
        // 280.84448 degrees -> 4.90166086204 radians + wrap-around
        var expectedLchUv = new CIELCh_uv(32.90281, 68.99183, toRadians(280.84448));

        assertThat(lchUv).isCloseTo(expectedLchUv, EXACT);
    }



    @Test
    void white_roundtrip() {
        var original_sRGB = new Srgb(1, 1, 1);
        var XYZ_from_RGB = original_sRGB.toXyz();

        CIELUV luvFromXyz = CIELUV.from(XYZ_from_RGB).usingWhitePoint(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);

        CIELCh_uv LCH_from_Luv = luvFromXyz.toLch();

        CIELUV Luv_from_LCH = LCH_from_Luv.toLuv();
        assertThat(Luv_from_LCH).isCloseTo(luvFromXyz, EXACT);

        CIEXYZ Xyz_from_Luv = Luv_from_LCH.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER();
        assertThat(Xyz_from_Luv).isCloseTo(XYZ_from_RGB, EXACT);

        var sRGB_from_XYZ = Srgb.from(Xyz_from_Luv);
        assertThat(sRGB_from_XYZ).isCloseTo(original_sRGB, EXACT);
    }

    @Test
    void white_roundtrip_D65() {
        var originalSrgb = new Srgb(1, 1, 1);
        var xyzFromSrgb = originalSrgb.toXyz();

        var luvFromXyzD65 = CIELUV.from(xyzFromSrgb).usingD65_2DEGREE_STANDARD_OBSERVER();
        CIELUV luvFromXyz = CIELUV.from(xyzFromSrgb).usingWhitePoint(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
        assertThat(luvFromXyzD65).isCloseTo(luvFromXyz, EXACT);

        var lchFromLuv = luvFromXyzD65.toLch();

        var luvFromLch = lchFromLuv.toLuv();
        assertThat(luvFromLch).isCloseTo(luvFromXyzD65, EXACT);

        var xyzFromLuvD65 = luvFromLch.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER();
        assertThat(xyzFromLuvD65).isCloseTo(xyzFromSrgb, EXACT);
        var xyzFromLuv = luvFromLch.toXyz().usingWhitePoint(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
        assertThat(xyzFromLuvD65).isCloseTo(xyzFromLuv, EXACT);

        var sRgbFromXyz = Srgb.from(xyzFromLuvD65);
        assertThat(sRgbFromXyz).isCloseTo(originalSrgb, EXACT);
    }
}
