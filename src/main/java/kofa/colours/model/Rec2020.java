package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;

import static java.lang.Math.pow;
import static kofa.colours.model.ConversionHelper.D65_WHITE_XYZ_IEC_61966_2_1;

public class Rec2020 extends Rgb<Rec2020> {
    private static final Rec2020 WHITE = new Rec2020(1, 1, 1);

    // values of sRGB primaries from http://www.brucelindbloom.com/index.html?WorkingSpaceInfo.html
    static final SpaceConversionMatrix<Rec2020, Xyz> TO_XYZ = new SpaceConversionMatrix<>(
            Xyz::new,
            calculateToXyzMatrix(
                    0.708, 0.292,
                    0.170, 0.797,
                    0.131, 0.046,
                    D65_WHITE_XYZ_IEC_61966_2_1
            )
    );

    static final SpaceConversionMatrix<Xyz, Rec2020> FROM_XYZ = TO_XYZ.invert(Rec2020::new);

    public static final SpaceConversionMatrix<Rec2020, Srgb> TO_SRGB = Srgb.FROM_XYZ.multiply(TO_XYZ);

    public Rec2020(double r, double g, double b) {
        super(r, g, b);
    }

    @Override
    protected SpaceConversionMatrix<Rec2020, Xyz> toXyzMatrix() {
        return TO_XYZ;
    }

    public Srgb toSRGB() {
        return TO_SRGB.multiply(this);
    }

    public static Rec2020 from(Xyz xyz) {
        return xyz.y() >= 1 ? WHITE : FROM_XYZ.multiply(xyz);
    }

    // https://en.wikipedia.org/wiki/Rec._2020#Transfer_characteristics
    public static double applyInverseOetf(double encoded) {
        return encoded < 0.0812428313 ?
                encoded / 4.5 :
                pow((encoded + 0.09929682680944) / 1.09929682680944, 1 / 0.45);
    }
}
