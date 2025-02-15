package kofa.noise;

import java.util.Arrays;

import static java.util.Arrays.fill;

/**
 * A utility class for padding arrays with average values.
 */
public class Padding {
    /**
     * Extracts a square from a monochromatic image (represented as float[]), and creates a new float[]
     * representing a padded version of the original square. The edges are filled with the average value
     * of the pixels; the original square is centred in the output.
     *
     * @param input      The input image
     * @param width      The width of the original image
     * @param paddedSize The size of the output square array (both width and height)
     * @param size       The size of the portion to be extracted from input (both width and height)
     * @param topLeftX   The x-coordinate of the top-left corner of the portion to be extracted
     * @param topLeftY   The y-coordinate of the top-left corner of the portion to be extracted
     * @return          A new square array of size paddedSize × paddedSize containing the padded values
     */
    public static double[] pad(float[] input, int width, int paddedSize, int size, int topLeftX, int topLeftY) {
        float sum = 0;
        for (int y = topLeftY; y < topLeftY + size; y++) {
            for (int x = topLeftX; x < topLeftX + size; x++) {
                int index = y * width + x;
                sum += input[index];
            }
        }
        float area = size * size;
        float avg = sum / area;

        double[] padded = new double[paddedSize * paddedSize];
        fill(padded, avg);

        int padEnd = (paddedSize - size) / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int inIndex = (y + topLeftY) * width + (x + topLeftX);
                int outIndex = (y + padEnd) * paddedSize + x + padEnd;
                padded[outIndex] = input[inIndex];
            }
        }
        return padded;
    }
}
