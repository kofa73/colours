package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;

import static kofa.colours.model.ConversionHelper.cubeOf;
import static kofa.colours.model.ConversionHelper.cubeRootOf;

/**
 * OKLab from https://bottosson.github.io/posts/oklab/
 */
public class OkLAB extends LAB<OkLAB, OkLCh> {
    // less than L of new Rec2020(0.0001 / 65535, 0.0001 / 65535, 0.0001 / 65535) ~ 0.00115
    public static final double BLACK_L_THRESHOLD = 1E-3;
    public static final OkLAB BLACK = new OkLAB(0, 0, 0);

    public static final double WHITE_L = 1;
    public static final double WHITE_L_THRESHOLD = WHITE_L - BLACK_L_THRESHOLD;
    public static final OkLAB WHITE = new OkLAB(WHITE_L, 0, 0);

    // original M1 values from https://bottosson.github.io/posts/oklab/#converting-from-xyz-to-oklab
    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_ORIGINAL =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {+0.8189330101, +0.3618667424, -0.1288597137},
                            {+0.0329845436, +0.9293118715, +0.0361456387},
                            {+0.0482003018, +0.2643662691, +0.6338517070}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_ORIGINAL =
            XYZ_TO_LMS_ORIGINAL.invert(CIEXYZ::new);

    // for the matrices below, see OkLABTuner

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_IEC_61966_2_1 =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8189260971685364, 0.3619373516365322, -0.1288182289340202},
                            {0.032993629808442636, 0.9292654058754899, 0.036155684901152806},
                            {0.048177856584029476, 0.26423904832527595, 0.6335478132127796}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_IEC_61966_2_1 =
            XYZ_TO_LMS_D65_IEC_61966_2_1.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_ASTM_E308_01 =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8189185058849386, 0.3619250232288143, -0.12883783525960107},
                            {0.032989744106165446, 0.929280894117544, 0.03615198924272081},
                            {0.04818496599041629, 0.2642778949982382, 0.6336378538376899}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_ASTM_E308_01 =
            XYZ_TO_LMS_D65_ASTM_E308_01.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8189662359148219, 0.36191961267228145, -0.12883502421019777},
                            {0.03299231280841035, 0.9292746988289813, 0.036154356267065285},
                            {0.048182596931219776, 0.2642683151531112, 0.6336096045633581}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_2DEGREE_STANDARD_OBSERVER =
            XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8159843432820131, 0.36371939026641636, -0.1280008061421776},
                            {0.032880132515493564, 0.9302414073489597, 0.03595827411754808},
                            {0.04868372868954661, 0.2669508466886979, 0.6401300644698302}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_10DEGREE_SUPPLEMENTARY_OBSERVER =
            XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER.invert(CIEXYZ::new);

    private static final SpaceConversionMatrix<LMSPrime, OkLAB> LMS_PRIME_TO_LAB = new SpaceConversionMatrix<>(
            OkLAB::new,
            new double[][]{
                    {+0.2104542553, +0.7936177850, -0.0040720468},
                    {+1.9779984951, -2.4285922050, +0.4505937099},
                    {+0.0259040371, +0.7827717662, -0.8086757660}
            }
    );

    private static final SpaceConversionMatrix<OkLAB, LMSPrime> LAB_TO_LMS_PRIME = LMS_PRIME_TO_LAB.invert(LMSPrime::new);

    public OkLAB(double L, double a, double b) {
        super(L, a, b, OkLCh::new);
    }

    public static XyzLabConverter from(CIEXYZ xyz) {
        return new XyzLabConverter(xyz);
    }

    public LabXyzConverter toXyz() {
        LMSPrime lmsPrime = LAB_TO_LMS_PRIME.multiply(this);
        LMS lms = lmsPrime.toLms();
        return new LabXyzConverter(lms);
    }

    public boolean isBlack() {
        return L() < BLACK_L_THRESHOLD;
    }

    public boolean isWhite() {
        return L() >= WHITE_L_THRESHOLD;
    }

    public static class LabXyzConverter implements WhitePointAwareConverter<CIEXYZ> {
        private final LMS lms;

        private LabXyzConverter(LMS lms) {
            this.lms = lms;
        }

        public CIEXYZ usingOriginalMatrix() {
            return LMS_TO_XYZ_ORIGINAL.multiply(lms);
        }

        @Override
        public CIEXYZ usingD65_IEC_61966_2_1() {
            return LMS_TO_XYZ_D65_IEC_61966_2_1.multiply(lms);
        }

        @Override
        public CIEXYZ usingD65_ASTM_E308_01() {
            return LMS_TO_XYZ_D65_ASTM_E308_01.multiply(lms);
        }

        @Override
        public CIEXYZ usingD65_2DEGREE_STANDARD_OBSERVER() {
            return LMS_TO_XYZ_D65_2DEGREE_STANDARD_OBSERVER.multiply(lms);
        }

        @Override
        public CIEXYZ usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER() {
            return LMS_TO_XYZ_D65_10DEGREE_SUPPLEMENTARY_OBSERVER.multiply(lms);
        }
    }

    public static class XyzLabConverter implements WhitePointAwareConverter<OkLAB> {
        private final CIEXYZ xyz;

        private XyzLabConverter(CIEXYZ xyz) {
            this.xyz = xyz;
        }

        public OkLAB usingOriginalMatrix() {
            return toLab(XYZ_TO_LMS_ORIGINAL);
        }

        @Override
        public OkLAB usingD65_IEC_61966_2_1() {
            return toLab(XYZ_TO_LMS_D65_IEC_61966_2_1);
        }

        @Override
        public OkLAB usingD65_ASTM_E308_01() {
            return toLab(XYZ_TO_LMS_D65_ASTM_E308_01);
        }

        @Override
        public OkLAB usingD65_2DEGREE_STANDARD_OBSERVER() {
            return toLab(XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER);
        }

        @Override
        public OkLAB usingD65_10DEGREE_SUPPLEMENTARY_OBSERVER() {
            return toLab(XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER);
        }

        private OkLAB toLab(SpaceConversionMatrix<CIEXYZ, LMS> xyzToLmsMatrix) {
            LMS lms = xyzToLmsMatrix.multiply(xyz);
            LMSPrime lmsPrime = LMSPrime.from(lms);
            return LMS_PRIME_TO_LAB.multiply(lmsPrime);
        }
    }

    private static class LMSPrime extends Vector3 {
        private final double lPrime;
        private final double mPrime;
        private final double sPrime;

        LMSPrime(double lPrime, double mPrime, double sPrime) {
            super(lPrime, mPrime, sPrime);
            this.lPrime = lPrime;
            this.mPrime = mPrime;
            this.sPrime = sPrime;
        }

        LMS toLms() {
            return new LMS(
                    cubeOf(lPrime),
                    cubeOf(mPrime),
                    cubeOf(sPrime)
            );
        }

        static LMSPrime from(LMS lms) {
            return new LMSPrime(
                    cubeRootOf(lms.l),
                    cubeRootOf(lms.m),
                    cubeRootOf(lms.s)
            );
        }
    }

    private static class LMS extends Vector3 {
        private final double l;
        private final double m;
        private final double s;

        protected LMS(double l, double m, double s) {
            super(l, m, s);
            this.l = l;
            this.m = m;
            this.s = s;
        }
    }
}
