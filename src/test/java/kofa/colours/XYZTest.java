package kofa.colours;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XYZTest {

    private final XYZ xyz = new XYZ(0.123f, 0.456f, 0.789f);

    @Test
    void toFloats() {
        assertThat(
                xyz.toFloats()
        ).containsExactly(0.123f, 0.456f, 0.789f);
    }

    @Test
    void x() {
        assertThat(
                xyz.toLuvUsingWhitePoint(Converter.D65_WHITE_XYZ)
        ).isEqualTo(Converter.convert_XYZ_to_Luv_D65(xyz));
    }
}