package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;

class SRGBTest {
    @Test
    void values() {
        assertThat(new SRGB(1, 2, 3).values()).contains(1, 2, 3);
    }

    @Test
    void fromXYZ() {
        assertIsCloseTo(
                SRGB.from(XYZ_663399),
                LINEAR_SRGB_663399,
                PRECISE
        );
    }

    @Test
    void toXYZ() {
        assertIsCloseTo(
                LINEAR_SRGB_663399.toXYZ(),
                XYZ_663399,
                PRECISE
        );
    }

    @Test
    void toRec2020() {
        assertIsCloseTo(
                LINEAR_SRGB_663399.toRec2020(),
                REC2020_663399,
                PRECISE
        );
    }
}