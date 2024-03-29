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

import static kofa.DoubleArrayAssert.assertThat;
import static kofa.NumericAssertions.*;
import static kofa.Vector3Assert.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(SoftAssertionsExtension.class)
class OkLABTest {
    @InjectSoftAssertions
    private SoftAssertions softly;

    @Test
    void white_labToXyz() {
        assertThat(new OkLAB(1, 0, 0).toXyz().usingOriginalMatrix()).isCloseTo(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, PRECISE);
        assertThat(new OkLAB(1, 0, 0).toXyz().usingD65_ASTM_E308_01()).isCloseTo(CIEXYZ.D65_WHITE_ASTM_E308_01, EXACT);
        assertThat(new OkLAB(1, 0, 0).toXyz().usingD65_IEC_61966_2_1()).isCloseTo(CIEXYZ.D65_WHITE_IEC_61966_2_1, EXACT);
        assertThat(new OkLAB(1, 0, 0).toXyz().usingD65_2DEGREE_STANDARD_OBSERVER()).isCloseTo(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, EXACT);
        assertThat(new OkLAB(1, 0, 0).toXyz().usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER()).isCloseTo(CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER, EXACT);
    }

    @Test
    void white_xyzToLab() {
        assertThat(OkLAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingOriginalMatrix()).isCloseTo(new OkLAB(1, 0, 0), PRECISE, 1E-4);
        assertThat(OkLAB.from(CIEXYZ.D65_WHITE_ASTM_E308_01).usingD65_ASTM_E308_01()).isCloseTo(new OkLAB(1, 0, 0), EXACT, 1E-15);
        assertThat(OkLAB.from(CIEXYZ.D65_WHITE_IEC_61966_2_1).usingD65_IEC_61966_2_1()).isCloseTo(new OkLAB(1, 0, 0), EXACT, 1E-15);
        assertThat(OkLAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DEGREE_STANDARD_OBSERVER()).isCloseTo(new OkLAB(1, 0, 0), EXACT, 1E-15);
        assertThat(OkLAB.from(CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER).usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER()).isCloseTo(new OkLAB(1, 0, 0), EXACT, 1E-15);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void xyzToLabToXyz(CIEXYZ xyz, OkLAB ignored) {
        assertThat(OkLAB.from(xyz).usingOriginalMatrix().toXyz().usingOriginalMatrix()).isCloseTo(xyz, PRECISE, 3E-9);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void labToXyzToLab(CIEXYZ ignored, OkLAB lab) {
        assertThat(OkLAB.from(lab.toXyz().usingOriginalMatrix()).usingOriginalMatrix()).isCloseTo(lab, EXACT, 1E-7);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void fromXyz_rounded(CIEXYZ xyz, OkLAB expectedOkLab) {
        assertThat(roundToThreeDecimals(OkLAB.from(xyz).usingOriginalMatrix())).isCloseTo(expectedOkLab, EXACT);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void toXyz_rounded(CIEXYZ expectedXyz, OkLAB okLab) {
        assertThat(okLab.toXyz().usingOriginalMatrix()).isCloseTo(expectedXyz, LENIENT, 0.005);
        assertThat(roundToThreeDecimals(okLab.toXyz().usingOriginalMatrix())).isCloseTo(expectedXyz, ROUGH, 0.005);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void fromXyz(CIEXYZ xyz, OkLAB expectedOkLab) {
        assertThat(OkLAB.from(xyz).usingOriginalMatrix()).isCloseTo(expectedOkLab, ROUGH, 3E-4);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void toXyz(CIEXYZ expectedXyz, OkLAB okLab) {
        assertThat(okLab.toXyz().usingOriginalMatrix()).isCloseTo(expectedXyz, LENIENT, 1E-3);
    }

    // https://bottosson.github.io/posts/oklab/#table-of-example-xyz-and-oklab-pairs
    private static Stream<Arguments> xyzAndLab() {
        return Stream.of(
                arguments(named("reference white", new CIEXYZ(0.950, 1.000, 1.089)), new OkLAB(1.000, 0.000, 0.000)),
                arguments(named("X", new CIEXYZ(1.000, 0.000, 0.000)), new OkLAB(0.450, 1.236, -0.019)),
                arguments(named("Y", new CIEXYZ(0.000, 1.000, 0.000)), new OkLAB(0.922, -0.671, 0.263)),
                arguments(named("Z", new CIEXYZ(0.000, 0.000, 1.000)), new OkLAB(0.153, -1.415, -0.449))
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
        assertThat(white.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER()).isCloseTo(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, EXACT);
        assertThat(white.toXyz().usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER()).isCloseTo(CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER, EXACT);
        assertThat(white.toXyz().usingD65_IEC_61966_2_1()).isCloseTo(CIEXYZ.D65_WHITE_IEC_61966_2_1, EXACT);
        assertThat(white.toXyz().usingD65_ASTM_E308_01()).isCloseTo(CIEXYZ.D65_WHITE_ASTM_E308_01, EXACT);
    }

    @Test
    void whitesFromXyzToLab() {
        var white = new OkLAB(1, 0, 0);
        assertThat(OkLAB.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER).usingD65_2DEGREE_STANDARD_OBSERVER()).isCloseTo(white, EXACT);
        assertThat(OkLAB.from(CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER).usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER()).isCloseTo(white, EXACT);
        assertThat(OkLAB.from(CIEXYZ.D65_WHITE_IEC_61966_2_1).usingD65_IEC_61966_2_1()).isCloseTo(white, EXACT);
        assertThat(OkLAB.from(CIEXYZ.D65_WHITE_ASTM_E308_01).usingD65_ASTM_E308_01()).isCloseTo(white, EXACT);
    }

    // not a real test, just documentation
    @Test
    void okLabWhitePoint() {
        CIEXYZ whiteXYZ = OkLAB.WHITE.toXyz().usingOriginalMatrix();
        // This is close to D65_WHITE_ASTM_E308_01 =
        //                             new CIEXYZ(0.95047,            1,                  1.08883);
        assertThat(whiteXYZ).isCloseTo(new CIEXYZ(0.9504700194181661, 1.0000000125279485, 1.0882999575170746), EXACT);

        CIExyY whitexyY = CIExyY.from(whiteXYZ);
        // Relatively close to xy of CIExyY D65_WHITE_2DEGREE_STANDARD_OBSERVER =
        //                             new CIExyY(0.31271,    0.32902,    1);
        assertThat(whitexyY).isCloseTo(new CIExyY(0.31278114, 0.32908050, 1.00000003), EXACT);

        UV whiteUV = UV.from(whiteXYZ);
        assertThat(new double[]{whiteUV.u(), whiteUV.v()}).isCloseTo(new double[]{0.19785619311384714, 0.4683750429349658}, EXACT);

        assertThat(Srgb.from(OkLAB.WHITE.toXyz().usingD65_2DEGREE_STANDARD_OBSERVER())).isCloseTo(Srgb.WHITE, EXACT);
    }
}
