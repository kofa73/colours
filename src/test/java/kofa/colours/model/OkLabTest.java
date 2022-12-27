package kofa.colours.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static kofa.NumericAssertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class OkLabTest {
    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void xyzToLabToXyz(Xyz xyz, OkLab ignored) {
        assertIsCloseTo(OkLab.from(xyz).toXyz(), xyz, PRECISE);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void labToXyzToLab(Xyz ignored, OkLab lab) {
        assertIsCloseTo(OkLab.from(lab.toXyz()), lab, PRECISE);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void fromXyz(Xyz xyz, OkLab expectedOkLab) {
        assertIsCloseTo(roundToThreeDecimals(OkLab.from(xyz)), expectedOkLab, LENIENT);
    }

    @ParameterizedTest
    @MethodSource("xyzAndLab")
    void toXyz(Xyz expectedXyz, OkLab okLab) {
        assertIsCloseTo(roundToThreeDecimals(okLab.toXyz()), expectedXyz, ROUGH, 0.005);
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
                roundToThreeDecimals(okLab.L()),
                roundToThreeDecimals(okLab.a()),
                roundToThreeDecimals(okLab.b())
        );
    }

    private Xyz roundToThreeDecimals(Xyz xyz) {
        return new Xyz(
                roundToThreeDecimals(xyz.X()),
                roundToThreeDecimals(xyz.Y()),
                roundToThreeDecimals(xyz.Z())
        );
    }

    private double roundToThreeDecimals(double d) {
        System.out.println("in: " + d + ", out: " + Math.round(d * 1000) / 1000.0);
        return Math.round(d * 1000) / 1000.0;
    }
}