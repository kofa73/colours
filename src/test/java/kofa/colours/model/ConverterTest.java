package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.*;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ;
import static kofa.colours.model.Rec2020.rec2020InverseOETF;

class ConverterTest {
    // standard (RGB: #663399) XYZ for test from https://ajalt.github.io/colormath/converter/
    public static final Xyz XYZ_663399 = new Xyz(0.12412, 0.07493, 0.3093);
    public static final Srgb LINEAR_SRGB_663399 = new Srgb(0.13287, 0.0331, 0.31855);
    // https://ajalt.github.io/colormath/converter/ does not provide linear Rec2020
    public static final Rec2020 REC2020_663399 = new Rec2020(
            rec2020InverseOETF(0.30459), rec2020InverseOETF(0.16817), rec2020InverseOETF(0.53086)
    );
    public static final Lab LAB_663399 = new Lab(32.90281, 42.88651, -47.14914);
    public static final Luv LUV_663399 = new Luv(32.90281, 12.9804, -67.75974);

    public static final Xyz TINY_XYZ = new Xyz(0.5, 1E-4, 1E-5);

    @Test
    void sRgb_Xyz_Rec2020() {
        // given - some random area average value picked in darktable with linear rec709/sRGB
        var sRgb = new Srgb(89 / 255.0, 115 / 255.0, 177 / 255.0);

        // when
        var xyz = sRgb.toXyz();
        var rec2020 = Rec2020.from(xyz);

        // then - same area average value picked in darktable with rec2020
        var expectedRec2020 = new Rec2020(101 / 255.0, 114 / 255.0, 170 / 255.0);
        // all integers, so need a lenient comparison
        assertIsCloseTo(rec2020, expectedRec2020, ROUGH, LENIENT, PRECISE);
    }

    @Test
    void sRgb_Xyz_Rec2020_doubles() {
        // given - some random area average value picked in darktable with linear rec709/sRGB
        var sRgb = new Srgb(0.089, 0.115, 0.177);

        // when
        var xyz = sRgb.toXyz();
        var rec2020 = Rec2020.from(xyz);

        // then - same area average value picked in darktable with rec2020
        var expectedRec2020 = new Rec2020(0.101, 0.114, 0.170);
        // were read from a UI, so need more lenient comparison
        assertIsCloseTo(rec2020, expectedRec2020, ROUGH, LENIENT, PRECISE);
    }

    @Test
    void rec2020_Xyz_sRgb() {
        var originalRec2020 = new Rec2020(101 / 255.0, 114 / 255.0, 170 / 255.0);

        // when
        var xyz = originalRec2020.toXyz();
        var sRGB = Srgb.from(xyz);

        // then - same area average value picked in darktable with rec2020
        var expectedSRGB = new Srgb(89 / 255.0, 115 / 255.0, 177 / 255.0);

        // all integers, so need very rough comparison
        assertIsCloseTo(sRGB, expectedSRGB, ROUGH);
    }

    @Test
    void convert_Luv_to_LCH_uv() {
        // values based on https://ajalt.github.io/colormath/converter/

        // given
        // RGB #663399 -> Luv(32.90281, 12.9804, -67.75974)
        var luv = new Luv(32.90281, 12.9804, -67.75974);

        // when
        var lchUv = luv.toLch();

        // then
        // 280.84448 degrees -> 4.90166086204 radians + wrap-around
        var expectedLchUv = new LchUv(32.90281, 68.99183, toRadians(280.84448));

        assertIsCloseTo(lchUv, expectedLchUv, PRECISE);
    }

    @Test
    void convert_LCH_uv_to_Luv() {
        // given
        // RGB #663399 -> LCh_uv(32.90281, 68.99183, -280.84448 degrees -> 4.90166086204 radians)
        var lchUv = new LchUv(32.90281, 68.99183, 4.90166086204);

        // when
        var luv = lchUv.toLuv();

        // then
        var expectedLuv = new Luv(32.90281, 12.9804, -67.75974);

        assertIsCloseTo(luv, expectedLuv, PRECISE);
    }

    @Test
    void white_roundtrip() {
        var original_sRGB = new Srgb(1, 1, 1);
        var XYZ_from_RGB = original_sRGB.toXyz();

        Luv luvFromXyz = Luv.from(XYZ_from_RGB).usingWhitePoint(D65_WHITE_XYZ);

        LchUv LCH_from_Luv = luvFromXyz.toLch();

        Luv Luv_from_LCH = LCH_from_Luv.toLuv();
        assertIsCloseTo(Luv_from_LCH, luvFromXyz, PRECISE);

        Xyz Xyz_from_Luv = Luv_from_LCH.toXyz().usingD65();
        assertIsCloseTo(Xyz_from_Luv, XYZ_from_RGB, PRECISE);

        var sRGB_from_XYZ = Srgb.from(Xyz_from_Luv);
        assertIsCloseTo(sRGB_from_XYZ, original_sRGB, PRECISE);
    }

    @Test
    void white_roundtrip_D65() {
        var originalSrgb = new Srgb(1, 1, 1);
        var xyzFromSrgb = originalSrgb.toXyz();

        var luvFromXyzD65 = Luv.from(xyzFromSrgb).usingD65();
        Luv luvFromXyz = Luv.from(xyzFromSrgb).usingWhitePoint(D65_WHITE_XYZ);
        assertIsCloseTo(luvFromXyzD65, luvFromXyz, PRECISE);

        var lchFromLuv = luvFromXyzD65.toLch();

        var luvFromLch = lchFromLuv.toLuv();
        assertIsCloseTo(luvFromLch, luvFromXyzD65, PRECISE);

        var xyzFromLuvD65 = luvFromLch.toXyz().usingD65();
        assertIsCloseTo(xyzFromLuvD65, xyzFromSrgb, PRECISE);
        var xyzFromLuv = luvFromLch.toXyz().usingWhitePoint(D65_WHITE_XYZ);
        assertIsCloseTo(xyzFromLuvD65, xyzFromLuv, PRECISE);

        var srgbFromXyz = Srgb.from(xyzFromLuvD65);
        assertIsCloseTo(srgbFromXyz, originalSrgb, PRECISE);
    }
}