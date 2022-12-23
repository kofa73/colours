package kofa.maths;

import static com.google.common.base.Preconditions.checkArgument;

// https://discuss.pixls.us/t/tone-curve-math-question/28978/6
public class Curve {
    private final double gradient;
    private final double shoulderStart;
    private final double mappedValueOfShoulderStart;
    private final double mappedValueOfShoulderStartMinusOne;

    public static Curve linearUntil(double shoulderStart) {
        return new Curve(1, shoulderStart);
    }

    public Curve(double gradient, double shoulderStart) {
        checkArgument(gradient > 0, "gradient must be > 0");
        checkArgument(shoulderStart >= 0, "shoulderStart must be >= 0");
        double shoulderStartUpperBound = 1 / gradient;
        checkArgument(shoulderStart < shoulderStartUpperBound, "shoulderStart must be < 1 / gradient = %s", shoulderStartUpperBound);
        this.gradient = gradient;
        this.shoulderStart = shoulderStart;
        mappedValueOfShoulderStart = gradient * shoulderStart;
        mappedValueOfShoulderStartMinusOne = mappedValueOfShoulderStart - 1;
    }

    public double mappedValueOf(double x) {
        if (x < shoulderStart) {
            return gradient * x;
        }

        return 1 + mappedValueOfShoulderStartMinusOne * Math.exp((gradient * x - mappedValueOfShoulderStart) / mappedValueOfShoulderStartMinusOne);
    }
}
