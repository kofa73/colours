package kofa.io;

import java.awt.image.Raster;
import java.util.stream.IntStream;

import static java.awt.image.DataBuffer.TYPE_BYTE;
import static java.awt.image.DataBuffer.TYPE_USHORT;

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
        var dataType = raster.getDataBuffer().getDataType();
        double divisor = switch (dataType) {
            case TYPE_BYTE -> 255;
            case TYPE_USHORT -> 65536;
            // case TYPE_FLOAT, TYPE_DOUBLE -> ???
            default -> throw new IllegalArgumentException("Unsupported data type: " + dataType);
        };
        IntStream.range(0, height).parallel().forEach(row -> {
            double[] pixel = new double[3];
            for (int column = 0; column < raster.getWidth(); column++) {
                raster.getPixel(column, row, pixel);
                redChannel[row][column] = pixel[0] / divisor;
                greenChannel[row][column] = pixel[1] / divisor;
                blueChannel[row][column] = pixel[2] / divisor;
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
