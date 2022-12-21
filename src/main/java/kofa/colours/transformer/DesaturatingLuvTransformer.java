package kofa.colours.transformer;

import kofa.colours.model.LchUv;
import kofa.colours.model.Luv;
import kofa.io.RgbImage;

public class DesaturatingLuvTransformer extends AbstractDesaturatingLchBasedTransformer<Luv, LchUv> {
    public DesaturatingLuvTransformer(RgbImage image) {
        super(
                image,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchUv(lch),
                rec2020 -> Luv.from(rec2020.toXyz()).usingD65(),
                xyz -> Luv.from(xyz).usingD65().toLch(),
                polarCoordinates -> new LchUv(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch_uv -> lch_uv
                        .toLuv()
                        .toXyz().usingD65()
        );
    }
}