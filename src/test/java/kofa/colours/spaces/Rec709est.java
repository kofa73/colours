package kofa.colours.spaces;

import org.junit.jupiter.api.Test;

import static kofa.colours.model.CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER;
import static kofa.colours.spaces.Buffer3.newBuffer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class Rec709est {
    @Test
    void rec709_to_XYZ_and_back() {
        // given
        double[] rec709 = new double[] {1, 1, 1};
        double[] XYZ = newBuffer();

        // when
        Rec709.rec709_to_XYZ(rec709, XYZ);

        // then
        assertThat(XYZ[0]).isCloseTo(D65_WHITE_2DEGREE_STANDARD_OBSERVER.X(), offset(1e-5));
        assertThat(XYZ[1]).isCloseTo(D65_WHITE_2DEGREE_STANDARD_OBSERVER.Y(), offset(1e-5));
        assertThat(XYZ[2]).isCloseTo(D65_WHITE_2DEGREE_STANDARD_OBSERVER.Z(), offset(1e-5));

        // when
        Rec709.XYZ_to_rec709(XYZ, rec709);

        // then
        assertThat(rec709[0]).isCloseTo(1, offset(1e-5));
        assertThat(rec709[1]).isCloseTo(1, offset(1e-5));
        assertThat(rec709[2]).isCloseTo(1, offset(1e-5));
    }

    @Test
    void rec709_and_rec2020_direct() {
        // given values picked in darktable
        double[] rec709 = new double[]{89 / 255.0, 115 / 255.0, 177 / 255.0};
        double[] rec2020 = newBuffer();

        // when
        Rec709.rec709toRec2020(rec709, rec2020);

        // then
        assertThat((rec2020[0])).isCloseTo(0.3975393269632675, offset(1e-6));
        assertThat((rec2020[1])).isCloseTo(0.4466974567230059, offset(1e-6));
        assertThat((rec2020[2])).isCloseTo(0.6670572844375361, offset(1e-6));

    }

    @Test
    void rec709_and_rec2020() {
        // given values picked in darktable
        double[] rec709 = new double[] {89 / 255.0, 115 / 255.0, 177 / 255.0};
        double[] XYZ = newBuffer();

        // when
        Rec709.rec709_to_XYZ(rec709, XYZ);

        // then
        assertThat(XYZ[0]).isCloseTo(0.43045161775850577, offset(1e-6));
        assertThat(XYZ[1]).isCloseTo(0.44684946987544294, offset(1e-6));
        assertThat(XYZ[2]).isCloseTo(0.7201727329888512, offset(1e-6));

        // when
        double[] rec2020 = newBuffer();
        Rec2020.XYZ_to_rec2020(XYZ, rec2020);

        // then
        // rough 8-bit values
        assertThat(rec2020[0]).isCloseTo(101 / 255.0, offset(1e-2));
        assertThat(rec2020[1]).isCloseTo(114 / 255.0, offset(1e-2));
        assertThat(rec2020[2]).isCloseTo(170 / 255.0, offset(1e-2));

        // exact values based on other code
        assertThat((rec2020[0])).isCloseTo(0.3975393269632675, offset(1e-6));
        assertThat((rec2020[1])).isCloseTo(0.4466974567230059, offset(1e-6));
        assertThat((rec2020[2])).isCloseTo(0.6670572844375361, offset(1e-6));
    }
}