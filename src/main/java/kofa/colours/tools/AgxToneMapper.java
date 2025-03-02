package kofa.colours.tools;

import static kofa.maths.MathHelpers.*;

// based on https://github.com/google/filament/blob/main/filament/src/ToneMapper.cpp#L263
// https://iolite-engine.com/blog_posts/minimal_agx_implementation
// and https://www.shadertoy.com/view/cd3XWr
public class AgxToneMapper {
    private static final double[] lw = {0.2126, 0.7152, 0.0722};
    // 'Look' params
    public static final double[] OFFSET = {0, 0, 0};

    public static final double[] GOLDEN_SLOPE = {1.0, 0.9, 0.5};
    public static final double[] GOLDEN_POWER = {0.8, 0.8, 0.8};
    public static final double GOLDEN_SAT = 1.3;

    public static final double[] PUNCHY_SLOPE = {1.0, 1.0, 1.0};
    public static final double[] PUNCHY_POWER = {1.35, 1.35, 1.35};
    public static final double PUNCHY_SAT = 1.4;

    private static double[] agxAscCdl(double[] color, double[] slope, double[] offset, double[] power, double sat) {
        double luma = dot3(color, lw);
        color = pow(add(mul(color, slope), offset), power);
        return add(luma, mul(sat, sub(color, luma)));
    }

    private static final double AgxMinEv = -12.47393;
    private static final double AgxMaxEv = 4.026069;

    public static final double[] GAMMA_2_2 = {2.2, 2.2, 2.2};

    // 0: Default, 1: Golden, 2: Punchy
    public enum Look {
        DEFAULT, GOLDEN, PUNCHY
    }

    // in/out: linear Rec2020
    public static double[] agx(double r, double g, double b, Look look) {
        double[] color = inset(r, g, b);

        color = convertToLogSpace(color);

        applySigmoid(color);

        color = applyLook(look, color);

        color = outSet(color);
        return linearise(color);
    }

    private static double[] linearise(double[] color) {
        color = clamp(color, 0, 1);
        return pow(color, GAMMA_2_2);
    }

    private static double[] outSet(double[] color) {
        // 3. agxEotf()
        // Inverse input transform (outset)
        color = new double[] {
                 1.1271005818144368  * color[0] - 0.11060664309660323 * color[1] - 0.016493938717834573 * color[2],
                -0.1413297634984383  * color[0] + 1.157823702216272   * color[1] - 0.016493938717834257 * color[2],
                -0.14132976349843826 * color[0] - 0.11060664309660294 * color[1] + 1.2519364065950405  * color[2]
        };
        return color;
    }

    private static void applySigmoid(double[] color) {
        color[0] = sigmoid(color[0]);
        color[1] = sigmoid(color[1]);
        color[2] = sigmoid(color[2]);
    }

    private static double[] convertToLogSpace(double[] color) {
        ensureMinimum(color, 1e-10);

        // Log2 space encoding
        log2InPlace(color);
        clampInPlace(color, AgxMinEv, AgxMaxEv);
        color = div(sub(color, AgxMinEv), (AgxMaxEv - AgxMinEv));

        clampInPlace(color, 0.0, 1.0);
        return color;
    }

    private static double[] inset(double r, double g, double b) {
        // Input transform (inset)

        double insetR = 0.856627153315983  * r + 0.0951212405381588 * g + 0.0482516061458583   * b;
        double insetG = 0.137318972929847 * r + 0.761241990602591 * g + 0.101439036467562 * b;
        double insetB = 0.11189821299995 * r + 0.0767994186031903 * g + 0.811302368396859  * b;

        double[] color = new double[]{insetR, insetG, insetB};
        return color;
    }

    private static double[] applyLook(Look look, double[] color) {
        color = switch (look) {
            case DEFAULT -> color;
            case GOLDEN -> agxAscCdl(color, GOLDEN_SLOPE, OFFSET, GOLDEN_POWER, GOLDEN_SAT);
            case PUNCHY -> agxAscCdl(color, PUNCHY_SLOPE, OFFSET, PUNCHY_POWER, PUNCHY_SAT);
        };
        return color;
    }

    private static double sigmoid(double x) {
        double x2 = x * x;
        double x4 = x2 * x2;

        return 15.5 * x4 * x2
                - 40.14 * x4 * x
                + 31.96 * x4
                - 6.868 * x2 * x
                + 0.4298 * x2
                + 0.1191 * x
                - 0.00232;
    }
}
