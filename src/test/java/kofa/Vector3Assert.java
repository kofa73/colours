package kofa;

import kofa.maths.Vector3;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.data.Percentage;

import static kofa.NumericAssertions.assertIsCloseTo;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class Vector3Assert<V extends Vector3> extends AbstractAssert<Vector3Assert<V>, V> {
    public static final Percentage EXACT = Percentage.withPercentage(1E-5);
    public static final Percentage PRECISE = Percentage.withPercentage(0.06);
    public static final Percentage LENIENT = Percentage.withPercentage(0.2);
    public static final Percentage ROUGH = Percentage.withPercentage(5);
    private static final double COMPARISON_THRESHOLD = 1E-15;

    private Vector3Assert(V actual) {
        super(actual, Vector3Assert.class);
    }

    public static <V extends Vector3> Vector3Assert<V> assertThat(V actual) {
        return new Vector3Assert<>(actual);
    }

    public Vector3Assert<V> isCloseTo(V expected, Percentage percentage) {
        return isCloseTo(expected, percentage, COMPARISON_THRESHOLD);
    }

    public Vector3Assert<V> isCloseTo(V expected, Percentage percentage, double comparisonThreshold) {
        isNotNull();
        if (actual.getClass() != expected.getClass()) {
            failWithMessage("actual is an instance of %s but expected is an instance of %s", actual.getClass(), expected.getClass());
        }
        try {
            DoubleArrayAssert.assertThat(actual.coordinates().toArray()).isCloseTo(expected.coordinates().toArray(), percentage, comparisonThreshold);
        } catch (AssertionError ae) {
            failWithMessage(
                    "Comparison failed for actual = %s and expected = %s; details: %s",
                    actual, expected, ae.getMessage()
            );
        }

        return this;
    }

    public void isCloseTo(
            V expectedVector,
            Percentage percentage0, Percentage percentage1, Percentage percentage2
    ) {
        double[] actualDoubles = actual.coordinates().toArray();
        double[] expectedDoubles = expectedVector.coordinates().toArray();
        assertSoftly(softly -> {
            assertIsCloseTo(softly, actualDoubles[0], expectedDoubles[0], percentage0);
            assertIsCloseTo(softly, actualDoubles[1], expectedDoubles[1], percentage1);
            assertIsCloseTo(softly, actualDoubles[2], expectedDoubles[2], percentage2);
        });
    }
}
