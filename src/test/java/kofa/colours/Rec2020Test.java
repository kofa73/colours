package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericHelper.assertApproximatelyEqual;

class Rec2020Test {
    @Test
    void roundTrip_XYZ() {
        var original = new Rec2020(0.123f, 0.456f, 0.789f);
        assertApproximatelyEqual(original, Rec2020.from(original.toXYZ()), 1E-5f);
    }
}