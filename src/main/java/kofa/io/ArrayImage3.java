package kofa.io;

public class ArrayImage3 {
    private final int width;
    private final float[] channels;

    public ArrayImage3(int width, int height) {
        this.width = width;
        this.channels = new float[3 * width * height];
    }

    public ArrayImage3(int width, int height, float[] channels) {
        int size = 3 * width * height;
        if (channels.length != size) {
            throw new IllegalArgumentException("width: %d, height: %d, channels.length: %d, but should be: %d"
                    .formatted(width, height, channels.length, size)
            );
        }
        this.width = width;
        this.channels = channels;
    }

    public ArrayImage3(int width, int height, float[] r, float[] g, float[] b) {
        int size = width * height;
        if (r.length != size || g.length != size || b.length != size) {
            throw new IllegalArgumentException("width: %d, height: %d, r.length: %d, g.length: %d, b.length: %d, but length should be: %d"
                    .formatted(width, height, r.length, g.length, b.length, size)
            );
        }
        this.width = width;
        channels = new float[3 * size];
        int index = 0;
        for (int i = 0; i < r.length; i++) {
            channels[index] = r[i];
            channels[index + 1] = g[i];
            channels[index + 2] = b[i];
            index += 3;
        }
    }

    public float[] read(int pixelIndex) {
        return new float[]{channels[pixelIndex], channels[pixelIndex + 1], channels[pixelIndex + 2]};
    }

    public float[] read(int x, int y) {
        return read(index(x, y));
    }

    public ArrayImage3 readInto(float[] pixel, int pixelIndex) {
        pixel[0] = channels[pixelIndex];
        pixel[1] = channels[pixelIndex + 1];
        pixel[2] = channels[pixelIndex + 2];
        return this;
    }

    public ArrayImage3 readInto(float[] pixel, int x, int y) {
        return readInto(pixel, index(x, y));
    }

    public ArrayImage3 write(float[] pixel, int pixelIndex) {
        channels[pixelIndex] = pixel[0];
        channels[pixelIndex + 1] = pixel[1];
        channels[pixelIndex + 2] = pixel[2];
        return this;
    }

    public ArrayImage3 write(float[] pixel, int x, int y) {
        return write(pixel, index(x, y));
    }

    public ArrayImage3 write(int pixelIndex, float c1, float c2, float c3) {
        channels[pixelIndex] = c1;
        channels[pixelIndex + 1] = c2;
        channels[pixelIndex + 2] = c3;
        return this;
    }

    public ArrayImage3 write(int x, int y, float c1, float c2, float c3) {
        return write(index(x, y), c1, c2, c3);
    }

    public int index(int x, int y) {
        return 3 * (y * width + x);
    }
}
