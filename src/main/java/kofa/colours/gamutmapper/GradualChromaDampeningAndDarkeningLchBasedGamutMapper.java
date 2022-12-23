package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.maths.Curve;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.min;

/**
 * A gamut mapper type that gradually dampens LCh chroma to the maximum value, independently for each pixel.
 * Additionally, it also darkens bright out-of-gamut pixels.
 * That is, for each out-of-gamut pixel the LCh representation is computed, and maxC = 'C at gamut boundary for L and h' is
 * determined. If C exceeds a certain percentage of maxC, L is checked and high values are reduced. If a darkening is performed,
 * maxC is re-determined. Finally, C is softly rolled off to maxC.
 *
 * @param <L> the polar LCh type
 */
public class GradualChromaDampeningAndDarkeningLchBasedGamutMapper<L extends Lch> extends GamutMapper {
    private final Function<Xyz, L> xyzToLch;
    private final Function<double[], L> lchCoordinatesToLch;
    private final Function<L, Xyz> lchToXyz;
    private final ToDoubleFunction<L> maxCFinder;
    private final String name;
    private final Curve dampeningCurve;
    // the shoulder of the curve; also, the ratio to maxC below which C is not modified
    private final double shoulder;

    private final double lShoulder;
    private final Curve lCurve;

    public static GradualChromaDampeningAndDarkeningLchBasedGamutMapper<LchAb> forLchAb(double shoulder, double lShoulder) {
        return new GradualChromaDampeningAndDarkeningLchBasedGamutMapper<>(
                shoulder, lShoulder,
                LchAb.class,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchAb(lch), xyz -> Lab.from(xyz).usingD65().toLch(),
                LchAb::new,
                lch -> lch
                        .toLab()
                        .toXyz().usingD65()
        );
    }

    public static GradualChromaDampeningAndDarkeningLchBasedGamutMapper<LchUv> forLchUv(double shoulder, double lShoulder) {
        return new GradualChromaDampeningAndDarkeningLchBasedGamutMapper<>(
                shoulder, lShoulder,
                LchUv.class,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchUv(lch), xyz -> Luv.from(xyz).usingD65().toLch(),
                LchUv::new,
                lch -> lch
                        .toLuv()
                        .toXyz().usingD65()
        );
    }

    private GradualChromaDampeningAndDarkeningLchBasedGamutMapper(
            double shoulder, double lShoulder,
            Class<L> type,
            ToDoubleFunction<L> maxCFinder,
            Function<Xyz, L> xyzToLch,
            Function<double[], L> lchCoordinatesToLch,
            Function<L, Xyz> lchToXyz
    ) {
        super(true);
        this.name = type.getSimpleName();
        this.maxCFinder = maxCFinder;
        this.xyzToLch = xyzToLch;
        this.lchCoordinatesToLch = lchCoordinatesToLch;
        this.lchToXyz = lchToXyz;
        this.dampeningCurve = new Curve(1, shoulder);
        this.shoulder = shoulder;
        this.lCurve = new Curve(1, lShoulder);
        this.lShoulder = lShoulder;
    }

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        var lch = xyzToLch.apply(xyz);
        var cAtGamutBoundary = maxCFinder.applyAsDouble(lch);
        double originalC = lch.C();
        double dampenedC;
        if (cAtGamutBoundary == 0) {
            cAtGamutBoundary = 1E-6;
        }
        var ratioToMaxC = originalC / cAtGamutBoundary;

        // FIXME: too much L dampening, see _DSC8850-GradualChromaDampeningAndDarkeningLchBasedGamutMapper-LchAb-shoulder-0-lShoulder-0.png
        // TODO idea: only keep new L if it caused maxC increase -- may not be the case for dark pixels?

        double L = lch.L();
        if (L / 100 > lShoulder) {
            // FIXME: even if L is high, it won't be reduced if not saturated?
            // TODO: move this logic to a separate method and write unit tests
            double lMultiplier = min(1, lCurve.mappedValueOf(L / 100) / (L / 100));
            var reducedL = L * lMultiplier;
            L = Math.max(reducedL, ratioToMaxC * reducedL + (1 - ratioToMaxC) * L);
            if (L > lch.L()) {
                System.out.println("oops");
            }
            lch = lchCoordinatesToLch.apply(
                    new double[]{L, 0, lch.h()}
            );
            cAtGamutBoundary = maxCFinder.applyAsDouble(lch);
            ratioToMaxC = originalC / cAtGamutBoundary;
        }
        if (ratioToMaxC > shoulder) {
            var curveValue = dampeningCurve.mappedValueOf(ratioToMaxC);
            dampenedC = curveValue * cAtGamutBoundary;
        } else {
            dampenedC = originalC;
        }
        return Srgb.from(
                lchToXyz.apply(
                        lchCoordinatesToLch.apply(
                                new double[]{L, dampenedC, lch.h()}
                        )
                )
        );
    }

    @Override
    public String name() {
        return "%s-%s-shoulder-%d-lShoulder-%d".formatted(
                super.name(), name, (int) (shoulder * 100), (int) (lShoulder * 100)
        );
    }
}
