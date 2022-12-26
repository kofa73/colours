package kofa.maths;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.Math.min;

// https://discuss.pixls.us/t/tone-curve-math-question/28978/6
public class ThanatomanicCurve6 implements Curve {
    private final double gradient;
    private final double shoulderStart;
    private final double mappedValueOfShoulderStart;
    private final double mappedValueOfShoulderStartMinusOne;

    public static ThanatomanicCurve6 linearUntil(double shoulderStart) {
        return new ThanatomanicCurve6(1, shoulderStart);
    }

    public ThanatomanicCurve6(double gradient, double shoulderStart) {
        checkArgument(gradient > 0, "gradient must be > 0");
        checkArgument(shoulderStart >= 0, "shoulderStart must be >= 0");
        double shoulderStartUpperBound = 1 / gradient;
        checkArgument(shoulderStart < shoulderStartUpperBound, "shoulderStart must be < 1 / gradient = %s", shoulderStartUpperBound);
        this.gradient = gradient;
        this.shoulderStart = shoulderStart;
        mappedValueOfShoulderStart = gradient * shoulderStart;
        mappedValueOfShoulderStartMinusOne = mappedValueOfShoulderStart - 1;
    }

    @Override
    public double mappedValueOf(double x) {
        if (x < shoulderStart) {
            return gradient * x;
        }
        // tiny numbers may cause small maths errors, and a value greater than the input being calculated, so use 'min'
        return min(
                x,
                1 + mappedValueOfShoulderStartMinusOne * Math.exp((gradient * x - mappedValueOfShoulderStart) / mappedValueOfShoulderStartMinusOne)
        );
    }
}
