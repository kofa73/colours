package kofa.colours;

import kofa.maths.Matrix3;

public class SRGB extends RGB<SRGB> {
    // http://www.brucelindbloom.com/Eqn_RGB_XYZ_Matrix.html - sRGB D65
    public static final Matrix3<SRGB, XYZ> TO_XYZ = new Matrix3<SRGB, XYZ>(XYZ::new)
            .row(0.4124564, 0.3575761, 0.1804375)
            .row(0.2126729, 0.7151522, 0.0721750)
            .row(0.0193339, 0.1191920, 0.9503041);

    public static final Matrix3<XYZ, SRGB> FROM_XYZ = new Matrix3<XYZ, SRGB>(SRGB::new)
            .row(3.2404542, -1.5371385, -0.4985314)
            .row(-0.9692660, 1.8760108, 0.0415560)
            .row(0.0556434, -0.2040259, 1.0572252);

    public SRGB(double[] doubles) {
        super(doubles);
    }

    public SRGB(double r, double g, double b) {
        super(r, g, b);
    }

    public static SRGB from(XYZ xyz) {
        return FROM_XYZ.multipliedBy(xyz);
    }

    @Override
    protected Matrix3<SRGB, XYZ> toXyzMatrix() {
        return TO_XYZ;
    }
}
