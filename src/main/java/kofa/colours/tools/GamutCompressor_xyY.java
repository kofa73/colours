package kofa.colours.tools;

import com.google.common.util.concurrent.AtomicDouble;
import kofa.colours.spaces.CIExyY;
import kofa.colours.spaces.Rec2020;
import kofa.colours.spaces.Rec709;
import kofa.colours.spaces.SpaceParameters;
import kofa.io.RgbImage;
import kofa.maths.ThanatomanicCurve6;

import static java.lang.Math.*;
import static kofa.colours.spaces.CIExyY.D65_WHITE_2DEG_x;
import static kofa.colours.spaces.CIExyY.D65_WHITE_2DEG_y;
import static kofa.colours.tools.CurveSolver.findOptimalShoulderStart;
import static kofa.maths.MathHelpers.vec3;

public class GamutCompressor_xyY {
    private static final double PI2 = 2 * PI;

    private final double[][] boundaries;
    private final int lumaResolution;
    private final int chromaResolution;

    public static GamutCompressor_xyY forRec709(int lumaResolution, int chromaResolution) {
        return new GamutCompressor_xyY(lumaResolution, chromaResolution, Rec709.PARAMS);
    }

    public static GamutCompressor_xyY forRec2020(int lumaResolution, int chromaResolution) {
        return new GamutCompressor_xyY(lumaResolution, chromaResolution, Rec2020.PARAMS);
    }

    public GamutCompressor_xyY(int lumaResolution, int chromaResolution, SpaceParameters spaceParameters) {
        this.lumaResolution = lumaResolution;
        this.chromaResolution = chromaResolution;
        boundaries = CIExyYGamutBoundariesFinder.findRgbGamutBoundaries(lumaResolution, chromaResolution, spaceParameters);
    }

    public void compressGamut_in_xyY(RgbImage image) {
        AtomicDouble maxGamutCompressionHolder = new AtomicDouble(1);
        image.forEachPixel((int row, int column, double red, double green, double blue) ->
                updateMaxGamutCompression(red, green, blue, maxGamutCompressionHolder, boundaries)
        );

        double shoulderStart = findOptimalShoulderStart(maxGamutCompressionHolder.get());

        if (shoulderStart != 1) {
            var gamutCompressor = new CurveBased_xyY_gamutCompressor(shoulderStart, boundaries);
            image.transformAllPixels(gamutCompressor);
        }
    }

    private void updateMaxGamutCompression(double red, double green, double blue, AtomicDouble maxGamutCompressionHolder, double[][] boundaries) {
        double[] valuesXYZ = rec2020_to_XYZ(red, green, blue);
        if (valuesXYZ[1] > 0.01 && valuesXYZ[1] < 1 - 0.01) {
            double[] values_xyY = vec3();
            CIExyY.XZY_to_xyY(valuesXYZ, values_xyY);
            int indexY = (int) min(round(values_xyY[2] * lumaResolution), lumaResolution);
            if (indexY != 0 && indexY != lumaResolution) {
                // not black and not white
                double x = values_xyY[0];
                double y = values_xyY[1];
                double dx = x - D65_WHITE_2DEG_x;
                double dy = y - D65_WHITE_2DEG_y;
                double distanceFromNeutral = sqrt(dx * dx + dy * dy);
                if (distanceFromNeutral > 1e-4) {
                    double angle = atan2(dy, dx);
                    // may wrap around the circle because of the rounding, 0 radian vs 2*PI radian
                    double normalisedAngle = ((angle < 0) ? (angle + PI2) : angle) / PI2;
                    int indexPolar = (int) (round(normalisedAngle * chromaResolution) % chromaResolution);
                    double maxDistanceFromNeutral = boundaries[indexY][indexPolar];
                    if (maxDistanceFromNeutral > 1e-4) {
                        double compression = distanceFromNeutral / maxDistanceFromNeutral;
                        maxGamutCompressionHolder.accumulateAndGet(compression, Math::max);
                    }
                }
            }
        }
    }

    private static class CurveBased_xyY_gamutCompressor implements RgbImage.PixelTransformer {
        private final ThanatomanicCurve6 curve;
        private final double[][] boundaries;
        private final int lumaResolution;
        private final int chromaResolution;

        private CurveBased_xyY_gamutCompressor(double shoulder, double[][] boundaries) {
            curve = ThanatomanicCurve6.linearUntil(shoulder);
            this.boundaries = boundaries;
            this.lumaResolution = boundaries.length;
            this.chromaResolution = boundaries[0].length;
        }

        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            double[] valuesXYZ = rec2020_to_XYZ(red, green, blue);
            double[] values_xyY = vec3();
            CIExyY.XZY_to_xyY(valuesXYZ, values_xyY);
            int indexY = (int) min(round(values_xyY[2] * lumaResolution), lumaResolution);
            if (indexY != 0 && indexY != lumaResolution) {
                // not black and not white
                double x = values_xyY[0];
                double y = values_xyY[1];
                double dx = x - D65_WHITE_2DEG_x;
                double dy = y - D65_WHITE_2DEG_y;
                double distanceFromNeutral = sqrt(dx * dx + dy * dy);
                if (distanceFromNeutral > 0) {
                    double angle = atan2(dy, dx);
                    // may wrap around the circle because of the rounding, 0 radian vs 2*PI radian
                    double normalisedAngle = ((angle < 0) ? (angle + PI2) : angle) / PI2;
                    int indexPolar = (int) (round(normalisedAngle * chromaResolution) % chromaResolution);
                    double maxDistanceFromNeutral = boundaries[indexY][indexPolar];
                    double ratioFromMax = distanceFromNeutral / maxDistanceFromNeutral;
                    double compressedRatioFromMax = curve.mappedValueOf(ratioFromMax);
                    double compressedDistance = maxDistanceFromNeutral * compressedRatioFromMax;
                    values_xyY[0] = (compressedDistance * cos(angle)) + D65_WHITE_2DEG_x;
                    values_xyY[1] = (compressedDistance * sin(angle)) + D65_WHITE_2DEG_y;
                }
            } else {
                values_xyY[0] = D65_WHITE_2DEG_x;
                values_xyY[1] = D65_WHITE_2DEG_y;
            }
            CIExyY.xyY_to_XYZ(values_xyY, valuesXYZ);
            var rec709 = vec3();
            Rec709.XYZ_to_rec709(valuesXYZ, rec709);
            // clip away any minor remaining under/overshoot
            rec709[0] = clamp(rec709[0], 0, 1);
            rec709[1] = clamp(rec709[1], 0, 1);
            rec709[2] = clamp(rec709[2], 0, 1);
            return rec709;
        }
    }

    private static double[] rec2020_to_XYZ(double red, double green, double blue) {
        double[] valuesRec2020 = new double[] {red, green, blue};
        double[] valuesXYZ = new double[3];
        Rec2020.rec2020_to_XYZ(valuesRec2020, valuesXYZ);
        return valuesXYZ;
    }
}
