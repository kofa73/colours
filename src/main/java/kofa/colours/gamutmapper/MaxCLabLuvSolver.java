package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.maths.Solver;

import java.util.Optional;
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

    private static final double MAX_C = 200;

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

    public double solveMaxCForLchAb(LchAb lch) {
        return solveMaxCForLch(lch, this::lchAbToXyz);
    }

    public double solveMaxCForLchUv(LchUv lch) {
        return solveMaxCForLch(lch, this::lchUvToXyz);
    }

    private double solveMaxCForLch(Lch lch, Function<double[], Xyz> lchCoordinatesToXyz) {
        var L = lch.L();
        var h = lch.h();
        if (L >= 100 || L <= 0) {
            return 0;
        }
        double cOutOfGamut = 0;
        do {
            cOutOfGamut++;
        } while (!Srgb.from(lchCoordinatesToXyz.apply(new double[]{L, cOutOfGamut, h})).isOutOfGamut());
        var solver = new Solver(clipDetectorForLch(L, h, lchCoordinatesToXyz));
        Optional<Double> solution = solver.solve(cOutOfGamut - 1, cOutOfGamut, cOutOfGamut - 0.5, 0);
        if (solution.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unable to solve C in %s for L=%f, h=%f. Best guess: C=%f".formatted(
                            lch.getClass().getSimpleName(), L, h, solver.lastValue()
                    )
            );
        }
        return solution.get();
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
            var sRgb = Srgb.from(xyz);
            if (sRgb.isOutOfGamut()) {
                return 1.0;
            }
            for (var coordinate : sRgb.coordinates()) {
                if ((coordinate > COMPONENT_MAX) || (coordinate < COMPONENT_MIN)) {
                    return 0.0;
                }
            }
            return -1.0;
        };
    }

    private void printMaxC(double[][] maxC, String space, Function<double[], Xyz> LchToXYZ) {
        System.out.println("======== " + space + " ========");
        for (int lIndex = 0; lIndex <= L_RESOLUTION; lIndex++) {
            for (int hIndex = 0; hIndex <= H_RESOLUTION; hIndex++) {
                var C = maxC[lIndex][hIndex];
                var sRgb = Srgb.from(LchToXYZ.apply(new double[]{lOrdinalToL(lIndex), C, hOrdinalToH(hIndex)}));
                System.out.printf(
                        "%d,%d,%f,%s%n",
                        lIndex, hIndex, C, sRgb
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
