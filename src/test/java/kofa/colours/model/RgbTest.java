package kofa.colours.model;

import kofa.DoubleArrayAssert;
import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.colours.model.Rgb.calculateToXyzMatrix;
import static org.assertj.core.api.Assertions.assertThat;

class RgbTest {
    @Test
    void calculateXyzMatrix() {
        // values of sRGB primaries from http://www.brucelindbloom.com/index.html?WorkingSpaceInfo.html
        var sRgbToXyz = calculateToXyzMatrix(
                0.6400, 0.3300,
                0.3000, 0.6000,
                0.1500, 0.0600,
                CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER
        );

        // values of sRGB to XYZ matrix from http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
        DoubleArrayAssert.assertThat(sRgbToXyz[0]).isCloseTo(new double[]{0.4124564, 0.3575761, 0.1804375}, PRECISE);
        DoubleArrayAssert.assertThat(sRgbToXyz[1]).isCloseTo(new double[]{0.2126729, 0.7151522, 0.0721750}, PRECISE);
        DoubleArrayAssert.assertThat(sRgbToXyz[2]).isCloseTo(new double[]{0.0193339, 0.1191920, 0.9503041}, PRECISE);
    }

    @Test
    void isOutOfGamut_atEdge() {
        assertThat(new Srgb(0, 0, 0).isOutOfGamut()).isFalse();
        assertThat(new Srgb(1, 1, 1).isOutOfGamut()).isFalse();
    }

    @Test
    void isOutOfGamut_overTheEdge() {
        assertThat(new Srgb(-1E-30, 0.5, 0.5).isOutOfGamut()).isTrue();
        assertThat(new Srgb(0.5, -1E-30, 0.5).isOutOfGamut()).isTrue();
        assertThat(new Srgb(0.5, 0.5, -1E-30).isOutOfGamut()).isTrue();
        assertThat(new Srgb(1 + 1E-15, 0.5, 0.5).isOutOfGamut()).isTrue();
        assertThat(new Srgb(0.5, 1 + 1E-15, 0.5).isOutOfGamut()).isTrue();
        assertThat(new Srgb(0.5, 0.5, 1 + 1E-15).isOutOfGamut()).isTrue();
    }
}