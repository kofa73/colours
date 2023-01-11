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
                            {0.819000675034942, 0.3619184812039237, -0.12886598814434513},
                            {0.03297134500099032, 0.9293121018200567, 0.03613225619255389},
                            {0.04818266855902102, 0.2642341625206832, 0.6335480999143406}
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
                            {0.8189625335336425, 0.36193051420879846, -0.12884180415807572},
                            {0.032984437518689624, 0.9293038863085773, 0.03613442553692302},
                            {0.048183751157980376, 0.26426142935535324, 0.6336149207409002}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_2DEGREE_STANDARD_OBSERVER =
            XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER.invert(CIEXYZ::new);

    static final SpaceConversionMatrix<CIEXYZ, LMS> XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER =
            new SpaceConversionMatrix<>(
                    LMS::new,
                    new double[][]{
                            {0.8188705673458518, 0.3621356674814235, -0.1290750293357169},
                            {0.033080872867520816, 0.9297527092351892, 0.036236338024257374},
                            {0.048681834435854915, 0.2670038020756251, 0.6400823878597188}
                    }
            );

    private static final SpaceConversionMatrix<LMS, CIEXYZ> LMS_TO_XYZ_D65_10DEGREE_SUPPLEMENTARY_OBSERVER =
            XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER.invert(CIEXYZ::new);

    private static final SpaceConversionMatrix<LMSPrime, OkLAB> LMS_PRIME_TO_LAB = new SpaceConversionMatrix<>(
            OkLAB::new,
            new double[][]{
                    {0.2104542519548019, 0.7936177948144867, -0.004072046769288452},
                    {1.977998495812036, -2.428592208389442, 0.45059371257740577},
                    {0.025904036510579237, 0.7827717514307554, -0.8086757879413347}
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
