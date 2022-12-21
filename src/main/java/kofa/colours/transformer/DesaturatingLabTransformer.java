package kofa.colours.transformer;

import kofa.colours.model.Lab;
import kofa.colours.model.LchAb;
import kofa.io.RgbImage;

public class DesaturatingLabTransformer extends AbstractDesaturatingLchBasedTransformer<Lab, LchAb> {
    public DesaturatingLabTransformer(RgbImage image) {
        super(
                image,
                lch -> new MaxCLabLuvSolver().solveMaxCForLchAb(lch),
                rec2020 -> Lab.from(rec2020.toXyz()).usingD65(),
                xyz -> Lab.from(xyz).usingD65().toLch(),
                polarCoordinates -> new LchAb(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch -> lch
                        .toLab()
                        .toXyz().usingD65()
        );
    }
}
