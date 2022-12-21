package kofa.colours.model;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.model.ConverterTest.*;
import static org.assertj.core.api.Assertions.assertThat;

class Rec2020Test {
    @Test
    void values() {
        assertThat(
                REC2020_663399.coordinates()
        ).contains(
                0.10805750115024938, 0.04324141440243367, 0.29037800273628517
        );
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
    void toXyz() {
        assertIsCloseTo(
                REC2020_663399.toXyz(),
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

    @Test
    void toXyzMatrix() {
        var rec2020ToXyz = Rec2020.TO_XYZ.values();
        // https://colour.readthedocs.io/en/v0.3.7/colour.models.dataset.rec_2020.html#colour.models.dataset.rec_2020.REC_2020_TO_XYZ_MATRIX
        assertIsCloseTo(rec2020ToXyz[0], new double[]{6.36953507E-01, 1.44619185E-01, 1.68855854E-01}, PRECISE);
        assertIsCloseTo(rec2020ToXyz[1], new double[]{2.62698339E-01, 6.78008766E-01, 5.92928953E-02}, PRECISE);
        assertIsCloseTo(rec2020ToXyz[2], new double[]{4.99407097E-17, 2.80731358E-02, 1.06082723E+00}, PRECISE);
    }

    @Test
    void fromXyzMatrix() {
        var rec2020FromXyz = Rec2020.FROM_XYZ.values();
        // https://colour.readthedocs.io/en/v0.3.7/colour.models.dataset.rec_2020.html#colour.models.dataset.rec_2020.XYZ_TO_REC_2020_MATRIX
        assertIsCloseTo(rec2020FromXyz[0], new double[]{1.71666343, -0.35567332, -0.25336809}, PRECISE);
        assertIsCloseTo(rec2020FromXyz[1], new double[]{-0.66667384, 1.61645574, 0.0157683}, PRECISE);
        assertIsCloseTo(rec2020FromXyz[2], new double[]{0.01764248, -0.04277698, 0.94224328}, PRECISE);
    }
}
