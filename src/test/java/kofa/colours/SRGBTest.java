package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;

class SRGBTest {
    @Test
    void roundTrip_XYZ() {
        var original = new SRGB(0.123, 0.456, 0.789);
        assertIsCloseTo(original, SRGB.from(original.toXYZ()), PRECISE);
    }
}