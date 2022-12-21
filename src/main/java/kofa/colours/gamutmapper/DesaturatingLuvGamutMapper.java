package kofa.colours.gamutmapper;

import kofa.colours.model.LchUv;
import kofa.colours.model.Luv;
import kofa.io.RgbImage;

public class DesaturatingLuvGamutMapper extends AbstractDesaturatingLchBasedGamutMapper<Luv, LchUv> {
    public DesaturatingLuvGamutMapper(RgbImage image) {
        super(
                image,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchUv(lch),
                rec2020 -> Luv.from(rec2020.toXyz()).usingD65(),
                xyz -> Luv.from(xyz).usingD65().toLch(),
                LchUv::new,
                lch_uv -> lch_uv
                        .toLuv()
                        .toXyz().usingD65()
        );
    }
}