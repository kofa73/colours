package kofa.noise;

import kofa.colours.model.BayerImage;
import kofa.colours.model.BayerImage2;
import kofa.colours.model.CFA;
import kofa.colours.model.XYCoordinates;
import kofa.colours.viewer.GreyscaleImageViewer;
import kofa.colours.viewer.RGBImageViewer;
import org.HdrHistogram.Histogram;

import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.DoubleStream;

import static java.lang.Float.parseFloat;
import static java.util.Arrays.setAll;
import static kofa.io.RawLoader.decodeRaw;
import static kofa.noise.SpectralPowerCalculator.SCALE;

/**
 * Loads raw files as greyscale, mosaicked data by decoding them using
 * dcraw -D -4 -T 2024-11-04-11-00-04-P1060036.RW2
 */
public class NoiseExperiment {

    private static final int FILTER_SIZE = 16;
    private static final int SEARCH_SEC = 5;
    private static final boolean SHOW_PANES = false;
    private final Path rawFilePath;
    private final CFA cfa;
    private final float rMultiplier;
    private final float bMultiplier;
    private final float additionalGamma;

    public NoiseExperiment(Path rawFilePath, CFA cfa, float rMultiplier, float bMultiplier, float additionalGamma) {
        this.cfa = cfa;
        if (!Files.isRegularFile(rawFilePath)) {
            die("Not a regular file: " + rawFilePath);
        }
        this.rawFilePath = rawFilePath;
        this.rMultiplier = rMultiplier;
        this.bMultiplier = bMultiplier;
        this.additionalGamma = additionalGamma;
    }

    public Raster load() {
        return decodeRaw(rawFilePath).getData();
    }

