package kofa.noise;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import static java.lang.Math.max;
import static kofa.noise.Padding.pad;

public class SpectrumSubtractingFilter {
    private static final FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);

    public static void filter(float[] monoPane, int width, int height, int size, double[] noiseMagnitudes) {
        for (int topRightY = 0; topRightY < height - size; topRightY += size) {
            for (int topRightX = 0; topRightX < width - size; topRightX += size) {
                double[] padded = pad(monoPane, width, height, size, topRightX, topRightY);

                Complex[] spectrum = transformer.transform(padded, TransformType.FORWARD);

                Complex[] filteredSpectrum = new Complex[spectrum.length];

                for (int freq = 0; freq < spectrum.length; freq++) {
//                    double magnitudeAtFreq = spectrum[freq].abs();
//                    double reducedMagnitude = max(0, magnitudeAtFreq - noiseMagnitudes[freq]);
//                    if (magnitudeAtFreq != 0 && !Double.isNaN(magnitudeAtFreq) && !Double.isInfinite(magnitudeAtFreq)) {
//                        filteredSpectrum[freq] = spectrum[freq].multiply(reducedMagnitude / magnitudeAtFreq);
//                    } else {
                        filteredSpectrum[freq] = spectrum[freq];
//                    }

                }

                Complex[] filtered = transformer.transform(filteredSpectrum, TransformType.INVERSE);
                int paddedRowLength = Integer.highestOneBit(size) * 2;
                int paddingEnd = (paddedRowLength - size) / 2;
                for (int y = 0; y < size; y++) {
                    for (int x = 0; x < size; x++) {
                        int indexInPane = (topRightY + y) * width + (topRightX + x);
                        int indexInFiltered = (paddingEnd + y) * paddedRowLength + (paddingEnd + x);
                        double filteredValue = filtered[indexInFiltered].getReal();
                        monoPane[indexInPane] = (float) max(0, filteredValue);
                    }
                }
            }
        }
    }
}
