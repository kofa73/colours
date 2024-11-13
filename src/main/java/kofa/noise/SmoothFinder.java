package kofa.noise;

import kofa.colours.model.XYCoordinates;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static kofa.noise.Padding.pad;

public class SmoothFinder {
    public record Result(XYCoordinates coordinates, double power, Complex[] spectrum) {
        public double[] magnitudes() {
            double[] magnitudes = new double[spectrum().length];
            for (int i = 0; i < spectrum.length; i++) {
                magnitudes[i] = spectrum[i].abs();
            }
            return magnitudes;
        }
    }

    private static final FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);

    public static Result findSmoothSquare(float[] monoPane, int width, int height, int size) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int topLeftX = random.nextInt(width - size);
        int topLeftY = random.nextInt(height - size);

        double[] padded = pad(monoPane, width, height, size, topLeftX, topLeftY);

        Complex[] spectrum = transformer.transform(padded, TransformType.FORWARD);
        double power = 0;
        // don't include the offset (frequency = 0) in the power calculation; it does not influence 'smoothness'
        for (int i = 1; i < spectrum.length; i++) {
            double magnitude = spectrum[i].abs();
            power += magnitude * magnitude;
        }

        // zero out the offset - keeping it is basically a black-level adjustment
//        spectrum[0] = new Complex(0, 0);

        return new Result(new XYCoordinates(topLeftX, topLeftY), power, spectrum);
    }
}