    public static void main(String[] args) {
        if (args.length != 5) {
            die("");
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

        var experiment = new NoiseExperiment(
                Path.of(args[0]),
                parseCFA(args[1]),
                rMultiplier,
                bMultiplier,
                additionalGamma
        );

        experiment.run();
    }

    private void run() {
        Raster raster = load();

        var bayerImage = new BayerImage(raster, cfa, rMultiplier, bMultiplier);
        var bayerImage2 = new BayerImage2(raster, cfa, rMultiplier, bMultiplier);

        float[] data = bayerImage2.bilinearDemosaic();
        RGBImageViewer.show("original", data, bayerImage.paneWidth * 2 + 32, bayerImage.paneHeight * 2 + 32, additionalGamma);
        try {
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        float accumulator = 0;
        for (float value : data) {
            accumulator += value;
        }

        float originalSum = accumulator;

        SpectralPowerCalculator.Result smoothest0 = new SpectralPowerCalculator.Result(new XYCoordinates(0, 0), Double.MAX_VALUE, new double[0]);
        SpectralPowerCalculator.Result smoothest1 = smoothest0;
        SpectralPowerCalculator.Result smoothest2 = smoothest0;
        SpectralPowerCalculator.Result smoothest3 = smoothest0;

        long start = System.currentTimeMillis();
        long stop = start + SEARCH_SEC * 1_000;
        long counter = 0;

        ForkJoinPool threadPool = ForkJoinPool.commonPool();

        var spCalculator0 = new SpectralPowerCalculator(bayerImage.pane0, bayerImage.paneWidth, bayerImage.paneHeight, FILTER_SIZE);
        var spCalculator1 = new SpectralPowerCalculator(bayerImage.pane1, bayerImage.paneWidth, bayerImage.paneHeight, FILTER_SIZE);
        var spCalculator2 = new SpectralPowerCalculator(bayerImage.pane2, bayerImage.paneWidth, bayerImage.paneHeight, FILTER_SIZE);
        var spCalculator3 = new SpectralPowerCalculator(bayerImage.pane3, bayerImage.paneWidth, bayerImage.paneHeight, FILTER_SIZE);

        SpectralPowerCalculator.Result result0;
        SpectralPowerCalculator.Result result1;
        SpectralPowerCalculator.Result result2;
        SpectralPowerCalculator.Result result3;
        do {
            ForkJoinTask<SpectralPowerCalculator.Result> future0 = threadPool.submit(() -> spCalculator0.measureRandomSquare());
            ForkJoinTask<SpectralPowerCalculator.Result> future1 = threadPool.submit(() -> spCalculator1.measureRandomSquare());
            ForkJoinTask<SpectralPowerCalculator.Result> future2 = threadPool.submit(() -> spCalculator2.measureRandomSquare());
            ForkJoinTask<SpectralPowerCalculator.Result> future3 = threadPool.submit(() -> spCalculator3.measureRandomSquare());
            try {
                result0 = future0.get();
                result1 = future1.get();
                result2 = future2.get();
                result3 = future3.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
            if (result0.power() < smoothest0.power() && result0.power() > 1) {
                double improvement = 1 - result0.power() / smoothest0.power();
                System.out.println((System.currentTimeMillis() - start) + " Found smoother pane0 area at " + result0.coordinates() + " with power " + result0.power() + ", improvement: " + improvement);
                smoothest0 = result0;
            }
            if (result1.power() < smoothest1.power() && result1.power() > 1) {
                double improvement = 1 - result1.power() / smoothest1.power();
                System.out.println((System.currentTimeMillis() - start) + " Found smoother pane1 area at " + result1.coordinates() + " with power " + result1.power() + ", improvement: " + improvement);
                smoothest1 = result1;
            }
            if (result2.power() < smoothest2.power() && result2.power() > 1) {
                double improvement = 1 - result2.power() / smoothest2.power();
                System.out.println((System.currentTimeMillis() - start) + " Found smoother pane2 area at " + result2.coordinates() + " with power " + result2.power() + ", improvement: " + improvement);
                smoothest2 = result2;
            }
            if (result3.power() < smoothest3.power() && result3.power() > 1) {
                double improvement = 1 - result3.power() / smoothest3.power();
                System.out.println((System.currentTimeMillis() - start) + " Found smoother pane3 area at " + result3.coordinates() + " with power " + result3.power() + ", improvement: " + improvement);
                smoothest3 = result3;
            }
            counter++;
        } while (System.currentTimeMillis() < stop);
        System.out.println("Total samples evaluated: " + counter);

        var filter = new SpectrumSubtractingFilter(bayerImage.paneWidth, bayerImage.paneHeight, FILTER_SIZE);

        filterAndShow(
                "filtered using smoothest",
                filter,
                bayerImage.clone(),
                smoothest0.magnitudes(), smoothest1.magnitudes(), smoothest2.magnitudes(), smoothest3.magnitudes(),
                originalSum
        );

        DoubleStream.of(1, 2, 5, 10, 20, 25, 50, 90).forEach(percentile -> {
            double[] magnitudes0 = new double[spCalculator0.histogramByFrequencyIndex.length];
            double[] magnitudes1 = new double[spCalculator0.histogramByFrequencyIndex.length];
            double[] magnitudes2 = new double[spCalculator0.histogramByFrequencyIndex.length];
            double[] magnitudes3 = new double[spCalculator0.histogramByFrequencyIndex.length];
            setAll(magnitudes0, index -> spCalculator0.histogramByFrequencyIndex[index].getValueAtPercentile(percentile) / SCALE);
            setAll(magnitudes1, index -> spCalculator1.histogramByFrequencyIndex[index].getValueAtPercentile(percentile) / SCALE);
            setAll(magnitudes2, index -> spCalculator2.histogramByFrequencyIndex[index].getValueAtPercentile(percentile) / SCALE);
            setAll(magnitudes3, index -> spCalculator3.histogramByFrequencyIndex[index].getValueAtPercentile(percentile) / SCALE);

            filterAndShow(
                    "filtered using values at percentile " + percentile,
                    filter,
                    bayerImage.clone(),
                    magnitudes0, magnitudes1, magnitudes2, magnitudes3,
                    originalSum
            );
        });
        DoubleStream.of(1_000, 2_000, 4_000, 5_000).forEach(divisor -> {
            double[] magnitudes0 = new double[spCalculator0.histogramByFrequencyIndex.length];
            double[] magnitudes1 = new double[spCalculator0.histogramByFrequencyIndex.length];
            double[] magnitudes2 = new double[spCalculator0.histogramByFrequencyIndex.length];
            double[] magnitudes3 = new double[spCalculator0.histogramByFrequencyIndex.length];
            setAll(magnitudes0, index -> spCalculator0.histogramByFrequencyIndex[index].getMaxValue() / SCALE / divisor);
            setAll(magnitudes1, index -> spCalculator1.histogramByFrequencyIndex[index].getMaxValue() / SCALE / divisor);
            setAll(magnitudes2, index -> spCalculator2.histogramByFrequencyIndex[index].getMaxValue() / SCALE / divisor);
            setAll(magnitudes3, index -> spCalculator3.histogramByFrequencyIndex[index].getMaxValue() / SCALE / divisor);

            filterAndShow(
                    "filtered using max / " + divisor,
                    filter,
                    bayerImage.clone(),
                    magnitudes0, magnitudes1, magnitudes2, magnitudes3,
                    originalSum
            );
        });

        DoubleStream.of(2, 3, 5, 10, 100, 1_000).forEach(divisor -> {
            double[] magnitudes0 = new double[spCalculator0.histogramByFrequencyIndex.length];
            double[] magnitudes1 = new double[spCalculator0.histogramByFrequencyIndex.length];
            double[] magnitudes2 = new double[spCalculator0.histogramByFrequencyIndex.length];
            double[] magnitudes3 = new double[spCalculator0.histogramByFrequencyIndex.length];
            setAll(magnitudes0, index -> spCalculator0.histogramByFrequencyIndex[index].getValueAtPercentile(50) / SCALE / divisor);
            setAll(magnitudes1, index -> spCalculator1.histogramByFrequencyIndex[index].getValueAtPercentile(50) / SCALE / divisor);
            setAll(magnitudes2, index -> spCalculator2.histogramByFrequencyIndex[index].getValueAtPercentile(50) / SCALE / divisor);
            setAll(magnitudes3, index -> spCalculator3.histogramByFrequencyIndex[index].getValueAtPercentile(50) / SCALE / divisor);

            filterAndShow(
                    "filtered using median / " + divisor,
                    filter,
                    bayerImage.clone(),
                    magnitudes0, magnitudes1, magnitudes2, magnitudes3,
                    originalSum
            );
        });

        System.out.println("\nStats for all pane0 samples:");
        for (int i = 0; i < spCalculator0.histogramByFrequencyIndex.length; i++) {
            Histogram histogramAtFreqencyIndex = spCalculator0.histogramByFrequencyIndex[i];
            System.out.println("freq[%d]:\tmin: %f,\tp10: %f,\tp25: %f,\tp50: %f,\tp75: %f,\tp90: %f,\tp95: %f,\tp99: %f,\tp99.9: %f,\tmax: %f,\tpercentiles for\tmax/10: %f,\tmax/100: %f,\tmax/1000: %f".formatted(
                    i,
                    histogramAtFreqencyIndex.getMinValue() / SCALE, // min
                    histogramAtFreqencyIndex.getValueAtPercentile(10) / SCALE,
                    histogramAtFreqencyIndex.getValueAtPercentile(25) / SCALE,
                    histogramAtFreqencyIndex.getValueAtPercentile(50) / SCALE,
                    histogramAtFreqencyIndex.getValueAtPercentile(75) / SCALE,
                    histogramAtFreqencyIndex.getValueAtPercentile(90) / SCALE,
                    histogramAtFreqencyIndex.getValueAtPercentile(95) / SCALE,
                    histogramAtFreqencyIndex.getValueAtPercentile(99) / SCALE,
                    histogramAtFreqencyIndex.getValueAtPercentile(99.9) / SCALE,
                    histogramAtFreqencyIndex.getMaxValue() / SCALE,
                    histogramAtFreqencyIndex.getPercentileAtOrBelowValue(histogramAtFreqencyIndex.getMaxValue() / 10),
                    histogramAtFreqencyIndex.getPercentileAtOrBelowValue(histogramAtFreqencyIndex.getMaxValue() / 100),
                    histogramAtFreqencyIndex.getPercentileAtOrBelowValue(histogramAtFreqencyIndex.getMaxValue() / 1000)
            ));
        }

        System.out.println("\nStats for 'smoothest' pane0 sample:");
        double[] magnitudes0 = smoothest0.magnitudes();
        for (int i = 0; i < magnitudes0.length; i++) {
            System.out.println("freq[%d]: %f, percentile: %f".formatted(i, magnitudes0[i], spCalculator0.histogramByFrequencyIndex[i].getPercentileAtOrBelowValue((long) (magnitudes0[i] * SCALE))));
        }
    }

    private void filterAndShow(
            String title,
            SpectrumSubtractingFilter filter,
            BayerImage bayerImage,
            double[] magnitudes0,
            double[] magnitudes1,
            double[] magnitudes2,
            double[] magnitudes3,
            float originalSum
    ) {
        long filterStartMillis = System.currentTimeMillis();
        filter.filter(bayerImage.pane0, magnitudes0);
        filter.filter(bayerImage.pane1, magnitudes1);
        filter.filter(bayerImage.pane2, magnitudes2);
        filter.filter(bayerImage.pane3, magnitudes3);
        long filterDurationms = System.currentTimeMillis() - filterStartMillis;
        System.out.println("filtering %d x %d took %d ms".formatted(bayerImage.paneWidth, bayerImage.paneHeight, filterDurationms));

        if (SHOW_PANES) {
            GreyscaleImageViewer.show(bayerImage.paneWidth, bayerImage.paneHeight, bayerImage.pane0, "g1");
            GreyscaleImageViewer.show(bayerImage.paneWidth, bayerImage.paneHeight, bayerImage.pane1, "pane1");
            GreyscaleImageViewer.show(bayerImage.paneWidth, bayerImage.paneHeight, bayerImage.pane2, "pane2");
            GreyscaleImageViewer.show(bayerImage.paneWidth, bayerImage.paneHeight, bayerImage.pane3, "pane3");
        }
        long debayerStartMillis = System.currentTimeMillis();
        float[] filtered = bayerImage.simpleDemosaic();
        long debayerDurationms = System.currentTimeMillis() - debayerStartMillis;
        int debayeredWidth = 2 * bayerImage.paneWidth;
        int debayeredHeight = 2 * bayerImage.paneHeight;
        System.out.println("debayering %d x %d took %d ms".formatted(debayeredWidth, debayeredHeight, debayerDurationms));

        float filteredSum = 0;
        for (float value : filtered) {
            filteredSum += value;
        }
        float multiplier = originalSum / filteredSum;
        System.out.println("Post-filter multiplier: " + multiplier);
        for (int i = 0; i < filtered.length; i++) {
            filtered[i] *= multiplier;
        }
        RGBImageViewer.show(title, filtered, debayeredWidth, debayeredHeight, additionalGamma);
    }

    private static CFA parseCFA(String name) {
        CFA cfa = null;
        try {
            cfa = CFA.valueOf(name);
        } catch (IllegalArgumentException e) {
            die("Cannot parse " + name);
        }
        return cfa;
    }

    private static void die(String message) {
        if (message != null && !message.isBlank()) {
            System.out.println(message);
        }
        System.out.println("RawLoader path-to-raw (RGGB|GRBG) rMultiplier bMultiplier additionalGamma");
        System.exit(-1);
    }
}
