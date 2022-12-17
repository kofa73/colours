package kofa.colours;

import kofa.maths.Matrix3x3;

public class Rec2020 extends RGB<Rec2020> {
    // http://www.russellcottrell.com/photo/matrixCalculator.htm
    static final Matrix3x3<Rec2020, XYZ> TO_XYZ = new Matrix3x3<>(
            XYZ::new,
            6.36953507E-01, 1.44619185E-01, 1.68855854E-01,
            2.62698339E-01, 6.78008766E-01, 5.92928953E-02,
            4.99407097E-17, 2.80731358E-02, 1.06082723E+00
    );

    static final Matrix3x3<XYZ, Rec2020> FROM_XYZ = new Matrix3x3<>(
            Rec2020::new,
            1.71666343, -0.35567332, -0.25336809,
            -0.66667384, 1.61645574, 0.0157683,
            0.01764248, -0.04277698, 0.94224328
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
