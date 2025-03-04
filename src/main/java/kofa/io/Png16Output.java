package kofa.io;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Png16Output {
    private Png16Output() {}

    public static void write(String filePrefix, RgbImage image) {
        BufferedImage bufferedImage = asBufferedImage(image);
        writePng(bufferedImage, filePrefix);
    }

    private static BufferedImage asBufferedImage(RgbImage image) {
        WritableRaster raster = rasterFrom(image);
        ColorSpace linearRgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel colourModel = new ComponentColorModel(linearRgb, false, false, Transparency.OPAQUE, DataBuffer.TYPE_USHORT);
        return new BufferedImage(colourModel, raster, colourModel.isAlphaPremultiplied(), null);
    }

    private static WritableRaster rasterFrom(RgbImage image) {
        WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_USHORT, image.width(), image.height(), 3, null);
        DataBufferUShort buffer = (DataBufferUShort) raster.getDataBuffer();
        short[][] bankData = buffer.getBankData();

        double[][] redChannel = image.redChannel();
        double[][] greenChannel = image.greenChannel();
        double[][] blueChannel = image.blueChannel();
        int index = 0;
        int height = image.height();
        int width = image.width();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                try {
                    bankData[0][index++] = roundPixelToShort(redChannel[row][column]);
                    bankData[0][index++] = roundPixelToShort(greenChannel[row][column]);
                    bankData[0][index++] = roundPixelToShort(blueChannel[row][column]);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("%s at (%d, %d)".formatted(e.getMessage(), row, column), e);
                }
            }
        }
        return raster;
    }

    private static short roundPixelToShort(double pixel) {
        long value = Math.round(65535 * pixel);

        if (value > 65535 || value < 0) {
            throw new IllegalArgumentException("Out of 16-bit range: " + value);
        }
        return (short) value;
    }

    private static void writePng(BufferedImage image, String filePrefix) {
        String file = pngFilenameFrom(filePrefix);
        try {
            Files.deleteIfExists(Path.of(file));
            ImageIO.write(image, "png", new File(file));
            System.out.println("Wrote " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String pngFilenameFrom(String filePrefix) {
        String file;
        if (!filePrefix.toLowerCase().endsWith(".png")) {
            file = filePrefix + ".png";
        } else {
            file = filePrefix;
        }
        return file;
    }
}
