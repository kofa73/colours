package kofa.colours.tools;

import kofa.io.ImageLoader;
import kofa.io.JpgOutput;
import kofa.io.RgbImage;

public class AgxRgbCompressor {
    private static final int lumaResolution = 4096;
    private static final int chromaResolution = 4096;
    public static final GamutCompressor_xyY GAMUT_COMPRESSOR_XY_Y = GamutCompressor_xyY.forRec709(lumaResolution, chromaResolution);

    public static void main(String[] args) {
        AgxToneMapper.Look look = AgxToneMapper.Look.valueOf(args[0]);
        for (int i = 1; i < args.length; i++) {
            process(args[i], look);
        }
    }

    private static void process(String baseName, AgxToneMapper.Look look) {
        RgbImage image = new ImageLoader().loadImageFrom(baseName);

        toneMapUsingAgx(image, look);

        GAMUT_COMPRESSOR_XY_Y.compressGamut_in_xyY(image);

        JpgOutput.write(baseName + "-Agx-" + look + "-ToneMapped-xyYCompressed", image);
    }

    private static void toneMapUsingAgx(RgbImage image, AgxToneMapper.Look look) {
        var transformer = new AgxPixelTransformer(look);
        image.transformAllPixels(transformer);
    }

    private static class AgxPixelTransformer implements RgbImage.PixelTransformer {
        private final AgxToneMapper.Look look;

        AgxPixelTransformer(AgxToneMapper.Look look) {
            this.look = look;
        }

        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            return AgxToneMapper.agx(red, green, blue, look);
        }
    }
}
