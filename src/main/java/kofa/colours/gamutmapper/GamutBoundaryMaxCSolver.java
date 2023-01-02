package kofa.colours.gamutmapper;

import kofa.colours.model.LCh;
import kofa.colours.model.Srgb;
import kofa.io.RgbImage;
import kofa.maths.PrimitiveDoubleToDoubleFunction;
import kofa.maths.Solver;
import kofa.maths.Vector3Constructor;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Tries to find max C values for LCh(ab) and LCh(uv) by scanning the LCh spaces in L and h, solving for
 * C where at least one sRGB components is 0 or 1.
 */
public class GamutBoundaryMaxCSolver<L extends LCh<L, ?>> {
    private final Function<L, Srgb> lchToSrgb;
    private final Vector3Constructor<L> lchConstructor;
    private final double roughChromaSearchStep;
    private final double solutionThreshold;
    private final Map<CacheKey, Double> cachedMaxCbyLh;
    private static final Map<Integer, GamutBoundaryMaxCSolver<?>> solvers = new ConcurrentHashMap<>();

    static <L extends LCh<L, ?>> GamutBoundaryMaxCSolver<L> createFor(GamutBoundarySearchParams<L> searchParams, RgbImage image) {
        int key = Objects.hash(searchParams, image);
        return (GamutBoundaryMaxCSolver<L>) solvers.computeIfAbsent(key, ignored -> new GamutBoundaryMaxCSolver<>(searchParams, image.size()));
    }

    private GamutBoundaryMaxCSolver(GamutBoundarySearchParams<L> searchParams, int size) {
        requireNonNull(searchParams);
        this.cachedMaxCbyLh = new ConcurrentHashMap<>(size);
        this.lchToSrgb = requireNonNull(searchParams.lchToSrgb());
        this.lchConstructor = requireNonNull(searchParams.lchConstructor());
        checkArgument(searchParams.roughChromaSearchStep() > 0, "roughChromaSearchStep = %s", searchParams.roughChromaSearchStep());
        this.roughChromaSearchStep = searchParams.roughChromaSearchStep();
        checkArgument(searchParams.solutionThreshold() > 0, "solutionThreshold = %s", searchParams.solutionThreshold());
        this.solutionThreshold = searchParams.solutionThreshold();
    }

    public double maxCFor(L lch) {
        var key = new CacheKey(lch.L(), lch.h());
        return cachedMaxCbyLh.computeIfAbsent(key, ignoredKey -> solveMaxCFor(lch));
    }

    private double solveMaxCFor(L lch) {
        double l = lch.L();
        if (lch.isOverMaxOrBelowZero()) {
            return 0;
        }
        double h = lch.h();
        double maxCUpperBound = findMaxCUpperBound(l, h);
        return findExactMaxC(lch, l, h, maxCUpperBound);
    }

    private double findExactMaxC(L lch, double l, double h, double cOutOfGamut) {
        var solver = new Solver(clipDetectorForLch(l, h));
        Optional<Double> solution = solver.solve(cOutOfGamut - roughChromaSearchStep, cOutOfGamut, 0);
        if (solution.isEmpty() && solver.lastValue() > solutionThreshold) {
            throw new IllegalArgumentException(
                    "Unable to solve C in %s for L=%f, h=%f. Best guess: C=%f".formatted(
                            lch.getClass().getSimpleName(), l, h, solver.lastValue()
                    )
            );
        }
        return solution.orElse(0.0);
    }

    private double findMaxCUpperBound(double l, double h) {
        double cOutOfGamut = 0;
        do {
            cOutOfGamut += roughChromaSearchStep;
        } while (!lchToSrgb.apply(lchConstructor.createFrom(l, cOutOfGamut, h)).isOutOfGamut());
        return cOutOfGamut;
    }

    private static final double COMPONENT_MIN = 1E-12;
    private static final double COMPONENT_MAX = 1 - COMPONENT_MIN;

    private PrimitiveDoubleToDoubleFunction clipDetectorForLch(double l, double h) {
        return (double c) -> {
            Srgb sRgb = lchToSrgb.apply(lchConstructor.createFrom(l, c, h));
            if (sRgb.isOutOfGamut()) {
                return 1.0;
            }
            if (sRgb.anyCoordinateMatches(coordinate -> (coordinate > COMPONENT_MAX) || (coordinate < COMPONENT_MIN))) {
                return 0.0;
            }
            return -1.0;
        };
    }
}
