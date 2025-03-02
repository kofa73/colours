package kofa.colours.tools;

import kofa.colours.spaces.CIExyY;
import kofa.colours.spaces.Rec709;
import kofa.io.RgbImage;
import kofa.io.TiffFloat32Output;

import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import static java.lang.Math.*;
import static kofa.maths.MathHelpers.vec3;

public class TestImageGeneratorRec709ExposureSeries {

    private static final int WIDTH = 3000;
    private static final int HEIGHT = 2000;
    private static final double TWO_PI = 2 * Math.PI;
    public static final int maxExposure = 8;

    public static void main(String[] args) {
        var raster = WritableRaster.createInterleavedRaster(DataBuffer.TYPE_USHORT, WIDTH, HEIGHT, 3, new Point());
        var image = new RgbImage(raster);

        int polarSteps = HEIGHT;
        double polarStepSize = TWO_PI / polarSteps;

        var gamutFinder = new RgbTriangleGamutFinder(Rec709.PARAMS);

        double[] XYZ = vec3();
        double[] rec709 = vec3();
        double[] xyY = vec3();

        // blue is the darkest primary; up to that, we have the full gamut triangle
        rec709[0] = 0;
        rec709[1] = 0;
        rec709[2] = 1;
        Rec709.rec709_to_XYZ(rec709, XYZ);
        double baseY = XYZ[1];

        for (int exposure = 0; exposure <= maxExposure; exposure += 1) {
            double multiplier = pow(2, exposure);
            for (int polarIndex = 0; polarIndex < polarSteps; polarIndex++) {
                double angle = polarIndex * polarStepSize;
                double dx = cos(angle);
                double dy = sin(angle);
                for (int saturationIndex = 0; saturationIndex < WIDTH; saturationIndex++) {
                    xyY[2] = baseY;
                    double relativeSaturation = ((double) saturationIndex) / (WIDTH - 1); // force full saturation at the right border

                    double distanceRec709 = gamutFinder.distanceFromGamut(angle);

                    xyY[0] = dx * distanceRec709 + CIExyY.D65_WHITE_2DEG_x;
                    xyY[1] = dy * distanceRec709 + CIExyY.D65_WHITE_2DEG_y;
                    CIExyY.xyY_to_XYZ(xyY, XYZ);
                    Rec709.XYZ_to_rec709(XYZ, rec709);

                    distanceRec709 *= relativeSaturation;
                    xyY[0] = dx * distanceRec709 + CIExyY.D65_WHITE_2DEG_x;
                    xyY[1] = dy * distanceRec709 + CIExyY.D65_WHITE_2DEG_y;
                    CIExyY.xyY_to_XYZ(xyY, XYZ);
                    Rec709.XYZ_to_rec709(XYZ, rec709);

                    image.redChannel()[polarIndex][saturationIndex] = rec709[0] * multiplier;
                    image.greenChannel()[polarIndex][saturationIndex] = rec709[1] * multiplier;
                    image.blueChannel()[polarIndex][saturationIndex] = rec709[2] * multiplier;
                }
            }

            TiffFloat32Output.write("/tmp/linear-Rec709+%dEV".formatted(exposure), image);
        }
    }

}
