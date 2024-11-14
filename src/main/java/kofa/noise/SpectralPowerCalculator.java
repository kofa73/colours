package kofa.noise;

import kofa.colours.model.XYCoordinates;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.concurrent.ThreadLocalRandom;

import static kofa.noise.Padding.pad;

public class SpectralPowerCalculator {
    public static final double SCALE = 1_000_000.0;

    public final Histogram histogram = new ConcurrentHistogram(5);

    private static final FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
    private final float[] pane;
    private final int width;
    private final int height;
    private final int filterSize;

    public SpectralPowerCalculator(float[] pane, int width, int height, int filterSize) {
        this.pane = pane;
        this.width = width;
        this.height = height;
        this.filterSize = filterSize;
    }

    public record Result(XYCoordinates coordinates, double power, Complex[] spectrum) {
        public double[] magnitudes() {
            double[] magnitudes = new double[spectrum().length];
            for (int i = 0; i < spectrum.length; i++) {
                magnitudes[i] = spectrum[i].abs();
            }
            return magnitudes;
        }
    }

    public Result measureRandomSquare() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int topLeftX = random.nextInt(width - filterSize);
        int topLeftY = random.nextInt(height - filterSize);

        return measure(pane, width, height, filterSize, topLeftX, topLeftY);
    }

    private Result measure(float[] monoPane, int width, int height, int size, int topLeftX, int topLeftY) {
        double[] padded = pad(monoPane, width, height, size, topLeftX, topLeftY);

        Complex[] spectrum = transformer.transform(padded, TransformType.FORWARD);
        double power = 0;
        // don't include the offset (frequency = 0) in the power calculation; it does not influence 'smoothness'
        for (int i = 1; i < spectrum.length; i++) {
            double magnitude = spectrum[i].abs();
            histogram.recordValue(Math.round(magnitude * SCALE));
            power += magnitude * magnitude;
        }

        // zero out the offset - keeping it is basically a black-level adjustment
//        spectrum[0] = new Complex(0, 0);

        return new Result(new XYCoordinates(topLeftX, topLeftY), power, spectrum);
    }
}
