package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;

import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ;

public class Rec2020 extends Rgb<Rec2020> {
    private static final Rec2020 WHITE = new Rec2020(1, 1, 1);

    // values of sRGB primaries from http://www.brucelindbloom.com/index.html?WorkingSpaceInfo.html
    static final SpaceConversionMatrix<Rec2020, XYZ> TO_XYZ = new SpaceConversionMatrix<>(
            XYZ::new,
            calculateToXyzMatrix(
                    0.708, 0.292,
                    0.170, 0.797,
                    0.131, 0.046,
                    D65_WHITE_XYZ
            )
    );

    static final SpaceConversionMatrix<XYZ, Rec2020> FROM_XYZ = TO_XYZ.invert(Rec2020::new);

    public static final SpaceConversionMatrix<Rec2020, Srgb> TO_SRGB = Srgb.FROM_XYZ.multiply(TO_XYZ);

    public Rec2020(double[] doubles) {
        super(doubles);
    }

    public Rec2020(double r, double g, double b) {
        super(r, g, b);
    }

    @Override
    protected SpaceConversionMatrix<Rec2020, XYZ> toXyzMatrix() {
        return TO_XYZ;
    }

    public Srgb toSRGB() {
        return TO_SRGB.multiply(this);
    }

    public static Rec2020 from(XYZ xyz) {
        return xyz.Y() >= 0.9995 ? WHITE : FROM_XYZ.multiply(xyz);
    }
}
