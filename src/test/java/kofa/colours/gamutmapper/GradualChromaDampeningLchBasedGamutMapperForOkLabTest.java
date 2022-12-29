package kofa.colours.gamutmapper;

import kofa.colours.model.OkLch;
import kofa.colours.model.Srgb;
import kofa.maths.PrimitiveDoubleToDoubleFunction;
import kofa.maths.Solver;
import org.junit.jupiter.api.Test;

class GradualChromaDampeningLchBasedGamutMapperForOkLabTest {
    @Test
    void x() {
        var lch = new OkLch(0.9995662130364427, 0.739506239487755, 0.7176225917093684);
        System.out.println(lch.toLab().toSrgb());
        new Solver(clipDetectorForLch(lch.l(), lch.h())).solve(0, 0.1, 0).get();
    }

    private static final double COMPONENT_MIN = 1E-12;
    private static final double COMPONENT_MAX = 1 - COMPONENT_MIN;

    private PrimitiveDoubleToDoubleFunction clipDetectorForLch(double l, double h) {
        return (double c) -> {
            Srgb sRgb = new OkLch(l, c, h).toLab().toSrgb();
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