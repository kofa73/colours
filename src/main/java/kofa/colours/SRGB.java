package kofa.colours;

import kofa.maths.Matrix3;

public class SRGB extends RGB<SRGB> {
    // http://www.brucelindbloom.com/Eqn_RGB_XYZ_Matrix.html - sRGB D65
    public static final Matrix3<SRGB, XYZ> TO_XYZ = new Matrix3<SRGB, XYZ>(XYZ::new)
            .row(0.4124564f, 0.3575761f, 0.1804375f)
            .row(0.2126729f, 0.7151522f, 0.0721750f)
            .row(0.0193339f, 0.1191920f, 0.9503041f);

    public static final Matrix3<XYZ, SRGB> FROM_XYZ = new Matrix3<XYZ, SRGB>(SRGB::new)
            .row(3.2404542f, -1.5371385f, -0.4985314f)
            .row(-0.9692660f, 1.8760108f, 0.0415560f)
            .row(0.0556434f, -0.2040259f, 1.0572252f);

    public SRGB(float[] floats) {
        super(floats);
    }

    public SRGB(float r, float g, float b) {
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
