package kofa.colours.transformer;

import kofa.colours.model.LchAb;
import kofa.colours.model.LchUv;
import kofa.colours.model.Srgb;
import kofa.colours.model.XYZ;
import kofa.maths.Solver;

import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.PI;

/**
 * Tries to find max C values for LCh(ab) and LCh(uv) by scanning the LCh spaces in L and h, solving for
 * C where at least one sRGB components is 0 or 1.
 */
public class MaxCLabLuvSolver {

    private static final int L_RESOLUTION = 10000;
    private static final int H_RESOLUTION = 3600;

    public static void main(String[] ignored) {
        var maxCfinder = new MaxCLabLuvSolver();
        var maxClab = maxCfinder.solveMaxCForLchAb();
        var maxCluv = maxCfinder.solveMaxCForLchUv();
        maxCfinder.printMaxC(maxClab, "Lab", maxCfinder::lch_ab_to_XYZ);
        maxCfinder.printMaxC(maxCluv, "Luv", maxCfinder::lch_uv_to_XYZ);
    }

    public double[][] solveMaxCForLchAb() {
        double[][] maxC_forLh_ab_byLByH = new double[L_RESOLUTION + 1][];
        initByHue(maxC_forLh_ab_byLByH);

        // sweep L and h, solve max C
        IntStream.range(1, L_RESOLUTION).parallel().forEach(lIndex -> {
            for (int hIndex = 0; hIndex <= H_RESOLUTION; hIndex++) {
                double L = lIndexToL(lIndex);
                double h = hIndexToH(hIndex);
                maxC_forLh_ab_byLByH[lIndex][hIndex] = solveMaxCForLchAb(L, h);
            }
        });
        return maxC_forLh_ab_byLByH;
    }

    public double[][] solveMaxCForLchUv() {
        double[][] maxC_forLh_uv_byLByH = new double[L_RESOLUTION + 1][];
        initByHue(maxC_forLh_uv_byLByH);

        // sweep L and h, solve max C
        IntStream.range(1, L_RESOLUTION).parallel().forEach(lIndex -> {
            for (int hIndex = 0; hIndex <= H_RESOLUTION; hIndex++) {
                double L = lIndexToL(lIndex);
                double h = hIndexToH(hIndex);
                maxC_forLh_uv_byLByH[lIndex][hIndex] = solveMaxCForLchUv(L, h);
            }
        });
        return maxC_forLh_uv_byLByH;
    }

    public static int hToIndex(double h) {
        return (int) Math.round(H_RESOLUTION * h / (2 * PI));
    }

    public double hIndexToH(int hIndex) {
        return 2 * PI * hIndex / H_RESOLUTION;
    }

    public static int lToIndex(double l) {
        return (int) Math.round(L_RESOLUTION * l / 100.0);
    }

    public double lIndexToL(int lIndex) {
        return 100.0 * lIndex / L_RESOLUTION;
    }

    private static final double MAX_C = 500;

    public double solveMaxCForLchAb(double L, double h) {
        if (L >= 99.95) {
            return 0;
        }
        var solver = new Solver(clipDetectorForLCh(L, h, this::lch_ab_to_XYZ));
        return solver.solve(0, MAX_C, 0, 0)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unable to solve C LCH_ab for L=%f, h=%f. Best guess: C=%f".formatted(
                                        L, h, solver.lastValue()
                                )
                        )
                );
    }

    public double solveMaxCForLchUv(double L, double h) {
        if (L >= 99.95) {
            return 0;
        }
        var solver = new Solver(clipDetectorForLCh(L, h, this::lch_uv_to_XYZ));
        return solver.solve(0, MAX_C, 0, 0).orElseThrow(() ->
                new IllegalArgumentException(
                        "Unable to solve C LCH_uv for L=%f, h=%f. Best guess: C=%f".formatted(
                                L, h, solver.lastValue()
                        )
                )
        );
    }

    private XYZ lch_ab_to_XYZ(double[] lch) {
        return new LchAb(lch).toLab().toXYZ().usingD65();
    }

    private XYZ lch_uv_to_XYZ(double[] lch) {
        return new LchUv(lch).toLuv().toXYZ().usingD65();
    }

    private static final double COMPONENT_MIN = 1E-12;
    private static final double COMPONENT_MAX = 1 - COMPONENT_MIN;

    private Function<Double, Double> clipDetectorForLCh(double L, double h, Function<double[], XYZ> xyzMapper) {
        return (Double C) -> {
            var xyz = xyzMapper.apply(new double[]{L, C, h});
            var srgb = Srgb.from(xyz);
            double[] components = srgb.values();
            for (double component : components) {
                if (component < 0 || component > 1) {
                    return 1.0;
                }
            }
            boolean onBoundary = false;
            for (double component : components) {
                onBoundary = onBoundary || ((component > COMPONENT_MAX) || (component < COMPONENT_MIN));
            }
            if (onBoundary) {
                return 0.0;
            }
            return -1.0;
        };
    }

    private void printMaxC(double[][] maxC, String space, Function<double[], XYZ> LChToXYZ) {
        System.out.println("======== " + space + " ========");
        for (int lIndex = 0; lIndex <= L_RESOLUTION; lIndex++) {
            for (int hIndex = 0; hIndex <= H_RESOLUTION; hIndex++) {
                var C = maxC[lIndex][hIndex];
                var srgb = Srgb.from(LChToXYZ.apply(new double[]{lIndexToL(lIndex), C, hIndexToH(hIndex)}));
                System.out.printf(
                        "%d,%d,%f,%s%n",
                        lIndex, hIndex, C, srgb
                );
            }
        }
    }

    private void initByHue(double[][] maxC_forLh) {
        for (int L = 0; L < maxC_forLh.length; L++) {
            maxC_forLh[L] = new double[H_RESOLUTION + 1];
        }
    }
}
