package kofa.io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Loads raw files as greyscale, mosaicked data by decoding them using
 * dcraw -D -4 -T 2024-11-04-11-00-04-P1060036.RW2
 */
public class RawLoader {

    public static BufferedImage decodeRaw(Path rawFilePath) {
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

        BufferedImage bufferedImage;
        int exitCode;
        try {
            Process dcraw = processBuilder.start();

            bufferedImage = readProcessOutput(dcraw);

            // Wait for the process to complete
            exitCode = dcraw.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("Failed do decode " + rawFilePath);
        }

        if (exitCode != 0) {
            throw new RuntimeException("dcraw failed with exit code: " + exitCode);
        }

        return bufferedImage;
    }

    private static BufferedImage readProcessOutput(Process process) throws IOException {
        BufferedImage image;
        try (InputStream dcrawOutput = process.getInputStream()) {
            image = ImageIO.read(dcrawOutput);
        }

        return image;
    }
}
