package kofa.colours.tools;

import kofa.colours.spaces.CIExyY;
import kofa.colours.spaces.Rec2020;
import kofa.io.ImageLoader;
import kofa.io.JpgOutput;
import kofa.io.RgbImage;

import static kofa.maths.MathHelpers.vec3;

public class GamutCompressor {
    private static final int lumaResolution = 4096;
    private static final int chromaResolution = 4096;
    public static final GamutCompressor_xyY GAMUT_COMPRESSOR_XY_Y = new GamutCompressor_xyY(lumaResolution, chromaResolution);

    public static void main(String[] args) {
        for (String fileName : args) {
            process(fileName);
        }
    }

    private static void process(String baseName) {
        RgbImage image = new ImageLoader().loadImageFrom(baseName);

        toneMapUsingRGB(image);

        GAMUT_COMPRESSOR_XY_Y.compressGamut_in_xyY(image);

        new JpgOutput().write(baseName + "-Yclipped-xyYCompressed", image);
    }

    private static void toneMapUsingRGB(RgbImage image) {
        YClippingPixelTransformer transformer = new YClippingPixelTransformer();
        image.transformAllPixels(transformer);
    }


    private static class YClippingPixelTransformer implements RgbImage.PixelTransformer {
        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            double[] rec2020 = {red, green, blue};
            double[] XYZ = vec3();
            Rec2020.rec2020_to_XYZ(rec2020, XYZ);
            double[] xyY = vec3();
            CIExyY.XZY_to_xyY(XYZ, xyY);
            xyY[2] = Math.min(1, xyY[2]);
            CIExyY.xyY_to_XYZ(xyY, XYZ);
            Rec2020.XYZ_to_rec2020(XYZ, rec2020);
            return rec2020;
        }
    }
}
