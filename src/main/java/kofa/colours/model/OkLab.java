package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3D;

import static kofa.colours.model.ConversionHelper.cubeOf;
import static kofa.colours.model.ConversionHelper.cubeRootOf;
import static kofa.colours.model.ConvertibleToLch.toPolar;

/**
 * OKLab from https://bottosson.github.io/posts/oklab/
 */
public record OkLab(double L, double a, double b) implements Vector3D, ConvertibleToLch<OkLch> {
    public OkLab(double[] values) {
        this(values[0], values[1], values[2]);
    }

    private static final SpaceConversionMatrix<Xyz, Lms> XYZ_TO_LMS = new SpaceConversionMatrix<>(
            Lms::new,
            new double[][]{
                    new double[]{+0.8189330101, +0.3618667424, -0.1288597137},
                    new double[]{+0.0329845436, +0.9293118715, +0.0361456387},
                    new double[]{+0.0482003018, +0.2643662691, +0.6338517070}
            }
    );

    private static final SpaceConversionMatrix<Lms, Xyz> LMS_TO_XYZ = XYZ_TO_LMS.invert(Xyz::new);

    private static final SpaceConversionMatrix<LmsPrime, OkLab> LMS_PRIME_TO_LAB = new SpaceConversionMatrix<>(
            OkLab::new,
            new double[][]{
                    new double[]{+0.2104542553, +0.7936177850, -0.0040720468},
                    new double[]{+1.9779984951, -2.4285922050, +0.4505937099},
                    new double[]{+0.0259040371, +0.7827717662, -0.8086757660}
            }
    );

    private static final SpaceConversionMatrix<OkLab, LmsPrime> LAB_TO_LMS_PRIME = LMS_PRIME_TO_LAB.invert(LmsPrime::new);

    public static OkLab from(Xyz xyz) {
        Lms lms = XYZ_TO_LMS.multiply(xyz);
        LmsPrime lmsPrime = LmsPrime.from(lms);
        return LMS_PRIME_TO_LAB.multiply(lmsPrime);
    }

    public Xyz toXyz() {
        LmsPrime lmsPrime = LAB_TO_LMS_PRIME.multiply(this);
        Lms lms = lmsPrime.toLms();
        return LMS_TO_XYZ.multiply(lms);
    }

    @Override
    public OkLch toLch() {
        return new OkLch(toPolar(L, a, b));
    }

    @Override
    public double[] coordinates() {
        return new double[]{L, a, b};
    }

    private static record LmsPrime(double lPrime, double mPrime, double sPrime) implements Vector3D {
        public LmsPrime(double[] coordinates) {
            this(coordinates[0], coordinates[1], coordinates[2]);
        }

        @Override
        public double[] coordinates() {
            return new double[]{lPrime, mPrime, sPrime};
        }

        Lms toLms() {
            return new Lms(
                    cubeOf(lPrime),
                    cubeOf(mPrime),
                    cubeOf(sPrime)
            );
        }

        static LmsPrime from(Lms lms) {
            return new LmsPrime(
                    cubeRootOf(lms.l()),
                    cubeRootOf(lms.m()),
                    cubeRootOf(lms.s())
            );
        }
    }
}
