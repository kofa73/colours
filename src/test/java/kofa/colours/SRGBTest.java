package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericHelper.assertApproximatelyEqual;

class SRGBTest {
    @Test
    void roundTrip_XYZ() {
        var original = new SRGB(0.123f, 0.456f, 0.789f);
        assertApproximatelyEqual(original, SRGB.from(original.toXYZ()), 1E-5f);
    }
}