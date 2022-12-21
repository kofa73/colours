package kofa.colours.transformer;

import kofa.colours.model.Lab;
import kofa.colours.model.LchAb;

public class ClippingLabTransformer extends AbstractLchCClippingTransformer<Lab, LchAb> {
    public ClippingLabTransformer() {
        super(
                xyz -> Lab.from(xyz).usingD65().toLch(),
                LchAb::new,
                lch -> lch
                        .toLab()
                        .toXyz().usingD65(),
                lch -> new MaxCLabLuvSolver().solveMaxCForLchAb(lch)
        );
    }
}
