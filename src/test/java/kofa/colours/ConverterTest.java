package kofa.colours;

import kofa.maths.Matrix3;
import kofa.maths.Vector3D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static kofa.NumericHelper.assertApproximatelyEqual;
import static kofa.colours.Converter.D65_WHITE_XYZ;
import static kofa.colours.Converter.convert;

class ConverterTest {
    private static final float PRECISE = 1E-5f;
    private static final float ROUGH_FOR_INT = 1f;
    private static final float LENIENT = 1E-3f;

    static Stream<Arguments> conversionMatrices() {
        return Stream.of(
                Arguments.of((Vector3D.ConstructorFromValues<Rec2020>) (float r, float g, float b) -> new Rec2020(r, g, b), Rec2020.TO_XYZ, Rec2020.FROM_XYZ),
                Arguments.of((Vector3D.ConstructorFromValues<SRGB>) ((float r, float g, float b) -> new SRGB(r, g, b)), SRGB.TO_XYZ, SRGB.FROM_XYZ)
        );
    }

    @ParameterizedTest
    @MethodSource("conversionMatrices")
    <S extends RGB<S>> void rgb_XYZ_roundtrip(
            Vector3D.ConstructorFromValues<S> constructor,
            Matrix3<S, XYZ> rgbToXyz, Matrix3<XYZ, S> xyzToRgb
    ) {
        // given
        var originalRgb = constructor.createNew(12, 34, 56);

        // when
        var xyz_fromConverter = convert(originalRgb, rgbToXyz);
        var xyz_fromRGB = originalRgb.toXYZ();
        var backInRgb_fromConverter = convert(xyz_fromConverter, xyzToRgb);

        // then
        assertApproximatelyEqual(backInRgb_fromConverter, originalRgb, PRECISE);
        assertApproximatelyEqual(xyz_fromRGB, xyz_fromConverter, 0);
    }

    @Test
    void linear_sRGB_XYZ_Rec2020() {
        // given - some random area average value picked in darktable with linear rec709/sRGB
        var linear_sRGB = new SRGB(89, 115, 177);

        // when
        var xyz = convert(linear_sRGB, SRGB.TO_XYZ);
        var rec2020 = convert(xyz, Rec2020.FROM_XYZ);

        // then - same area average value picked in darktable with rec2020
        var expectedRec2020 = new Rec2020(101, 114, 170);
        // all integers, so need very rough comparison
        assertApproximatelyEqual(rec2020, expectedRec2020, ROUGH_FOR_INT);
    }

    @Test
    void linear_sRGB_XYZ_Rec2020_floats() {
        // given - some random area average value picked in darktable with linear rec709/sRGB
        var linear_sRGB = new SRGB(0.089f, 0.115f, 0.177f);

        // when
        var xyz = convert(linear_sRGB, SRGB.TO_XYZ);
        var rec2020 = convert(xyz, Rec2020.FROM_XYZ);

        // then - same area average value picked in darktable with rec2020
        var expectedRec2020 = new Rec2020(0.101f, 0.114f, 0.170f);
        // were all integers, so need more lenient comparison
        assertApproximatelyEqual(rec2020, expectedRec2020, LENIENT);
    }

    @Test
    void rec2020_XYZ_linear_sRGB() {
        var original_Rec2020 = new Rec2020(101, 114, 170);

        // when
        var xyz = convert(original_Rec2020, Rec2020.TO_XYZ);
        var sRGB = convert(xyz, SRGB.FROM_XYZ);

        // then - same area average value picked in darktable with rec2020
        var expectedSRGB = new SRGB(89, 115, 177);

        // all integers, so need very rough comparison
        assertApproximatelyEqual(sRGB, expectedSRGB, ROUGH_FOR_INT);
    }

    @Test
    void convert_Luv_to_LCH_uv() {
        // values based on https://ajalt.github.io/colormath/converter/

        // given
        // RGB #663399 -> Luv(32.90281, 12.9804, -67.75974)
        var values_LUV = new Luv(32.90281f, 12.9804f, -67.75974f);

        // when
        LCh values_LCH_uv = Converter.convert_Luv_to_LCH_uv(values_LUV);

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
        var values_Luv = Converter.convert_LCH_uv_to_Luv(values_LCH_uv);

        // then
        var expected_Luv = new Luv(32.90281f, 12.9804f, -67.75974f);

        assertApproximatelyEqual(values_Luv, expected_Luv, PRECISE);
    }

    @Test
    void white_roundtrip() {
        var original_sRGB = new SRGB(1f, 1f, 1f);
        var XYZ_from_RGB = convert(original_sRGB, SRGB.TO_XYZ);

        Luv Luv_from_XYZ = Converter.convert_XYZ_to_Luv(XYZ_from_RGB, D65_WHITE_XYZ);

        LCh LCH_from_Luv = Converter.convert_Luv_to_LCH_uv(Luv_from_XYZ);

        Luv Luv_from_LCH = Converter.convert_LCH_uv_to_Luv(LCH_from_Luv);
        assertApproximatelyEqual(Luv_from_LCH, Luv_from_XYZ, PRECISE);

        XYZ XYZ_from_Luv = Converter.convert_Luv_to_XYZ(Luv_from_LCH, D65_WHITE_XYZ);
        assertApproximatelyEqual(XYZ_from_Luv, XYZ_from_RGB, PRECISE);

        var sRGB_from_XYZ = convert(XYZ_from_Luv, SRGB.FROM_XYZ);
        assertApproximatelyEqual(sRGB_from_XYZ, original_sRGB, PRECISE);
    }

    @Test
    void white_roundtrip_D65() {
        var original_sRGB = new SRGB(1f, 1f, 1f);
        var XYZ_from_SRGB = convert(original_sRGB, SRGB.TO_XYZ);

        var Luv_from_XYZ_D65 = Converter.convert_XYZ_to_Luv_D65(XYZ_from_SRGB);
        Luv Luv_from_XYZ = Converter.convert_XYZ_to_Luv(XYZ_from_SRGB, D65_WHITE_XYZ);
        assertApproximatelyEqual(Luv_from_XYZ_D65, Luv_from_XYZ, PRECISE);

        var LCH_from_Luv = Converter.convert_Luv_to_LCH_uv(Luv_from_XYZ_D65);

        var Luv_from_LCH = Converter.convert_LCH_uv_to_Luv(LCH_from_Luv);
        assertApproximatelyEqual(Luv_from_LCH, Luv_from_XYZ_D65, PRECISE);

        var XYZ_from_Luv_D65 = Converter.convert_Luv_to_XYZ_D65(Luv_from_LCH);
        assertApproximatelyEqual(XYZ_from_Luv_D65, XYZ_from_SRGB, PRECISE);
        var XYZ_from_Luv = Converter.convert_Luv_to_XYZ(Luv_from_LCH, D65_WHITE_XYZ);
        assertApproximatelyEqual(XYZ_from_Luv_D65, XYZ_from_Luv, PRECISE);

        var sRGB_from_XYZ = convert(XYZ_from_Luv_D65, SRGB.FROM_XYZ);
        assertApproximatelyEqual(sRGB_from_XYZ, original_sRGB, PRECISE);
    }
}
