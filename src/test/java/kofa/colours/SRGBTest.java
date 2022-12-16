package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;

class SRGBTest {
    private final SRGB srgb = new SRGB(0.123, 0.456, 0.789);

    @Test
    void roundTrip_XYZ() {
        assertIsCloseTo(SRGB.from(srgb.toXYZ()), srgb, PRECISE);
    }

    @Test
    void toRec2020() {
        assertIsCloseTo(srgb.toRec2020(), Rec2020.from(srgb.toXYZ()), PRECISE);
    }
}