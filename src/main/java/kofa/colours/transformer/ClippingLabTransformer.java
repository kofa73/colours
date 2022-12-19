package kofa.colours.transformer;

import kofa.colours.model.Lab;
import kofa.colours.model.LchAb;

public class ClippingLabTransformer extends AbstractLchCClippingTransformer<Lab, LchAb> {
    public ClippingLabTransformer() {
        super(
                xyz -> Lab.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LchAb(polarCoordinates),
                lch_ab -> lch_ab
                        .toLab()
                        .toXYZ().usingD65(),
                lch_ab -> new MaxCLabLuvSolver().solveMaxCForLchAb(lch_ab.L(), lch_ab.h())
        );
    }
}
