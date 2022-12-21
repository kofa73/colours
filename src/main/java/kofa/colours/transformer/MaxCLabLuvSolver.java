package kofa.colours.transformer;

import kofa.colours.model.LchAb;
import kofa.colours.model.LchUv;
import kofa.colours.model.Srgb;
import kofa.colours.model.Xyz;
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
        maxCfinder.printMaxC(maxClab, "Lab", maxCfinder::lchAbToXyz);
        maxCfinder.printMaxC(maxCluv, "Luv", maxCfinder::lchUvToXyz);
    }

    public double[][] solveMaxCForLchAb() {
        double[][] maxC_forLh_ab_byLByH = new double[L_RESOLUTION + 1][];
        initByHue(maxC_forLh_ab_byLByH);

        // sweep L and h, solve max C
        IntStream.range(1, L_RESOLUTION).parallel().forEach(lIndex -> {
            for (int hIndex = 0; hIndex <= H_RESOLUTION; hIndex++) {
                double L = lOrdinalToL(lIndex);
                double h = hOrdinalToH(hIndex);
                maxC_forLh_ab_byLByH[lIndex][hIndex] = solveMaxCForLchAb(new LchAb(L, 0, h));
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
                double L = lOrdinalToL(lIndex);
                double h = hOrdinalToH(hIndex);
                maxC_forLh_uv_byLByH[lIndex][hIndex] = solveMaxCForLchUv(new LchUv(L, 0, h));
            }
        });
        return maxC_forLh_uv_byLByH;
    }

    public double hOrdinalToH(int hIndex) {
        return 2 * PI * hIndex / H_RESOLUTION;
    }

    public double lOrdinalToL(int lIndex) {
        return 100.0 * lIndex / L_RESOLUTION;
    }

    private static final double MAX_C = 500;

    public double solveMaxCForLchAb(LchAb lch) {
        var L = lch.L();
        var h = lch.h();
        if (L >= 100 || L <= 0) {
            return 0;
        }
        var solver = new Solver(clipDetectorForLch(L, h, this::lchAbToXyz));
        return solver.solve(0, MAX_C, 0, 0)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Unable to solve C LCH_ab for L=%f, h=%f. Best guess: C=%f".formatted(
                                        L, h, solver.lastValue()
                                )
                        )
                );
    }

    public double solveMaxCForLchUv(LchUv lch) {
        var L = lch.L();
        var h = lch.h();
        if (L >= 100 || L <= 0) {
            return 0;
        }
        var solver = new Solver(clipDetectorForLch(L, h, this::lchUvToXyz));
        return solver.solve(0, MAX_C, 0, 0).orElseThrow(() ->
                new IllegalArgumentException(
                        "Unable to solve C LCH_uv for L=%f, h=%f. Best guess: C=%f".formatted(
                                L, h, solver.lastValue()
                        )
                )
        );
    }

    private Xyz lchAbToXyz(double[] lch) {
        return new LchAb(lch).toLab().toXyz().usingD65();
    }

    private Xyz lchUvToXyz(double[] lch) {
        return new LchUv(lch).toLuv().toXyz().usingD65();
    }

    private static final double COMPONENT_MIN = 1E-12;
    private static final double COMPONENT_MAX = 1 - COMPONENT_MIN;

    private Function<Double, Double> clipDetectorForLch(double L, double h, Function<double[], Xyz> xyzMapper) {
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

    private void printMaxC(double[][] maxC, String space, Function<double[], Xyz> LchToXYZ) {
        System.out.println("======== " + space + " ========");
        for (int lIndex = 0; lIndex <= L_RESOLUTION; lIndex++) {
            for (int hIndex = 0; hIndex <= H_RESOLUTION; hIndex++) {
                var C = maxC[lIndex][hIndex];
                var srgb = Srgb.from(LchToXYZ.apply(new double[]{lOrdinalToL(lIndex), C, hOrdinalToH(hIndex)}));
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
