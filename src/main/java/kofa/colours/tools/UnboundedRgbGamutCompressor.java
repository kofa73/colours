package kofa.colours.tools;

import kofa.io.RgbImage;

import static java.lang.Math.max;
import static java.lang.Math.*;
import static kofa.maths.MathHelpers.*;

public class UnboundedRgbGamutCompressor {
    private static final double cyan = 0.3;
    private static final double magenta = 0.3;
    private static final double yellow = 0.3;
    private static final double threshold = 0.2;
    // Amount of outer gamut to affect
    private static final double[] th = vec3(1.0 - threshold);
    // Distance limit: How far beyond the gamut boundary to compress
    private static final double[] dl = add(vec3(cyan, magenta, yellow), 1);

    public static void compressGamut_in_xyY(RgbImage image) {
        image.transformAllPixels(UnboundedRgbGamutCompressor::transformPixel);
    }

    public static double[] compressPixel(double[] rgb) {
        return transformPixel(0, 0, rgb[0], rgb[1], rgb[2]);
    }

    private static double[] transformPixel(int row, int column, double red, double green, double blue) {
        double[] rgb = vec3(red, green, blue);

        // Calculate scale so compression function passes through distance limit: (x=dl, y=1)
        double[] s = vec3(
                (1.0 - th[0]) / sqrt(max(1.001, dl[0]) - 1.0),
                (1.0 - th[1]) / sqrt(max(1.001, dl[1]) - 1.0),
                (1.0 - th[2]) / sqrt(max(1.001, dl[2]) - 1.0)
        );

        // Achromatic axis
        double ac = max(red, max(green, blue));
        // Inverse RGB Ratios: distance from achromatic axis
        double[] d = ac == 0 ? vec3(0) : div(sub(vec3(ac), rgb), ac);

        double[] cd = vec3(
                d[0] < th[0] ? d[0] : s[0] * sqrt(d[0] - th[0] + s[0] * s[0] / 4.0) - s[0] * sqrt(s[0] * s[0] / 4.0) + th[0],
                d[1] < th[1] ? d[1] : s[1] * sqrt(d[1] - th[1] + s[1] * s[1] / 4.0) - s[1] * sqrt(s[1] * s[1] / 4.0) + th[1],
                d[2] < th[2] ? d[2] : s[2] * sqrt(d[2] - th[2] + s[2] * s[2] / 4.0) - s[2] * sqrt(s[2] * s[2] / 4.0) + th[2]
        );

        // Inverse RGB Ratios to RGB
        double[] crgb = sub(vec3(ac), mul(cd, abs(ac)));

        return crgb;
    }
}
