package kofa.noise;

import kofa.colours.model.XYCoordinates;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.Random;

import static kofa.noise.Padding.pad;

public class SmoothFinder {
    public record Result (XYCoordinates coordinates, double magnitude, Complex[] spectrum) {
        public double[] magnitudes() {
            double[] magnitudes = new double[spectrum().length];
            for (int i = 0; i < spectrum.length; i++) {
                magnitudes[i] = spectrum[i].abs();
            }
            return magnitudes;
        }
    }

    private static final Random random = new Random();
    private static final FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);

    public static Result findSmoothSquare(int[] monoPane, int width, int height, int size) {
        float[] floatPane = new float[monoPane.length];
        for (int i = 0; i < monoPane.length; i++) {
            floatPane[i] = monoPane[i];
        }
        return findSmoothSquare(floatPane, width, height, size);
    }

    public static Result findSmoothSquare(float[] monoPane, int width, int height, int size) {
        int topRightX = random.nextInt(width - size);
        int topRightY = random.nextInt(height - size);

        double[] padded = pad(monoPane, width, height, size, topRightX, topRightY);

        Complex[] spectrum = transformer.transform(padded, TransformType.FORWARD);
        double magnitude = 0;
        for (int i = 0; i < spectrum.length; i++) {
            magnitude += spectrum[i].abs();
        }

        return new Result(new XYCoordinates(topRightX, topRightY), magnitude, spectrum);
    }
}
