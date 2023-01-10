package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;
import kofa.maths.Vector3Constructor;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;
import static kofa.colours.model.ConversionHelper.cubeOf;
import static kofa.colours.model.ConversionHelper.cubeRootOf;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;
import static org.apache.commons.math3.linear.MatrixUtils.inverse;

// A brute-force tool that tries to gradually refine the XYZ -> LMS matrix, starting from
// Björn Ottosson's version.
// Change the standardWhite in getError to target a specific white point.
// Experiment with maxRange (values too high will cause the matrix to shift far from Björn's;
// values too low will take forever to complete).
class OkLABTuner extends Vector3 {

    private final SpaceConversionMatrix<LMS, CIEXYZ> lmsToXyz;

    private static final double[][] LMS_PRIME_TO_LAB_VALUES = {
            {+0.2104542553, +0.7936177850, -0.0040720468},
            {+1.9779984951, -2.4285922050, +0.4505937099},
            {+0.0259040371, +0.7827717662, -0.8086757660}
    };

    private static SpaceConversionMatrix<LMSPrime, OkLABTuner> lmsPrimeToLab(Vector3Constructor<OkLABTuner> constructor) {
        return new SpaceConversionMatrix<>(
                constructor,
                LMS_PRIME_TO_LAB_VALUES
        );
    }

    private static final SpaceConversionMatrix<OkLABTuner, LMSPrime> LAB_TO_LMS_PRIME = new SpaceConversionMatrix<>(
            LMSPrime::new, inverse(createRealMatrix(LMS_PRIME_TO_LAB_VALUES)).getData()
    );

    private OkLABTuner(double coordinate1, double coordinate2, double coordinate3, double[][] xyzToLmsMatrix) {
        super(coordinate1, coordinate2, coordinate3);
        lmsToXyz =
                new SpaceConversionMatrix<>(CIEXYZ::new, inverse(createRealMatrix(xyzToLmsMatrix)).getData());
    }

    private CIEXYZ toXyz() {
        LMSPrime lmsPrime = LAB_TO_LMS_PRIME.multiply(this);
        LMS lms = lmsPrime.toLms();
        return lmsToXyz.multiply(lms);
    }

    private static OkLABTuner from(CIEXYZ xyz, double[][] xyzToLmsMatrix) {
        SpaceConversionMatrix<CIEXYZ, LMS> conversionMatrix = new SpaceConversionMatrix<>(
                LMS::new, xyzToLmsMatrix
        );
        LMS lms = conversionMatrix.multiply(xyz);
        LMSPrime lmsPrime = LMSPrime.from(lms);
        return lmsPrimeToLab((l, a, b) -> new OkLABTuner(l, a, b, xyzToLmsMatrix)).multiply(lmsPrime);
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

        private LMS(double l, double m, double s) {
            super(l, m, s);
            this.l = l;
            this.m = m;
            this.s = s;
        }
    }

    public static void main(String[] args) {
        double[][] originalMatrix = OkLAB.XYZ_TO_LMS_ORIGINAL.values();

        double originalError = getError(originalMatrix);
        var currentBest = new Result(originalError, originalMatrix);
        double maxRange = 0.0003;
        double range = maxRange;
        int attempts = 50;
        printMatrix("Initial", currentBest);
        double lastPrint = 0;
        do {
//            System.out.println("Range: " + range);
            var newBest = optimise(currentBest, range);
            if (newBest != currentBest) {
                currentBest = newBest;
                attempts = 50;
//                printMatrix("New optimised bestResult", currentBest);
            } else {
                attempts--;
//                System.out.println("No improvement, " + attempts + " refinements left");
                range /= 3;
            }

            if (lastPrint < System.currentTimeMillis() - 10000) {
                lastPrint = System.currentTimeMillis();
                printMatrix("current best", currentBest);
                System.out.println("Range: " + range);
            }
        } while (attempts != 0 && currentBest.error != 0.0);

        printMatrix("Final bestResult", currentBest);
    }

