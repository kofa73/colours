package kofa.colours.tools;

import com.google.common.util.concurrent.AtomicDouble;
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
import static kofa.colours.tools.CurveSolver.findOptimalShoulderStart;
import static kofa.colours.tools.SrgbOut.SRGB_OUT;

public class SimpleRgbCompressor {
    private static final int lumaResolution = 4096;
    private static final int chromaResolution = 4096;
    public static void main(String[] args) {
        String baseName = args[0];
        RgbImage image = new ImageLoader().loadImageFrom(baseName);

        toneMapUsingRGB(image);

        new GamutCompressor_xyY(lumaResolution, chromaResolution).compressGamut_in_xyY(image);

        image.transformAllPixels(SRGB_OUT);

        new PngOutput().write(baseName + "-" + "rgbToneMapped-xyYCompressed", image);
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
        maxHolder.getAndAccumulate(red, Math::max);
        maxHolder.getAndAccumulate(green, Math::max);
        maxHolder.getAndAccumulate(blue, Math::max);
    }

    private static class CurveBasedRgbCompressor implements RgbImage.PixelTransformer {
        private final ThanatomanicCurve6 curve;
        private final AtomicDouble maxMappedComponent = new AtomicDouble(0);

        private CurveBasedRgbCompressor(double shoulder) {
            curve = ThanatomanicCurve6.linearUntil(shoulder);
        }

        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            double mappedRed = curve.mappedValueOf(red);
            double mappedGreen = curve.mappedValueOf(green);
            double mappedBlue = curve.mappedValueOf(blue);
            maxMappedComponent.accumulateAndGet(mappedRed, Math::max);
            maxMappedComponent.accumulateAndGet(mappedGreen, Math::max);
            maxMappedComponent.accumulateAndGet(mappedBlue, Math::max);
            return new double[] {mappedRed, mappedGreen, mappedBlue};
        }
    }
}
