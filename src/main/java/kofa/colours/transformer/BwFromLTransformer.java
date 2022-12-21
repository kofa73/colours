package kofa.colours.transformer;

import kofa.colours.model.Lab;
import kofa.colours.model.Srgb;
import kofa.colours.model.Xyz;

public class BwFromLTransformer extends Transformer {
    public BwFromLTransformer() {
        super(true);
    }

    @Override
    public Srgb getInsideGamut(Xyz xyz) {
        var lab = Lab.from(xyz).usingD65().values();
        lab[1] = 0;
        lab[2] = 0;
        return Srgb.from(new Lab(lab).toXyz().usingD65());
    }
}
