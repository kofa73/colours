package kofa.io;

import kofa.colours.model.BayerImage;
import kofa.colours.viewer.GrayscaleImageViewer;
import kofa.colours.viewer.RGBImageViewer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.lang.Float.parseFloat;

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
        if (args.length != 5) {
            die("");
        }
        Path rawFilePath = Path.of(args[0]);
        if (!Files.isRegularFile(rawFilePath)) {
            die("Not a regular file: " + rawFilePath);
        }
        BayerImage.CFA cfa = null;
        try {
            cfa = BayerImage.CFA.valueOf(args[1]);
        } catch (IllegalArgumentException e) {
            die("Cannot parse " + args[1]);
        }
        float rMultiplier = 1;
        float bMultiplier = 1;
        float additionalGamma = 0;
        try {
             rMultiplier = parseFloat(args[2]);
             bMultiplier = parseFloat(args[3]);
            additionalGamma = parseFloat(args[4]);
        } catch (NumberFormatException e) {
            die("Cannot parse %s, %s, %s".formatted(args[1], args[2], args[3]));
        }
        Raster raster = decodeRaw(rawFilePath).getData();

        BayerImage bayerImage = new BayerImage(raster, rMultiplier, bMultiplier);
        GrayscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.pane1, "red");
        GrayscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.pane0, "g1");
        GrayscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.pane3, "g2");
        GrayscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.pane2, "blue");
        float[] data = bayerImage.simpleDemosaic(cfa);

        RGBImageViewer.show("simple", data, bayerImage.width * 2, bayerImage.height * 2, additionalGamma);
    }

    private static void die(String message) {
        if (message != null && !message.isBlank()) {
            System.out.println(message);
        }
        System.out.println("RawLoader path-to-raw (RGGB|GRBG) rMultiplier bMultiplier additionalGamma");
        System.exit(-1);
    }
}
