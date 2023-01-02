package kofa.io;

import java.awt.image.Raster;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.awt.image.DataBuffer.*;

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
            case TYPE_FLOAT, TYPE_DOUBLE -> 1;
            default -> throw new IllegalArgumentException("Unsupported data type: " + dataType);
        };
        forEachPixel((row, column, ignoredRed, ignoredGreen, ignoredBlue) -> {
                    double[] components = new double[3];
                    raster.getPixel(column, row, components);
                    redChannel[row][column] = components[0] / divisor;
                    greenChannel[row][column] = components[1] / divisor;
                    blueChannel[row][column] = components[2] / divisor;
                }
        );
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

    public int size() {
        return width * height;
    }

    public interface PixelConsumer {
        void consume(int row, int column, double red, double green, double blue);
    }

    public interface PixelTransformer {
        double[] transform(int row, int column, double red, double green, double blue);
    }

    public void forEachPixel(PixelConsumer consumer) {
        IntStream.range(0, height)
                .parallel()
                .forEach(row -> {
                    for (int column = 0; column < raster.getWidth(); column++) {
                        consumer.consume(
                                row, column,
                                redChannel[row][column],
                                greenChannel[row][column],
                                blueChannel[row][column]
                        );
                    }
                });
    }

    public void transformAllPixels(PixelTransformer transformer) {
        forEachPixel((row, column, red, green, blue) -> {
                    double[] transformed = transformer.transform(
                            row, column,
                            redChannel[row][column],
                            greenChannel[row][column],
                            blueChannel[row][column]
                    );
                    redChannel[row][column] = transformed[0];
                    greenChannel[row][column] = transformed[1];
                    blueChannel[row][column] = transformed[2];
                }
        );
    }

    public Stream<double[]> pixelStream() {
        return IntStream.range(0, height)
                .parallel()
                .mapToObj(row ->
                        IntStream.range(0, width).mapToObj(column ->
                                new double[]{
                                        redChannel[row][column],
                                        greenChannel[row][column],
                                        blueChannel[row][column]
                                })
                ).flatMap(Function.identity());
    }
}
