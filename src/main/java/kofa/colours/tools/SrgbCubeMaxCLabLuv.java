package kofa.colours.tools;

import kofa.colours.LCh_ab;
import kofa.colours.LCh_uv;
import kofa.colours.SRGB;
import kofa.colours.XYZ;
import kofa.maths.Solver;

import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.PI;

/**
 * Tries to find max C values for LCh(ab) and LCh(uv) by scanning the LCh spaces in L and h, solving for
 * C where at least one sRGB components is 0 or 1.
 */
class SrgbCubeMaxCLabLuv {

    private static final int L_RESOLUTION = 1000;
    private static final int H_RESOLUTION = 360;

    public static void main(String[] ignored) {
        double[][] maxC_forLh_ab_byLByH = new double[L_RESOLUTION + 1][];
        double[][] maxC_forLh_uv_byLByH = new double[L_RESOLUTION + 1][];
        initByHue(maxC_forLh_ab_byLByH, maxC_forLh_uv_byLByH);

        // sweep L and h, solve max C
        IntStream.range(1, L_RESOLUTION).parallel().forEach(lIndex -> {
            for (int hIndex = 0; hIndex <= H_RESOLUTION; hIndex++) {
                double L = lIndexToL(lIndex);
                double h = hIndexToH(hIndex);
                maxC_forLh_ab_byLByH[lIndex][hIndex] = solveLab(L, h);
                maxC_forLh_uv_byLByH[lIndex][hIndex] = solveLuv(L, h);
            }
        });

        printMaxC(maxC_forLh_ab_byLByH, "Lab", SrgbCubeMaxCLabLuv::lch_ab_to_XYZ);
        printMaxC(maxC_forLh_uv_byLByH, "Luv", SrgbCubeMaxCLabLuv::lch_uv_to_XYZ);
    }

    private static double hIndexToH(int hIndex) {
        return 2 * PI * hIndex / H_RESOLUTION;
    }

    private static double lIndexToL(int lIndex) {
        return 100.0 * lIndex / L_RESOLUTION;
    }

    private static final double MAX_C = 500;

    private static double solveLab(double L, double h) {
        var solver = new Solver(clipDetectorForLCh(L, h, SrgbCubeMaxCLabLuv::lch_ab_to_XYZ));
        return solver.solve(0, MAX_C, 0, 0).orElseThrow(() ->
                new IllegalArgumentException(
                        "Unable to solve C LCH_ab for L=%f, h=%f. Best guess: C=%f".formatted(
                                L, h, solver.lastValue()
                        )
                )
        );
    }

    private static double solveLuv(double L, double h) {
        var solver = new Solver(clipDetectorForLCh(L, h, SrgbCubeMaxCLabLuv::lch_uv_to_XYZ));
        return solver.solve(0, MAX_C, 0, 0).orElseThrow(() ->
                new IllegalArgumentException(
                        "Unable to solve C LCH_uv for L=%f, h=%f. Best guess: C=%f".formatted(
                                L, h, solver.lastValue()
                        )
                )
        );
    }

    private static XYZ lch_ab_to_XYZ(double[] lch) {
        return new LCh_ab(lch).toLab().toXYZ().usingD65();
    }

    private static XYZ lch_uv_to_XYZ(double[] lch) {
        return new LCh_uv(lch).toLuv().toXYZ().usingD65();
    }

    private static final double COMPONENT_MIN = 1E-12;
    private static final double COMPONENT_MAX = 1 - COMPONENT_MIN;

    private static Function<Double, Double> clipDetectorForLCh(double L, double h, Function<double[], XYZ> xyzMapper) {
        return (Double C) -> {
            var xyz = xyzMapper.apply(new double[]{L, C, h});
            var srgb = SRGB.from(xyz);
            double[] components = srgb.values();
            for (double component : components) {
                if (component < 0 || component > 1) {
                    return 1.0;
                }
            }
            boolean onBoundary = false;
            for (double component : components) {
                onBoundary |= (component > COMPONENT_MAX) || (component < COMPONENT_MIN);
            }
            if (onBoundary) {
                return 0.0;
            }
            return -1.0;
        };
    }

    private static void printMaxC(double[][] maxC, String space, Function<double[], XYZ> LChToXYZ) {
        System.out.println("======== " + space + " ========");
        for (int lIndex = 0; lIndex <= L_RESOLUTION; lIndex++) {
            for (int hIndex = 0; hIndex <= H_RESOLUTION; hIndex++) {
                var C = maxC[lIndex][hIndex];
                var srgb = SRGB.from(LChToXYZ.apply(new double[]{lIndexToL(lIndex), C, hIndexToH(hIndex)}));
                System.out.println(
                        "%d,%d,%f,%s".formatted(
                                lIndex, hIndex, C, srgb
                        )
                );
            }
        }
    }

    private static void initByHue(double[][] maxC_forLh_ab_byLByH, double[][] maxC_forLh_uv_byLByH) {
        for (int L = 0; L < maxC_forLh_ab_byLByH.length; L++) {
            maxC_forLh_ab_byLByH[L] = new double[H_RESOLUTION + 1];
            maxC_forLh_uv_byLByH[L] = new double[H_RESOLUTION + 1];
        }
    }
}
