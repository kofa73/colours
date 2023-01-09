package kofa.colours.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static kofa.DoubleArrayAssert.assertThat;
import static kofa.NumericAssertions.EXACT;
import static kofa.NumericAssertions.PRECISE;
import static kofa.Vector3Assert.assertThat;
import static kofa.colours.model.ConverterTest.*;
import static org.assertj.core.data.Offset.offset;

class Rec2020Test {
    @Test
    void white() {
        assertThat(new Rec2020(1, 1, 1).toXyz()).isCloseTo(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, EXACT);
    }

    @Test
    void values() {
        Assertions.assertThat(
                REC2020_663399.coordinates()
        ).contains(
                0.10805750115024938, 0.04324141440243367, 0.29037800273628517
        );
    }

    @Test
    void fromXYZ() {
        assertThat(Rec2020.from(XYZ_663399)).isCloseTo(REC2020_663399, PRECISE);
    }

    @Test
    void toXyz() {
        assertThat(REC2020_663399.toXyz()).isCloseTo(XYZ_663399, PRECISE);
    }

    @Test
    void toSRGB() {
        assertThat(REC2020_663399.toSRGB()).isCloseTo(LINEAR_SRGB_663399, PRECISE);
    }

    @Test
    void toXyzMatrix() {
        var rec2020ToXyz = Rec2020.TO_XYZ.values();
        // https://colour.readthedocs.io/en/v0.3.7/colour.models.dataset.rec_2020.html#colour.models.dataset.rec_2020.REC_2020_TO_XYZ_MATRIX
        assertThat(rec2020ToXyz[0]).isCloseTo(new double[]{6.36953507E-01, 1.44619185E-01, 1.68855854E-01}, EXACT);
        assertThat(rec2020ToXyz[1]).isCloseTo(new double[]{2.62698339E-01, 6.78008766E-01, 5.92928953E-02}, EXACT);
        assertThat(rec2020ToXyz[2]).isCloseTo(new double[]{4.99407097E-17, 2.80731358E-02, 1.06082723E+00}, EXACT);
    }

    @Test
    void fromXyzMatrix() {
        var rec2020FromXyz = Rec2020.FROM_XYZ.values();
        // https://colour.readthedocs.io/en/v0.3.7/colour.models.dataset.rec_2020.html#colour.models.dataset.rec_2020.XYZ_TO_REC_2020_MATRIX
        assertThat(rec2020FromXyz[0]).isCloseTo(new double[]{1.71666343, -0.35567332, -0.25336809}, PRECISE);
        assertThat(rec2020FromXyz[1]).isCloseTo(new double[]{-0.66667384, 1.61645574, 0.0157683}, PRECISE);
        assertThat(rec2020FromXyz[2]).isCloseTo(new double[]{0.01764248, -0.04277698, 0.94224328}, PRECISE);
    }

    @Test
    void applyInverseOetf() {
        var linearBelowThreshold = Rec2020.applyInverseOetf(0.0812428312);
        double linearAtThreshold = Rec2020.applyInverseOetf(0.0812428313);
        Assertions.assertThat(linearBelowThreshold)
                .isEqualTo(linearAtThreshold, offset(1E-10))
                .isLessThan(linearAtThreshold);
    }
}
