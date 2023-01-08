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
                            {0.818967383714981, 0.3619492664312499, -0.12886520149725447},
                            {0.03299352333036043, 0.9292592106075834, 0.03616146647732637},
                            {0.04817785658521273, 0.2642390482831809, 0.6335478132503992}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_IEC_61966_2_1 =
            XYZ_TO_LMS_D65_IEC_61966_2_1.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_ASTM_E308_01 =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8189509622312074, 0.3619239498645853, -0.1288651815112738},
                            {0.0329912500697916, 0.9292746987980047, 0.03615636453206032},
                            {0.04818496599039293, 0.26427789499842835, 0.6336378538375353}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_ASTM_E308_01 = XYZ_TO_LMS_D65_ASTM_E308_01.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8189761623397687, 0.3619321156970304, -0.12885517057340956},
                            {0.03299330404204958, 0.9292685035507454, 0.03615918056654339},
                            {0.04818259693608417, 0.2642683151174946, 0.6336096045918206}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_2DEGREE_STANDARD_OBSERVER =
            XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8197842415296197, 0.36088871802835165, -0.12872024855803574},
                            {0.0327208042008903, 0.9304891853249381, 0.03586813926096813},
                            {0.048664577663383925, 0.2669141563783153, 0.6401811778989841}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_10DEGREE_SUPPLEMENTARY_OBSERVER =
            XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<LMSPrime, OkLAB> LMS_PRIME_TO_LAB = new SpaceConversionMatrix<>(
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
