package colours;

import kofa.maths.SquareMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static colours.Converter.*;
import static kofa.NumericHelper.assertApproximatelyEqual;

class ConverterTest {
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
        assertApproximatelyEqual(backInRgb, originalRgb, 0.00001f);
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
        assertApproximatelyEqual(rec2020, expectedRec2020, 1f);
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
        assertApproximatelyEqual(rec2020, expectedRec2020, 0.001f);
    }

    @Test
    void rec2020_XYZ_linear_sRGB() {
        var linear_sRGB = new float[]{89, 115, 177};

        // when
        float[] xyz = converter.convert(linear_sRGB, LINEAR_SRGB_TO_XYZ);
        float[] rec2020 = converter.convert(xyz, XYZ_TO_REC2020);

        // then - same area average value picked in darktable with rec2020
        float[] expectedRec2020 = new float[] {101, 114, 170};
        // all integers, so need very rough comparison
        assertApproximatelyEqual(rec2020, expectedRec2020, 1f);

    }
}
