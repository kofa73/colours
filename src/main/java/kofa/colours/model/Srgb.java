package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;

public class Srgb extends Rgb<Srgb> {
    public static final Srgb BLACK = new Srgb(0, 0, 0);
    public static final Srgb WHITE = new Srgb(1, 1, 1);

    // values of sRGB primaries from http://www.brucelindbloom.com/index.html?WorkingSpaceInfo.html
    public static final SpaceConversionMatrix<Srgb, CIEXYZ> TO_XYZ = new SpaceConversionMatrix<>(
            CIEXYZ::new,
            calculateToXyzMatrix(
                    0.6400, 0.3300,
                    0.3000, 0.6000,
                    0.1500, 0.0600,
                    CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER
            )
    );

    public static final SpaceConversionMatrix<CIEXYZ, Srgb> FROM_XYZ = TO_XYZ.invert(Srgb::new);

    public static final SpaceConversionMatrix<Srgb, Rec2020> TO_REC2020 = Rec2020.FROM_XYZ.multiply(TO_XYZ);

    public Srgb(double r, double g, double b) {
        super(r, g, b);
    }

    public static Srgb from(CIEXYZ xyz) {
        return xyz.Y >= 1 ? WHITE : FROM_XYZ.multiply(xyz);
    }

    public Rec2020 toRec2020() {
        return TO_REC2020.multiply(this);
    }

    @Override
    protected SpaceConversionMatrix<Srgb, CIEXYZ> toXyzMatrix() {
        return TO_XYZ;
    }
}
