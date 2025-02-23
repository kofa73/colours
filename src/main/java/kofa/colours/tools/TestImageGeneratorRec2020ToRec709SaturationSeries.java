package kofa.colours.tools;

import kofa.colours.spaces.CIExyY;
import kofa.colours.spaces.Rec709;
import kofa.io.PngOutput;
import kofa.io.RgbImage;
import kofa.maths.ThanatomanicCurve6;

import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import static java.lang.Math.*;
import static kofa.maths.MathHelpers.vec3;

public class TestImageGeneratorRec2020ToRec709SaturationSeries {

    private static final int WIDTH = 1800;
    private static final int HEIGHT = 1080;
    private static final double TWO_PI = 2 * Math.PI;
    private static final double MAX_LUMA_Y = 1;
    public static final int BAND = 20;

    public static void main(String[] args) {
        var raster = WritableRaster.createInterleavedRaster(DataBuffer.TYPE_USHORT, WIDTH, HEIGHT, 3, new Point());
        var image = new RgbImage(raster);

        int polarSteps = 12;
        double polarStepSize = TWO_PI / polarSteps;
        double[][] rec709GamutBoundaries = new CIExyYGamutBoundariesFinder(WIDTH, polarSteps).findRgbGamutBoundaries();
        double[][] rec2020GamutBoundaries = CIExyYGamutBoundariesFinder.forRec2020(WIDTH, polarSteps).findRgbGamutBoundaries();

        double lumaStepSize = MAX_LUMA_Y / (WIDTH);

        var curve = ThanatomanicCurve6.linearUntil(0.8);
        double[] XYZ = vec3();
        double[] rec709 = vec3();
        for (int percent = 0; percent <= 100; percent += 10) {
            for (int polarIndex = 0; polarIndex < polarSteps; polarIndex++) {
                double angle = polarIndex * polarStepSize;
                double dx = cos(angle);
                double dy = sin(angle);
                double[] xyY = vec3();
                for (int lumaIndex = 0; lumaIndex < WIDTH; lumaIndex++) {
                    int row = polarIndex * (4 * BAND + 10);
                    xyY[2] = lumaIndex * lumaStepSize;

                    double maxDistanceRec2020 = rec2020GamutBoundaries[lumaIndex][polarIndex];

                    // gamut clipping
                    double distanceRec2020 = maxDistanceRec2020 * percent / 100.0;
                    xyY[0] = dx * distanceRec2020 + CIExyY.D65_WHITE_2DEG_x;
                    xyY[1] = dy * distanceRec2020 + CIExyY.D65_WHITE_2DEG_y;
                    CIExyY.xyY_to_XYZ(xyY, XYZ);
                    Rec709.XYZ_to_rec709(XYZ, rec709);
                    for (int rowForColour = 0; rowForColour < BAND; rowForColour++) {
                        image.redChannel()[row][lumaIndex] = clamp(rec709[0], 0, 1);
                        image.greenChannel()[row][lumaIndex] = clamp(rec709[1], 0, 1);
                        image.blueChannel()[row][lumaIndex] = clamp(rec709[2], 0, 1);
                        row++;
                    }

                    // gamut compression
                    double maxRec709Distance = rec709GamutBoundaries[lumaIndex][polarIndex];
                    double ratioToMax709Distance = maxRec709Distance == 0 ? 0 : distanceRec2020 / maxRec709Distance;
                    double compressedRec709RatioFromMax = curve.mappedValueOf(ratioToMax709Distance);
                    double compressedDistance = maxRec709Distance * compressedRec709RatioFromMax;
                    xyY[0] = dx * compressedDistance + CIExyY.D65_WHITE_2DEG_x;
                    xyY[1] = dy * compressedDistance + CIExyY.D65_WHITE_2DEG_y;
                    CIExyY.xyY_to_XYZ(xyY, XYZ);
                    Rec709.XYZ_to_rec709(XYZ, rec709);
                    for (int rowForColour = 0; rowForColour < BAND; rowForColour++) {
                        image.redChannel()[row][lumaIndex] = falseColourfOutOfGamut(rec709[0]);
                        image.greenChannel()[row][lumaIndex] = falseColourfOutOfGamut(rec709[1]);
                        image.blueChannel()[row][lumaIndex] = falseColourfOutOfGamut(rec709[2]);
                        row++;
                    }

                    // hue reference: no compression needed
                    double referenceDistanceRec2020 = maxDistanceRec2020 * 30 / 100.0;
                    xyY[0] = dx * referenceDistanceRec2020 + CIExyY.D65_WHITE_2DEG_x;
                    xyY[1] = dy * referenceDistanceRec2020 + CIExyY.D65_WHITE_2DEG_y;
                    CIExyY.xyY_to_XYZ(xyY, XYZ);
                    Rec709.XYZ_to_rec709(XYZ, rec709);
                    for (int rowForColour = 0; rowForColour < BAND; rowForColour++) {
                        image.redChannel()[row][lumaIndex] = clamp(rec709[0], 0, 1);
                        image.greenChannel()[row][lumaIndex] = clamp(rec709[1], 0, 1);
                        image.blueChannel()[row][lumaIndex] = clamp(rec709[2], 0, 1);
                        row++;
                    }

                    double compressionStrength = distanceRec2020 > 0 ? 1 - compressedDistance / distanceRec2020 : 0;
                    for (int rowForColour = 0; rowForColour < BAND; rowForColour++) {
                        image.redChannel()[row][lumaIndex] = compressionStrength;
                        image.greenChannel()[row][lumaIndex] = compressionStrength;
                        image.blueChannel()[row][lumaIndex] = compressionStrength;
                        row++;
                    }
                }
            }

            image.transformAllPixels(SrgbOut.SRGB_OUT);
            new PngOutput().write("/tmp/Rec2020-%03d-Rec709".formatted(percent), image);
        }
    }

    private static double falseColourfOutOfGamut(double v) {
        if (v < 0) return 1;
        if (v > 1) return 0;
        return v;
    }
}
