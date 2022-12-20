package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ;
import static kofa.colours.model.Rgb.calculateToXyzMatrix;

class RgbTest {
    @Test
    void calculateXyzMatrix() {
        // values of sRGB primaries from http://www.brucelindbloom.com/index.html?WorkingSpaceInfo.html
        var sRgbToXyz = calculateToXyzMatrix(
                0.6400, 0.3300,
                0.3000, 0.6000,
                0.1500, 0.0600,
                D65_WHITE_XYZ
        );

        // values of sRGB to XYZ matrix from http://www.brucelindbloom.com/index.html?Eqn_RGB_XYZ_Matrix.html
        assertIsCloseTo(sRgbToXyz[0], new double[]{0.4124564, 0.3575761, 0.1804375}, PRECISE);
        assertIsCloseTo(sRgbToXyz[1], new double[]{0.2126729, 0.7151522, 0.0721750}, PRECISE);
        assertIsCloseTo(sRgbToXyz[2], new double[]{0.0193339, 0.1191920, 0.9503041}, PRECISE);
    }
}