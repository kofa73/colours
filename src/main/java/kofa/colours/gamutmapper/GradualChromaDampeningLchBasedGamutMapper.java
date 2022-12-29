package kofa.colours.gamutmapper;

import kofa.colours.model.*;
import kofa.maths.ThanatomanicCurve6;
import kofa.maths.Vector3Constructor;

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
public class GradualChromaDampeningLchBasedGamutMapper<L extends Lch<L, ?>> extends GamutMapper {
    private final ToDoubleFunction<Srgb> maxCFinder;
    private final String name;
    private final ThanatomanicCurve6 dampeningCurve;
    // the shoulder of the curve; also, the ratio to maxC below which C is not modified
    private final double shoulder;
    private final Function<Srgb, L> sRgbToLch;
    private final Function<L, Srgb> lchToSrgb;
    private final Vector3Constructor<L> lchConstructor;

    public static GradualChromaDampeningLchBasedGamutMapper<CieLchAb> forLchAb(double shoulder) {
        return new GradualChromaDampeningLchBasedGamutMapper<>(
                shoulder,
                GamutBoundarySearchParams.FOR_CIELAB
        );
    }

    public static GradualChromaDampeningLchBasedGamutMapper<CieLchUv> forLchUv(double shoulder) {
        return new GradualChromaDampeningLchBasedGamutMapper<>(
                shoulder,
                GamutBoundarySearchParams.FOR_CIELUV
        );
    }

    public static GradualChromaDampeningLchBasedGamutMapper<OkLch> forOkLch(double shoulder) {
        return new GradualChromaDampeningLchBasedGamutMapper<>(
                shoulder,
                GamutBoundarySearchParams.FOR_OKLAB
        );
    }

    private GradualChromaDampeningLchBasedGamutMapper(
            double shoulder,
            GamutBoundarySearchParams<L> searchParams
    ) {
        super(true);
        this.shoulder = shoulder;
        this.name = searchParams.type().getSimpleName();
        this.dampeningCurve = new ThanatomanicCurve6(1, shoulder);
        this.sRgbToLch = searchParams.sRgbToLch();
        this.lchToSrgb = searchParams.lchToSrgb();
        this.lchConstructor = searchParams.lchConstructor();
        this.maxCFinder = sRgb -> new MaxCLabLuvSolver<>(searchParams).solveMaxCForLch(sRgb);
    }

    @Override
    public Srgb getInsideGamut(Srgb sRgb) {
        var originalLch = sRgbToLch.apply(sRgb);
        if (sRgb.isBlack()) {
            return Srgb.BLACK;
        }
        if (sRgb.isWhite()) {
            return Srgb.WHITE;
        }
        var cAtGamutBoundary = maxCFinder.applyAsDouble(sRgb);
        double originalC = originalLch.c();
        double dampenedC;
        if (cAtGamutBoundary != 0) {
            var ratioToMaxC = originalC / cAtGamutBoundary;
            if (ratioToMaxC > shoulder) {
                var curveValue = dampeningCurve.mappedValueOf(ratioToMaxC);
                dampenedC = curveValue * cAtGamutBoundary;
            } else {
                dampenedC = originalC;
            }
        } else {
            dampenedC = 0;
        }
        return lchToSrgb.apply(
                lchConstructor.createFrom(originalLch.l(), dampenedC, originalLch.h())
        );
    }

    @Override
    public String name() {
        return super.name() + name + "-shoulder-%d".formatted((int) (shoulder * 100));
    }
}

