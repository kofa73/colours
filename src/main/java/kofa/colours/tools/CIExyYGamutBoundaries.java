package kofa.colours.tools;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static java.util.Arrays.fill;
import static kofa.colours.spaces.CIExyY.*;
import static kofa.colours.spaces.Rec709.cieXYZ_to_Rec709;

public class CIExyYGamutBoundaries {
    private static final int POLAR_RESOLUTION = 1000;
    private static final int LUMA_RESOLUTION = 1000;
    public static final float PI2 = (float) (2 * Math.PI);
    public static final int STARTING_MAX_DISTANCE = 10;
    // the min and max length of the WP -> gamut boundary vector
    private static float[][] boundaries = new float[LUMA_RESOLUTION][POLAR_RESOLUTION];

    public static void main(String[] args) {
        for (float[] boundariesForLuma : boundaries) {
            fill(boundariesForLuma, STARTING_MAX_DISTANCE);
        }
        long start = System.currentTimeMillis();
        findRec709GamutBoundaries(10_000);
        System.out.println("Search took %d ms".formatted(System.currentTimeMillis() - start));

        float[] xyY = new float[3];
        float[] XYZ = new float[3];
        float[] rgb = new float[3];

        System.out.println("=== final result ===");

        for (int indexY = 0; indexY < LUMA_RESOLUTION; indexY++) {
            float Y = 1f / LUMA_RESOLUTION * indexY;
            xyY[2] = Y;
            float[] boundariesForLuma = boundaries[indexY];
            for (int indexPolar = 0; indexPolar < POLAR_RESOLUTION; indexPolar++) {
                float angle = PI2 / POLAR_RESOLUTION * indexPolar;
                float distanceFromNeutral = boundariesForLuma[indexPolar];
                float x = (float) (distanceFromNeutral * cos(angle) + D65_WHITE_2DEG_x);
                float y = (float) (distanceFromNeutral * sin(angle) + D65_WHITE_2DEG_y);
                xyY[0] = x;
                xyY[1] = y;
                xyY_to_XYZ(xyY, XYZ);
                cieXYZ_to_Rec709(XYZ, rgb);
//                System.out.println("indexY: %d, indexPolar: %d, angle: %f, Y: %f"
//                        .formatted(indexY, indexPolar, angle, Y));

                System.out.println("indexY: %d, indexPolar: %d, angle: %f, distanceFromNeutral: %f, xyY: %s, rgb: %s"
                        .formatted(indexY, indexPolar, angle, distanceFromNeutral, Arrays.toString(xyY), Arrays.toString(rgb)));

            }
        }
        for (int indexY = 0; indexY < LUMA_RESOLUTION; indexY++) {
            float Y = 1f / LUMA_RESOLUTION * indexY;
            xyY[2] = Y;
            float[] boundariesForLuma = boundaries[indexY];
            for (int indexPolar = 0; indexPolar < POLAR_RESOLUTION; indexPolar++) {
                float angle = PI2 / POLAR_RESOLUTION * indexPolar;
                float distanceFromNeutral = boundariesForLuma[indexPolar];
                float x = (float) (distanceFromNeutral * cos(angle) + D65_WHITE_2DEG_x);
                float y = (float) (distanceFromNeutral * sin(angle) + D65_WHITE_2DEG_y);
                xyY[0] = x;
                xyY[1] = y;
                xyY_to_XYZ(xyY, XYZ);
                cieXYZ_to_Rec709(XYZ, rgb);
                if (outOfGamut(rgb)) {
                    System.out.println("Found out-of-gamut colour " + Arrays.toString(rgb));
                } else {
                    if (
                            0.01 < rgb[0] && rgb[0] < 0.99
                            && 0.01 < rgb[1] && rgb[1] < 0.99
                            && 0.01 < rgb[2] && rgb[2] < 0.99
                    ) {
                        System.out.println("Colour is not on edge of gamut: " + Arrays.toString(rgb));
                    }
                }
            }
        }
    }

    public static void findRec709GamutBoundaries(int steps) {
        System.out.println("=== running with " + steps + " steps");
        float step = 1f / steps;
        float polarStep = PI2 / steps;
        float lumaIndexStep = (float) LUMA_RESOLUTION / steps;
        float polarIndexStep = (float) POLAR_RESOLUTION / steps;

        IntStream.range(0, steps).parallel().forEach(countY -> {
            float[] rgb = new float[3];
            float[] XYZ = new float[3];
            float[] xyY = new float[3];
            float Y = countY * step;
            int indexY = round(lumaIndexStep * countY);
            if (indexY < LUMA_RESOLUTION) {
//                System.out.println("starting countY: %d, indexY: %d, Y: %f"
//                        .formatted(countY, indexY, Y));
                xyY[2] = XYZ[1] = Y;
                for (int countPolar = 0; countPolar < steps; countPolar++) {
                    float angle = polarStep * countPolar;
                    int indexPolar = round(polarIndexStep * countPolar);
                    if (!(indexPolar < POLAR_RESOLUTION)) {
                        continue;
                    }
//                    System.out.println("countY: %d, countPolar: %d, indexY: %d, indexPolar: %d, Y: %f, angle: %f"
//                            .formatted(countY, countPolar, indexY, indexPolar, Y, angle));

                    double cos = cos(angle);
                    double sin = sin(angle);

                    double minDistanceForThisRound = 0;
                    double maxDistanceForThisRound = STARTING_MAX_DISTANCE;
                    int count = 0;
                    while (count < 100 && indexPolar < POLAR_RESOLUTION && maxDistanceForThisRound - minDistanceForThisRound > 0) {
                        count++;
                        double distanceFromNeutral = (minDistanceForThisRound + maxDistanceForThisRound) / 2;
                        float x = (float) (distanceFromNeutral * cos + D65_WHITE_2DEG_x);
                        float y = (float) (distanceFromNeutral * sin + D65_WHITE_2DEG_y);
                        xyY[0] = x;
                        xyY[1] = y;
                        xyY_to_XYZ(xyY, XYZ);
                        cieXYZ_to_Rec709(XYZ, rgb);
                        boolean outOfGamut = outOfGamut(rgb);
                        if (outOfGamut) {
                            maxDistanceForThisRound = min(distanceFromNeutral, maxDistanceForThisRound);
//                            System.out.println("new maxDistance: " + maxDistanceForThisRound);
                        } else {
                            minDistanceForThisRound = max(distanceFromNeutral, minDistanceForThisRound);
//                            System.out.println("new minDistance: " + minDistanceForThisRound);
                            //                            System.out.println("boundariesMin indexY: %d, indexPolar: %d, angle: %f, xyY: %s, rgb: %s, outOfGamut: %b"
                            //                                    .formatted(indexY, indexPolar, angle, Arrays.toString(xyY), Arrays.toString(rgb), outOfGamut));
                        }
                    }
                    synchronized (boundaries[indexY]) {
                        if (boundaries[indexY][indexPolar] > minDistanceForThisRound) {
                            boundaries[indexY][indexPolar] = (float) minDistanceForThisRound;
//                        System.out.println("new boundaries[%d][%d]: %f".formatted(indexY, indexPolar, minDistanceForThisRound));
                        }
                    }
                }
            } else {
//                System.out.println("skipped countY: %d, indexY: %d, Y: %f"
//                        .formatted(countY, indexY, Y));
            }
        });
    }

    private static boolean outOfGamut(float[] rgb) {
        return rgb[0] < 0 || rgb[0] > 1
                || rgb[1] < 0 || rgb[1] > 1
                || rgb[2] < 0 || rgb[2] > 1;
    }
}
