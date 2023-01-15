package kofa;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Percentage;

import static java.lang.Math.*;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.data.Offset.offset;

public class NumericAssertions {
    public static final Percentage EXACT = Percentage.withPercentage(1E-5);
    public static final Percentage PRECISE = Percentage.withPercentage(0.06);
    public static final Percentage LENIENT = Percentage.withPercentage(0.2);
    public static final Percentage ROUGH = Percentage.withPercentage(5);
    public static final double DEFAULT_COMPARISON_THRESHOLD = 1E-14;

    public static void assertDegreesAreClose(double actualDegrees, double expectedDegrees) {
        try {
            assertRadiansAreClose(toRadians(actualDegrees), toRadians(expectedDegrees));
        } catch (AssertionError ae) {
            throw new AssertionError("Expecting actual degrees: " + actualDegrees + " to be close to " + expectedDegrees, ae);
        }
    }

    public static void assertRadiansAreClose(double actualRadians, double expectedRadians) {
        try {
            assertIsCloseTo(sin(actualRadians), sin(expectedRadians));
            assertIsCloseTo(cos(actualRadians), cos(expectedRadians));
        } catch (AssertionError ae) {
            throw new AssertionError("Expecting actual: " + actualRadians + " to be close to " + expectedRadians, ae);
        }
    }

    public static void assertIsCloseTo(double actualValue, double expectedValue) {
        assertSoftly(softly -> assertIsCloseTo(softly, actualValue, expectedValue, EXACT));
    }


    public static void assertIsCloseTo(SoftAssertions softly, double actualValue, double expectedValue, Percentage percentage) {
        assertIsCloseTo(softly, actualValue, expectedValue, percentage, DEFAULT_COMPARISON_THRESHOLD);
    }

    public static void assertIsCloseTo(SoftAssertions softly, double actualValue, double expectedValue, Percentage percentage, double comparisonThreshold) {
        if (actualValue == 0) {
            softly.assertThat(expectedValue)
                    .overridingErrorMessage("Expected %s to be close to %s", actualValue, expectedValue)
                    .isCloseTo(0, offset(comparisonThreshold));
        } else if (expectedValue == 0) {
            softly.assertThat(actualValue).isCloseTo(0, offset(comparisonThreshold));
        } else {
            softly.assertThat(actualValue).isCloseTo(expectedValue, percentage);
        }
    }
}
