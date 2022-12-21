package kofa;

import kofa.maths.DoubleVector;
import kofa.maths.Vector3D;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Percentage;

import static java.lang.Math.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class NumericAssertions {
    public static final Percentage PRECISE = Percentage.withPercentage(0.06);
    public static final Percentage LENIENT = Percentage.withPercentage(0.25);
    public static final Percentage ROUGH = Percentage.withPercentage(5);
    private static final double COMPARISON_THRESHOLD = 1E-6;

    public static <V extends DoubleVector> void assertIsCloseTo(V actualVector, V expectedVector, Percentage percentage) {
        assertIsCloseTo(actualVector.coordinates(), expectedVector.coordinates(), percentage);
    }

    public static void assertIsCloseTo(double[] actualVector, double[] expectedVector, Percentage percentage) {
        assertThat(actualVector).hasSameSizeAs(expectedVector);
        assertSoftly(softly -> {
                    for (int row = 0; row < actualVector.length; row++) {
                        assertIsCloseTo(softly, actualVector[row], expectedVector[row], percentage);
                    }
                }
        );
    }

    public static <V extends Vector3D> void assertIsCloseTo(
            V actualVector, V expectedVector,
            Percentage percentage0, Percentage percentage1, Percentage percentage2
    ) {
        double[] actualDoubles = actualVector.coordinates();
        double[] expectedDoubles = expectedVector.coordinates();
        assertSoftly(softly -> {
            assertIsCloseTo(softly, actualDoubles[0], expectedDoubles[0], percentage0);
            assertIsCloseTo(softly, actualDoubles[1], expectedDoubles[1], percentage1);
            assertIsCloseTo(softly, actualDoubles[2], expectedDoubles[2], percentage2);
        });
    }

    public static void assertDegreesAreClose(double actualDegrees, double expectedDegrees) {
        try {
            assertRadiansAreClose(toRadians(actualDegrees), toRadians(expectedDegrees));
        } catch (AssertionError ae) {
            throw new AssertionError("Expecting actual degrees: " + actualDegrees + " to be close to " + expectedDegrees, ae);
        }
    }

    public static void assertRadiansAreClose(double actualRadians, double expectedRadians) {
        try {
            assertIsCloseTo(sin(actualRadians), sin(expectedRadians), PRECISE);
            assertIsCloseTo(cos(actualRadians), cos(expectedRadians), PRECISE);
        } catch (AssertionError ae) {
            throw new AssertionError("Expecting actual: " + actualRadians + " to be close to " + expectedRadians, ae);
        }
    }

    // don't compare tiny numbers, numerical imprecision could cause test failures
    private static void assertIsCloseTo(double actualValue, double expectedValue, Percentage percentage) {
        if (Math.abs(actualValue) > COMPARISON_THRESHOLD || Math.abs(expectedValue) > COMPARISON_THRESHOLD) {
            assertThat(actualValue).isCloseTo(expectedValue, percentage);
        }
    }

    private static void assertIsCloseTo(SoftAssertions softly, double actualValue, double expectedValue, Percentage percentage) {
        if (Math.abs(actualValue) > COMPARISON_THRESHOLD || Math.abs(expectedValue) > COMPARISON_THRESHOLD) {
            softly.assertThat(actualValue).isCloseTo(expectedValue, percentage);
        }
    }
}
