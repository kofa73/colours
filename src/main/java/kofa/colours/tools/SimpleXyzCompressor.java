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

public class SimpleXyzCompressor {
    private static final int lumaResolution = 4096;
    private static final int chromaResolution = 4096;
    public static void main(String[] args) {
        String baseName = args[0];
        RgbImage image = new ImageLoader().loadImageFrom(baseName);

        toneMapUsingY(image);

        new GamutCompressor_xyY(lumaResolution, chromaResolution).compressGamut_in_xyY(image);

        image.transformAllPixels(SRGB_OUT);

        new PngOutput().write(baseName + "-" + "yMapped", image);
    }

    private static void toneMapUsingY(RgbImage image) {
        AtomicDouble maxY_holder = new AtomicDouble(0);
        image.forEachPixel((int row, int column, double red, double green, double blue) -> {
            findMaxY(red, green, blue, maxY_holder);
        });
        double maxY = maxY_holder.get();

        double shoulderYStart = findOptimalShoulderStart(maxY);

        if (shoulderYStart != 1) {
            CurveBasedYCompressor yCompressor = new CurveBasedYCompressor(shoulderYStart);
            image.transformAllPixels(yCompressor);
        }
    }

    private static void findMaxY(double red, double green, double blue, AtomicDouble maxY_holder) {
        double[] valuesXYZ = rec2020_to_XYZ(red, green, blue);
        maxY_holder.getAndAccumulate(valuesXYZ[1], Math::max);
    }

    private static double[] rec2020_to_XYZ(double red, double green, double blue) {
        double[] valuesRec2020 = new double[] {red, green, blue};
        double[] valuesXYZ = new double[3];
        Rec2020.rec2020_to_XYZ(valuesRec2020, valuesXYZ);
        return valuesXYZ;
    }

    private static class CurveBasedYCompressor implements RgbImage.PixelTransformer {
        private final ThanatomanicCurve6 curve;
        private final AtomicDouble maxMappedY = new AtomicDouble(0);

        private CurveBasedYCompressor(double shoulder) {
            curve = ThanatomanicCurve6.linearUntil(shoulder);
        }

        @Override
        public double[] transform(int row, int column, double red, double green, double blue) {
            double[] valuesXYZ = rec2020_to_XYZ(red, green, blue);
            double mappedY = curve.mappedValueOf(valuesXYZ[1]);
            valuesXYZ[1] = mappedY;
            maxMappedY.accumulateAndGet(mappedY, Math::max);
            var valuesRec2020 = new double[3];
            Rec2020.XYZ_to_rec2020(valuesXYZ, valuesRec2020);
            return new double[] {valuesRec2020[0], valuesRec2020[1], valuesRec2020[2]};
        }
    }
}
