package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;

class Rec2020Test {
    @Test
    void roundTrip_XYZ() {
        var original = new Rec2020(0.123, 0.456, 0.789);
        assertIsCloseTo(original, Rec2020.from(original.toXYZ()), PRECISE);
    }
}