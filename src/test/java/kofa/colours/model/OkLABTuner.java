package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

import static kofa.colours.model.ConversionHelper.cubeOf;
import static kofa.colours.model.ConversionHelper.cubeRootOf;

class OkLABTuner extends LAB<OkLABTuner, OkLABTunerLCh> {

    public static final double BLACK_L_THRESHOLD = 1E-3;
    public static final OkLABTuner BLACK = new OkLABTuner(0, 0, 0);

    public static final double WHITE_L = 1;
    public static final double WHITE_L_THRESHOLD = WHITE_L - BLACK_L_THRESHOLD;
    public static final OkLABTuner WHITE = new OkLABTuner(WHITE_L, 0, 0);

    private static final ThreadLocal<SpaceConversionMatrix<CIEXYZ, LMS>> XYZ_TO_LMS = new ThreadLocal<>();
    private static final ThreadLocal<SpaceConversionMatrix<LMS, CIEXYZ>> LMS_to_XYZ = new ThreadLocal<>();

    static void set_XYZ_TO_LMS(double[][] matrix) {
        SpaceConversionMatrix<CIEXYZ, LMS> xyzToLmsMatrix = new SpaceConversionMatrix<>(LMS::new, matrix);
        XYZ_TO_LMS.set(xyzToLmsMatrix);
        LMS_to_XYZ.set(xyzToLmsMatrix.invert(CIEXYZ::new));
    }

    private static final SpaceConversionMatrix<LMSPrime, OkLABTuner> LMS_PRIME_TO_LAB = new SpaceConversionMatrix<>(
            OkLABTuner::new,
            new double[][]{
                    new double[]{+0.2104542553, +0.7936177850, -0.0040720468},
                    new double[]{+1.9779984951, -2.4285922050, +0.4505937099},
                    new double[]{+0.0259040371, +0.7827717662, -0.8086757660}
            }
    );

    private static final SpaceConversionMatrix<OkLABTuner, LMSPrime> LAB_TO_LMS_PRIME = LMS_PRIME_TO_LAB.invert(LMSPrime::new);

    protected OkLABTuner(double coordinate1, double coordinate2, double coordinate3) {
        super(coordinate1, coordinate2, coordinate3, OkLABTunerLCh::new);
    }

    public static OkLABTuner from(CIEXYZ xyz) {
        LMS lms = XYZ_TO_LMS.get().multiply(xyz);
        LMSPrime lmsPrime = LMSPrime.from(lms);
        return LMS_PRIME_TO_LAB.multiply(lmsPrime);
    }

    public CIEXYZ toXyz() {
        LMSPrime lmsPrime = LAB_TO_LMS_PRIME.multiply(this);
        LMS lms = lmsPrime.toLms();
        return LMS_to_XYZ.get().multiply(lms);
    }

    public boolean isBlack() {
        return L() < BLACK_L_THRESHOLD;
    }

