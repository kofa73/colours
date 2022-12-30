package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;

class CIExyYTest {
    @Test
    void white_toXyz() {
        assertIsCloseTo(new CIExyY(0.31271, 0.32902, 1).toXyz(), CIEXYZ.D65_WHITE_IEC_61966_2_1, PRECISE);
    }

    @Test
    void white_fromXyz() {
        assertIsCloseTo(CIExyY.from(CIEXYZ.D65_WHITE_IEC_61966_2_1), new CIExyY(0.31271, 0.32902, 1), PRECISE);
    }

    @Test
    void black_toXyz() {
        assertIsCloseTo(new CIExyY(0.31271, 0.32902, 0).toXyz(), new CIEXYZ(0, 0, 0), PRECISE);
    }

    @Test
    void black_fromXyz() {
        assertIsCloseTo(CIExyY.from(new CIEXYZ(0, 0, 0)), new CIExyY(0.31271, 0.32902, 0), PRECISE);
    }
}
