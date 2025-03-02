package kofa.colours.tools;

import kofa.io.RgbImage;

import static java.lang.Math.min;
import static kofa.maths.MathHelpers.*;

// see https://modelviewer.dev/examples/tone-mapping for the design; some sections quoted below (in comments: "design")
// see https://github.com/KhronosGroup/ToneMapping/blob/main/PBR_Neutral/README.md (in comments: "Readme")
public class PbrNeutralPixelTransformer implements RgbImage.PixelTransformer {
    /* design: [...] for a common dielectric material with index of refraction of 1.5, the normal Fresnel
       reflection adds 4% of the incident light color (the highlight) to the material's colored diffuse reflection.
       Assuming the lighting is even and white, this leads to a 4% desaturation of the rendered color as compared
       to the baseColor. This is physically correct, but confusing from a color-management perspective.

       I correct our saturation by shifting the 1:1 portion of our tone mapping curve down by 0.04.
     */
    // Readme: F90 = 0.04 , Fresnel reflection at normal incidence of common IoR = 1.5 materials
    private static final double FRESNEL_OFFSET_CORRECTION = 0.04;
    /* design: I chose to fit a simple 1/x function and match the piecewise slope of our 1:1 portion, as this gives an
       asymptote with a reasonable tail. It has only a single parameter: the value where we switch from the linear to
       the nonlinear function. The purpose of the Khronos PBR Neutral tone mapper is to be a standard and thus without
       parameters, so I chose 0.8 after much testing.
     */
    private static final double COMPRESSION_THRESHOLD = 0.8;
    // Readme:  Ks = 0.8 − F90 , parameter controlling when highlight compression starts.
    private static final double START_COMPRESSION = COMPRESSION_THRESHOLD - FRESNEL_OFFSET_CORRECTION;
    private static final double D = 1 - START_COMPRESSION;
    private static final double D_SQUARED = D * D;

    //  Readme: Kd = 0.15 , parameter controlling the speed of desaturation.
    private static final double DESATURATION = 0.15;

    @Override
    public double[] transform(int row, int column, double red, double green, double blue) {
        double x = min(red, min(green, blue));
        // Readme: x − x^2 / (4 * F90) -> 1 / (4 * 0.04) = 6.25
        double offset = (x < 2 * FRESNEL_OFFSET_CORRECTION) ? x - 6.25 * x * x : FRESNEL_OFFSET_CORRECTION;

        double[] color = vec3(red - offset, green - offset, blue - offset);

        // Readme: p = max (R − f, G − f, B − f)
        double peak = maxComponentValue(color);

        if (peak < START_COMPRESSION) {
            return color;
        }

        //  pn = 1 − (1 − Ks)^2 / (p + 1 − 2 * Ks)
        double newPeak = 1 - D_SQUARED / (peak + D - START_COMPRESSION);
        color = mul(color, newPeak / peak);

        /* design: The final piece is to create the path to white for desaturating bright highlights. [..]
         * I accomplish this by taking a convex combination of the compressed color and white, only in the nonlinear
         * compression region. In fact, "white" is slightly darkened in order to maintain constant brightness with the
         * compressed color [...] The convex parameter function I chose is another 1/x, this time based on the amount
         * of brightness removed in the compression step, which ensures it starts smoothly with a zero derivative where
         * it begins to take effect. The only other parameter in this tone mapper controls the rate of desaturation,
         * which I chose as 0.15
         */
        double g = 1 / (DESATURATION * (peak - newPeak) + 1);
        double mixFactor = 1 - g;
        return mix(color, vec3(newPeak), mixFactor);
    }
}
