package kofa.colours.spaces;

import org.junit.jupiter.api.Test;

import static kofa.colours.model.CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER;
import static kofa.colours.spaces.Buffer3.newBuffer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class Rec2020Test {
    @Test
    void rec2020_to_XYZ_and_back() {
        // given
        float[] rec2020 = new float[] {1, 1, 1};
        float[] XYZ = newBuffer();

        // when
        Rec2020.rec2020_to_XYZ(rec2020, XYZ);

        // then
        assertThat(XYZ[0]).isCloseTo((float) D65_WHITE_2DEGREE_STANDARD_OBSERVER.X(), offset(1e-6f));
        assertThat(XYZ[1]).isCloseTo((float) D65_WHITE_2DEGREE_STANDARD_OBSERVER.Y(), offset(1e-6f));
        assertThat(XYZ[2]).isCloseTo((float) D65_WHITE_2DEGREE_STANDARD_OBSERVER.Z(), offset(1e-6f));

        // when
        Rec2020.XYZ_to_rec2020(XYZ, rec2020);

        // then
        assertThat(rec2020[0]).isCloseTo(1, offset(1e-6f));
        assertThat(rec2020[1]).isCloseTo(1, offset(1e-6f));
        assertThat(rec2020[2]).isCloseTo(1, offset(1e-6f));
    }

    @Test
    void rec2020_and_rec709() {
        // given values picked in darktable
        float[] rec2020 = new float[] {101 / 255.0f, 114 / 255.0f, 170 / 255.0f};
        float[] valuesXYZ = newBuffer();

        // when
        Rec2020.rec2020_to_XYZ(rec2020, valuesXYZ);
        // then
        assertThat(valuesXYZ[0]).isCloseTo(0.42950739766460216f, offset(1e-6f));
        assertThat(valuesXYZ[1]).isCloseTo(0.4466875440341956f, offset(1e-6f));
        assertThat(valuesXYZ[2]).isCloseTo(0.719768499718504f, offset(1e-6f));

        // when
        float[] rec709 = newBuffer();
        Rec709.XYZ_to_rec709(valuesXYZ, rec709);

        // then we get values picked in darktable
        // rough comparison, 8-bit values
        assertThat(rec709[0]).isCloseTo(89 / 255f, offset(1e-2f));
        assertThat(rec709[1]).isCloseTo(115 / 255f, offset(1e-3f));
        assertThat(rec709[2]).isCloseTo(177 / 255f, offset(1e-3f));
        // exact values based on other code
        assertThat((rec709[0])).isCloseTo(0.3464098892162477f, offset(1e-6f));
        assertThat((rec709[1])).isCloseTo(0.45157499402975027f, offset(1e-6f));
        assertThat((rec709[2])).isCloseTo(0.6936708110984687f, offset(1e-6f));
    }
}