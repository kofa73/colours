package kofa.colours.transformer;

import kofa.colours.LCh_ab;
import kofa.colours.Lab;
import kofa.colours.tools.MaxCLabLuv;
import kofa.io.RgbImage;

public class DesaturatingLabTransformer extends AbstractDesaturatingLChBasedTransformer<Lab, LCh_ab> {
    public DesaturatingLabTransformer(RgbImage image) {
        super(
                image,
                new MaxCLabLuv().solveLab(),
                rec2020 -> Lab.from(rec2020.toXYZ()).usingD65(),
                xyz -> Lab.from(xyz).usingD65().toLCh(),
                polarCoordinates -> new LCh_ab(polarCoordinates[0], polarCoordinates[1], polarCoordinates[2]),
                lch_ab -> lch_ab
                        .toLab()
                        .toXYZ().usingD65()
        );
    }
}