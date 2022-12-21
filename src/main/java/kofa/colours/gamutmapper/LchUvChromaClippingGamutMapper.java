package kofa.colours.gamutmapper;

import kofa.colours.model.LchUv;
import kofa.colours.model.Luv;

/**
 * A gamut mapper that clips C (chroma) of LCh(uv)
 */
public class LchUvChromaClippingGamutMapper extends AbstractLchChromaClippingGamutMapper<LchUv> {
    public LchUvChromaClippingGamutMapper() {
        super(
                lch -> new MaxCLabLuvSolver().solveMaxCForLchUv(lch), xyz -> Luv.from(xyz).usingD65().toLch(),
                LchUv::new,
                lch -> lch
                        .toLuv()
                        .toXyz().usingD65()
        );
    }
}
