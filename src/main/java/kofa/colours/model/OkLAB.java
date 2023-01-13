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

    static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_ORIGINAL =
            XYZ_TO_LMS_ORIGINAL.invert(CIEXYZ::new);

    // scaled versions of the original matrix, fit for the different reference whites

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_IEC_61966_2_1 =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8190224659913279, 0.3619062708004724, -0.12887378965053944},
                            {0.03298365583383246, 0.9292868594322689, 0.0361446658541868},
                            {0.048177192362893285, 0.26423951977601745, 0.6335478093974771}
                    }
            );

    static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_IEC_61966_2_1 =
            XYZ_TO_LMS_D65_IEC_61966_2_1.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_ASTM_E308_01 =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8189889647022367, 0.36189146738896, -0.128868518197969},
                            {0.03298391207546648, 0.9292940788255503, 0.03614494665290377},
                            {0.048184113668356454, 0.26427748135788043, 0.6336388271114471}
                    }
            );

    static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_ASTM_E308_01 =
            XYZ_TO_LMS_D65_ASTM_E308_01.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8190241986802571, 0.3619070364339133, -0.12887406229042156},
                            {0.032983873279402304, 0.9292929857789572, 0.036144904138793656},
                            {0.04818206148759924, 0.2642662256737862, 0.6336118401792608}
                    }
            );

    static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_2DEGREE_STANDARD_OBSERVER =
            XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8189155534626621, 0.3618590287330593, -0.12885696688522788},
                            {0.03300531931251419, 0.9298972097879098, 0.03616840546637942},
                            {0.04867634422965124, 0.2669772394956293, 0.6401118401396407}
                    }
            );

    static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_10DEGREE_SUPPLEMENTARY_OBSERVER =
            XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER.invert(CIEXYZ::new);

    // https://bottosson.github.io/posts/oklab/#converting-from-xyz-to-oklab

    static final SpaceConversionMatrix<LMSPrime, OkLAB> LMS_PRIME_TO_LAB_ORIGINAL = new SpaceConversionMatrix<>(
            OkLAB::new,
            new double[][]{
                    {0.2104542553, 0.7936177850, -0.0040720468},
                    {1.9779984951, -2.4285922050, 0.4505937099},
                    {0.0259040371, 0.7827717662, -0.8086757660}
            }
    );

    // 1st row has been scaled so white L'M'S'(1, 0, 0) is mapped exactly to LAB with L = 1
    static final SpaceConversionMatrix<LMSPrime, OkLAB> LMS_PRIME_TO_LAB = new SpaceConversionMatrix<>(
            OkLAB::new,
            new double[][]{
                    {0.21045425666795267, 0.7936177901585156, -0.004072046826468304},
                    {1.977998495812036, -2.428592208389442, 0.45059371257740577},
                    {0.025904036510579237, 0.7827717514307554, -0.8086757879413347}
            }
    );

    static final SpaceConversionMatrix<OkLAB, LMSPrime> LAB_TO_LMS_PRIME = LMS_PRIME_TO_LAB.invert(LMSPrime::new);

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
