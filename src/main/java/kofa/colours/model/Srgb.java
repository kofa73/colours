package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;

import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ;

public class Srgb extends Rgb<Srgb> {
    private static final Srgb WHITE = new Srgb(1, 1, 1);

    // values of sRGB primaries from http://www.brucelindbloom.com/index.html?WorkingSpaceInfo.html
    public static final SpaceConversionMatrix<Srgb, XYZ> TO_XYZ = new SpaceConversionMatrix<>(
            XYZ::new,
            calculateToXyzMatrix(
                    0.6400, 0.3300,
                    0.3000, 0.6000,
                    0.1500, 0.0600,
                    D65_WHITE_XYZ
            )
    );

    public static final SpaceConversionMatrix<XYZ, Srgb> FROM_XYZ = TO_XYZ.invert(Srgb::new);

    public static final SpaceConversionMatrix<Srgb, Rec2020> TO_REC2020 = Rec2020.FROM_XYZ.multiply(TO_XYZ);

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
    protected SpaceConversionMatrix<Srgb, XYZ> toXyzMatrix() {
        return TO_XYZ;
    }
}
