package kofa.colours.tools;

import com.google.common.util.concurrent.AtomicDouble;
import kofa.io.ImageLoader;
import kofa.io.JpgOutput;
import kofa.io.RgbImage;
import kofa.maths.ThanatomanicCurve6;

import static java.lang.Math.max;
import static kofa.colours.tools.CurveSolver.findOptimalShoulderStart;

public class SimpleRgbCompressor {
    private static final int lumaResolution = 4096;
    private static final int chromaResolution = 4096;
    public static void main(String[] args) {
        String baseName = args[0];
        RgbImage image = new ImageLoader().loadImageFrom(baseName);

        toneMapUsingRGB(image);

        GamutCompressor_xyY.forRec709(lumaResolution, chromaResolution).compressGamut_in_xyY(image);

        JpgOutput.write(baseName + "-" + "rgbToneMapped-xyYCompressed", image);
    }

    private static void toneMapUsingRGB(RgbImage image) {
        AtomicDouble max_holder = new AtomicDouble(0);
        image.forEachPixel((int row, int column, double red, double green, double blue) -> {
            findMaxComponent(red, green, blue, max_holder);
        });
        double maxComponent = max_holder.get();

        double shoulderStart = findOptimalShoulderStart(maxComponent);

        if (shoulderStart != 1) {
            CurveBasedRgbCompressor rgbCompressor = new CurveBasedRgbCompressor(shoulderStart);
            image.transformAllPixels(rgbCompressor);
        }
    }

    private static void findMaxComponent(double red, double green, double blue, AtomicDouble maxHolder) {
        maxHolder.getAndAccumulate(max(red, max(green, blue)), Math::max);
    }

    private static class CurveBasedRgbCompressor implements RgbImage.PixelTransformer {
        private final ThanatomanicCurve6 curve;

        private CurveBasedRgbCompressor(double shoulder) {
            curve = ThanatomanicCurve6.linearUntil(shoulder);
        }

        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            double mappedRed = curve.mappedValueOf(red);
            double mappedGreen = curve.mappedValueOf(green);
            double mappedBlue = curve.mappedValueOf(blue);
            return new double[] {mappedRed, mappedGreen, mappedBlue};
        }
    }
}
