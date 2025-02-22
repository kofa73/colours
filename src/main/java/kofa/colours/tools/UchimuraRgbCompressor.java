package kofa.colours.tools;

import com.google.common.util.concurrent.AtomicDouble;
import kofa.colours.spaces.CIExyY;
import kofa.colours.spaces.Rec2020;
import kofa.colours.spaces.Rec709;
import kofa.io.ImageLoader;
import kofa.io.PngOutput;
import kofa.io.RgbImage;
import kofa.maths.ThanatomanicCurve6;

import static java.lang.Math.*;
import static kofa.colours.spaces.CIExyY.D65_WHITE_2DEG_x;
import static kofa.colours.spaces.CIExyY.D65_WHITE_2DEG_y;
import static kofa.colours.tools.CurveSolver.findOptimalShoulderStart;
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
        private final AtomicDouble maxMappedComponent = new AtomicDouble(0);

        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            double mappedRed = UchimuraToneMapper.toneMap(red);
            double mappedGreen = UchimuraToneMapper.toneMap(green);
            double mappedBlue = UchimuraToneMapper.toneMap(blue);
            maxMappedComponent.accumulateAndGet(mappedRed, Math::max);
            maxMappedComponent.accumulateAndGet(mappedGreen, Math::max);
            maxMappedComponent.accumulateAndGet(mappedBlue, Math::max);
            return new double[] {mappedRed, mappedGreen, mappedBlue};
        }
    }
}
