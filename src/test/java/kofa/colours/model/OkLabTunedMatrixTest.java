package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.data.Percentage.withPercentage;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(SoftAssertionsExtension.class)
public class OkLabTunedMatrixTest {
    @InjectSoftAssertions
    private SoftAssertions softly;

    @ParameterizedTest
    @MethodSource("matchedMatrices")
    void changesComparedToOriginalMatrix_closelyMatched(SpaceConversionMatrix<?, CIEXYZ> conversionMatrix, double percentage) {
        double[][] matrix = conversionMatrix.values();
        double[][] originalMatrix = OkLAB.XYZ_TO_LMS_ORIGINAL.values();

        for (int row = 0; row < matrix.length; row++) {
            for (int column = 0; column < matrix.length; column++) {
                softly.assertThat(matrix[row][column]).describedAs("%s,%s", row, column)
                        .isCloseTo(originalMatrix[row][column], withPercentage(percentage));
            }
        }
    }

    // most of the updated matrices are very close to the original at https://bottosson.github.io/posts/oklab/,
    // within 0.05% of the original values, but the 10-degree observer needed modifications up to 1%,
    // 0.0482003018 -> 0.048664577663383925
    private static Stream<Arguments> matchedMatrices() {
        return Stream.of(
                arguments(named("XYZ_TO_LMS_D65_IEC_61966_2_1", OkLAB.XYZ_TO_LMS_D65_IEC_61966_2_1), 0.05),
                arguments(named("XYZ_TO_LMS_D65_ASTM_E308_01", OkLAB.XYZ_TO_LMS_D65_ASTM_E308_01), 0.04),
                arguments(named("XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER", OkLAB.XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER), 0.04),
                arguments(named("XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER", OkLAB.XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER), 1)
        );
    }
}
