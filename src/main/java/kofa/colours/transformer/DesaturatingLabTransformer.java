package kofa.colours.transformer;

import kofa.colours.model.Lab;
import kofa.colours.model.LchAb;
import kofa.colours.tools.MaxCLabLuvSolver;
import kofa.io.RgbImage;

public class DesaturatingLabTransformer extends AbstractDesaturatingLChBasedTransformer<Lab, LchAb> {
    public DesaturatingLabTransformer(RgbImage image) {
        super(
                image,
                new MaxCLabLuvSolver().solveLab(),
                rec2020 -> Lab.from(rec2020.toXYZ()).usingD65(),
                xyz -> Lab.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LchAb(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch_ab -> lch_ab
                        .toLab()
                        .toXYZ().usingD65()
        );
    }
}
