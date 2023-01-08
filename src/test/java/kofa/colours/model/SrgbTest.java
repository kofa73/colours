package kofa.colours.model;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static kofa.NumericAssertions.*;
import static kofa.Vector3Assert.assertThat;
import static kofa.colours.model.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
@ExtendWith(SoftAssertionsExtension.class)
class SrgbTest {
    @InjectSoftAssertions
    private SoftAssertions softly;

    @Test
    void white() {
        assertThat(new Srgb(1, 1, 1).toXyz()).isCloseTo(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, EXACT);
    }

    @Test
    void values() {
        assertThat(new Srgb(1, 2, 3).coordinates()).contains(1.0, 2.0, 3.0);
    }

    @Test
    void fromXYZ() {
        assertThat(Srgb.from(XYZ_663399)).isCloseTo(LINEAR_SRGB_663399, PRECISE);
    }

    @Test
    void toXyz() {
        assertThat(LINEAR_SRGB_663399.toXyz()).isCloseTo(XYZ_663399, PRECISE);
    }

    @Test
    void toRec2020() {
        assertThat(LINEAR_SRGB_663399.toRec2020()).isCloseTo(REC2020_663399, PRECISE);
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
        assertThat(rec2020).isCloseTo(expectedRec2020, ROUGH, LENIENT, PRECISE);
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
        assertThat(rec2020).isCloseTo(expectedRec2020, ROUGH, LENIENT, PRECISE);
    }

    // not a real test, just documentation
    @ParameterizedTest
    @MethodSource("referenceWhitesAndMatrices")
    void sRGB_to_XYZ(CIEXYZ referenceWhite, double[][] matrix) {
        double[][] calculatedMatrix = Rgb.calculateToXyzMatrix(
                0.6400, 0.3300,
                0.3000, 0.6000,
                0.1500, 0.0600,
                referenceWhite
        );
        softly.assertThat(calculatedMatrix[0]).containsExactly(matrix[0]);
        softly.assertThat(calculatedMatrix[1]).containsExactly(matrix[1]);
        softly.assertThat(calculatedMatrix[2]).containsExactly(matrix[2]);
    }

    private static Stream<Arguments> referenceWhitesAndMatrices() {
        return Stream.of(
                arguments(named("D65_WHITE_ASTM_E308_01", CIEXYZ.D65_WHITE_ASTM_E308_01), new double[][]{
                        {0.41245643908969265, 0.35757607764390886, 0.18043748326639886},
                        {0.21267285140562275, 0.7151521552878177, 0.07217499330655955},
                        {0.01933389558232932, 0.11919202588130293, 0.9503040785363674}
                }),
                arguments(named("D65_WHITE_IEC_61966_2_1", CIEXYZ.D65_WHITE_IEC_61966_2_1), new double[][]{
                        {0.41239075298527483, 0.3575843494912984, 0.18048079752342697},
                        {0.21263898200803233, 0.7151686989825968, 0.07219231900937079},
                        {0.019330816546184737, 0.11919478316376611, 0.9505322002900488}
                }),
                arguments(named("D65_WHITE_2DEGREE_STANDARD_OBSERVER", CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER), new double[][]{
                        {0.41238656325299233, 0.35759149092062525, 0.1804504912035636},
                        {0.21263682167732417, 0.7151829818412505, 0.07218019648142544},
                        {0.019330620152483994, 0.1191971636402084, 0.950372587005435}
                }),
                arguments(named("D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER", CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER), new double[][]{
                        {0.41252882628196613, 0.35816417735392725, 0.17740367310126703},
                        {0.21271017605163878, 0.7163283547078545, 0.07096146924050681},
                        {0.01933728873196714, 0.11938805911797572, 0.9343260116666731}
                })
        );
    }
}
