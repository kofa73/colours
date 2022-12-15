package kofa.colours;

import org.junit.jupiter.api.Test;

import static kofa.NumericAssertions.PRECISE;
import static kofa.NumericAssertions.assertIsCloseTo;
import static org.assertj.core.api.Assertions.assertThat;

class LabTest {
    private final Lab lab = new Lab(32.90281, 42.88651, -47.14914);

    @Test
    void values() {
        assertThat(lab.values()).containsExactly(32.90281, 42.88651, -47.14914);
    }

    @Test
    void fromXYZ() {
        assertIsCloseTo(
                Lab.from(new XYZ(0.12412833273360171, 0.07492777924493509, 0.30924055280859232)).usingD65(),
                lab,
                PRECISE
        );
    }
}