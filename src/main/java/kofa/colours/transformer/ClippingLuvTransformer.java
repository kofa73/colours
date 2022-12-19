package kofa.colours.transformer;

import kofa.colours.LCh_uv;
import kofa.colours.Luv;
import kofa.colours.tools.MaxCLabLuv;

public class ClippingLuvTransformer extends Abstract_C_ClippingTransformer<Luv, LCh_uv> {
    public ClippingLuvTransformer() {
        super(
                new MaxCLabLuv().solveLuv(),
                xyz -> Luv.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LCh_uv(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch_uv -> lch_uv
                        .toLuv()
                        .toXYZ().usingD65()
        );
    }
}
