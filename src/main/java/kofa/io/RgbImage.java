package kofa.io;

import java.awt.image.Raster;
import java.util.stream.IntStream;

public class RgbImage {
    private final Raster raster;
    private final double[][] redChannel;
    private final double[][] greenChannel;
    private final double[][] blueChannel;
    private final int height;
    private final int width;

    public RgbImage(Raster raster) {
        this.raster = raster;
        this.width = raster.getWidth();
        this.height = raster.getHeight();
        redChannel = new double[height][width];
        greenChannel = new double[height][width];
        blueChannel = new double[height][width];
        init();
    }

    public void init() {
        IntStream.range(0, height).parallel().forEach(row -> {
            double[] pixel = new double[3];
            for (int column = 0; column < raster.getWidth(); column++) {
                raster.getPixel(column, row, pixel);
                redChannel[row][column] = pixel[0] / 65535.0;
                greenChannel[row][column] = pixel[1] / 65535.0;
                blueChannel[row][column] = pixel[2] / 65535.0;
            }
        });
    }

    public double[][] redChannel() {
        return redChannel;
    }

    public double[][] greenChannel() {
        return greenChannel;
    }

    public double[][] blueChannel() {
        return blueChannel;
    }

    public int width() {
        return width;
    }
    
    public int height() {
        return height;
    }
}
