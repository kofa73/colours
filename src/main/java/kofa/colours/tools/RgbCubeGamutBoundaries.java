package kofa.colours.tools;

import kofa.colours.spaces.CIExyY;
import kofa.colours.spaces.Rec709;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RgbCubeGamutBoundaries {
    private static final int POLAR_RESOLUTION = 1_000;
    private static final int LUMA_RESOLUTION = 1_000;
    private static final boolean[] FULLY_POPULATED_LUMAS = new boolean[LUMA_RESOLUTION];
    private static int remainingLumas = LUMA_RESOLUTION - 2;
    private static float[][] boundaries = new float[LUMA_RESOLUTION][POLAR_RESOLUTION];

    public static void main(String[] args) {
        FULLY_POPULATED_LUMAS[0] = true;
        FULLY_POPULATED_LUMAS[LUMA_RESOLUTION - 1] = true;
        for (int i = 6400; i < 1_000_000; i*=2) {
            long start = System.currentTimeMillis();
            findRec709GamutBoundaries(i);
            System.out.println(i + " steps took " + (System.currentTimeMillis() - start));
        }
    }

    public static void findRec709GamutBoundaries(int steps) {
        System.out.println("=== running with " + steps + " steps");
        float step = 1f / steps;

        IntStream.rangeClosed(0, steps).parallel().forEach(r -> {
            float[] rgb = new float[3];
            float[] XYZ = new float[3];
            float[] xyY = new float[3];
            rgb[0] = r * step;
            for (int g = 0; g <= steps; g ++) {
                rgb[1] = g * step;
                for (int b = 0; b <= steps; b ++) {
                    rgb[2] = b * step;
                    Rec709.rec709ToXyz(rgb, XYZ);
                    CIExyY.XZY_to_xyY(XYZ, xyY);

                    float Y = xyY[2];
                    int lumaIndex = Math.round(Y * (LUMA_RESOLUTION - 1));
//                    if (lumaIndex < 0 || lumaIndex >= LUMA_RESOLUTION) {
//                        throw new RuntimeException("Got lumaIndex " + lumaIndex);
//                    }
                    if (FULLY_POPULATED_LUMAS[lumaIndex]) {
                        continue;
                    }

                    float x = xyY[0];
                    float y = xyY[1];
                    float dx = CIExyY.D65_WHITE_2DEG_x - x;
                    float dy = CIExyY.D65_WHITE_2DEG_y - y;
                    int polarIndex = (int) Math.round(Math.atan2(dx, dy) / Math.PI / 2 * (POLAR_RESOLUTION - 1) + (POLAR_RESOLUTION / 2 - 1));
//                    if (polarIndex < 0 || polarIndex >= POLAR_RESOLUTION) {
//                        throw new RuntimeException("Got polarIndex " + polarIndex);
//                    }
                    if (boundaries[lumaIndex][polarIndex] == 0) {
                        float distanceSquared = dx * dx + dy * dy;
                        boundaries[lumaIndex][polarIndex] = distanceSquared;
                    }
                }
            }
        });
        int count = 0;
        for (int luma = 1; luma < LUMA_RESOLUTION - 2; luma++) {
            boolean full = true;
            for (int polar = 0; polar < POLAR_RESOLUTION; polar++) {
                if (Float.isNaN(boundaries[luma][polar])) {
                    throw new RuntimeException("NaN at luma = %d, polar = %d".formatted(luma, polar));
                }
                if (boundaries[luma][polar] == 0) {
                    count++;
                    full = false;
                }
            }
            if (!FULLY_POPULATED_LUMAS[luma] && full) {
                System.out.println("Luma %d is now fully populated".formatted(luma));
                FULLY_POPULATED_LUMAS[luma] = true;
                remainingLumas--;
            }
        }
        System.out.println("steps: %d, zero count: %d, remaining lumas: %d".formatted(steps, count, remainingLumas));
        System.out.println(IntStream.range(1, LUMA_RESOLUTION - 1)
                .filter(luma -> !FULLY_POPULATED_LUMAS[luma])
                .mapToObj(Integer::toString)
                .collect(Collectors.joining(", "))
        );

    }
}
