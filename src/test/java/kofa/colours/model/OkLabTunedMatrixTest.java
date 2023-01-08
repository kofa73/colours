package kofa.colours.model;

import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class OkLabTunedMatrixTest {
    @Test
    void changesComparedToOriginalMatrix_D65_WHITE_ASTM_E308_01() {
        assertThat(0.8190448227373099).isCloseTo(0.8189330101, Percentage.withPercentage(0.01));
        assertThat(0.3618535159212018).isCloseTo(0.3618667424, Percentage.withPercentage(0.01));
        assertThat(-0.12888242722626334).isCloseTo(-0.1288597137, Percentage.withPercentage(0.01));

        assertThat(0.03298770152682678).isCloseTo(0.0329845436, Percentage.withPercentage(0.01));
        assertThat(0.9292808872100808).isCloseTo(0.9293118715, Percentage.withPercentage(0.01));
        assertThat(0.036153778610985066).isCloseTo(0.0361456387, Percentage.withPercentage(0.01));

        assertThat(0.048177415343936617).isCloseTo(0.0482003018, Percentage.withPercentage(0.01));
        assertThat(0.26425059322535277).isCloseTo(0.2643662691, Percentage.withPercentage(0.01));
        assertThat(0.6336695194198694).isCloseTo(0.6338517070, Percentage.withPercentage(0.01));
    }

    // not a test, but can be used to print the error as it'd normally be done when running the optimisation
    @Test
    void printError() {
        double[][] matrix = {
                {0.8085104474513791, 0.3678260598951263, -0.12522432564646657},
                {0.03280282965336907, 0.9293118714999982, 0.03689282997710722},
                {0.047994383865680015, 0.2655670976538347, 0.6420286824189418}
        };
        OkLABTuner.printMatrix("testing", new OkLABTuner.Result(0, matrix));
    }
}
