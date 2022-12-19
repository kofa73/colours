package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.model.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;

class Rec2020Test {
    @Test
    void values() {
        assertThat(REC2020_663399.values()).contains(0.10805750115024938, 0.04324141440243367, 0.29037800273628517);
    }

    @Test
    void fromXYZ() {
        assertIsCloseTo(
                Rec2020.from(XYZ_663399),
                REC2020_663399,
                PRECISE
        );
    }

    @Test
    void toXYZ() {
        assertIsCloseTo(
                REC2020_663399.toXYZ(),
                XYZ_663399,
                PRECISE
        );
    }

    @Test
    void toSRGB() {
        assertIsCloseTo(
                REC2020_663399.toSRGB(),
                LINEAR_SRGB_663399,
                PRECISE
        );
    }
}
