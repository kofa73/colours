package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;

import static java.lang.Math.abs;
import static kofa.colours.model.ConversionHelper.cubeOf;
import static kofa.colours.model.ConversionHelper.cubeRootOf;

/**
 * OKLab from https://bottosson.github.io/posts/oklab/
 */
public class OkLab extends ConvertibleToLch<OkLab, OkLch> {
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

    public OkLab(double l, double a, double b) {
        super(l, a, b, OkLch::new);
    }

    public double l() {
        return coordinate1;
    }

    public double a() {
        return coordinate2;
    }

    public double b() {
        return coordinate3;
    }

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

    private static double zeroOr(double x) {
        return abs(x) < 1E-4 ? 0 : x;
    }

    private static class LmsPrime extends Vector3 {
        LmsPrime(double lPrime, double mPrime, double sPrime) {
            super(lPrime, mPrime, sPrime);
        }

        double lPrime() {
            return coordinate1;
        }

        double mPrime() {
            return coordinate2;
        }

        double sPrime() {
            return coordinate3;
        }

        Lms toLms() {
            return new Lms(
                    cubeOf(lPrime()),
                    cubeOf(mPrime()),
                    cubeOf(sPrime())
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

    @Override
    public String toString() {
        return "%s(%f, %f, %f)".formatted(this.getClass().getSimpleName(), l(), a(), b());
    }

    public static OkLab from(Srgb sRgb) {
        double l = 0.4122214708 * sRgb.r() + 0.5363325363 * sRgb.g() + 0.0514459929 * sRgb.b();
        double m = 0.2119034982 * sRgb.r() + 0.6806995451 * sRgb.g() + 0.1073969566 * sRgb.b();
        double s = 0.0883024619 * sRgb.r() + 0.2817188376 * sRgb.g() + 0.6299787005 * sRgb.b();

        double lPrime = cubeRootOf(l);
        double mPrime = cubeRootOf(m);
        double sPrime = cubeRootOf(s);

        return new OkLab(
                0.2104542553 * lPrime + 0.7936177850 * mPrime - 0.0040720468 * sPrime,
                1.9779984951 * lPrime - 2.4285922050 * mPrime + 0.4505937099 * sPrime,
                0.0259040371 * lPrime + 0.7827717662 * mPrime - 0.8086757660 * sPrime
        );
    }

    public Srgb toSrgb() {
        double lPrime = l() + 0.3963377774 * a() + 0.2158037573 * b();
        double mPrime = l() - 0.1055613458 * a() - 0.0638541728 * b();
        double sPrime = l() - 0.0894841775 * a() - 1.2914855480 * b();

        double l = lPrime * lPrime * lPrime;
        double m = mPrime * mPrime * mPrime;
        double s = sPrime * sPrime * sPrime;

        return new Srgb(
                +4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
                -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
                -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
        );
    }
}
