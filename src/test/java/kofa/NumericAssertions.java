package kofa;

import kofa.maths.Vector3;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Percentage;

import java.util.Arrays;

import static java.lang.Math.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class NumericAssertions {
    public static final Percentage EXACT = Percentage.withPercentage(1E-5);
    public static final Percentage PRECISE = Percentage.withPercentage(0.06);
    public static final Percentage LENIENT = Percentage.withPercentage(0.2);
    public static final Percentage ROUGH = Percentage.withPercentage(5);
    private static final double COMPARISON_THRESHOLD = 1E-15;

    public static <V extends Vector3> void assertIsCloseTo(V actualVector, V expectedVector, Percentage percentage) {
        assertIsCloseTo(actualVector, expectedVector, percentage, COMPARISON_THRESHOLD);
    }

    public static <V extends Vector3> void assertIsCloseTo(V actualVector, V expectedVector, Percentage percentage, double comparisonThreshold) {
        assertThat(actualVector).hasSameClassAs(expectedVector);
        try {
            assertIsCloseTo(actualVector.coordinates().toArray(), expectedVector.coordinates().toArray(), percentage, comparisonThreshold);
        } catch (AssertionError ae) {
            throw new AssertionError(
                    "Comparison failed for actual = %s and expected = %s".formatted(
                            actualVector, expectedVector
                    ), ae
            );
        }
    }

    public static void assertIsCloseTo(double[] actualVector, double[] expectedVector, Percentage percentage) {
        assertIsCloseTo(actualVector, expectedVector, percentage, COMPARISON_THRESHOLD);
    }

    public static void assertIsCloseTo(double[] actualVector, double[] expectedVector, Percentage percentage, double comparisonThreshold) {
        assertThat(actualVector).hasSameSizeAs(expectedVector);
        try {
            assertSoftly(softly -> {
                        for (int row = 0; row < actualVector.length; row++) {
                            assertIsCloseTo(softly, actualVector[row], expectedVector[row], percentage, comparisonThreshold);
                        }
                    }
            );
        } catch (AssertionError ae) {
            throw new AssertionError(
                    "Comparison failed for actual = %s and expected = %s".formatted(
                            Arrays.toString(actualVector), Arrays.toString(expectedVector)
                    ), ae
            );
        }
    }

    public static <V extends Vector3> void assertIsCloseTo(
            V actualVector, V expectedVector,
            Percentage percentage0, Percentage percentage1, Percentage percentage2
    ) {
        double[] actualDoubles = actualVector.coordinates().toArray();
        double[] expectedDoubles = expectedVector.coordinates().toArray();
        assertSoftly(softly -> {
            assertIsCloseTo(softly, actualDoubles[0], expectedDoubles[0], percentage0, COMPARISON_THRESHOLD);
            assertIsCloseTo(softly, actualDoubles[1], expectedDoubles[1], percentage1, COMPARISON_THRESHOLD);
            assertIsCloseTo(softly, actualDoubles[2], expectedDoubles[2], percentage2, COMPARISON_THRESHOLD);
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
            assertIsCloseTo(sin(actualRadians), sin(expectedRadians), EXACT, COMPARISON_THRESHOLD);
            assertIsCloseTo(cos(actualRadians), cos(expectedRadians), EXACT, COMPARISON_THRESHOLD);
        } catch (AssertionError ae) {
            throw new AssertionError("Expecting actual: " + actualRadians + " to be close to " + expectedRadians, ae);
        }
    }

    // don't compare tiny numbers, numerical imprecision could cause test failures
    private static void assertIsCloseTo(double actualValue, double expectedValue, Percentage percentage, double comparisonThreshold) {
        if (Math.abs(actualValue) > comparisonThreshold || Math.abs(expectedValue) > comparisonThreshold) {
            assertThat(actualValue).isCloseTo(expectedValue, percentage);
        }
    }

    private static void assertIsCloseTo(SoftAssertions softly, double actualValue, double expectedValue, Percentage percentage, double comparisonThreshold) {
        if (Math.abs(actualValue) > comparisonThreshold || Math.abs(expectedValue) > comparisonThreshold) {
            softly.assertThat(actualValue).isCloseTo(expectedValue, percentage);
        }
    }
}
