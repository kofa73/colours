package kofa.colours.tools;

import kofa.io.ImageLoader;
import kofa.io.PngOutput;
import kofa.io.RgbImage;

import static kofa.colours.tools.SrgbOut.SRGB_OUT;

public class UchimuraRgbCompressor {
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
