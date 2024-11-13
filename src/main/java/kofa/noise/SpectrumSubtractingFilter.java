package kofa.noise;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.util.Arrays;

import static java.lang.Math.max;
import static kofa.noise.Padding.pad;

public class SpectrumSubtractingFilter {
    private static final FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
    private static final int STRENGTH = 2;

    public static void filter(float[] monoPane, int width, int height, int size, double[] noiseMagnitudes) {
        int paddedRowLength = Integer.highestOneBit(size) * 2;
        int paddingEnd = (paddedRowLength - size) / 2;
        int halfPoint = size / 2;
        float[][] multipliers = new float[size][size];
        for (int y = 0; y < size; y++) {
            float yRamp = y < halfPoint ? (y / (float) halfPoint) : (size - y) / (float) halfPoint;
            for (int x = 0; x < size; x++) {
                float xRamp = x < halfPoint ? (x / (float) halfPoint) : (size - x) / (float) halfPoint;
                float multiplier = yRamp * xRamp;
                multipliers[y][x] = multiplier;
            }
        }

        float[] copy = monoPane.clone();
        Arrays.fill(monoPane, 0f);

        for (int topRightY = 0; topRightY < height - size; topRightY += size / 2) {
            for (int topRightX = 0; topRightX < width - size; topRightX += size / 2) {
                double[] padded = pad(copy, width, height, size, topRightX, topRightY);

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

                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        int indexInPane = (topRightY + y) * width + (topRightX + x);
                        int indexInFiltered = (paddingEnd + y) * paddedRowLength + (paddingEnd + x);
                        double filteredValue = filtered[indexInFiltered].getReal();
                        monoPane[indexInPane] += multipliers[y][x] * (float) max(0, filteredValue);
                    }
                }
            }
        }
    }
}
