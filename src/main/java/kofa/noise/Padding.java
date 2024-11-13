package kofa.noise;

public class Padding {
    public static double[] pad(float[] input, int width, int height, int size, int topLeftX, int topLeftY) {
        float sum = 0;
        for (int y = topLeftY; y < topLeftY + size; y++) {
            for (int x = topLeftX; x < topLeftX + size; x++) {
                int index = y * width + x;
                sum += input[index];
            }
        }
        float area = size * size;
        float avg = sum / area;

        int nextPowerOf2 = Integer.highestOneBit(size) * 2;

        double[] padded = new double[nextPowerOf2 * nextPowerOf2];
        for (int i = 0; i < padded.length; i++) {
            padded[i] = avg;
        }

        int padEnd = (nextPowerOf2 - size) / 2;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int inIndex = (y + topLeftY) * width + (x + topLeftX);
                int outIndex = (y + padEnd) * nextPowerOf2 + x + padEnd;
                padded[outIndex] = input[inIndex];
            }
        }
        return padded;
    }
}
