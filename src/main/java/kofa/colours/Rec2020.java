package kofa.colours;

import kofa.maths.Matrix3;

public class Rec2020 extends RGB<Rec2020> {
    // http://www.russellcottrell.com/photo/matrixCalculator.htm
    public static final Matrix3<Rec2020, XYZ> TO_XYZ = new Matrix3<Rec2020, XYZ>(XYZ::new)
            .row(0.6369580, 0.1446169, 0.1688810)
            .row(0.2627002, 0.6779981, 0.0593017)
            .row(0.0000000, 0.0280727, 1.0609851);

    public static final Matrix3<XYZ, Rec2020> FROM_XYZ = new Matrix3<XYZ, Rec2020>(Rec2020::new)
            .row(1.7166512, -0.3556708, -0.2533663)
            .row(-0.6666844, 1.6164812, 0.0157685)
            .row(0.0176399, -0.0427706, 0.9421031);

    public Rec2020(double[] doubles) {
        super(doubles);
    }

    public Rec2020(double r, double g, double b) {
        super(r, g, b);
    }

    @Override
    protected Matrix3<Rec2020, XYZ> toXyzMatrix() {
        return TO_XYZ;
    }

    public static Rec2020 from(XYZ xyz) {
        return FROM_XYZ.multipliedBy(xyz);
    }
}
