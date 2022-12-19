package kofa.colours.transformer;

import kofa.colours.model.LchUv;
import kofa.colours.model.Luv;

public class ClippingLuvTransformer extends AbstractLchCClippingTransformer<Luv, LchUv> {
    public ClippingLuvTransformer() {
        super(
                xyz -> Luv.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LchUv(polarCoordinates),
                lch_uv -> lch_uv
                        .toLuv()
                        .toXYZ().usingD65(),
                lch_uv -> new MaxCLabLuvSolver().solveMaxCForLchUv(lch_uv.L(), lch_uv.h())
        );
    }
}
