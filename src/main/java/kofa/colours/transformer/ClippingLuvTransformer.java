package kofa.colours.transformer;

import kofa.colours.LCh_uv;
import kofa.colours.Luv;
import kofa.colours.tools.MaxCLabLuv;

public class ClippingLuvTransformer extends Abstract_C_ClippingTransformer<Luv, LCh_uv> {
    public ClippingLuvTransformer() {
        super(
                xyz -> Luv.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LCh_uv(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch_uv -> lch_uv
                        .toLuv()
                        .toXYZ().usingD65(),
                lch_uv -> new MaxCLabLuv().solveLuv(lch_uv.L(), lch_uv.h())
        );
    }
}
