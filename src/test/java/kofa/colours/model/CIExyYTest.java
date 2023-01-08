package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.EXACT;
import static kofa.Vector3Assert.assertThat;

class CIExyYTest {
    @Test
    void white_toXyz() {
        assertThat(new CIExyY(0.31271, 0.32902, 1).toXyz())
                .isCloseTo(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, EXACT);
    }

    @Test
    void white_fromXyz() {
        assertThat(CIExyY.from(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER))
                .isCloseTo(new CIExyY(0.31271, 0.32902, 1), EXACT);
    }

    @Test
    void black_toXyz() {
        assertThat(new CIExyY(0.31271, 0.32902, 0).toXyz())
                .isCloseTo(new CIEXYZ(0, 0, 0), EXACT);
    }

    @Test
    void black_fromXyz() {
        assertThat(CIExyY.from(new CIEXYZ(0, 0, 0)))
                .isCloseTo(new CIExyY(0.31271, 0.32902, 0), EXACT);
    }

    @Test
    void truncatedValuesAreUsedBy_D65_WHITE_IEC_61966_2_1() {
        // https://en.wikipedia.org/wiki/Illuminant_D65#Definition
        assertThat(new CIExyY(0.3127, 0.329, 1).toXyz()).isCloseTo(CIEXYZ.D65_WHITE_IEC_61966_2_1, EXACT);
        assertThat(CIExyY.from(CIEXYZ.D65_WHITE_IEC_61966_2_1)).isCloseTo(new CIExyY(0.3127, 0.329, 1), EXACT);
    }
}
