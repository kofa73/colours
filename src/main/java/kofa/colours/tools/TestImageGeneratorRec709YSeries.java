package kofa.colours.tools;

import kofa.colours.spaces.CIExyY;
import kofa.colours.spaces.Rec709;
import kofa.io.RgbImage;
import kofa.io.TiffFloat32Output;

import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static kofa.colours.tools.CIExyYGamutBoundariesFinder.findRgbGamutBoundariesForRec709;
import static kofa.maths.MathHelpers.vec3;

public class TestImageGeneratorRec709YSeries {

    private static final int WIDTH = 3000;
    private static final int HEIGHT = 2000;
    private static final double TWO_PI = 2 * Math.PI;
    public static final int MAX_Y = 256;
    public static final int LUMA_RESOLUTION = 100;

    public static void main(String[] args) {
        var raster = WritableRaster.createInterleavedRaster(DataBuffer.TYPE_USHORT, WIDTH, HEIGHT, 3, new Point());
        var image = new RgbImage(raster);

        int polarSteps = HEIGHT;
        double polarStepSize = TWO_PI / polarSteps;

        double[] XYZ = vec3();
        double[] rec709 = vec3();
        for (int luma = 1; luma < MAX_Y; luma *= 2) {
            // FIXME needs unbounded
            double[][] rec709GamutBoundaries = findRgbGamutBoundariesForRec709(LUMA_RESOLUTION, polarSteps);
            double Y = luma;
            for (int polarIndex = 0; polarIndex < polarSteps; polarIndex++) {
                double angle = polarIndex * polarStepSize;
                double dx = cos(angle);
                double dy = sin(angle);
                double[] xyY = vec3();
                xyY[2] = Y;
                for (int saturationIndex = 0; saturationIndex < WIDTH; saturationIndex++) {
                    double relativeSaturation = ((double) saturationIndex) / WIDTH;

                    double distanceRec709 = rec709GamutBoundaries[50][polarIndex] * relativeSaturation;

                    xyY[0] = dx * distanceRec709 + CIExyY.D65_WHITE_2DEG_x;
                    xyY[1] = dy * distanceRec709 + CIExyY.D65_WHITE_2DEG_y;
                    CIExyY.xyY_to_XYZ(xyY, XYZ);
                    Rec709.XYZ_to_rec709(XYZ, rec709);

                    image.redChannel()[polarIndex][saturationIndex] = falseColourfOutOfGamut(rec709[0]);
                    image.greenChannel()[polarIndex][saturationIndex] = falseColourfOutOfGamut(rec709[1]);
                    image.blueChannel()[polarIndex][saturationIndex] = falseColourfOutOfGamut(rec709[2]);
                }
            }

            TiffFloat32Output.write("/tmp/linear-Rec709-Y%03d".formatted(luma), image);
        }
    }

    private static double falseColourfOutOfGamut(double v) {
        //if (v >= 0 && v <= 1) {return v;}
        if (v < 0) return 1;
        return v;
    }
}