    static void printMatrix(String description, Result result) {
        System.out.println(description);
        System.out.println(Arrays.toString(result.matrix[0]));
        System.out.println(Arrays.toString(result.matrix[1]));
        System.out.println(Arrays.toString(result.matrix[2]));

        System.out.println();
        System.out.println("Matrix produces squared XYZ white error sum = " + result.error);

        double[][] originalMatrix = OkLAB.XYZ_TO_LMS_ORIGINAL.values();
        double totalError = 0;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                double error = result.matrix[row][column] - originalMatrix[row][column];
                totalError += error * error;
            }
        }

        System.out.println("RMS deviation from original matrix: " + sqrt(totalError / 9));
        System.out.println();
    }

    private static Result optimise(Result currentBest, double range) {
        int nSteps = 3;
        double stepSize = range / nSteps;
        double[][] bestMatrix = currentBest.matrix;

        Result result = IntStream.rangeClosed(-nSteps, nSteps)
                .parallel()
                .mapToObj(i1 ->
                        IntStream.rangeClosed(-nSteps, nSteps)
                                .parallel()
                                .mapToObj(i2 -> {
                                    double[][] candidate = new double[][]{
                                            new double[3],
                                            new double[3],
                                            new double[3]
                                    };
                                    candidate[0][0] = bestMatrix[0][0] * (1 + i1 * stepSize);
                                    candidate[0][1] = bestMatrix[0][1] * (1 + i2 * stepSize);
                                    return doInnerScan(currentBest, range, candidate, 3);
                                })
                ).flatMap(Function.identity())
                .reduce(
                        currentBest,
                        (r1, r2) -> r1.error < r2.error ? r1 : r2
                );

        return result;
    }

    private static Result doInnerScan(Result currentBest, double range, double[][] candidate, int nSteps) {
        double[][] matrix = currentBest.matrix;
        double stepSize = range / nSteps;
        var result = currentBest;
        for (int i3 = -nSteps; i3 <= nSteps; i3++) {
            double m3 = 1 + i3 * stepSize;
            candidate[0][2] = matrix[0][2] * m3;
            for (int i4 = -nSteps; i4 <= nSteps; i4++) {
                double m4 = 1 + i4 * stepSize;
                candidate[1][0] = matrix[1][0] * m4;
                for (int i5 = -nSteps; i5 <= nSteps; i5++) {
                    double m5 = 1 + i5 * stepSize;
                    candidate[1][1] = matrix[1][1] * m5;
                    for (int i6 = -nSteps; i6 <= nSteps; i6++) {
                        double m6 = 1 + i6 * stepSize;
                        candidate[1][2] = matrix[1][2] * m6;
                        for (int i7 = -nSteps; i7 <= nSteps; i7++) {
                            double m7 = 1 + i7 * stepSize;
                            candidate[2][0] = matrix[2][0] * m7;
                            for (int i8 = -nSteps; i8 <= nSteps; i8++) {
                                double m8 = 1 + i8 * stepSize;
                                candidate[2][1] = matrix[2][1] * m8;
                                for (int i9 = -nSteps; i9 <= nSteps; i9++) {
                                    double m9 = 1 + i9 * stepSize;
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

    private static double getError(double[][] xyzToLmsMatrix) {
        return getError_lab_to_white(xyzToLmsMatrix);
    }

    private static double getError_white_to_lab(double[][] xyzToLmsMatrix) {
        double error;
        try {
            CIEXYZ standardWhite = CIEXYZ.D65_WHITE_ASTM_E308_01;
            OkLABTuner lab = OkLABTuner.from(standardWhite, xyzToLmsMatrix);
            double error_L = lab.coordinate1 - 1;
            double error_a = lab.coordinate2 - 0;
            double error_b = lab.coordinate3 - 0;

            error = error_L * error_L + error_a * error_a + error_b * error_b;
        } catch (RuntimeException rte) {
            error = Double.POSITIVE_INFINITY;
        }

        return error;
    }

    private static double getError_lab_to_white(double[][] xyzToLmsMatrix) {
        double error;
        try {
            OkLABTuner labWhite = new OkLABTuner(1, 0, 0, xyzToLmsMatrix);
            CIEXYZ whiteXYZ = labWhite.toXyz();
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

    private static double[][] copyOf(double[][] original) {
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
ASTM
[0.8189509622312074, 0.3619239498645853, -0.1288651815112738]
[0.0329912500697916, 0.9292746987980047, 0.03615636453206032]
[0.04818496599039293, 0.26427789499842835, 0.6336378538375353]

Matrix produces squared XYZ white error sum = 0.0
RMS deviation from original matrix: 8.09286681415129E-5

maxRange = 0.001
Matrix produces squared XYZ white error sum = 0.0
RMS deviation from original matrix: 2.2076755719756114E-4

maxRange = 0.0005
Matrix produces squared XYZ white error sum = 0.0
RMS deviation from original matrix: 1.9119506476085148E-4

maxRange = 0.0003
Matrix produces squared XYZ white error sum = 0.0
RMS deviation from original matrix: 1.6194547852138065E-4
---

D65_WHITE_IEC_61966_2_1
[0.818967383714981, 0.3619492664312499, -0.12886520149725447]
[0.03299352333036043, 0.9292592106075834, 0.03616146647732637]
[0.04817785658521273, 0.2642390482831809, 0.6335478132503992]

Matrix produces squared XYZ white error sum = 0.0
RMS deviation from original matrix: 1.1555015057794739E-4

---

D65_WHITE_2DEGREE_STANDARD_OBSERVER
[0.8189761623397687, 0.3619321156970304, -0.12885517057340956]
[0.03299330404204958, 0.9292685035507454, 0.03615918056654339]
[0.04818259693608417, 0.2642683151174946, 0.6336096045918206]

Matrix produces squared XYZ white error sum = 0.0
RMS deviation from original matrix: 9.23876802868499E-5

---

D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER
maxRange = 0.0002
[0.8197842415296197, 0.36088871802835165, -0.12872024855803574]
[0.0327208042008903, 0.9304891853249381, 0.03586813926096813]
[0.048664577663383925, 0.2669141563783153, 0.6401811778989841]

Matrix produces squared XYZ white error sum = 0.0
RMS deviation from original matrix: 0.002357086075768541
*/