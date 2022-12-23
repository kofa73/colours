package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.maths.Curve;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

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

    private static final double L_SHOULDER = 90;
    private static final Curve L_CURVE = new Curve(1, L_SHOULDER / 100);

    public static GradualChromaDampeningAndDarkeningLchBasedGamutMapper<LchAb> forLchAb(double shoulder) {
        return new GradualChromaDampeningAndDarkeningLchBasedGamutMapper<>(
                shoulder,
                LchAb.class,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchAb(lch), xyz -> Lab.from(xyz).usingD65().toLch(),
                LchAb::new,
                lch -> lch
                        .toLab()
                        .toXyz().usingD65()
        );
    }

    public static GradualChromaDampeningAndDarkeningLchBasedGamutMapper<LchUv> forLchUv(double shoulder) {
        return new GradualChromaDampeningAndDarkeningLchBasedGamutMapper<>(
                shoulder,
                LchUv.class,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchUv(lch), xyz -> Luv.from(xyz).usingD65().toLch(),
                LchUv::new,
                lch -> lch
                        .toLuv()
                        .toXyz().usingD65()
        );
    }

    private GradualChromaDampeningAndDarkeningLchBasedGamutMapper(
            double shoulder,
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
    }

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        var lch = xyzToLch.apply(xyz);
        var cAtGamutBoundary = maxCFinder.applyAsDouble(lch);
        double originalC = lch.C();
        double dampenedC;
        var ratioToMaxC = originalC / cAtGamutBoundary;
        double L = lch.L();
        if (ratioToMaxC > shoulder) {
            if (L > L_SHOULDER) {
                L = L_CURVE.mappedValueOf(lch.L() / 100) * 100;
                var darkened = lchCoordinatesToLch.apply(
                        new double[]{L, 0, lch.h()}
                );
                cAtGamutBoundary = maxCFinder.applyAsDouble(darkened);
                ratioToMaxC = originalC / cAtGamutBoundary;
            }
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
        return super.name() + name + "-shoulder-%d".formatted((int) (shoulder * 100));
    }
}

