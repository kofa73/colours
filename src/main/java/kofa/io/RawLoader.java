package kofa.io;

import kofa.colours.model.BayerImage;
import kofa.colours.model.XYCoordinates;
import kofa.colours.viewer.GreyscaleImageViewer;
import kofa.colours.viewer.RGBImageViewer;
import kofa.noise.SmoothFinder;
import kofa.noise.SpectrumSubtractingFilter;
import org.apache.commons.math3.complex.Complex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

import static java.lang.Float.parseFloat;

/**
 * Loads raw files as greyscale, mosaicked data by decoding them using
 * dcraw -D -4 -T 2024-11-04-11-00-04-P1060036.RW2
 */
public class RawLoader {

    private static final int FILTER_SIZE = 16;
    private static final int SEARCH_SEC = 10;
    private static final boolean SHOW_PANES = false;

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

        float[] data = bayerImage.simpleDemosaic(cfa);
        float originalSum = 0;
        for (float value : data) {
            originalSum += value;
        }
        RGBImageViewer.show("original", data, bayerImage.width * 2, bayerImage.height * 2, additionalGamma);

        SmoothFinder.Result smoothest0 = new SmoothFinder.Result(new XYCoordinates(0, 0), Double.MAX_VALUE, new Complex[0]);
        SmoothFinder.Result smoothest1 = smoothest0;
        SmoothFinder.Result smoothest2 = smoothest0;
        SmoothFinder.Result smoothest3 = smoothest0;

        long start = System.currentTimeMillis();
        long stop = start + SEARCH_SEC * 1_000;
        long counter = 0;

        ForkJoinPool threadPool = ForkJoinPool.commonPool();

        while (System.currentTimeMillis() < stop) {
            ForkJoinTask<SmoothFinder.Result> future0 = threadPool.submit(() -> SmoothFinder.findSmoothSquare(bayerImage.pane0, bayerImage.width, bayerImage.height, FILTER_SIZE));
            ForkJoinTask<SmoothFinder.Result> future1 = threadPool.submit(() -> SmoothFinder.findSmoothSquare(bayerImage.pane1, bayerImage.width, bayerImage.height, FILTER_SIZE));
            ForkJoinTask<SmoothFinder.Result> future2 = threadPool.submit(() -> SmoothFinder.findSmoothSquare(bayerImage.pane2, bayerImage.width, bayerImage.height, FILTER_SIZE));
            ForkJoinTask<SmoothFinder.Result> future3 = threadPool.submit(() -> SmoothFinder.findSmoothSquare(bayerImage.pane3, bayerImage.width, bayerImage.height, FILTER_SIZE));
            SmoothFinder.Result result0 = future0.get();
            SmoothFinder.Result result1 = future1.get();
            SmoothFinder.Result result2 = future2.get();
            SmoothFinder.Result result3 = future3.get();
            if (result0.power() < smoothest0.power() && result0.power() > 1) {
                double improvement = 1 - result0.power() / smoothest0.power();
                System.out.println((System.currentTimeMillis() - start) + " Found smooth pane0 area at " + result0.coordinates() + " with power " + result0.power() + ", improvement: " + improvement);
                smoothest0 = result0;
            }
            if (result1.power() < smoothest1.power() && result1.power() > 1) {
                double improvement = 1 - result1.power() / smoothest1.power();
                System.out.println((System.currentTimeMillis() - start) + " Found smooth pane1 area at " + result1.coordinates() + " with power " + result1.power() + ", improvement: " + improvement);
                smoothest1 = result1;
            }
            if (result2.power() < smoothest2.power() && result2.power() > 1) {
                double improvement = 1 - result2.power() / smoothest2.power();
                System.out.println((System.currentTimeMillis() - start) + " Found smooth pane2 area at " + result2.coordinates() + " with power " + result2.power() + ", improvement: " + improvement);
                smoothest2 = result2;
            }
            if (result3.power() < smoothest3.power() && result3.power() > 1) {
                double improvement = 1 - result3.power() / smoothest3.power();
                System.out.println((System.currentTimeMillis() - start) + " Found smooth pane3 area at " + result3.coordinates() + " with power " + result3.power() + ", improvement: " + improvement);
                smoothest3 = result3;
            }
            counter++;
        }
        System.out.println("Total samples evaluated: " + counter);

        var filter = new SpectrumSubtractingFilter(bayerImage.width, bayerImage.height, FILTER_SIZE);

        filter.filter(bayerImage.pane0, smoothest0.magnitudes());
        filter.filter(bayerImage.pane1, smoothest1.magnitudes());
        filter.filter(bayerImage.pane2, smoothest2.magnitudes());
        filter.filter(bayerImage.pane3, smoothest3.magnitudes());

        if (SHOW_PANES) {
            GreyscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.pane0, "g1");
            GreyscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.pane1, "pane1");
            GreyscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.pane2, "pane2");
            GreyscaleImageViewer.show(bayerImage.width, bayerImage.height, bayerImage.pane3, "pane3");
        }
        float[] filtered = bayerImage.simpleDemosaic(cfa);

        float filteredSum = 0;
        for (float value : filtered) {
            filteredSum += value;
        }
        float multiplier = originalSum / filteredSum;
        System.out.println("Post-filter multiplier: " + multiplier);
        for (int i = 0; i < filtered.length; i++) {
            filtered[i] *= multiplier;
        }
        RGBImageViewer.show("filtered", filtered, bayerImage.width * 2, bayerImage.height * 2, additionalGamma);
    }

    private static void die(String message) {
        if (message != null && !message.isBlank()) {
            System.out.println(message);
        }
        System.out.println("RawLoader path-to-raw (RGGB|GRBG) rMultiplier bMultiplier additionalGamma");
        System.exit(-1);
    }
}
