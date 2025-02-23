package kofa.colours.tools;

import kofa.io.ImageLoader;
import kofa.io.PngOutput;
import kofa.io.RgbImage;

import static kofa.colours.tools.SrgbOut.SRGB_OUT;

public class AgxRgbCompressorRec709 {
    private static final int lumaResolution = 4096;
    private static final int chromaResolution = 4096;
    public static void main(String[] args) {
        AgxToneMapperRec709.Look look = AgxToneMapperRec709.Look.valueOf(args[0]);
        String baseName = args[1];
        RgbImage image = new ImageLoader().loadImageFrom(baseName);

        toneMapUsingAgx(image, look);

        new GamutCompressor_xyY(lumaResolution, chromaResolution).compressGamut_in_xyY(image);

        image.transformAllPixels(SRGB_OUT);


        new PngOutput().write(baseName + "-AgxRec709-" + look + "-ToneMapped-xyYCompressed", image);
    }

    private static void toneMapUsingAgx(RgbImage image, AgxToneMapperRec709.Look look) {
        var transformer = new AgxRec709PixelTransformer(look);
        image.transformAllPixels(transformer);
    }

    private static class AgxRec709PixelTransformer implements RgbImage.PixelTransformer {
        private final AgxToneMapperRec709.Look look;

        AgxRec709PixelTransformer(AgxToneMapperRec709.Look look) {
            this.look = look;
        }

        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            return AgxToneMapperRec709.agx(red, green, blue, look);
        }
    }
}
