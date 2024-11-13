package kofa.noise;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static kofa.noise.Padding.pad;

public class SpectrumSubtractingFilter {
    private static final FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
    private static final int STRENGTH = 2;

    private final int width;
    private final int height;
    private final int blockSize;
    private final int paddedRowLength;
    private final int paddingEnd;
    private final int halfPoint;
    private final float[][] multipliers;

    public SpectrumSubtractingFilter(int width, int height, int blockSize) {
        this.width = width;
        this.height = height;
        this.blockSize = blockSize;
        paddedRowLength = Integer.highestOneBit(blockSize) * 2;
        paddingEnd = (paddedRowLength - blockSize) / 2;
        halfPoint = blockSize / 2;

        multipliers = new float[blockSize][blockSize];
        for (int y = 0; y < blockSize; y++) {
            float yRamp = y < halfPoint ? y / (float) halfPoint : (blockSize - y) / (float) halfPoint;
            for (int x = 0; x < blockSize; x++) {
                float xRamp = x < halfPoint ? x / (float) halfPoint : (blockSize - x) / (float) halfPoint;
                float multiplier = yRamp * xRamp;
                multipliers[y][x] = multiplier;
            }
        }

    }

    public void filter(float[] monoPane, double[] noiseMagnitudes) {
        float[] copy = monoPane.clone();
        Arrays.fill(monoPane, 0f);

        IntStream.iterate(0, i -> i < height - blockSize, i -> i + blockSize / 2).parallel().forEach(topLeftY ->
                processOneRow(topLeftY, copy, monoPane, noiseMagnitudes)
        );
    }

    private void processOneRow(int topLeftY, float[] copy, float[] monoPane, double[] noiseMagnitudes) {
        for (int topLeftX = 0; topLeftX < width - blockSize; topLeftX += blockSize / 2) {
            double[] padded = pad(copy, width, height, blockSize, topLeftX, topLeftY);

            Complex[] spectrum = transformer.transform(padded, TransformType.FORWARD);

            for (int freq = 0; freq < spectrum.length; freq++) {
                double magnitudeAtFreq = spectrum[freq].abs();
                double strength = freq == 0 ? 1 : STRENGTH;
                double reducedMagnitude = max(0, magnitudeAtFreq - strength * noiseMagnitudes[freq]);
                if (magnitudeAtFreq != 0 && !Double.isNaN(magnitudeAtFreq) && !Double.isInfinite(magnitudeAtFreq)) {
                    spectrum[freq] = spectrum[freq].multiply(reducedMagnitude / magnitudeAtFreq);
                }
            }

            Complex[] filtered = transformer.transform(spectrum, TransformType.INVERSE);

            for (int y = 0; y < blockSize; y++) {
                for (int x = 0; x < blockSize; x++) {
                    int indexInPane = (topLeftY + y) * width + (topLeftX + x);
                    int indexInFiltered = (paddingEnd + y) * paddedRowLength + (paddingEnd + x);
                    double filteredValue = filtered[indexInFiltered].getReal();
                    monoPane[indexInPane] += multipliers[y][x] * (float) max(0, filteredValue);
                }
            }
        }
    }
}
