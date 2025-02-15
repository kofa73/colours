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

/**
 * Calculates spectral power and frequency analysis of 2D monochromatic image data using Fast Fourier Transform (FFT).
 * Maintains histograms of frequency components and can analyze square regions of an image.
 */
public class SpectralPowerCalculator {
    /**
     * Scaling factor used for histogram value recording.
     */
    public static final double SCALE = 1;

    /**
     * Histograms for each frequency index in the transformed data.
     */
    public final Histogram[] histogramByFrequencyIndex;

    private final float[] image;
    private final int width;
    private final int height;
    private final int filterSize;
    private final int paddedSize;

    /**
     * Constructs a new SpectralPowerCalculator.
     *
     * @param image       The input image data as a float array
     * @param width       The width of the input image
     * @param height      The height of the input image
     * @param filterSize  The size of the square region to analyze
     */
    public SpectralPowerCalculator(float[] image, int width, int height, int filterSize) {
        this.image = image;
        this.width = width;
        this.height = height;
        this.filterSize = filterSize;
        paddedSize = Integer.highestOneBit(filterSize) * 2;
        histogramByFrequencyIndex = new Histogram[paddedSize * paddedSize / 2];
        setAll(histogramByFrequencyIndex, ignoredIndex -> new ConcurrentHistogram(3));
    }

    /**
     * Represents the result of a spectral power measurement.
     */
    public record Result(XYCoordinates coordinates, double power, double[] spectrum) {
        public double[] magnitudes() {
            double[] magnitudes = new double[spectrum.length / 2];
            // the 'spectrum' is made up by pair representing complex(Re, Im)
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
        double[] buffer = pad(image, width, paddedSize, filterSize, topLeftX, topLeftY);

        fft.realForward(buffer);
        histogramByFrequencyIndex[0].recordValue((long) (magnitude(buffer[0], buffer[1]) * SCALE));
        double power = 0;
        // don't include the offset (frequency = 0) in the power calculation; it does not influence 'smoothness'
        for (int i = 1; i < buffer.length - 1; i += 2) {
            double freqPower = power(buffer[i], buffer[i + 1]);
            power += freqPower;
            histogramByFrequencyIndex[i / 2].recordValue(Math.round(sqrt(freqPower) * SCALE));
        }

        // zero out the offset - keeping it is basically a black-level adjustment
//        spectrum[0] = 0; spectrum[1] = 0;

        return new Result(new XYCoordinates(topLeftX, topLeftY), power, buffer);
    }
}
