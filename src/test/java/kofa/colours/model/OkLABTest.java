package kofa.colours.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static kofa.NumericAssertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class OkLABTest {
    @Test
    void white_labToXyz() {
        assertIsCloseTo(new OkLAB(1, 0, 0).toXyz(), CIEXYZ.D65_WHITE_ASTM_E308_01, PRECISE);
    }

    @Test
    void white_xyzToLab() {
        assertIsCloseTo(OkLAB.from(CIEXYZ.D65_WHITE_ASTM_E308_01), new OkLAB(1, 0, 0), PRECISE, 1E-4);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void xyzToLabToXyz(CIEXYZ xyz, OkLAB ignored) {
        assertIsCloseTo(OkLAB.from(xyz).toXyz(), xyz, PRECISE, 1E-7);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void labToXyzToLab(CIEXYZ ignored, OkLAB lab) {
        assertIsCloseTo(OkLAB.from(lab.toXyz()), lab, PRECISE, 1E-7);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void fromXyz_rounded(CIEXYZ xyz, OkLAB expectedOkLab) {
        assertIsCloseTo(roundToThreeDecimals(OkLAB.from(xyz)), expectedOkLab, PRECISE);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void toXyz_rounded(CIEXYZ expectedXyz, OkLAB okLab) {
        assertIsCloseTo(okLab.toXyz(), expectedXyz, LENIENT, 0.005);
        assertIsCloseTo(roundToThreeDecimals(okLab.toXyz()), expectedXyz, ROUGH, 0.005);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void fromXyz(CIEXYZ xyz, OkLAB expectedOkLab) {
        assertIsCloseTo(OkLAB.from(xyz), expectedOkLab, ROUGH, 3E-4);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void toXyz(CIEXYZ expectedXyz, OkLAB okLab) {
        assertIsCloseTo(okLab.toXyz(), expectedXyz, LENIENT, 1E-3);
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
}
