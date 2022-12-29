package kofa.colours.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static kofa.NumericAssertions.*;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ_ASTM_E308_01;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class OkLabTest {
    @Test
    void white_labToXyz() {
        assertIsCloseTo(new OkLab(1, 0, 0).toXyz(), D65_WHITE_XYZ_ASTM_E308_01, PRECISE);
    }

    @Test
    void white_xyzToLab() {
        assertIsCloseTo(OkLab.from(D65_WHITE_XYZ_ASTM_E308_01), new OkLab(1, 0, 0), PRECISE, 1E-4);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void xyzToLabToXyz(Xyz xyz, OkLab ignored) {
        assertIsCloseTo(OkLab.from(xyz).toXyz(), xyz, PRECISE, 1E-7);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void labToXyzToLab(Xyz ignored, OkLab lab) {
        assertIsCloseTo(OkLab.from(lab.toXyz()), lab, PRECISE, 1E-7);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void fromXyz_rounded(Xyz xyz, OkLab expectedOkLab) {
        assertIsCloseTo(roundToThreeDecimals(OkLab.from(xyz)), expectedOkLab, PRECISE);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void toXyz_rounded(Xyz expectedXyz, OkLab okLab) {
        assertIsCloseTo(okLab.toXyz(), expectedXyz, PRECISE, 0.005);
        assertIsCloseTo(roundToThreeDecimals(okLab.toXyz()), expectedXyz, PRECISE, 0.005);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void fromXyz(Xyz xyz, OkLab expectedOkLab) {
        assertIsCloseTo(OkLab.from(xyz), expectedOkLab, ROUGH, 3E-4);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void toXyz(Xyz expectedXyz, OkLab okLab) {
        assertIsCloseTo(okLab.toXyz(), expectedXyz, LENIENT, 1E-3);
    }

    // https://bottosson.github.io/posts/oklab/#table-of-example-xyz-and-oklab-pairs
    private static Stream<Arguments> xyzAndLab() {
        return Stream.of(
                arguments(new Xyz(0.950, 1.000, 1.089), new OkLab(1.000, 0.000, 0.000)),
                arguments(new Xyz(1.000, 0.000, 0.000), new OkLab(0.450, 1.236, -0.019)),
                arguments(new Xyz(0.000, 1.000, 0.000), new OkLab(0.922, -0.671, 0.263)),
                arguments(new Xyz(0.000, 0.000, 1.000), new OkLab(0.153, -1.415, -0.449))
        );
    }

    private OkLab roundToThreeDecimals(OkLab okLab) {
        return new OkLab(
                roundToThreeDecimals(okLab.l()),
                roundToThreeDecimals(okLab.a()),
                roundToThreeDecimals(okLab.b())
        );
    }

    private Xyz roundToThreeDecimals(Xyz xyz) {
        return new Xyz(
                roundToThreeDecimals(xyz.x()),
                roundToThreeDecimals(xyz.y()),
                roundToThreeDecimals(xyz.z())
        );
    }

    private double roundToThreeDecimals(double d) {
        System.out.println("in: " + d + ", out: " + Math.round(d * 1000) / 1000.0);
        return Math.round(d * 1000) / 1000.0;
    }
}
