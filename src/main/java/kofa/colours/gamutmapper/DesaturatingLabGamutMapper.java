package kofa.colours.gamutmapper;

import kofa.colours.model.Lab;
import kofa.colours.model.LchAb;
import kofa.io.RgbImage;

public class DesaturatingLabGamutMapper extends AbstractDesaturatingLchBasedGamutMapper<Lab, LchAb> {
    public DesaturatingLabGamutMapper(RgbImage image) {
        super(
                image,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchAb(lch),
                rec2020 -> Lab.from(rec2020.toXyz()).usingD65(),
                xyz -> Lab.from(xyz).usingD65().toLch(),
                LchAb::new,
                lch -> lch
                        .toLab()
                        .toXyz().usingD65()
        );
    }
}
