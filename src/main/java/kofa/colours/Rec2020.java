package kofa.colours;

import kofa.maths.Matrix3x3;

public class Rec2020 extends RGB<Rec2020> {
    // http://www.russellcottrell.com/photo/matrixCalculator.htm
    static final Matrix3x3<Rec2020, XYZ> TO_XYZ = new Matrix3x3<>(
            XYZ::new,
            0.6369580, 0.1446169, 0.1688810,
            0.2627002, 0.6779981, 0.0593017,
            0.0000000, 0.0280727, 1.0609851
    );

    static final Matrix3x3<XYZ, Rec2020> FROM_XYZ = new Matrix3x3<>(
            Rec2020::new,
            1.7166512, -0.3556708, -0.2533663,
            -0.6666844, 1.6164812, 0.0157685,
            0.0176399, -0.0427706, 0.9421031
    );

    public static final Matrix3x3<Rec2020, SRGB> TO_SRGB = SRGB.FROM_XYZ.multiply(TO_XYZ);

    public Rec2020(double[] doubles) {
        super(doubles);
    }

    public Rec2020(double r, double g, double b) {
        super(r, g, b);
    }

    @Override
    protected Matrix3x3<Rec2020, XYZ> toXyzMatrix() {
        return TO_XYZ;
    }

    public SRGB toSRGB() {
        return TO_SRGB.multiply(this);
    }

    public static Rec2020 from(XYZ xyz) {
        return FROM_XYZ.multiply(xyz);
    }
}
