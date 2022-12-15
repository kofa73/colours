package kofa;

import kofa.maths.DoubleVector;
import kofa.maths.Matrix;
import kofa.maths.Vector3D;
import org.assertj.core.data.Percentage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class NumericAssertions {
    // precentages
    public static final double PRECISE = 0.05;
    public static final double LENIENT = 0.25;
    public static final double ROUGH_FOR_INT = 5;

    public static void assertIsCloseTo(Matrix<?> result, Matrix<?> expectedResult, double percentage) {
        for (int column = 0; column < result.nColumns(); column++) {
            double[] resultColumn = result.column(column);
            double[] expectedColumn = expectedResult.column(column);

            assertIsCloseTo(resultColumn, expectedColumn, percentage);
        }
    }

    public static <V extends DoubleVector> void assertIsCloseTo(V actualVector, V expectedVector, double percentage) {
        assertIsCloseTo(actualVector.values(), expectedVector.values(), percentage);
    }

    public static void assertIsCloseTo(double[] actualVector, double[] expectedVector, double percentage) {
        assertThat(actualVector).hasSameSizeAs(expectedVector);
        assertSoftly(softly -> {
                    for (int row = 0; row < actualVector.length; row++) {
                        softly.assertThat(actualVector[row]).isCloseTo(expectedVector[row], Percentage.withPercentage(percentage));
                    }
                }
        );
    }

    public static <V extends Vector3D> void assertIsCloseTo(V actualVector, V expectedVector, double percentage0, double percentage1, double percentage2) {
        double[] actualDoubles = actualVector.values();
        double[] expectedDoubles = expectedVector.values();
        assertSoftly(softly -> {
            softly.assertThat(actualDoubles[0]).isCloseTo(expectedDoubles[0], Percentage.withPercentage(percentage0));
            softly.assertThat(actualDoubles[1]).isCloseTo(expectedDoubles[1], Percentage.withPercentage(percentage1));
            softly.assertThat(actualDoubles[2]).isCloseTo(expectedDoubles[2], Percentage.withPercentage(percentage2));
        });
    }
}
