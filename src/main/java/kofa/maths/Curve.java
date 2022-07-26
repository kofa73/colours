package kofa.maths;

import org.checkerframework.checker.units.qual.C;

import static com.google.common.base.Preconditions.checkArgument;

// https://discuss.pixls.us/t/tone-curve-math-question/28978/6
public class Curve {
    private final float gradient;
    private final float shoulderStart;
    private final float mappedValueOfShoulderStart;
    private final float mappedValueOfShoulderStartMinusOne;

    public static Curve linearUntil(float shoulderStart) {
        return new Curve(1, shoulderStart);
    }

    public Curve(float gradient, float shoulderStart) {
        checkArgument(gradient > 0f, "gradient must be > 0");
        checkArgument(shoulderStart >= 0f, "shoulderStart must be >= 0");
        float shoulderStartUpperBound = 1 / gradient;
        checkArgument(shoulderStart < shoulderStartUpperBound, "shoulderStart must be < 1 / gradient = %s", shoulderStartUpperBound);
        this.gradient = gradient;
        this.shoulderStart = shoulderStart;
        mappedValueOfShoulderStart = gradient * shoulderStart;
        mappedValueOfShoulderStartMinusOne = mappedValueOfShoulderStart - 1;
    }

    public float mappedValueOf(float x) {
        if (x < shoulderStart) {
            return gradient * x;
        }

        return (float) (1 + mappedValueOfShoulderStartMinusOne * Math.exp((gradient * x - mappedValueOfShoulderStart) / mappedValueOfShoulderStartMinusOne));
    }
}
