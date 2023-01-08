package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.lang.Math.sqrt;
import static kofa.colours.model.ConversionHelper.cubeOf;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;
import static org.apache.commons.math3.linear.MatrixUtils.inverse;

class OkLABTuner extends Vector3 {

    private final SpaceConversionMatrix<LMS, CIEXYZ> lmsToXyz;

    private static final SpaceConversionMatrix<OkLABTuner, LMSPrime> LAB_TO_LMS_PRIME =
            new SpaceConversionMatrix<>(LMSPrime::new,
                    inverse(createRealMatrix(new double[][]{
                            new double[]{+0.2104542553, +0.7936177850, -0.0040720468},
                            new double[]{+1.9779984951, -2.4285922050, +0.4505937099},
                            new double[]{+0.0259040371, +0.7827717662, -0.8086757660}
                    })).getData());

    private OkLABTuner(double coordinate1, double coordinate2, double coordinate3, double[][] matrix) {
        super(coordinate1, coordinate2, coordinate3);
        lmsToXyz =
                new SpaceConversionMatrix<>(CIEXYZ::new, inverse(createRealMatrix(matrix)).getData());
    }

    private CIEXYZ toXyz() {
        LMSPrime lmsPrime = LAB_TO_LMS_PRIME.multiply(this);
        LMS lms = lmsPrime.toLms();
        return lmsToXyz.multiply(lms);
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
    }

    private static class LMS extends Vector3 {
        private LMS(double l, double m, double s) {
            super(l, m, s);
        }
    }

    public static void main(String[] args) {
        double[][] originalMatrix = OkLAB.XYZ_TO_LMS_ORIGINAL.values();

        double originalError = getError(originalMatrix);
        var currentBest = new Result(originalError, originalMatrix);
        double maxRange = 0.0001;
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
            OkLABTuner labWhite = new OkLABTuner(1, 0, 0, matrix);
            CIEXYZ whiteXYZ = labWhite.toXyz();
            CIEXYZ standardWhite = CIEXYZ.D65_WHITE_ASTM_E308_01;
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
maxRange = 0.001
[0.8189509622312074, 0.3619239498645853, -0.1288651815112738]
[0.0329912500697916, 0.9292746987980047, 0.03615636453206032]
[0.04818496599039293, 0.26427789499842835, 0.6336378538375353]

Matrix produces squared XYZ white error sum = 0.0
RMS deviation from original matrix: 8.09286681415129E-5

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