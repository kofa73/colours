package kofa.colours.transformer;

import kofa.colours.model.LchUv;
import kofa.colours.model.Luv;

public class ClippingLuvTransformer extends AbstractLchCClippingTransformer<Luv, LchUv> {
    public ClippingLuvTransformer() {
        super(
                xyz -> Luv.from(xyz).usingD65().toLch(),
                LchUv::new,
                lch -> lch
                        .toLuv()
                        .toXyz().usingD65(),
                lch -> new MaxCLabLuvSolver().solveMaxCForLchUv(lch)
        );
    }
}
