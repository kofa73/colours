package kofa.noise;

import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.jtransforms.fft.DoubleFFT_2D;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.Math.max;
import static kofa.noise.FFTUtils.fft2d;
import static kofa.noise.FFTUtils.magnitude;
import static kofa.noise.Padding.pad;

public class SpectrumSubtractingFilter {
    private static final FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
    private static final double STRENGTH = 1.5;

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
        DoubleFFT_2D fft = new DoubleFFT_2D(paddedRowLength, paddedRowLength);
        for (int topLeftX = 0; topLeftX < width - blockSize; topLeftX += blockSize / 2) {
            double[] padded = pad(copy, width, paddedRowLength, blockSize, topLeftX, topLeftY);
//            Complex[] buffer = transformer.transform(padded, TransformType.FORWARD);
            double[] buffer = fft2d(padded, fft);

            double offset = magnitude(buffer[0], buffer[1]);
            double reducedOffset = max(0, offset - noiseMagnitudes[0]);
            if (offset != 0 && !Double.isNaN(offset) && !Double.isInfinite(offset)) {
                double multiplier = reducedOffset / offset;
                buffer[0] *= multiplier;
                buffer[1] *= multiplier;
            }

            for (int freq = 1; freq < padded.length; freq++) {
                int reIndex = 2 * freq;
                int imIndex = reIndex + 1;
                double magnitudeAtFreq = magnitude(buffer[reIndex], buffer[imIndex]);
                double reducedMagnitude = max(0, magnitudeAtFreq - STRENGTH * noiseMagnitudes[freq]);
                if (magnitudeAtFreq != 0 && !Double.isNaN(magnitudeAtFreq) && !Double.isInfinite(magnitudeAtFreq)) {
                    double multiplier = reducedMagnitude / magnitudeAtFreq;
                    buffer[reIndex] *= multiplier;
                    buffer[imIndex] *= multiplier;
                }
            }

            //Complex[] filtered = transformer.transform(buffer, TransformType.INVERSE);
            fft.complexInverse(buffer, true);

            // monoPane is updated using read - increment - write, need to sync
            // It's more efficient to get the lock once for the loop than racing for it for each individual update.
            // Ideally, each thread would have its own area that can be merged in the end.
            synchronized (monoPane) {
                for (int y = 0; y < blockSize; y++) {
                    int rowStartInPane = (topLeftY + y) * width;
                    int rowStartInFiltered = (paddingEnd + y) * paddedRowLength;
                    for (int x = 0; x < blockSize; x++) {
                        int indexInPane = rowStartInPane + (topLeftX + x);
                        int indexInFiltered = rowStartInFiltered + (paddingEnd + x);
                        double filteredValue = buffer[2 * indexInFiltered];
                        monoPane[indexInPane] += multipliers[y][x] * (float) max(0, filteredValue);
                    }
                }
            }
        }
    }
}
