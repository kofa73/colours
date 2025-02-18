package kofa.colours.tools;

import com.google.common.util.concurrent.AtomicDouble;
import kofa.colours.spaces.CIExyY;
import kofa.colours.spaces.Rec2020;
import kofa.colours.spaces.Rec709;
import kofa.io.ImageLoader;
import kofa.io.PngOutput;
import kofa.io.RgbImage;
import kofa.maths.Solver;
import kofa.maths.ThanatomanicCurve6;

import java.util.Optional;

import static java.lang.Math.*;
import static kofa.colours.spaces.CIExyY.D65_WHITE_2DEG_x;
import static kofa.colours.spaces.CIExyY.D65_WHITE_2DEG_y;
import static kofa.colours.tools.CIExyYGamutBoundaries.LUMA_RESOLUTION;
import static kofa.colours.tools.CIExyYGamutBoundaries.POLAR_RESOLUTION;

public class SimpleXyzCompressor {
    public static void main(String[] args) {
        String baseName = args[0];
        RgbImage image = new ImageLoader().loadImageFrom(baseName);

        toneMapUsingY(image);

        compressGamut_in_xyY(image);

        new PngOutput().write(baseName + "-" + "yMapped", image);
    }

    private static void toneMapUsingY(RgbImage image) {
        AtomicDouble maxY_holder = new AtomicDouble(0);
        image.forEachPixel((int row, int column, double red, double green, double blue) -> {
            findMaxY((float) red, (float) green, (float) blue, maxY_holder);
        });
        double maxY = maxY_holder.get();

        double shoulderYStart = findOptimalShoulderStart(maxY);

        if (shoulderYStart != 1) {
            CurveBasedYCompressor yCompressor = new CurveBasedYCompressor(shoulderYStart);
            image.transformAllPixels(yCompressor);
        }
    }

    private static void findMaxY(float red, float green, float blue, AtomicDouble maxY_holder) {
        float[] valuesXYZ = rec2020_to_XYZ(red, green, blue);
        maxY_holder.getAndAccumulate(valuesXYZ[1], Math::max);
    }

    private static float[] rec2020_to_XYZ(float red, float green, float blue) {
        float[] valuesRec2020 = new float[] {red, green, blue};
        float[] valuesXYZ = new float[3];
        Rec2020.rec2020_to_XYZ(valuesRec2020, valuesXYZ);
        return valuesXYZ;
    }

    private static <S> double findOptimalShoulderStart(double maxValue) {
        double shoulderStart;
        if (maxValue <= 1) {
            System.out.println("Tone mapping is not needed, maxBrightness = " + maxValue);
            shoulderStart = 1;
        } else {
            var shoulderSearchLow = 0.99;
            Optional<Double> shoulder;
            do {
                var shoulderSolver = new Solver(currentShoulder -> {
                    var curve = ThanatomanicCurve6.linearUntil(currentShoulder);
                    double mappedValue = curve.mappedValueOf(maxValue);
                    return mappedValue < 0.99 ?
                            -1.0 :
                            mappedValue > 0.9999 ?
                                    1 :
                                    0;
                });
                shoulder = shoulderSolver.solve(shoulderSearchLow, shoulderSearchLow + 0.01, 0);
                if (shoulder.isEmpty()) {
                    shoulderSearchLow -= 0.01;
                }
            } while (shoulder.isEmpty() && shoulderSearchLow > 0.5);
            shoulderStart = shoulder.orElse(0.5);
            System.out.println("Will use shoulderStart = " + shoulderStart + " for tone mapping, maxBrightness = " + maxValue);
        }
        return shoulderStart;
    }

    private static class CurveBasedYCompressor implements RgbImage.PixelTransformer {
        private final ThanatomanicCurve6 curve;

