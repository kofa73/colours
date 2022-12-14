package kofa;

import kofa.maths.FloatVector;
import kofa.maths.Matrix;
import org.assertj.core.data.Offset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class NumericHelper {
    public static void assertApproximatelyEqual(Matrix<?> result, Matrix<?> expectedResult, float delta) {
        for (int column = 0; column < result.nColumns(); column++) {
            float[] resultColumn = result.column(column);
            float[] expectedColumn = expectedResult.column(column);

            assertApproximatelyEqual(resultColumn, expectedColumn, delta);
        }
    }

    public static void assertApproximatelyEqual(FloatVector actualVector, FloatVector expectedVector, float delta) {
        assertApproximatelyEqual(actualVector.toFloats(), expectedVector.toFloats(), delta);
    }

    public static void assertApproximatelyEqual(float[] actualVector, float[] expectedVector, float delta) {
        assertThat(actualVector).hasSameSizeAs(expectedVector);
        assertSoftly(softly -> {
                    for (int row = 0; row < actualVector.length; row++) {
                        softly.assertThat(actualVector[row]).isEqualTo(expectedVector[row], Offset.offset(delta));
                    }
                }
        );
    }

}
