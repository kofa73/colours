package kofa;

import kofa.maths.FloatVector;
import kofa.maths.Matrix;
import kofa.maths.Vector3D;
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

    public static <V extends FloatVector> void assertApproximatelyEqual(V actualVector, V expectedVector, float delta) {
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

    public static <V extends Vector3D> void assertApproximatelyEqual(V actualVector, V expectedVector, float delta0, float delta1, float delta2) {
        float[] actualFloats = actualVector.toFloats();
        float[] expectedFloats = expectedVector.toFloats();
        assertSoftly(softly -> {
            softly.assertThat(actualFloats[0]).isEqualTo(expectedFloats[0], Offset.offset(delta0));
            softly.assertThat(actualFloats[1]).isEqualTo(expectedFloats[1], Offset.offset(delta1));
            softly.assertThat(actualFloats[2]).isEqualTo(expectedFloats[2], Offset.offset(delta2));
        });
    }
}
