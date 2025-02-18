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
        float[] rec709 = new float[] {1, 1, 1};
        float[] XYZ = newBuffer();

        // when
        Rec709.rec709_to_XYZ(rec709, XYZ);

        // then
        assertThat(XYZ[0]).isCloseTo((float) D65_WHITE_2DEGREE_STANDARD_OBSERVER.X(), offset(1e-5f));
        assertThat(XYZ[1]).isCloseTo((float) D65_WHITE_2DEGREE_STANDARD_OBSERVER.Y(), offset(1e-5f));
        assertThat(XYZ[2]).isCloseTo((float) D65_WHITE_2DEGREE_STANDARD_OBSERVER.Z(), offset(1e-5f));

        // when
        Rec709.XYZ_to_rec709(XYZ, rec709);

        // then
        assertThat(rec709[0]).isCloseTo(1, offset(1e-5f));
        assertThat(rec709[1]).isCloseTo(1, offset(1e-5f));
        assertThat(rec709[2]).isCloseTo(1, offset(1e-5f));
    }

    @Test
    void rec709_and_rec2020_direct() {
        // given values picked in darktable
        float[] rec709 = new float[]{89 / 255.0f, 115 / 255.0f, 177 / 255.0f};
        float[] rec2020 = newBuffer();

        // when
        Rec709.rec709toRec2020(rec709, rec2020);

        // then
        assertThat((rec2020[0])).isCloseTo(0.3975393269632675f, offset(1e-6f));
        assertThat((rec2020[1])).isCloseTo(0.4466974567230059f, offset(1e-6f));
        assertThat((rec2020[2])).isCloseTo(0.6670572844375361f, offset(1e-6f));

    }

    @Test
    void rec709_and_rec2020() {
        // given values picked in darktable
        float[] rec709 = new float[] {89 / 255.0f, 115 / 255.0f, 177 / 255.0f};
        float[] XYZ = newBuffer();

        // when
        Rec709.rec709_to_XYZ(rec709, XYZ);

        // then
        assertThat(XYZ[0]).isCloseTo(0.43045161775850577f, offset(1e-6f));
        assertThat(XYZ[1]).isCloseTo(0.44684946987544294f, offset(1e-6f));
        assertThat(XYZ[2]).isCloseTo(0.7201727329888512f, offset(1e-6f));

        // when
        float[] rec2020 = newBuffer();
        Rec2020.XYZ_to_rec2020(XYZ, rec2020);

        // then
        // rough 8-bit values
        assertThat(rec2020[0]).isCloseTo(101 / 255.0f, offset(1e-2f));
        assertThat(rec2020[1]).isCloseTo(114 / 255.0f, offset(1e-2f));
        assertThat(rec2020[2]).isCloseTo(170 / 255.0f, offset(1e-2f));

        // exact values based on other code
        assertThat((rec2020[0])).isCloseTo(0.3975393269632675f, offset(1e-6f));
        assertThat((rec2020[1])).isCloseTo(0.4466974567230059f, offset(1e-6f));
        assertThat((rec2020[2])).isCloseTo(0.6670572844375361f, offset(1e-6f));
    }
}