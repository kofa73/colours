package kofa.colours.transformer;

import kofa.colours.model.Lab;
import kofa.colours.model.LchAb;
import kofa.colours.tools.MaxCLabLuvSolver;

public class ClippingLabTransformer extends AbstractLchCClippingTransformer<Lab, LchAb> {
    public ClippingLabTransformer() {
        super(
                xyz -> Lab.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LchAb(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch_ab -> lch_ab
                        .toLab()
                        .toXYZ().usingD65(),
                lch_ab -> new MaxCLabLuvSolver().solveLab(lch_ab.L(), lch_ab.h())
        );
    }
}
