package kofa.colours.tools;

import java.util.Arrays;
import java.util.stream.IntStream;

import static java.lang.Math.*;
import static java.util.Arrays.fill;
import static kofa.colours.spaces.CIExyY.*;
import static kofa.colours.spaces.Rec709.XYZ_to_rec709;

public class CIExyYGamutBoundaries {
    public static final int POLAR_RESOLUTION = 1000;
    public static final int LUMA_RESOLUTION = 1000;
    private static final float PI2 = (float) (2 * Math.PI);
    private static final float STARTING_MAX_DISTANCE = 2;

    // the distance of the gamut boundary from the white point, by luma and polar index
    private final float[][] boundaries = new float[LUMA_RESOLUTION][POLAR_RESOLUTION];

    public CIExyYGamutBoundaries() {
        for (float[] boundariesForLuma : boundaries) {
            fill(boundariesForLuma, STARTING_MAX_DISTANCE);
        }
        fill(boundaries[0], 0);
    }

    public float[][] findRec709GamutBoundaries(int steps) {
        long start = System.currentTimeMillis();
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
            if (indexY != 0 && indexY < LUMA_RESOLUTION) {
                xyY[2] = XYZ[1] = Y;
                for (int countPolar = 0; countPolar < steps; countPolar++) {
                    float angle = polarStep * countPolar;
                    int indexPolar = round(polarIndexStep * countPolar);
                    if (!(indexPolar < POLAR_RESOLUTION)) {
                        continue;
                    }
                    double cos = cos(angle);
                    double sin = sin(angle);

                    float minDistanceForThisRound = 0;
                    float maxDistanceForThisRound = STARTING_MAX_DISTANCE;
                    int count = 0;
//                    if (indexY == 537 && indexPolar == 669) {
//                        System.out.println("break");
//                    }
                    while (count < 100 && indexPolar < POLAR_RESOLUTION && maxDistanceForThisRound > minDistanceForThisRound) {
                        count++;
                        float distanceFromNeutral = (minDistanceForThisRound + maxDistanceForThisRound) / 2;
//                        if (indexY == 21 && indexPolar == 850) {
//                            System.out.println("indexY: %d, indexPolar: %d, minDistanceForThisRound: %.20f, maxDistanceForThisRound: %.20f, distanceFromNeutral: %.20f"
//                                    .formatted(indexY, indexPolar, minDistanceForThisRound, maxDistanceForThisRound, distanceFromNeutral)
//                            );
//                        }
                        if (distanceFromNeutral == maxDistanceForThisRound || distanceFromNeutral == minDistanceForThisRound) {
                            // resolution limit reached, cannot refine further
//                            if (indexY == 21 && indexPolar == 850) {
//                                System.out.println("indexY: %d, indexPolar: %d, distanceFromNeutral: %.20f, forced break"
//                                        .formatted(indexY, indexPolar, distanceFromNeutral)
//                                );
//                            }
                            break;
                        }
                        float x = (float) (distanceFromNeutral * cos + D65_WHITE_2DEG_x);
                        float y = (float) (distanceFromNeutral * sin + D65_WHITE_2DEG_y);
                        xyY[0] = x;
                        xyY[1] = y;
                        xyY_to_XYZ(xyY, XYZ);
                        XYZ_to_rec709(XYZ, rgb);
                        boolean outOfGamut = outOfGamut(rgb);
//                        if (indexY == 21 && indexPolar == 850) {
//                            System.out.println("indexY: %d, indexPolar: %d, Y: %.20f, angle: %.20f, distanceFromNeutral: %.20f, xyY: %s, rgb: %s, outOfGamut: %b"
//                                    .formatted(indexY, indexPolar, Y, angle, distanceFromNeutral, Arrays.toString(xyY), Arrays.toString(rgb), outOfGamut)
//                            );
//                        }
                        if (outOfGamut) {
                            maxDistanceForThisRound = distanceFromNeutral;
                        } else {
                            minDistanceForThisRound = distanceFromNeutral;
                        }
                    }
                    synchronized (boundaries[indexY]) {
//                        if (indexY == 21 && indexPolar == 850) {
//                            System.out.println("indexY: %d, indexPolar: %d, current boundaries[indexY][indexPolar]: %.20f, minDistanceForThisRound: %.20f"
//                                    .formatted(indexY, indexPolar, boundaries[indexY][indexPolar], minDistanceForThisRound)
//                            );
//                        }
                        boundaries[indexY][indexPolar] = min(boundaries[indexY][indexPolar], minDistanceForThisRound);
                    }
                }
            }
        });
        System.out.println("Gamut search took %d ms".formatted(System.currentTimeMillis() - start));
        return boundaries;
    }

    public static void main(String[] args) {
        float[][] boundaries = new CIExyYGamutBoundaries().findRec709GamutBoundaries(16_384);

        float[] xyY = new float[3];
        float[] XYZ = new float[3];
        float[] rgb = new float[3];

        //System.out.println("=== final result ===");

        StringBuilder result = new StringBuilder();
        StringBuilder outOfGamut = new StringBuilder();
        StringBuilder notOnGamutBoundary = new StringBuilder();
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
                XYZ_to_rec709(XYZ, rgb);
//                System.out.println("indexY: %d, indexPolar: %d, Y: %.20f, angle: %.20f, distanceFromNeutral: %.20f, xyY: %s, rgb: %s"
//                        .formatted(indexY, indexPolar, Y, angle, distanceFromNeutral, Arrays.toString(xyY), Arrays.toString(rgb)));
                result.append("xyY: %.20f, %.20f, %.20f; radius: %.20f, angle: %.20f, rgb: %s\n".formatted(xyY[0], xyY[1], xyY[2], distanceFromNeutral, angle, Arrays.toString(rgb)));

                if (outOfGamut(rgb)) {
                    // a few of these can happen: we did not scan directly using coordinates derived from the index; instead,
                    // we scanned at a higher resolution and then derived the index from the coordinates by rounding
                    outOfGamut.append("Out of gamut: indexY: %d, indexPolar: %d, Y: %.20f, angle: %.20f, distanceFromNeutral: %.20f, xyY: %s, rgb: %s\n"
                            .formatted(indexY, indexPolar, Y, angle, distanceFromNeutral, Arrays.toString(xyY), Arrays.toString(rgb)));
                } else {
                    if (
                        // we supersampled, so it can happen that a fractional angle or Y not directly represented
                        // in the output (but rounding to the same indexes) required a slightly smaller radius
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

    private static boolean outOfGamut(float[] rgb) {
        return rgb[0] < 0 || rgb[0] > 1
                || rgb[1] < 0 || rgb[1] > 1
                || rgb[2] < 0 || rgb[2] > 1;
    }
}
