package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class XYZTest {
    // for RGB #663399; https://ajalt.github.io/colormath/converter/
    private final XYZ xyz = new XYZ(0.12412, 0.07493, 0.3093);

    @Test
    void toDoubles() {
        assertThat(
                xyz.values()
        ).containsExactly(0.12412, 0.07493, 0.3093);
    }

    @Test
    void toLuvUsingWhitePoint() {
        // from https://ajalt.github.io/colormath/converter/
        assertIsCloseTo(
                Luv.fromXYZ(xyz).usingWhitePoint(Converter.D65_WHITE_XYZ),
                new Luv(32.90281, 12.9804, -67.75974),
                PRECISE
        );
        double expected_hRadians = (6.1006 / 360 * 2 * Math.PI);
        assertIsCloseTo(
                // 154/255 = 0.60392; 58/255 = 0.22745, 61/255 = 0.23922
                Luv
                        .fromXYZ(
                                new Rec2020(154.0 / 255, 58.0 / 255, 61.0 / 255).toXYZ()
                        ).usingWhitePoint(Converter.D65_WHITE_XYZ)
                        .toLch_uv(),
                new LCh_uv(63.91936, 83.81409, expected_hRadians),
                // picked from UI
                PRECISE, PRECISE, LENIENT
        );
        assertIsCloseTo(
                // linear Rec709 reading from a photo in darktable
                Luv.fromXYZ(
                                new SRGB(217.0 / 255, 46.0 / 255, 59.0 / 255).toXYZ()
                        ).usingWhitePoint(Converter.D65_WHITE_XYZ)
                        .toLch_uv(),
                // LCh from https://ajalt.github.io/colormath/converter/, h in degrees
                new LCh_uv(63.91936, 83.81409, (6.1006 / 360 * 2 * Math.PI)),
                // allow some slack because of the integers
                PRECISE, LENIENT, ROUGH_FOR_INT
                // FIXME: darktable LCh 64.44, 50.26, 17.26 (degrees -> 0.30124 rad)
                // Maybe that's LCH(ab)? https://ajalt.github.io/colormath/converter/ says
                // LCH(ab) is 63.91936, 49.42461, 16.07154, which is quite close
        );
    }
}
