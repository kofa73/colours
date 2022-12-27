package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.maths.ThanatomanicCurve6;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * A gamut mapper type that gradually dampens LCh chroma to the maximum value, independently for each pixel.
 * That is, for each out-of-gamut pixel the LCh representation is computed, and 'C at gamut boundary for L and h' is
 * determined. Then C is kept unchanged if it is below a certain threshold, and is gradually reduced afterwards,
 * making sure its value never exceeds 'C at gamut boundary'.
 *
 * @param <L> the polar LCh type
 */
public class GradualChromaDampeningLchBasedGamutMapper<L extends Lch> extends GamutMapper {
    private final Function<Xyz, L> xyzToLch;
    private final Function<double[], L> lchCoordinatesToLch;
    private final Function<L, Xyz> lchToXyz;
    private final ToDoubleFunction<L> maxCFinder;
    private final String name;
    private final ThanatomanicCurve6 dampeningCurve;
    // the shoulder of the curve; also, the ratio to maxC below which C is not modified
    private final double shoulder;

    public static GradualChromaDampeningLchBasedGamutMapper<CieLchAb> forLchAb(double shoulder) {
        return new GradualChromaDampeningLchBasedGamutMapper<>(
                shoulder,
                CieLchAb.class,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchAb(lch), xyz -> CieLab.from(xyz).usingD65().toLch(),
                CieLchAb::new,
                lch -> lch
                        .toLab()
                        .toXyz().usingD65()
        );
    }

    public static GradualChromaDampeningLchBasedGamutMapper<CieLchUv> forLchUv(double shoulder) {
        return new GradualChromaDampeningLchBasedGamutMapper<>(
                shoulder,
                CieLchUv.class,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchUv(lch), xyz -> CieLuv.from(xyz).usingD65().toLch(),
                CieLchUv::new,
                lch -> lch
                        .toLuv()
                        .toXyz().usingD65()
        );
    }

    private GradualChromaDampeningLchBasedGamutMapper(
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
        this.dampeningCurve = new ThanatomanicCurve6(1, shoulder);
        this.shoulder = shoulder;
    }

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        var lch = xyzToLch.apply(xyz);
        var cAtGamutBoundary = maxCFinder.applyAsDouble(lch);
        double originalC = lch.C();
        double dampenedC;
        var ratioToMaxC = originalC / cAtGamutBoundary;
        if (ratioToMaxC > shoulder) {
            var curveValue = dampeningCurve.mappedValueOf(ratioToMaxC);
            dampenedC = curveValue * cAtGamutBoundary;
        } else {
            dampenedC = originalC;
        }
        return Srgb.from(
                lchToXyz.apply(
                        lchCoordinatesToLch.apply(
                                new double[]{lch.L(), dampenedC, lch.h()}
                        )
                )
        );
    }

    @Override
    public String name() {
        return super.name() + name + "-shoulder-%d".formatted((int) (shoulder * 100));
    }
}

