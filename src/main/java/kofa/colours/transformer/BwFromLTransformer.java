package kofa.colours.transformer;

import kofa.colours.model.Lab;
import kofa.colours.model.Srgb;
import kofa.colours.model.XYZ;

public class BwFromLTransformer extends Transformer {
    public BwFromLTransformer() {
        super(true);
    }

    @Override
    public Srgb getInsideGamut(XYZ xyz) {
        var lab = Lab.from(xyz).usingD65().values();
        lab[1] = 0;
        lab[2] = 0;
        return Srgb.from(new Lab(lab).toXYZ().usingD65());
    }
}
