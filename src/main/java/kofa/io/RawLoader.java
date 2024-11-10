package kofa.io;

import kofa.colours.model.BayerImage;
import kofa.colours.viewer.GrayscaleImageViewer;
import kofa.colours.viewer.RGBImageViewer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Loads raw files as greyscale, mosaicked data by decoding them using
 * dcraw -D -4 -T 2024-11-04-11-00-04-P1060036.RW2
 */
public class RawLoader {
    public static BufferedImage decodeRaw(Path rawFilePath) throws IOException, InterruptedException {
        // Build the command array
        String[] command = {
                "dcraw",
                "-c",    // Write to stdout
                "-D",    // Document mode (no color interpolation)
                "-4",    // 16-bit linear
                "-T",    // Write TIFF instead of PPM
                rawFilePath.toString()
        };

        // Create and start the process
        ProcessBuilder processBuilder = new ProcessBuilder(command);

        Process dcraw = processBuilder.start();

        BufferedImage image = readProcessOutput(dcraw);

        // Wait for the process to complete
        int exitCode = dcraw.waitFor();
        if (exitCode != 0) {
            throw new IOException("DCRaw process failed with exit code: " + exitCode);
        }
        return image;
    }

    private static BufferedImage readProcessOutput(Process process) throws IOException {
        BufferedImage image;
        try (InputStream dcrawOutput = process.getInputStream()) {
            image = ImageIO.read(dcrawOutput);
        }

        return image;
    }

    public static void main(String[] args) throws Exception {
        Raster raster = decodeRaw(Path.of("/home/kofa/digicam/import/2024-11-04/2024-11-04-11-00-04-P1060036.RW2")).getData();

        BayerImage bayerImage = new BayerImage(raster);
        bayerImage.simpleDemosaic();
        GrayscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.r, "red");
        GrayscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.g1, "g1");
        GrayscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.g2, "g2");
        GrayscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.b, "blue");
        RGBImageViewer.show(bayerImage.simpleDemosaic(), bayerImage.width * 2, bayerImage.height * 2);
    }
}
