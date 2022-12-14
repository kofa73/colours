package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericHelper.assertApproximatelyEqual;
import static org.assertj.core.api.Assertions.assertThat;

class XYZTest {
    // for RGB #663399; https://ajalt.github.io/colormath/converter/
    private final XYZ xyz = new XYZ(0.12412f, 0.07493f, 0.3093f);

    @Test
    void toFloats() {
        assertThat(
                xyz.toFloats()
        ).containsExactly(0.12412f, 0.07493f, 0.3093f);
    }

    @Test
    void toLuvUsingWhitePoint() {
        // from https://ajalt.github.io/colormath/converter/
        assertApproximatelyEqual(
                xyz.toLuvUsingWhitePoint(Converter.D65_WHITE_XYZ),
                new Luv(32.90281f, 12.9804f, -67.75974f),
                2E-3f, 5E-3f, 3E-3f
        );
        float expected_hRadians = (float) (6.1006f / 360 * 2 * Math.PI);
        assertApproximatelyEqual(
                // 154/255 = 0.60392f; 58/255 = 0.22745f, 61/255 = 0.23922f
                new Rec2020(154 / 255f, 58 / 255f, 61 / 255f).toXYZ().toLuvUsingWhitePoint(Converter.D65_WHITE_XYZ).toLch_uv(),
                new LCh_uv(63.91936f, 83.81409f, expected_hRadians),
                1f
        );
        assertApproximatelyEqual(
                // linear Rec709 reading from a photo in darktable
                new SRGB(217 / 255f, 46 / 255f, 59 / 255f).toXYZ().toLuvUsingWhitePoint(Converter.D65_WHITE_XYZ).toLch_uv(),
                // LCh from https://ajalt.github.io/colormath/converter/, h in degrees
                new LCh_uv(63.91936f, 83.81409f, (float) (6.1006f / 360 * 2 * Math.PI)),
                // allow some slack because of the integers
                0.1f, 0.2f, 0.05f
                // FIXME: darktable LCh 64.44, 50.26, 17.26 (degrees -> 0.30124 rad)
                // Maybe that's LCH(ab)? https://ajalt.github.io/colormath/converter/ says
                // LCH(ab) is 63.91936, 49.42461, 16.07154, which is quite close
        );
    }
}
