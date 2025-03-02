package kofa.io;

import kofa.colours.tools.SrgbOut;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JpgOutput {
    private JpgOutput() {}

    /**
     * To be called with 'linear sRGB', applies the transfer function.
     * @param filePrefix the base file name, ".jpg" will be appended if needed
     * @param image linear Rec 709 image, all channels already in [0..1]
     */
    public static void write(String filePrefix, RgbImage image) {
        BufferedImage bufferedImage = asBufferedImage(image);
        writeJpg(bufferedImage, filePrefix);
    }

    private static BufferedImage asBufferedImage(RgbImage image) {
        image.transformAllPixels(SrgbOut.SRGB_OUT);
        WritableRaster raster = rasterFrom(image);
        ColorSpace sRgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel colourModel = new ComponentColorModel(sRgb, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        return new BufferedImage(colourModel, raster, colourModel.isAlphaPremultiplied(), null);
    }

    private static WritableRaster rasterFrom(RgbImage image) {
        WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, image.width(), image.height(), 3, null);
        DataBufferByte buffer = (DataBufferByte) raster.getDataBuffer();
        byte[] bankData = buffer.getData();

        double[][] redChannel = image.redChannel();
        double[][] greenChannel = image.greenChannel();
        double[][] blueChannel = image.blueChannel();
        int index = 0;
        int height = image.height();
        int width = image.width();
        for (int row = 0; row < height; row++) {
            for (int column = 0; column < width; column++) {
                try {
                    bankData[index++] = roundPixelToByte(redChannel[row][column]);
                    bankData[index++] = roundPixelToByte(greenChannel[row][column]);
                    bankData[index++] = roundPixelToByte(blueChannel[row][column]);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("%s at (%d, %d)".formatted(e.getMessage(), row, column), e);
                }
            }
        }
        return raster;
    }

    private static byte roundPixelToByte(double pixel) {
        long value = Math.round(255 * pixel);

        if (value > 255 || value < 0) {
            throw new IllegalArgumentException("Out of 8-bit range: " + value);
        }
        return (byte) value;
    }

    private static void writeJpg(BufferedImage image, String filePrefix) {
        String file = jpgFilenameFrom(filePrefix);
        try {
            Files.deleteIfExists(Path.of(file));
            ImageIO.write(image, "jpg", new File(file));
            System.out.println("Wrote " + file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String jpgFilenameFrom(String filePrefix) {
        String file;
        if (!filePrefix.toLowerCase().endsWith(".jpg") && !filePrefix.toLowerCase().endsWith(".jpeg")) {
            file = filePrefix + ".jpg";
        } else {
            file = filePrefix;
        }
        return file;
    }
}
