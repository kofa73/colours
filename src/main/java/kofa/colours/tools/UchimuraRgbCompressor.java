package kofa.colours.tools;

import kofa.io.ImageLoader;
import kofa.io.PngOutput;
import kofa.io.RgbImage;

import static kofa.colours.tools.SrgbOut.SRGB_OUT;

public class UchimuraRgbCompressor {
    private static final int lumaResolution = 4096;
    private static final int chromaResolution = 4096;
    public static void main(String[] args) {
        String baseName = args[0];
        RgbImage image = new ImageLoader().loadImageFrom(baseName);

        toneMapUsingRGB(image);

        new GamutCompressor_xyY(lumaResolution, chromaResolution).compressGamut_in_xyY(image);

        image.transformAllPixels(SRGB_OUT);

        new PngOutput().write(baseName + "-UchimuraToneMapped-xyYCompressed", image);
    }

    private static void toneMapUsingRGB(RgbImage image) {
        UchimuraPixelTransformer transformer = new UchimuraPixelTransformer();
        image.transformAllPixels(transformer);
    }


    private static class UchimuraPixelTransformer implements RgbImage.PixelTransformer {
        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            double mappedRed = UchimuraToneMapper.toneMap(red);
            double mappedGreen = UchimuraToneMapper.toneMap(green);
            double mappedBlue = UchimuraToneMapper.toneMap(blue);
            return new double[] {mappedRed, mappedGreen, mappedBlue};
        }
    }
}
