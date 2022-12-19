package kofa.colours.transformer;

import kofa.colours.LCh_ab;
import kofa.colours.Lab;
import kofa.colours.tools.MaxCLabLuv;

public class ClippingLabTransformer extends Abstract_C_ClippingTransformer<Lab, LCh_ab> {
    public ClippingLabTransformer() {
        super(
                xyz -> Lab.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LCh_ab(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch_ab -> lch_ab
                        .toLab()
                        .toXYZ().usingD65(),
                lch_ab -> new MaxCLabLuv().solveLab(lch_ab.L(), lch_ab.h())
        );
    }
}
