package kofa.colours.tools;

import kofa.colours.spaces.CIExyY;
import kofa.colours.spaces.Rec709;
import kofa.io.Png16Output;
import kofa.io.RgbImage;
import kofa.maths.ThanatomanicCurve6;

import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static kofa.colours.tools.CIExyYGamutBoundariesFinder.findRgbGamutBoundariesForRec2020;
import static kofa.colours.tools.CIExyYGamutBoundariesFinder.findRgbGamutBoundariesForRec709;
import static kofa.maths.MathHelpers.vec3;

public class TestImageGeneratorRec2020ToRec709YSeries {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;
    private static final double TWO_PI = 2 * Math.PI;
    private static final double MAX_LUMA_Y = 1;

    public static void main(String[] args) {
        var raster = WritableRaster.createInterleavedRaster(DataBuffer.TYPE_USHORT, WIDTH, HEIGHT, 3, new Point());
        var image = new RgbImage(raster);

        int polarSteps = HEIGHT;
        double polarStepSize = TWO_PI / polarSteps;
        double[][] rec709GamutBoundaries = findRgbGamutBoundariesForRec709(100, polarSteps);
        double[][] rec2020GamutBoundaries = findRgbGamutBoundariesForRec2020(100, polarSteps);

        var curve = ThanatomanicCurve6.linearUntil(0.8);
        double[] XYZ = vec3();
        double[] rec709 = vec3();
        for (int percent = 10; percent < 100; percent += 10) {
            double Y = percent / 100.0;
            for (int polarIndex = 0; polarIndex < polarSteps; polarIndex++) {
                double angle = polarIndex * polarStepSize;
                double dx = cos(angle);
                double dy = sin(angle);
                double[] xyY = vec3();
                xyY[2] = Y;
                for (int saturationIndex = 0; saturationIndex < WIDTH; saturationIndex++) {
                    double relativeSaturation = ((double) saturationIndex) / WIDTH;

                    double distanceRec2020 = rec2020GamutBoundaries[percent][polarIndex] * relativeSaturation;

                    xyY[0] = dx * distanceRec2020 + CIExyY.D65_WHITE_2DEG_x;
                    xyY[1] = dy * distanceRec2020 + CIExyY.D65_WHITE_2DEG_y;
                    CIExyY.xyY_to_XYZ(xyY, XYZ);
                    Rec709.XYZ_to_rec709(XYZ, rec709);

                    // gamut compression
                    double maxRec709Distance = rec709GamutBoundaries[percent][polarIndex];
                    double ratioToMax709Distance = maxRec709Distance == 0 ? 0 : distanceRec2020 / maxRec709Distance;
                    double compressedRec709RatioFromMax = curve.mappedValueOf(ratioToMax709Distance);
                    double compressedDistance = maxRec709Distance * compressedRec709RatioFromMax;
                    double[] compressedxyY = vec3(
                            dx * compressedDistance + CIExyY.D65_WHITE_2DEG_x, dy * compressedDistance + CIExyY.D65_WHITE_2DEG_y, Y);
                    double[] compressedXYZ = vec3();
                    CIExyY.xyY_to_XYZ(compressedxyY, compressedXYZ);
                    double[] compressedRec709 = vec3();
                    Rec709.XYZ_to_rec709(compressedXYZ, compressedRec709);
                    image.redChannel()[polarIndex][saturationIndex] = falseColourfOutOfGamut(compressedRec709[0]);//clamp(rec709[0], 0, 1);
                    image.greenChannel()[polarIndex][saturationIndex] = falseColourfOutOfGamut(compressedRec709[1]);//clamp(rec709[1], 0, 1);
                    image.blueChannel()[polarIndex][saturationIndex] = falseColourfOutOfGamut(compressedRec709[2]);//clamp(rec709[2], 0, 1);
                }
            }

            image.transformAllPixels(SrgbOut.SRGB_OUT);
            Png16Output.write("/tmp/Rec2020-Y%02d-Rec709".formatted(percent), image);
        }
    }

    private static double falseColourfOutOfGamut(double v) {
        if (v >= 0 && v <= 1) {return v;}
        if (v < 0) return 1;
        return 0;
    }
}
