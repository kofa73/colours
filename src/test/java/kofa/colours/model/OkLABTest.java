package kofa.colours.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static kofa.NumericAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class OkLABTest {
    @Test
    void white_labToXyz() {
        assertIsCloseTo(new OkLAB(1, 0, 0).toXyz().usingOriginalMatrix(), CIEXYZ.D65_WHITE_ASTM_E308_01, PRECISE);
    }

    @Test
    void white_xyzToLab() {
        assertIsCloseTo(OkLAB.from(CIEXYZ.D65_WHITE_ASTM_E308_01).usingD65_ASTM_E308_01(), new OkLAB(1, 0, 0), PRECISE, 1E-4);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void xyzToLabToXyz(CIEXYZ xyz, OkLAB ignored) {
        assertIsCloseTo(OkLAB.from(xyz).usingOriginalMatrix().toXyz().usingOriginalMatrix(), xyz, EXACT, 1E-7);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void labToXyzToLab(CIEXYZ ignored, OkLAB lab) {
        assertIsCloseTo(OkLAB.from(lab.toXyz().usingOriginalMatrix()).usingOriginalMatrix(), lab, EXACT, 1E-7);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void fromXyz_rounded(CIEXYZ xyz, OkLAB expectedOkLab) {
        assertIsCloseTo(roundToThreeDecimals(OkLAB.from(xyz).usingOriginalMatrix()), expectedOkLab, EXACT);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void toXyz_rounded(CIEXYZ expectedXyz, OkLAB okLab) {
        assertIsCloseTo(okLab.toXyz().usingOriginalMatrix(), expectedXyz, LENIENT, 0.005);
        assertIsCloseTo(roundToThreeDecimals(okLab.toXyz().usingOriginalMatrix()), expectedXyz, ROUGH, 0.005);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void fromXyz(CIEXYZ xyz, OkLAB expectedOkLab) {
        assertIsCloseTo(OkLAB.from(xyz).usingOriginalMatrix(), expectedOkLab, ROUGH, 3E-4);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void toXyz(CIEXYZ expectedXyz, OkLAB okLab) {
        assertIsCloseTo(okLab.toXyz().usingOriginalMatrix(), expectedXyz, LENIENT, 1E-3);
    }

    // https://bottosson.github.io/posts/oklab/#table-of-example-xyz-and-oklab-pairs
    private static Stream<Arguments> xyzAndLab() {
        return Stream.of(
                arguments(new CIEXYZ(0.950, 1.000, 1.089), new OkLAB(1.000, 0.000, 0.000)),
                arguments(new CIEXYZ(1.000, 0.000, 0.000), new OkLAB(0.450, 1.236, -0.019)),
                arguments(new CIEXYZ(0.000, 1.000, 0.000), new OkLAB(0.922, -0.671, 0.263)),
                arguments(new CIEXYZ(0.000, 0.000, 1.000), new OkLAB(0.153, -1.415, -0.449))
        );
    }

    private OkLAB roundToThreeDecimals(OkLAB okLab) {
        return new OkLAB(
                roundToThreeDecimals(okLab.L()),
                roundToThreeDecimals(okLab.a()),
                roundToThreeDecimals(okLab.b())
        );
    }

    private CIEXYZ roundToThreeDecimals(CIEXYZ xyz) {
        return new CIEXYZ(
                roundToThreeDecimals(xyz.X()),
                roundToThreeDecimals(xyz.Y()),
                roundToThreeDecimals(xyz.Z())
        );
    }

    private double roundToThreeDecimals(double d) {
        return Math.round(d * 1000) / 1000.0;
    }

    @Test
    void whitesFromLabToXyz() {
        var white = new OkLAB(1, 0, 0);
        assertThat(white.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER()).isEqualTo(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
        assertThat(white.toXyz().usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER()).isEqualTo(CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER);
        assertThat(white.toXyz().usingD65_IEC_61966_2_1()).isEqualTo(CIEXYZ.D65_WHITE_IEC_61966_2_1);
        assertThat(white.toXyz().usingD65_ASTM_E308_01()).isEqualTo(CIEXYZ.D65_WHITE_ASTM_E308_01);
    }

    @Test
    void whitesFromXyzToLab() {
        var white = new OkLAB(1, 0, 0);
        assertIsCloseTo(OkLAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DEGREE_STANDARD_OBSERVER(), white, EXACT);
        assertIsCloseTo(OkLAB.from(CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER).usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER(), white, EXACT);
        assertIsCloseTo(OkLAB.from(CIEXYZ.D65_WHITE_IEC_61966_2_1).usingD65_IEC_61966_2_1(), white, EXACT);
        assertIsCloseTo(OkLAB.from(CIEXYZ.D65_WHITE_ASTM_E308_01).usingD65_ASTM_E308_01(), white, EXACT);
    }

    // not a real test, just documentation
    @Test
    void okLabWhitePoint() {
        CIEXYZ whiteXYZ = OkLAB.WHITE.toXyz().usingOriginalMatrix();
        // This is close to D65_WHITE_ASTM_E308_01 =
        //                        new CIEXYZ(0.95047,    1,          1.08883);
        // but with Z ~= 1.0883 instead of 1.08883
        // and to D65_WHITE_2DEGREE_STANDARD_OBSERVER =
        //                        new CIEXYZ(0.95042855, 1.00000000, 1.08890037)
        // but with X ~= 0.95047 instead of ~0.95043
        assertIsCloseTo(whiteXYZ, new CIEXYZ(0.95047004, 1.00000003, 1.08830021), EXACT);

        CIExyY whitexyY = CIExyY.from(whiteXYZ);
        // Relatively close to xy of CIExyY D65_WHITE_2DEGREE_STANDARD_OBSERVER =
        //                        new CIExyY(0.31271,    0.32902,    1);
        assertIsCloseTo(whitexyY, new CIExyY(0.31278114, 0.32908050, 1.00000003), EXACT);

        UV whiteUV = UV.from(whiteXYZ);
        assertIsCloseTo(new double[]{whiteUV.u(), whiteUV.v()}, new double[]{0.19785619311384714, 0.4683750429349658}, EXACT);

        assertIsCloseTo(Srgb.from(OkLAB.WHITE.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER()), Srgb.WHITE, EXACT);
    }
}
