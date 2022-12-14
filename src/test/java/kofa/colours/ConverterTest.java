package kofa.colours;

import kofa.maths.SquareMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static kofa.NumericHelper.assertApproximatelyEqual;
import static kofa.colours.Converter.*;

class ConverterTest {
    private static final float PRECISE = 1E-5f;
    private static final float ROUGH_FOR_INT = 1f;
    private static final float LENIENT = 1E-3f;
    private final Converter converter = new Converter();

    static Stream<Arguments> conversionMatrices() {
        return Stream.of(
                Arguments.of(REC2020_TO_XYZ, XYZ_TO_REC2020),
                Arguments.of(LINEAR_SRGB_TO_XYZ, XYZ_TO_LINEAR_SRGB)
        );
    }

    @ParameterizedTest
    @MethodSource("conversionMatrices")
    void rgb_XYZ_roundtrip(SquareMatrix rgbToXyz, SquareMatrix xyzToRgb) {
        // given
        var originalRgb = new float[]{12, 34, 56};

        // when
        float[] xyz = converter.convert(originalRgb, rgbToXyz);
        float[] backInRgb = converter.convert(xyz, xyzToRgb);

        // then
        assertApproximatelyEqual(backInRgb, originalRgb, PRECISE);
    }

    @Test
    void linear_sRGB_XYZ_Rec2020() {
        // given - some random area average value picked in darktable with linear rec709/sRGB
        var linear_sRGB = new float[]{89, 115, 177};

        // when
        float[] xyz = converter.convert(linear_sRGB, LINEAR_SRGB_TO_XYZ);
        float[] rec2020 = converter.convert(xyz, XYZ_TO_REC2020);

        // then - same area average value picked in darktable with rec2020
        float[] expectedRec2020 = new float[] {101, 114, 170};
        // all integers, so need very rough comparison
        assertApproximatelyEqual(rec2020, expectedRec2020, ROUGH_FOR_INT);
    }

    @Test
    void linear_sRGB_XYZ_Rec2020_floats() {
        // given - some random area average value picked in darktable with linear rec709/sRGB
        var linear_sRGB = new float[]{0.089f, 0.115f, 0.177f};

        // when
        float[] xyz = converter.convert(linear_sRGB, LINEAR_SRGB_TO_XYZ);
        float[] rec2020 = converter.convert(xyz, XYZ_TO_REC2020);

        // then - same area average value picked in darktable with rec2020
        float[] expectedRec2020 = new float[] {0.101f, 0.114f, 0.170f};
        // were all integers, so need more lenient comparison
        assertApproximatelyEqual(rec2020, expectedRec2020, LENIENT);
    }

    @Test
    void rec2020_XYZ_linear_sRGB() {
        var linear_sRGB = new float[]{89, 115, 177};

        // when
        float[] xyz = converter.convert(linear_sRGB, LINEAR_SRGB_TO_XYZ);
        float[] rec2020 = converter.convert(xyz, XYZ_TO_REC2020);

        // then - same area average value picked in darktable with rec2020
        float[] expectedRec2020 = new float[]{101, 114, 170};
        // all integers, so need very rough comparison
        assertApproximatelyEqual(rec2020, expectedRec2020, ROUGH_FOR_INT);
    }

    @Test
    void convert_Luv_to_LCH_uv() {
        // values based on https://ajalt.github.io/colormath/converter/

        // given
        // RGB #663399 -> Luv(32.90281, 12.9804, -67.75974)
        var values_LUV = new Luv(32.90281f, 12.9804f, -67.75974f);

        // when
        LCh values_LCH_uv = converter.convert_Luv_to_LCH_uv(values_LUV);

        // then
        // 280.84448 degress -> 4.90166086204 radians + wrap-around
        var expected_LCH_Uv = new LCh(32.90281f, 68.99183f, (float) (4.90166086204f - 2 * Math.PI));

        assertApproximatelyEqual(values_LCH_uv, expected_LCH_Uv, PRECISE);
    }

    @Test
    void convert_LCH_uv_to_Luv() {
        // given
        // RGB #663399 -> LCh_uv(32.90281, 68.99183, -280.84448 degrees -> 4.90166086204 radians)
        var values_LCH_uv = new LCh(32.90281f, 68.99183f, 4.90166086204f);

        // when
        var values_Luv = converter.convert_LCH_uv_to_Luv(values_LCH_uv);

        // then
        var expected_Luv = new Luv(32.90281f, 12.9804f, -67.75974f);

        assertApproximatelyEqual(values_Luv, expected_Luv, PRECISE);
    }

    @Test
    void white_roundtrip() {
        float[] RGB = {1f, 1f, 1f};
        float[] XYZ_from_RGB_floats = converter.convert(RGB, LINEAR_SRGB_TO_XYZ);
        XYZ XYZ_from_RGB = new XYZ(XYZ_from_RGB_floats);

        Luv Luv_from_XYZ = converter.convert_XYZ_to_Luv(XYZ_from_RGB, D65_WHITE_XYZ);

        LCh LCH_from_Luv = converter.convert_Luv_to_LCH_uv(Luv_from_XYZ);

        Luv Luv_from_LCH = converter.convert_LCH_uv_to_Luv(LCH_from_Luv);
        assertApproximatelyEqual(Luv_from_LCH, Luv_from_XYZ, PRECISE);

        XYZ XYZ_from_Luv = converter.convert_Luv_to_XYZ(Luv_from_LCH, D65_WHITE_XYZ);
        assertApproximatelyEqual(XYZ_from_Luv, XYZ_from_RGB, PRECISE);

        float[] RGB_from_XYZ = converter.convert(XYZ_from_Luv.toFloats(), XYZ_TO_LINEAR_SRGB);
        assertApproximatelyEqual(RGB_from_XYZ, RGB, PRECISE);
    }

    @Test
    void white_roundtrip_D65() {
        float[] RGB = {1f, 1f, 1f};
        var XYZ_from_RGB = new XYZ(converter.convert(RGB, LINEAR_SRGB_TO_XYZ));

        var Luv_from_XYZ_D65 = converter.convert_XYZ_to_Luv_D65(XYZ_from_RGB);
        Luv Luv_from_XYZ = converter.convert_XYZ_to_Luv(XYZ_from_RGB, D65_WHITE_XYZ);
        assertApproximatelyEqual(Luv_from_XYZ_D65, Luv_from_XYZ, PRECISE);

        var LCH_from_Luv = converter.convert_Luv_to_LCH_uv(Luv_from_XYZ_D65);

        var Luv_from_LCH = converter.convert_LCH_uv_to_Luv(LCH_from_Luv);
        assertApproximatelyEqual(Luv_from_LCH, Luv_from_XYZ_D65, PRECISE);

        var XYZ_from_Luv_D65 = converter.convert_Luv_to_XYZ_D65(Luv_from_LCH);
        assertApproximatelyEqual(XYZ_from_Luv_D65, XYZ_from_RGB, PRECISE);
        var XYZ_from_Luv = converter.convert_Luv_to_XYZ(Luv_from_LCH, D65_WHITE_XYZ);
        assertApproximatelyEqual(XYZ_from_Luv_D65, XYZ_from_Luv, PRECISE);

        var RGB_from_XYZ = converter.convert(XYZ_from_Luv_D65.toFloats(), XYZ_TO_LINEAR_SRGB);
        assertApproximatelyEqual(RGB_from_XYZ, RGB, PRECISE);
    }
}
