package kofa.colours;

import org.junit.jupiter.api.Test;

import static java.lang.Math.toRadians;
import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static kofa.colours.Converter.D65_WHITE_XYZ;
import static org.assertj.core.api.Assertions.assertThat;

class LabTest {
    // values from https://ajalt.github.io/colormath/converter/
    // RGB #663399
    private final Lab lab = new Lab(32.90281, 42.88651, -47.14914);
    private final XYZ xyz = new XYZ(0.12412, 0.07493, 0.3093);
    private final LCh_ab lCh = new LCh_ab(32.90281, 63.73612, toRadians(312.28943));

    @Test
    void values() {
        assertThat(lab.values()).containsExactly(32.90281, 42.88651, -47.14914);
    }

    @Test
    void fromXYZ() {
        assertIsCloseTo(
                Lab.from(xyz).usingD65(),
                lab,
                PRECISE
        );
    }

    @Test
    void toXYZ() {
        assertIsCloseTo(
                lab.toXyz(D65_WHITE_XYZ),
                xyz,
                PRECISE
        );
    }

    @Test
    void toLCh_ab() {
        assertIsCloseTo(
                lab.toLCh(),
                lCh,
                PRECISE
        );
    }
}
