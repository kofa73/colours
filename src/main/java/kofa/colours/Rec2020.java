package kofa.colours;

import kofa.maths.Matrix3;

public class Rec2020 extends RGB<Rec2020> {
    // http://www.russellcottrell.com/photo/matrixCalculator.htm
    public static final Matrix3<Rec2020, XYZ> TO_XYZ = new Matrix3<Rec2020, XYZ>(XYZ::new)
            .row(0.6369580f, 0.1446169f, 0.1688810f)
            .row(0.2627002f, 0.6779981f, 0.0593017f)
            .row(0.0000000f, 0.0280727f, 1.0609851f);

    public static final Matrix3<XYZ, Rec2020> FROM_XYZ = new Matrix3<XYZ, Rec2020>(Rec2020::new)
            .row(1.7166512f, -0.3556708f, -0.2533663f)
            .row(-0.6666844f, 1.6164812f, 0.0157685f)
            .row(0.0176399f, -0.0427706f, 0.9421031f);

    public Rec2020(float[] floats) {
        super(floats);
    }

    public Rec2020(float r, float g, float b) {
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
