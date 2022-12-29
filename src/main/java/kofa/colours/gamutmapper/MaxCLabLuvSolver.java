package kofa.colours.gamutmapper;

import kofa.colours.model.Lch;
import kofa.colours.model.Srgb;
import kofa.maths.PrimitiveDoubleToDoubleFunction;
import kofa.maths.Solver;
import kofa.maths.Vector3Constructor;

import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * Tries to find max C values for LCh(ab) and LCh(uv) by scanning the LCh spaces in L and h, solving for
 * C where at least one sRGB components is 0 or 1.
 */
public class MaxCLabLuvSolver<L extends Lch<L, ?>> {
    private final Function<Srgb, L> sRgbToLch;
    private final Function<L, Srgb> lchToSrgb;
    private final Vector3Constructor<L> lchConstructor;
    private final double roughChromaSearchStep;
    private final double solutionThreshold;
    private final double maxL;

    public MaxCLabLuvSolver(GamutBoundarySearchParams<L> searchParams) {
        requireNonNull(searchParams);
        this.sRgbToLch = requireNonNull(searchParams.sRgbToLch());
        this.lchToSrgb = requireNonNull(searchParams.lchToSrgb());
        this.lchConstructor = requireNonNull(searchParams.lchConstructor());
        checkArgument(searchParams.roughChromaSearchStep() > 0, "roughChromaSearchStep = %s", searchParams.roughChromaSearchStep());
        this.roughChromaSearchStep = searchParams.roughChromaSearchStep();
        checkArgument(searchParams.solutionThreshold() > 0, "solutionThreshold = %s", searchParams.solutionThreshold());
        this.solutionThreshold = searchParams.solutionThreshold();
        checkArgument(searchParams.maxL() > 0, "maxL = %s", searchParams.maxL());
        this.maxL = searchParams.maxL();
    }

    public double solveMaxCForLch(Srgb inputSrgb) {
        L lch = sRgbToLch.apply(inputSrgb);
        double l = lch.l();
        if (l >= maxL || l <= 0 || inputSrgb.isBlack() || inputSrgb.isWhite()) {
            return 0;
        }
        double h = lch.h();
        double cOutOfGamut = 0;
        do {
            cOutOfGamut += roughChromaSearchStep;
        } while (!lchToSrgb.apply(lchConstructor.createFrom(l, cOutOfGamut, h)).isOutOfGamut());
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