    public boolean isWhite() {
        return L() >= WHITE_L_THRESHOLD;
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

    public static OkLABTuner from(Srgb sRgb) {
        double l = 0.4122214708 * sRgb.r() + 0.5363325363 * sRgb.g() + 0.0514459929 * sRgb.b();
        double m = 0.2119034982 * sRgb.r() + 0.6806995451 * sRgb.g() + 0.1073969566 * sRgb.b();
        double s = 0.0883024619 * sRgb.r() + 0.2817188376 * sRgb.g() + 0.6299787005 * sRgb.b();

        double lPrime = cubeRootOf(l);
        double mPrime = cubeRootOf(m);
        double sPrime = cubeRootOf(s);

        return new OkLABTuner(
                0.2104542553 * lPrime + 0.7936177850 * mPrime - 0.0040720468 * sPrime,
                1.9779984951 * lPrime - 2.4285922050 * mPrime + 0.4505937099 * sPrime,
                0.0259040371 * lPrime + 0.7827717662 * mPrime - 0.8086757660 * sPrime
        );
    }

    public Srgb toSrgb() {
        double lPrime = L() + 0.3963377774 * a() + 0.2158037573 * b();
        double mPrime = L() - 0.1055613458 * a() - 0.0638541728 * b();
        double sPrime = L() - 0.0894841775 * a() - 1.2914855480 * b();

        double l = lPrime * lPrime * lPrime;
        double m = mPrime * mPrime * mPrime;
        double s = sPrime * sPrime * sPrime;

        return new Srgb(
                4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s,
                -1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s,
                -0.0041960863 * l - 0.7034186147 * m + 1.7076147010 * s
        );
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

    public static void main(String[] args) {
        double[][] originalMatrix = new double[][]{
                {0.8189330101, 0.3618667424, -0.1288597137},
                {0.0329845436, 0.9293118715, 0.0361456387},
                {0.0482003018, 0.2643662691, 0.6338517070}
        };

        double originalError = getError(originalMatrix);
        var currentBest = new Result(originalError, originalMatrix);
        double maxRange = 0.00001;
        double range = maxRange;
        int attempts = 50;
        printMatrix("Initial", currentBest);
        do {
            System.out.println("Range: " + range);
            var newBest = optimise(currentBest, range);
            if (newBest != currentBest) {
                currentBest = newBest;
                attempts = 50;
                printMatrix("New optimised bestResult", currentBest);
            } else {
                attempts--;
                System.out.println("No improvement, " + attempts + " refinements left");
                range /= 3;
            }

        } while (attempts != 0 && currentBest.error != 0.0);

        printMatrix("Final bestResult", currentBest);
    }

    static void printMatrix(String description, Result result) {
        System.out.println(description);
        System.out.println("Matrix has error = " + result.error);
        System.out.println(Arrays.toString(result.matrix[0]));
        System.out.println(Arrays.toString(result.matrix[1]));
        System.out.println(Arrays.toString(result.matrix[2]));

        var err1 = result.matrix[0][0] - 0.8189330101;
        var err2 = result.matrix[0][1] - 0.3618667424;
        var err3 = result.matrix[0][2] - (-0.1288597137);

        var err4 = result.matrix[1][0] - 0.0329845436;
        var err5 = result.matrix[1][1] - 0.9293118715;
        var err6 = result.matrix[1][2] - 0.0361456387;

        var err7 = result.matrix[2][0] - 0.0482003018;
        var err8 = result.matrix[2][1] - 0.2643662691;
        var err9 = result.matrix[2][2] - 0.6338517070;

        System.out.println("RMS deviation from original matrix: " +
                Math.sqrt(
                        err1 * err1 + err2 * err2 + err3 * err3 +
                                err4 * err4 + err5 * err5 + err6 * err6 +
                                err7 * err7 + err8 * err8 + err9 * err9
                )
        );

        System.out.println();
    }

    private static Result optimise(Result currentBest, double range) {
        double step = range / 3;
        double[][] bestMatrix = currentBest.matrix;

        Result result = IntStream.rangeClosed(-3, 3).mapToDouble(m1Count -> 1 + m1Count * step)
                .parallel()
                .mapToObj(m1 -> {
                    double[][] candidate = new double[][]{
                            new double[3],
                            new double[3],
                            new double[3]
                    };
                    candidate[0][0] = bestMatrix[0][0] * m1;
                    return IntStream.rangeClosed(-3, 3).mapToDouble(m2Count -> 1 + m2Count * step)
                            .parallel()
                            .mapToObj(m2 -> {
                                double[][] l2Candidate = candidate.clone();
                                l2Candidate[0][1] = bestMatrix[0][1] * m2;
                                return doInnerScan(currentBest, range, l2Candidate, step);
                            });
                }).flatMap(Function.identity())
                .reduce(
                        currentBest,
                        (r1, r2) -> r1.error < r2.error ? r1 : r2
                );

        return result;
    }

    private static Result doInnerScan(Result currentBest, double range, double[][] candidate, double step) {
        double[][] matrix = currentBest.matrix;
        var result = currentBest;
        for (double m3 = 1 - range; m3 <= 1 + range; m3 += step) {
            candidate[0][2] = matrix[0][2] * m3;
            for (double m4 = 1 - range; m4 <= 1 + range; m4 += step) {
                candidate[1][0] = matrix[1][0] * m4;
                for (double m5 = 1 - range; m5 <= 1 + range; m5 += step) {
                    candidate[1][1] = matrix[1][1] * m5;
                    for (double m6 = 1 - range; m6 <= 1 + range; m6 += step) {
                        candidate[1][2] = matrix[1][2] * m6;
                        for (double m7 = 1 - range; m7 <= 1 + range; m7 += step) {
                            candidate[2][0] = matrix[2][0] * m7;
                            for (double m8 = 1 - range; m8 <= 1 + range; m8 += step) {
                                candidate[2][1] = matrix[2][1] * m8;
                                for (double m9 = 1 - range; m9 <= 1 + range; m9 += step) {
                                    candidate[2][2] = matrix[2][2] * m9;
                                    double error = getError(candidate);
                                    if (error < result.error) {
                                        result = new Result(error, copyOf(candidate));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static double getError(double[][] matrix) {
        double error;
        try {
            set_XYZ_TO_LMS(matrix);
            CIEXYZ whiteXYZ = WHITE.toXyz();
            CIEXYZ standardWhite = CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER;
            double errorX = whiteXYZ.X() - standardWhite.X();
            double errorY = whiteXYZ.Y() - standardWhite.Y();
            double errorZ = whiteXYZ.Z() - standardWhite.Z();

            error = errorX * errorX + errorY * errorY + errorZ * errorZ;
        } catch (RuntimeException rte) {
            error = Double.POSITIVE_INFINITY;
        }

        return error;
    }

    static double[][] copyOf(double[][] original) {
        return new double[][]{
                original[0].clone(),
                original[1].clone(),
                original[2].clone()
        };
    }

    static class Result {
        double error;
        double[][] matrix;

        Result(double error, double[][] matrix) {
            this.error = error;
            this.matrix = matrix;
        }
    }
}

/*
ASTM - range: 0.001
Matrix has error = 0.0
[0.8189509622312074, 0.3619239498645853, -0.1288651815112738]
[0.0329912500697916, 0.9292746987980047, 0.03615636453206032]
[0.04818496599039293, 0.26427789499842835, 0.6336378538375353]
RMS deviation from original matrix: 2.4278600442453871E-4

D65_WHITE_IEC_61966_2_1
Matrix has error = 0.0
[0.818967383714981, 0.3619492664312499, -0.12886520149725447]
[0.03299352333036043, 0.9292592106075834, 0.03616146647732637]
[0.04817785658521273, 0.2642390482831809, 0.6335478132503992]
RMS deviation from original matrix: 3.4665045173384217E-4

D65_WHITE_2DEGREE_STANDARD_OBSERVER
Matrix has error = 0.0
[0.8189761623397687, 0.3619321156970304, -0.12885517057340956]
[0.03299330404204958, 0.9292685035507454, 0.03615918056654339]
[0.04818259693608417, 0.2642683151174946, 0.6336096045918206]
RMS deviation from original matrix: 2.7716304086054973E-4


D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER
maxRange = 0.0002
Matrix has error = 0.0
[0.8197842415296197, 0.36088871802835165, -0.12872024855803574]
[0.0327208042008903, 0.9304891853249381, 0.03586813926096813]
[0.048664577663383925, 0.2669141563783153, 0.6401811778989841]
RMS deviation from original matrix: 0.0070712582273056235
 */