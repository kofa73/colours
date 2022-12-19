package kofa.colours;

import kofa.maths.Matrix3x3;
import kofa.maths.Vector3D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static java.lang.Math.pow;
import static kofa.NumericAssertions.*;
import static kofa.colours.Converter.D65_WHITE_XYZ;
import static kofa.colours.Converter.convert;
import static org.assertj.core.api.Assertions.assertThat;

class ConverterTest {
    // standard (RGB: #663399) XYZ for test from https://ajalt.github.io/colormath/converter/
    public static final XYZ XYZ_663399 = new XYZ(0.12412, 0.07493, 0.3093);
    public static final SRGB LINEAR_SRGB_663399 = new SRGB(0.13287, 0.0331, 0.31855);
    public static final Rec2020 REC2020_663399 = new Rec2020(rec2020InverseOETF(0.30459), rec2020InverseOETF(0.16817), rec2020InverseOETF(0.53086));
    public static final Lab LAB_663399 = new Lab(32.90281, 42.88651, -47.14914);
    public static final Luv Luv_663399 = new Luv(32.90281, 12.9804, -67.75974);

    public static final XYZ TINY_XYZ = new XYZ(0.5, 1E-4, 1E-5);

    // https://ajalt.github.io/colormath/converter/ does not provide linear Rec2020
    private static double rec2020InverseOETF(double encoded) {
        return encoded < 0.0812428313 ?
                encoded / 4.5 :
                pow((encoded + 0.09929682680944) / 1.09929682680944, 1 / 0.45);
    }

    static Stream<Arguments> conversionMatrices() {
        return Stream.of(
                Arguments.of((Vector3D.ConstructorFromValues<Rec2020>) (double r, double g, double b) -> new Rec2020(r, g, b), Rec2020.TO_XYZ, Rec2020.FROM_XYZ),
                Arguments.of((Vector3D.ConstructorFromValues<SRGB>) ((double r, double g, double b) -> new SRGB(r, g, b)), SRGB.TO_XYZ, SRGB.FROM_XYZ)
        );
    }

    @ParameterizedTest
    @MethodSource("conversionMatrices")
    <S extends RGB<S>> void rgb_XYZ_roundtrip(
            Vector3D.ConstructorFromValues<S> constructor,
            Matrix3x3<S, XYZ> rgbToXyz, Matrix3x3<XYZ, S> xyzToRgb
    ) {
        // given
        var originalRgb = constructor.createNew(12, 34, 56);

        // when
        var xyz_fromConverter = convert(originalRgb, rgbToXyz);
        var xyz_fromRGB = originalRgb.toXYZ();
        var backInRgb_fromConverter = convert(xyz_fromConverter, xyzToRgb);

        // then
        assertIsCloseTo(backInRgb_fromConverter, originalRgb, PRECISE);
        assertThat(xyz_fromConverter).isEqualTo(xyz_fromRGB);
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
        // all integers, so need a lenient comparison
        assertIsCloseTo(rec2020, expectedRec2020, ROUGH, LENIENT, PRECISE);
    }

    @Test
    void linear_sRGB_XYZ_Rec2020_doubles() {
        // given - some random area average value picked in darktable with linear rec709/sRGB
        var linear_sRGB = new SRGB(0.089, 0.115, 0.177);

        // when
        var xyz = convert(linear_sRGB, SRGB.TO_XYZ);
        var rec2020 = convert(xyz, Rec2020.FROM_XYZ);

        // then - same area average value picked in darktable with rec2020
        var expectedRec2020 = new Rec2020(0.101, 0.114, 0.170);
        // were read from a UI, so need more lenient comparison
        assertIsCloseTo(rec2020, expectedRec2020, ROUGH, LENIENT, PRECISE);
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
        assertIsCloseTo(sRGB, expectedSRGB, ROUGH);
    }

    @Test
    void convert_Luv_to_LCH_uv() {
        // values based on https://ajalt.github.io/colormath/converter/

        // given
        // RGB #663399 -> Luv(32.90281, 12.9804, -67.75974)
        var values_LUV = new Luv(32.90281, 12.9804, -67.75974);

        // when
        LCh_uv values_LCH_uv = Converter.convert_Luv_to_LCH_uv(values_LUV);

        // then
        // 280.84448 degress -> 4.90166086204 radians + wrap-around
        var expected_LCH_Uv = new LCh_uv(32.90281, 68.99183, (4.90166086204 - 2 * Math.PI));

        assertIsCloseTo(values_LCH_uv, expected_LCH_Uv, PRECISE);
    }

    @Test
    void convert_LCH_uv_to_Luv() {
        // given
        // RGB #663399 -> LCh_uv(32.90281, 68.99183, -280.84448 degrees -> 4.90166086204 radians)
        var values_LCH_uv = new LCh_uv(32.90281, 68.99183, 4.90166086204);

        // when
        var values_Luv = Converter.convert_LCH_uv_to_Luv(values_LCH_uv);

        // then
        var expected_Luv = new Luv(32.90281, 12.9804, -67.75974);

        assertIsCloseTo(values_Luv, expected_Luv, PRECISE);
    }

    @Test
    void white_roundtrip() {
        var original_sRGB = new SRGB(1, 1, 1);
        var XYZ_from_RGB = convert(original_sRGB, SRGB.TO_XYZ);

        Luv Luv_from_XYZ = Luv.from(XYZ_from_RGB).usingWhitePoint(D65_WHITE_XYZ);

        LCh_uv LCH_from_Luv = Luv_from_XYZ.toLCh();

        Luv Luv_from_LCH = LCH_from_Luv.toLuv();
        assertIsCloseTo(Luv_from_LCH, Luv_from_XYZ, PRECISE);

        XYZ XYZ_from_Luv = Converter.convert_Luv_to_XYZ(Luv_from_LCH, D65_WHITE_XYZ);
        assertIsCloseTo(XYZ_from_Luv, XYZ_from_RGB, PRECISE);

        var sRGB_from_XYZ = convert(XYZ_from_Luv, SRGB.FROM_XYZ);
        assertIsCloseTo(sRGB_from_XYZ, original_sRGB, PRECISE);
    }

    @Test
    void white_roundtrip_D65() {
        var original_sRGB = new SRGB(1, 1, 1);
        var XYZ_from_SRGB = convert(original_sRGB, SRGB.TO_XYZ);

        var Luv_from_XYZ_D65 = Luv.from(XYZ_from_SRGB).usingD65();
        Luv Luv_from_XYZ = Luv.from(XYZ_from_SRGB).usingWhitePoint(D65_WHITE_XYZ);
        assertIsCloseTo(Luv_from_XYZ_D65, Luv_from_XYZ, PRECISE);

        var LCH_from_Luv = Luv_from_XYZ_D65.toLCh();

        var Luv_from_LCH = LCH_from_Luv.toLuv();
        assertIsCloseTo(Luv_from_LCH, Luv_from_XYZ_D65, PRECISE);

        var XYZ_from_Luv_D65 = Converter.convert_Luv_to_XYZ_D65(Luv_from_LCH);
        assertIsCloseTo(XYZ_from_Luv_D65, XYZ_from_SRGB, PRECISE);
        var XYZ_from_Luv = Converter.convert_Luv_to_XYZ(Luv_from_LCH, D65_WHITE_XYZ);
        assertIsCloseTo(XYZ_from_Luv_D65, XYZ_from_Luv, PRECISE);

        var sRGB_from_XYZ = convert(XYZ_from_Luv_D65, SRGB.FROM_XYZ);
        assertIsCloseTo(sRGB_from_XYZ, original_sRGB, PRECISE);
    }
}
