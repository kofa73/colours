package kofa.io;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

public class TiffFloat32Output {
    private TiffFloat32Output() {}

    public static void write(String filePrefix, RgbImage image) {
        double[][] red = image.redChannel();
        double[][] green = image.greenChannel();
        double[][] blue = image.blueChannel();

        int width = image.width();
        int height = image.height();

        try {
            writeFloatTIFF(red, green, blue, width, height, tiffFilenameFrom(filePrefix));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String tiffFilenameFrom(String filePrefix) {
        String file;
        if (!filePrefix.toLowerCase().endsWith(".tiff")) {
            file = filePrefix + ".tiff";
        } else {
            file = filePrefix;
        }
        return file;
    }

    private static void writeFloatTIFF(double[][] red, double[][] green, double[][] blue,
                                       int width, int height, String outputPath) throws IOException {

        // 1. Create ColorModel for 32-bit float RGB.
        ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB); // Linear RGB colorspace
        int[] nBits = {32, 32, 32}; // 32 bits per component (R, G, B)
        ComponentColorModel colorModel = new ComponentColorModel(
                colorSpace,
                nBits,
                false, // hasAlpha
                false, // isAlphaPreMultiplied
                ComponentColorModel.OPAQUE, // transparency
                DataBuffer.TYPE_FLOAT // data type
        );

        // 2. Create SampleModel for Pixel Interleaved 32-bit float RGB.
        int[] bandOffsets = {0, 1, 2}; // R, G, B bands are in order
        PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(
                DataBuffer.TYPE_FLOAT,
                width,
                height,
                3,      // pixel stride (components per pixel)
                width * 3, // scanline stride (bytes per row if using byte-based datatypes, elements per row here as it's float)
                bandOffsets
        );

        // 3. Create DataBufferFloat.
        DataBufferFloat dataBuffer = new DataBufferFloat(width * height * 3); // 3 components per pixel

        // 4. Create WritableRaster.
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, null);

        // 5. Create BufferedImage using the custom ColorModel and Raster.
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);


        // 6. Get the DataBuffer to directly manipulate pixel data (already have dataBuffer).
        float[] floatData = dataBuffer.getData();

        // 7. Interleave the red, green, and blue channels into the float array.
        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                floatData[index++] = (float) red[y][x];   // Red channel
                floatData[index++] = (float) green[y][x]; // Green channel
                floatData[index++] = (float) blue[y][x];  // Blue channel
            }
        }

        // 8. Write the BufferedImage to a TIFF file using ImageIO.
        ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(new File(outputPath))) {
            writer.setOutput(ios);
            writer.write(new IIOImage(image, null, null));
        } finally {
            writer.dispose();
        }
        System.out.println("Wrote " + outputPath);
    }
}
