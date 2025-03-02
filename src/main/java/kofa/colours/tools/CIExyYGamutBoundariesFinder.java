package kofa.colours.tools;

import kofa.colours.spaces.Rec2020;
import kofa.colours.spaces.Rec709;
import kofa.colours.spaces.SpaceParameters;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static java.util.Arrays.fill;
import static kofa.colours.spaces.CIExyY.xyY_to_XYZ;
import static kofa.maths.MathHelpers.vec3;

public class CIExyYGamutBoundariesFinder {
    private static final double TWO_PI = 2 * Math.PI;
    private static final double STARTING_MAX_DISTANCE = 2;

    public static double[][] findRgbGamutBoundaries(int lumaResolution, int chromaResolution, SpaceParameters spaceParameters) {
        double lumaStep = 1.0 / lumaResolution;
        double polarStep = TWO_PI / chromaResolution;
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
                double angle = polarStep * indexPolar;
                double minDistanceForThisRound = findBoundaryDistance(angle, xyY, XYZ, rgb, spaceParameters);
                synchronized (boundaries[indexY]) {
                    boundaries[indexY][indexPolar] = min(boundaries[indexY][indexPolar], minDistanceForThisRound);
                }
            }
        });
        System.out.println("Gamut search took %d ms".formatted(System.currentTimeMillis() - start));
        return boundaries;
    }

    private static double findBoundaryDistance(
            double angle, double[] xyY, double[] XYZ, double[] rgb, SpaceParameters spaceParameters
    ) {

        double minDistanceForThisRound = 0;
        double maxDistanceForThisRound = STARTING_MAX_DISTANCE;
        double distanceFromNeutral = (minDistanceForThisRound + maxDistanceForThisRound) / 2;
        double cos = cos(angle);
        double sin = sin(angle);
        int count = 0;
        do {
            count++;
            double x = distanceFromNeutral * cos + spaceParameters.whitePointX();
            double y = distanceFromNeutral * sin + spaceParameters.whitePointY();
            xyY[0] = x;
            xyY[1] = y;
            xyY_to_XYZ(xyY, XYZ);
            spaceParameters.XYZ_to_rgb().accept(XYZ, rgb);
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
        double[][] boundaries = findRgbGamutBoundariesForRec709(lumaResolution, chromaResolution);

        testBoundaries(boundaries, lumaResolution, chromaResolution, Rec709.PARAMS);
    }

    public static double[][] findRgbGamutBoundariesForRec709(int lumaResolution, int chromaResolution) {
        return findRgbGamutBoundaries(lumaResolution, chromaResolution, Rec709.PARAMS);
    }

    public static double[][] findRgbGamutBoundariesForRec2020(int lumaResolution, int chromaResolution) {
        return findRgbGamutBoundaries(lumaResolution, chromaResolution, Rec2020.PARAMS);
    }

    private static void testBoundaries(double[][] boundaries, int lumaResolution, int chromaResolution, SpaceParameters spaceParameters) {
        double[] xyY = new double[3];
        double[] XYZ = new double[3];
        double[] rgb = new double[3];

        //System.out.println("=== final result ===");

        var result = new StringBuilder();
        var outOfGamut = new StringBuilder();
        var notOnGamutBoundary = new StringBuilder();
        for (int indexY = 0; indexY < lumaResolution; indexY++) {
            double Y = 1f / lumaResolution * indexY;
            xyY[2] = Y;
            double[] boundariesForLuma = boundaries[indexY];
            for (int indexPolar = 0; indexPolar < chromaResolution; indexPolar++) {
                double angle = TWO_PI / chromaResolution * indexPolar;
                double distanceFromNeutral = boundariesForLuma[indexPolar];
                double x = (distanceFromNeutral * cos(angle) + spaceParameters.whitePointX());
                double y = (distanceFromNeutral * sin(angle) + spaceParameters.whitePointY());
                xyY[0] = x;
                xyY[1] = y;
                xyY_to_XYZ(xyY, XYZ);
                spaceParameters.XYZ_to_rgb().accept(XYZ, rgb);
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

    private static boolean outOfGamut(double[] rgb) {
        return rgb[0] < 0 || rgb[1] < 0 || rgb[2] < 0
                || rgb[0] > 1 || rgb[1] > 1 || rgb[2] > 1;
    }
}
