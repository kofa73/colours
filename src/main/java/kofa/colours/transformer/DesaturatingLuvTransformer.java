package kofa.colours.transformer;

import kofa.colours.model.LCh_uv;
import kofa.colours.model.Luv;
import kofa.colours.tools.MaxCLabLuvSolver;
import kofa.io.RgbImage;

public class DesaturatingLuvTransformer extends AbstractDesaturatingLChBasedTransformer<Luv, LCh_uv> {
    public DesaturatingLuvTransformer(RgbImage image) {
        super(
                image,
                new MaxCLabLuvSolver().solveLuv(),
                rec2020 -> Luv.from(rec2020.toXYZ()).usingD65(),
                xyz -> Luv.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LCh_uv(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch_uv -> lch_uv
                        .toLuv()
                        .toXYZ().usingD65()
        );
    }
}