package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.model.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;

class SrgbTest {
    @Test
    void white() {
        assertIsCloseTo(new Srgb(1, 1, 1).toXyz(), CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, PRECISE);
    }

    @Test
    void values() {
        assertThat(new Srgb(1, 2, 3).coordinates()).contains(1.0, 2.0, 3.0);
    }

    @Test
    void fromXYZ() {
        assertIsCloseTo(
                Srgb.from(XYZ_663399),
                LINEAR_SRGB_663399,
                PRECISE
        );
    }

    @Test
    void toXyz() {
        assertIsCloseTo(
                LINEAR_SRGB_663399.toXyz(),
                XYZ_663399,
                PRECISE
        );
    }

    @Test
    void toRec2020() {
        assertIsCloseTo(
                LINEAR_SRGB_663399.toRec2020(),
                REC2020_663399,
                PRECISE
        );
    }

    @Test
    void toXyzMatrix() {
        var sRgbToXyz = Srgb.TO_XYZ.values();

        // values of sRGB to XYZ matrix from http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
        assertIsCloseTo(sRgbToXyz[0], new double[]{0.4124564, 0.3575761, 0.1804375}, PRECISE);
        assertIsCloseTo(sRgbToXyz[1], new double[]{0.2126729, 0.7151522, 0.0721750}, PRECISE);
        assertIsCloseTo(sRgbToXyz[2], new double[]{0.0193339, 0.1191920, 0.9503041}, PRECISE);
    }

    @Test
    void fromXyzMatrix() {
        var sRgbFromXyz = Srgb.FROM_XYZ.values();
        // https://colour.readthedocs.io/en/v0.3.7/colour.models.dataset.rec_2020.html#colour.models.dataset.rec_2020.XYZ_TO_REC_2020_MATRIX
        assertIsCloseTo(sRgbFromXyz[0], new double[]{3.2404542, -1.5371385, -0.4985314}, PRECISE);
        assertIsCloseTo(sRgbFromXyz[1], new double[]{-0.9692660, 1.8760108, 0.0415560}, PRECISE);
        assertIsCloseTo(sRgbFromXyz[2], new double[]{0.0556434, -0.2040259, 1.0572252}, PRECISE);
    }
}