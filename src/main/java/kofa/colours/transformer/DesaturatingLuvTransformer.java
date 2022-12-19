package kofa.colours.transformer;

import kofa.colours.model.LchUv;
import kofa.colours.model.Luv;
import kofa.io.RgbImage;

public class DesaturatingLuvTransformer extends AbstractDesaturatingLChBasedTransformer<Luv, LchUv> {
    public DesaturatingLuvTransformer(RgbImage image) {
        super(
                image,
                new MaxCLabLuvSolver().solveMaxCForLchUv(),
                rec2020 -> Luv.from(rec2020.toXYZ()).usingD65(),
                xyz -> Luv.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LchUv(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch_uv -> lch_uv
                        .toLuv()
                        .toXYZ().usingD65()
        );
    }
}