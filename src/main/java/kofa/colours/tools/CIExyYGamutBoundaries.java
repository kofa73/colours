package kofa.colours.tools;

import kofa.colours.spaces.Rec709;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static java.util.Arrays.fill;
import static kofa.colours.spaces.CIExyY.*;
import static kofa.colours.spaces.Rec709.XYZ_to_rec709;
import static kofa.maths.MathHelpers.vec3;

public class CIExyYGamutBoundaries {
    private static double TWO_PI = 2 * Math.PI;
    private static final double STARTING_MAX_DISTANCE = 2;
    // the distance of the gamut boundary from the white point, by luma and polar index
    private final int lumaResolution;
    private final int chromaResolution;
    private final double lumaStep;
    private final double polarStep;
    private final double maxY;
    private final BiConsumer<double[], double[]> XYZ_to_rgb;
    private final double whitePoint_x;
    private final double whitePoint_y;

    public CIExyYGamutBoundaries(int lumaResolution, int chromaResolution) {
        this(lumaResolution, chromaResolution, 1);
    }

    public CIExyYGamutBoundaries(int lumaResolution, int chromaResolution, double maxY) {
        this.lumaResolution = lumaResolution;
        this.chromaResolution = chromaResolution;
        this.maxY = maxY;
        lumaStep = maxY / lumaResolution;
        polarStep = TWO_PI / chromaResolution;
        XYZ_to_rgb = Rec709::XYZ_to_rec709;
        whitePoint_x = D65_WHITE_2DEG_x;
        whitePoint_y = D65_WHITE_2DEG_y;
    }

    public double[][] findRec709GamutBoundaries() {
        double[][] boundaries = new double[lumaResolution][chromaResolution];
        for (double[] boundariesForLuma : boundaries) {
            fill(boundariesForLuma, STARTING_MAX_DISTANCE);
        }
        fill(boundaries[0], 0);

        long start = System.currentTimeMillis();

        // start with indexY = 1, skip black
        IntStream.range(1, lumaResolution).parallel().forEach(indexY -> {
            double[] rgb = vec3();
            double[] XYZ = vec3();
            double[] xyY = vec3();
            double Y = indexY * lumaStep;
            xyY[2] = Y;
            for (int indexPolar = 0; indexPolar < chromaResolution; indexPolar++) {
                double minDistanceForThisRound = findBoundaryDistance(indexPolar, xyY, XYZ, rgb);
                synchronized (boundaries[indexY]) {
                    boundaries[indexY][indexPolar] = min(boundaries[indexY][indexPolar], minDistanceForThisRound);
                }
            }
        });
        System.out.println("Gamut search took %d ms".formatted(System.currentTimeMillis() - start));
        return boundaries;
    }

    private double findBoundaryDistance(int indexPolar, double[] xyY, double[] XYZ, double[] rgb) {
        double angle = polarStep * indexPolar;

        double minDistanceForThisRound = 0;
        double maxDistanceForThisRound = STARTING_MAX_DISTANCE;
        double distanceFromNeutral = (minDistanceForThisRound + maxDistanceForThisRound) / 2;
        double cos = cos(angle);
        double sin = sin(angle);
        int count = 0;
        do {
            count++;
            double x = distanceFromNeutral * cos + whitePoint_x;
            double y = distanceFromNeutral * sin + whitePoint_y;
            xyY[0] = x;
            xyY[1] = y;
            xyY_to_XYZ(xyY, XYZ);
            XYZ_to_rgb.accept(XYZ, rgb);
            boolean outOfGamut = outOfGamut(rgb);
            if (outOfGamut) {
                maxDistanceForThisRound = distanceFromNeutral;
            } else {
                minDistanceForThisRound = distanceFromNeutral;
            }
            distanceFromNeutral = (minDistanceForThisRound + maxDistanceForThisRound) / 2;
        } while (maxDistanceForThisRound > minDistanceForThisRound
                && count < 100 // break out if no solution found
                && (distanceFromNeutral != maxDistanceForThisRound || distanceFromNeutral != minDistanceForThisRound));

        return minDistanceForThisRound;
    }

    public static void main(String[] args) {
        int lumaResolution = 4096;
        int chromaResolution = 4096;
        CIExyYGamutBoundaries finder = new CIExyYGamutBoundaries(lumaResolution, chromaResolution);
        double[][] boundaries = finder.findRec709GamutBoundaries();

        finder.testBoundaries(boundaries);
    }

    private void testBoundaries(double[][] boundaries) {
        double[] xyY = new double[3];
        double[] XYZ = new double[3];
        double[] rgb = new double[3];

        //System.out.println("=== final result ===");

        StringBuilder result = new StringBuilder();
        StringBuilder outOfGamut = new StringBuilder();
        StringBuilder notOnGamutBoundary = new StringBuilder();
        for (int indexY = 0; indexY < lumaResolution; indexY++) {
            double Y = 1f / lumaResolution * indexY;
            xyY[2] = Y;
            double[] boundariesForLuma = boundaries[indexY];
            for (int indexPolar = 0; indexPolar < chromaResolution; indexPolar++) {
                double angle = TWO_PI / chromaResolution * indexPolar;
                double distanceFromNeutral = boundariesForLuma[indexPolar];
                double x = (distanceFromNeutral * cos(angle) + whitePoint_x);
                double y = (distanceFromNeutral * sin(angle) + whitePoint_y);
                xyY[0] = x;
                xyY[1] = y;
                xyY_to_XYZ(xyY, XYZ);
                XYZ_to_rgb.accept(XYZ, rgb);
                result.append("xyY: %.20f, %.20f, %.20f; radius: %.20f, angle: %.20f, rgb: %s\n".formatted(xyY[0], xyY[1], xyY[2], distanceFromNeutral, angle, Arrays.toString(rgb)));

                if (outOfGamut(rgb)) {
                    // a few of these can happen: we did not scan directly using coordinates derived from the index; instead,
                    // we scanned at a higher resolution and then derived the index from the coordinates by rounding
                    outOfGamut.append("Out of gamut: indexY: %d, indexPolar: %d, Y: %.20f, angle: %.20f, distanceFromNeutral: %.20f, xyY: %s, rgb: %s\n"
                            .formatted(indexY, indexPolar, Y, angle, distanceFromNeutral, Arrays.toString(xyY), Arrays.toString(rgb)));
                } else {
                    if (
                            0.01 < rgb[0] && rgb[0] < 0.99
                                    && 0.01 < rgb[1] && rgb[1] < 0.99
                                    && 0.01 < rgb[2] && rgb[2] < 0.99
                    ) {
                        notOnGamutBoundary.append("Colour is not on gamut boundary: " + Arrays.toString(rgb) + "\n");
                    }
                }
            }
        }

        System.out.println("Look-up table:");
        System.out.println(result);

        System.out.println("Sanity checks:");
        System.out.println(outOfGamut);
        System.out.println(notOnGamutBoundary);
    }

    private boolean outOfGamut(double[] rgb) {
        return rgb[0] < 0 || rgb[1] < 0 || rgb[2] < 0
                || rgb[0] > maxY || rgb[1] > maxY || rgb[2] > maxY;
    }
}
