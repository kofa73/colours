package kofa.colours.transformer;

import kofa.colours.model.LCh_uv;
import kofa.colours.model.Luv;
import kofa.colours.tools.MaxCLabLuvSolver;

public class ClippingLuvTransformer extends AbstractLchCClippingTransformer<Luv, LCh_uv> {
    public ClippingLuvTransformer() {
        super(
                xyz -> Luv.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LCh_uv(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch_uv -> lch_uv
                        .toLuv()
                        .toXYZ().usingD65(),
                lch_uv -> new MaxCLabLuvSolver().solveLuv(lch_uv.L(), lch_uv.h())
        );
    }
}
