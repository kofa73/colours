package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;

import static java.lang.Math.pow;

public class Rec2020 extends Rgb<Rec2020> {
    private static final Rec2020 WHITE = new Rec2020(1, 1, 1);

    // values of sRGB primaries from http://www.brucelindbloom.com/index.html?WorkingSpaceInfo.html
    // white point from https://www.colour-science.org/api/0.3.1/html/colour.models.dataset.rec_2020.html#colour.models.dataset.rec_2020.REC_2020_COLOURSPACE
    static final SpaceConversionMatrix<Rec2020, CIEXYZ> TO_XYZ = new SpaceConversionMatrix<>(
            CIEXYZ::new,
            calculateToXyzMatrix(
                    0.708, 0.292,
                    0.170, 0.797,
                    0.131, 0.046,
                    CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER
            )
    );

    static final SpaceConversionMatrix<CIEXYZ, Rec2020> FROM_XYZ = TO_XYZ.invert(Rec2020::new);

    public static final SpaceConversionMatrix<Rec2020, Srgb> TO_SRGB = Srgb.FROM_XYZ.multiply(TO_XYZ);

    public Rec2020(double r, double g, double b) {
        super(r, g, b);
    }

    @Override
    protected SpaceConversionMatrix<Rec2020, CIEXYZ> toXyzMatrix() {
        return TO_XYZ;
    }

    public Srgb toSRGB() {
        return TO_SRGB.multiply(this);
    }

    public static Rec2020 from(CIEXYZ xyz) {
        return xyz.Y() >= 1 ? WHITE : FROM_XYZ.multiply(xyz);
    }

    // https://en.wikipedia.org/wiki/Rec._2020#Transfer_characteristics
    public static double applyInverseOetf(double encoded) {
        return encoded < 0.0812428313 ?
                encoded / 4.5 :
                pow((encoded + 0.09929682680944) / 1.09929682680944, 1 / 0.45);
    }
}
