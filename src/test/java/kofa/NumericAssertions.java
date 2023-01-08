package kofa;

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
