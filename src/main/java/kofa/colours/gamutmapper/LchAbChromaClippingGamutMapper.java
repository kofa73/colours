package kofa.colours.gamutmapper;

import kofa.colours.model.Lab;
import kofa.colours.model.LchAb;

/**
 * A gamut mapper that clips C (chroma) of LCh(ab)
 */
public class LchAbChromaClippingGamutMapper extends AbstractLchChromaClippingGamutMapper<LchAb> {
    public LchAbChromaClippingGamutMapper() {
        super(
                lch -> new MaxCLabLuvSolver().solveMaxCForLchAb(lch), xyz -> Lab.from(xyz).usingD65().toLch(),
                LchAb::new,
                lch -> lch
                        .toLab()
                        .toXyz().usingD65()
        );
    }
}
