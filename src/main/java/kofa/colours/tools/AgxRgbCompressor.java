package kofa.colours.tools;

import com.google.common.util.concurrent.AtomicDouble;
import kofa.colours.spaces.Buffer3;
import kofa.colours.spaces.CIExyY;
import kofa.colours.spaces.Rec2020;
import kofa.colours.spaces.Rec709;
import kofa.io.ImageLoader;
import kofa.io.PngOutput;
import kofa.io.RgbImage;
import kofa.maths.Solver;
import kofa.maths.ThanatomanicCurve6;

import java.util.Optional;

import static java.lang.Math.*;
import static kofa.colours.spaces.CIExyY.D65_WHITE_2DEG_x;
import static kofa.colours.spaces.CIExyY.D65_WHITE_2DEG_y;
import static kofa.colours.tools.SrgbOut.SRGB_OUT;

public class AgxRgbCompressor {
    private static final int lumaResolution = 4096;
    private static final int chromaResolution = 4096;
    public static void main(String[] args) {
        AgxToneMapper.Look look = AgxToneMapper.Look.valueOf(args[0]);
        String baseName = args[1];
        RgbImage image = new ImageLoader().loadImageFrom(baseName);

        toneMapUsingAgx(image, look);

        new GamutCompressor_xyY(lumaResolution, chromaResolution).compressGamut_in_xyY(image);

        image.transformAllPixels(SRGB_OUT);

        new PngOutput().write(baseName + "-Agx-" + look + "-ToneMapped-xyYCompressed", image);
    }

    private static void toneMapUsingAgx(RgbImage image, AgxToneMapper.Look look) {
        AgxPixelTransformer transformer = new AgxPixelTransformer(look);
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
