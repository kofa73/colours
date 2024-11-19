package kofa.noise;

import kofa.colours.model.XYCoordinates;
import org.HdrHistogram.ConcurrentHistogram;
import org.HdrHistogram.Histogram;
import org.jtransforms.fft.DoubleFFT_2D;

import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.sqrt;
import static java.util.Arrays.setAll;
import static kofa.noise.FFTUtils.*;
import static kofa.noise.Padding.pad;

public class SpectralPowerCalculator {
    public static final double SCALE = 1;

    public final Histogram[] histogramByFrequencyIndex;

    private final float[] pane;
    private final int width;
    private final int height;
    private final int filterSize;
    private final int paddedSize;

    public SpectralPowerCalculator(float[] pane, int width, int height, int filterSize) {
        this.pane = pane;
        this.width = width;
        this.height = height;
        this.filterSize = filterSize;
        paddedSize = Integer.highestOneBit(filterSize) * 2;
        histogramByFrequencyIndex = new Histogram[paddedSize * paddedSize];
        setAll(histogramByFrequencyIndex, ignoredIndex -> new ConcurrentHistogram(3));
    }

    public record Result(XYCoordinates coordinates, double power, double[] spectrum) {
        public double[] magnitudes() {
            double[] magnitudes = new double[spectrum.length / 2];
            setAll(magnitudes, index -> magnitude(spectrum[2 * index], spectrum[2 * index + 1]));
            return magnitudes;
        }
    }

    public Result measureRandomSquare() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int topLeftX = random.nextInt(width - filterSize);
        int topLeftY = random.nextInt(height - filterSize);

        return measure(topLeftX, topLeftY);
    }

    public Result measure(int topLeftX, int topLeftY) {
        DoubleFFT_2D fft = new DoubleFFT_2D(paddedSize, paddedSize);
        double[] buffer = pad(pane, width, paddedSize, filterSize, topLeftX, topLeftY);

        fft.realForward(buffer);
        histogramByFrequencyIndex[0].recordValue((long) (magnitude(buffer[0], buffer[1]) * SCALE));
        double power = 0;
        // don't include the offset (frequency = 0) in the power calculation; it does not influence 'smoothness'
        for (int i = 1; i < buffer.length - 1; i += 2) {
            double freqPower = power(buffer[i], buffer[i + 1]);
            power += freqPower;
            histogramByFrequencyIndex[i/2].recordValue(Math.round(sqrt(freqPower) * SCALE));
        }

        // zero out the offset - keeping it is basically a black-level adjustment
//        spectrum[0] = 0; spectrum[1] = 0;

        return new Result(new XYCoordinates(topLeftX, topLeftY), power, buffer);
    }
}
