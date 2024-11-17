package kofa.noise;

public class Padding {
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
        for (int i = 0; i < padded.length; i++) {
            padded[i] = avg;
        }

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
