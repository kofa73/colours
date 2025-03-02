package kofa.colours.spaces;

import org.junit.jupiter.api.Test;

import static kofa.colours.model.CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER;
import static kofa.maths.MathHelpers.vec3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class Rec2020Test {
    @Test
    void rec2020_to_XYZ_and_back() {
        // given
        double[] rec2020 = new double[] {1, 1, 1};
        double[] XYZ = vec3();

        // when
        Rec2020.rec2020_to_XYZ(rec2020, XYZ);

        // then
        assertThat(XYZ[0]).isCloseTo(D65_WHITE_2DEGREE_STANDARD_OBSERVER.X(), offset(1e-6));
        assertThat(XYZ[1]).isCloseTo(D65_WHITE_2DEGREE_STANDARD_OBSERVER.Y(), offset(1e-6));
        assertThat(XYZ[2]).isCloseTo(D65_WHITE_2DEGREE_STANDARD_OBSERVER.Z(), offset(1e-6));

        // when
        Rec2020.XYZ_to_rec2020(XYZ, rec2020);

        // then
        assertThat(rec2020[0]).isCloseTo(1, offset(1e-6));
        assertThat(rec2020[1]).isCloseTo(1, offset(1e-6));
        assertThat(rec2020[2]).isCloseTo(1, offset(1e-6));
    }

    @Test
    void rec2020_and_rec709() {
        // given values picked in darktable
        double[] rec2020 = new double[] {101 / 255.0, 114 / 255.0, 170 / 255.0};
        double[] valuesXYZ = vec3();

        // when
        Rec2020.rec2020_to_XYZ(rec2020, valuesXYZ);
        // then
        assertThat(valuesXYZ[0]).isCloseTo(0.42950739766460216, offset(1e-6));
        assertThat(valuesXYZ[1]).isCloseTo(0.4466875440341956, offset(1e-6));
        assertThat(valuesXYZ[2]).isCloseTo(0.719768499718504, offset(1e-6));

        // when
        double[] rec709 = vec3();
        Rec709.XYZ_to_rec709(valuesXYZ, rec709);

        // then we get values picked in darktable
        // rough comparison, 8-bit values
        assertThat(rec709[0]).isCloseTo(89.0 / 255, offset(1e-2));
        assertThat(rec709[1]).isCloseTo(115.0 / 255, offset(1e-3));
        assertThat(rec709[2]).isCloseTo(177.0 / 255, offset(1e-3));
        // exact values based on other code
        assertThat((rec709[0])).isCloseTo(0.3464098892162477, offset(1e-6));
        assertThat((rec709[1])).isCloseTo(0.45157499402975027, offset(1e-6));
        assertThat((rec709[2])).isCloseTo(0.6936708110984687, offset(1e-6));
    }
}