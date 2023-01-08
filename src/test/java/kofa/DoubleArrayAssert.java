package kofa;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.data.Percentage;

import java.util.Arrays;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

public class DoubleArrayAssert extends AbstractAssert<DoubleArrayAssert, double[]> {
    public static final Percentage EXACT = Percentage.withPercentage(1E-5);
    public static final Percentage PRECISE = Percentage.withPercentage(0.06);
    public static final Percentage LENIENT = Percentage.withPercentage(0.2);
    public static final Percentage ROUGH = Percentage.withPercentage(5);
    private static final double COMPARISON_THRESHOLD = 1E-15;

    private DoubleArrayAssert(double[] actual) {
        super(actual, DoubleArrayAssert.class);
    }

    public static DoubleArrayAssert assertThat(double[] actual) {
        return new DoubleArrayAssert(actual);
    }

    public void isCloseTo(double[] expected, Percentage percentage) {
        isCloseTo(expected, percentage, COMPARISON_THRESHOLD);
    }

    public void isCloseTo(double[] expected, Percentage percentage, double comparisonThreshold) {
        isNotNull();
        Assertions.assertThat(actual).hasSameSizeAs(expected);
        try {
            assertSoftly(softly -> {
                        for (int row = 0; row < actual.length; row++) {
                            isCloseTo(softly, actual[row], expected[row], percentage, comparisonThreshold);
                        }
                    }
            );
        } catch (AssertionError ae) {
            failWithMessage(
                    "Comparison failed for actual = %s and expected = %s, details: %s",
                    Arrays.toString(actual), Arrays.toString(expected), ae.getMessage()
            );
        }
    }

    // don't compare tiny numbers, numerical imprecision could cause test failures
    private void isCloseTo(double actualValue, double expectedValue, Percentage percentage, double comparisonThreshold) {
        if (Math.abs(actualValue) > comparisonThreshold || Math.abs(expectedValue) > comparisonThreshold) {
            Assertions.assertThat(actualValue).isCloseTo(expectedValue, percentage);
        }
    }

    private void isCloseTo(SoftAssertions softly, double actualValue, double expectedValue, Percentage percentage, double comparisonThreshold) {
        if (Math.abs(actualValue) > comparisonThreshold || Math.abs(expectedValue) > comparisonThreshold) {
            softly.assertThat(actualValue).isCloseTo(expectedValue, percentage);
        }
    }
}
