package kofa.colours.model;

import kofa.maths.Matrix3x3;

public class Srgb extends Rgb<Srgb> {
    private static final Srgb WHITE = new Srgb(1, 1, 1);

    // http://www.brucelindbloom.com/Eqn_RGB_XYZ_Matrix.html - sRGB D65
    public static final Matrix3x3<Srgb, XYZ> TO_XYZ = new Matrix3x3<>(
            XYZ::new,
            0.4124564, 0.3575761, 0.1804375,
            0.2126729, 0.7151522, 0.0721750,
            0.0193339, 0.1191920, 0.9503041
    );

    public static final Matrix3x3<XYZ, Srgb> FROM_XYZ = new Matrix3x3<>(
            Srgb::new,
            3.2404542, -1.5371385, -0.4985314,
            -0.9692660, 1.8760108, 0.0415560,
            0.0556434, -0.2040259, 1.0572252
    );

    public static final Matrix3x3<Srgb, Rec2020> TO_REC2020 = Rec2020.FROM_XYZ.multiply(TO_XYZ);

    public Srgb(double[] doubles) {
        super(doubles);
    }

    public Srgb(double r, double g, double b) {
        super(r, g, b);
    }

    public static Srgb from(XYZ xyz) {
        return xyz.Y() >= 0.9995 ? WHITE : FROM_XYZ.multiply(xyz);
    }

    public Rec2020 toRec2020() {
        return TO_REC2020.multiply(this);
    }

    @Override
    protected Matrix3x3<Srgb, XYZ> toXyzMatrix() {
        return TO_XYZ;
    }
}