        private CurveBasedYCompressor(double shoulder) {
            curve = ThanatomanicCurve6.linearUntil(shoulder);
        }

        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            float[] valuesXYZ = rec2020_to_XYZ((float) red, (float) green, (float) blue);
            valuesXYZ[1] = (float) curve.mappedValueOf(valuesXYZ[1]);
            var valuesRec2020 = new float[3];
            Rec2020.XYZ_to_rec2020(valuesXYZ, valuesRec2020);
            return new double[] {valuesRec2020[0], valuesRec2020[1], valuesRec2020[2]};
        }
    }

    private static void compressGamut_in_xyY(RgbImage image) {
        float[][] boundaries = new CIExyYGamutBoundaries().findRec709GamutBoundaries(16_384);

        AtomicDouble maxGamutCompressionHolder = new AtomicDouble(1);
        image.forEachPixel((int row, int column, double red, double green, double blue) ->
                    updateMaxGamutCompression((float) red, (float) green, (float) blue, maxGamutCompressionHolder, boundaries)
        );
        double maxGamutCompression = maxGamutCompressionHolder.get();
        System.out.println("max gamut compression: " + maxGamutCompression);

        double shoulderStart = findOptimalShoulderStart(maxGamutCompression);

        if (shoulderStart != 1) {
            var gamutCompressor = new CurveBased_xyY_gamutCompressor(shoulderStart, boundaries);
            image.transformAllPixels(gamutCompressor);
        }

    }

    private static final double PI2 = 2 * PI;

    private static void updateMaxGamutCompression(float red, float green, float blue, AtomicDouble maxGamutCompressionHolder, float[][] boundaries) {
        float[] valuesXYZ = rec2020_to_XYZ(red, green, blue);
        float[] values_xyY = new float[3];
        CIExyY.XZY_to_xyY(valuesXYZ, values_xyY);
        int indexY = min(round(values_xyY[2] * LUMA_RESOLUTION), LUMA_RESOLUTION);
        if (indexY != 0 && indexY != LUMA_RESOLUTION) {
            // not black and not white
            float x = values_xyY[0];
            float y = values_xyY[1];
            float dx = x - D65_WHITE_2DEG_x;
            float dy = y - D65_WHITE_2DEG_y;
            float distanceFromNeutral = (float) sqrt(dx * dx + dy * dy);
            if (distanceFromNeutral > 1e-4) {
                double angle = atan2(dy, dx);
                // may wrap around the circle because of the rounding, 0 radian vs 2*PI radian
                double normalisedAngle = ((angle < 0) ? (angle + PI2) : angle) / PI2;
                int indexPolar = (int) (round(normalisedAngle * POLAR_RESOLUTION) % POLAR_RESOLUTION);
                float maxDistanceFromNeutral = boundaries[indexY][indexPolar];
                float compression = distanceFromNeutral / maxDistanceFromNeutral;
                maxGamutCompressionHolder.accumulateAndGet(compression, Math::max);
            }
        }
    }

    private static class CurveBased_xyY_gamutCompressor implements RgbImage.PixelTransformer {
        private final ThanatomanicCurve6 curve;
        private final float[][] boundaries;

        private CurveBased_xyY_gamutCompressor(double shoulder, float[][] boundaries) {
            curve = ThanatomanicCurve6.linearUntil(shoulder);
            this.boundaries = boundaries;
        }

        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            /*
            float[] valuesXYZ = rec2020_to_XYZ((float) red, (float) green, (float) blue);
            valuesXYZ[1] = (float) curve.mappedValueOf(valuesXYZ[1]);
            var valuesRec2020 = new float[3];
            Rec2020.XYZ_to_rec2020(valuesXYZ, valuesRec2020);
            return new double[] {valuesRec2020[0], valuesRec2020[1], valuesRec2020[2]};

             */
            float[] valuesXYZ = rec2020_to_XYZ((float) red, (float) green, (float) blue);
            float[] values_xyY = new float[3];
            CIExyY.XZY_to_xyY(valuesXYZ, values_xyY);
            int indexY = min(round(values_xyY[2] * LUMA_RESOLUTION), LUMA_RESOLUTION);
            if (indexY != 0 && indexY != LUMA_RESOLUTION) {
                // not black and not white
                float x = values_xyY[0];
                float y = values_xyY[1];
                float dx = x - D65_WHITE_2DEG_x;
                float dy = y - D65_WHITE_2DEG_y;
                float distanceFromNeutral = (float) sqrt(dx * dx + dy * dy);
                if (distanceFromNeutral > 1e-4) {
                    double angle = atan2(dy, dx);
                    // may wrap around the circle because of the rounding, 0 radian vs 2*PI radian
                    double normalisedAngle = ((angle < 0) ? (angle + PI2) : angle) / PI2;
                    int indexPolar = (int) (round(normalisedAngle * POLAR_RESOLUTION) % POLAR_RESOLUTION);
                    float maxDistanceFromNeutral = boundaries[indexY][indexPolar];
                    float ratioFromMax = distanceFromNeutral / maxDistanceFromNeutral;
                    double compressedRatioFromMax = curve.mappedValueOf(ratioFromMax);
                    double compressedDistance = distanceFromNeutral * compressedRatioFromMax;
                    values_xyY[0] = (float) (compressedDistance * cos(angle)) + D65_WHITE_2DEG_x;
                    values_xyY[1] = (float) (compressedDistance * sin(angle)) + D65_WHITE_2DEG_y;
                }
            } else {
                values_xyY[0] = D65_WHITE_2DEG_x;
                values_xyY[1] = D65_WHITE_2DEG_y;
            }
            CIExyY.xyY_to_XYZ(values_xyY, valuesXYZ);
            var rec709 = new float[3];
            Rec709.XYZ_to_rec709(valuesXYZ, rec709);
            if (
                    rec709[0] < 0 || rec709[0] > 1
                    || rec709[1] < 0 || rec709[1] > 1
                    || rec709[2] < 0 || rec709[2] > 1
            ) {
                System.out.println("out");
            }
            return new double[] {rec709[0], rec709[1], rec709[2]};
        }
    }
}
