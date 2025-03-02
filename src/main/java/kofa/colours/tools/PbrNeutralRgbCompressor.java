package kofa.colours.tools;

import kofa.io.ImageLoader;
import kofa.io.JpgOutput;
import kofa.io.RgbImage;

public class PbrNeutralRgbCompressor {
    private static final int lumaResolution = 4096;
    private static final int chromaResolution = 4096;
    public static final GamutCompressor_xyY GAMUT_COMPRESSOR_XY_Y = GamutCompressor_xyY.forRec709(lumaResolution, chromaResolution);

    public static void main(String[] args) {
        for (String inFile : args) {
            process(inFile);
        }
    }

    private static void process(String baseName) {
        RgbImage image = new ImageLoader().loadImageFrom(baseName);

        toneMapUsingPbrNeutral(image);

        GAMUT_COMPRESSOR_XY_Y.compressGamut_in_xyY(image);

        JpgOutput.write(baseName + "-PbrNeutral-ToneMapped-xyYCompressed", image);
    }

    private static void toneMapUsingPbrNeutral(RgbImage image) {
        PbrNeutralPixelTransformer transformer = new PbrNeutralPixelTransformer();
        image.transformAllPixels(transformer);
    }
}
