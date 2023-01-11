package kofa.colours.model;

import org.apache.commons.math3.linear.MatrixUtils;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

// A brute-force tool that tries to gradually refine a matrix, given an input
// and the desired result multiplying the input by the matrix.
class MatrixTuner {

    private final double[][] original;
    private final double[] input;
    private final double[] targetOutput;
    private final double maxDeviation;
    private final double maxCoordinateDifferenceRatio;

    private MatrixTuner(double[][] original, double[] input, double[] targetOutput, double maxDeviation, double maxCoordinateDifferenceRatio) {
        this.original = original;
        this.input = input;
        this.targetOutput = targetOutput;
        this.maxDeviation = maxDeviation;
        this.maxCoordinateDifferenceRatio = maxCoordinateDifferenceRatio;
    }

    public static void main(String[] args) {
        optimiseLmsPrimeToLab();
        optimiseLmsToXYZ(CIEXYZ.D65_WHITE_ASTM_E308_01);
        optimiseLmsToXYZ(CIEXYZ.D65_WHITE_IEC_61966_2_1);
        optimiseLmsToXYZ(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER);
        optimiseLmsToXYZ(CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER);
    }

    private static void optimiseLmsToXYZ(CIEXYZ whiteReference) {

    }

    private static void optimiseLmsPrimeToLab() {
        double[][] lmsPrimeToOkLab = new double[][]{
                {+0.2104542553, +0.7936177850, -0.0040720468},
                {+1.9779984951, -2.4285922050, +0.4505937099},
                {+0.0259040371, +0.7827717662, -0.8086757660}
        };

        double[] lmsWhite = {1, 1, 1};
        double[] labWhite = {1, 0, 0};
        double maxDeviation = 1E-8;
        double maxCoordinateDifferencePercent = 0.000003;
        new MatrixTuner(
                lmsPrimeToOkLab,
                lmsWhite,
                labWhite,
                maxDeviation,
                maxCoordinateDifferencePercent / 100.0
        ).optimise("L'M'S' -> LAB");
    }

    private void optimise(String name) {
        double originalError = getError(original);
        var currentBest = new Result(originalError, original, totalDeviationFromOriginal(original));
        double maxRange = 0.05;
        double range = maxRange;
        printMatrix("Initial", currentBest);
        double lastPrint = 0;
        do {
            var newBest = optimiseWithRange(currentBest, range);
            if (newBest != currentBest) {
                currentBest = newBest;
            }

            range *= 0.5;

            if (lastPrint < System.currentTimeMillis() - 10000) {
                lastPrint = System.currentTimeMillis();
                printMatrix("current " + name, currentBest);
                System.out.println("Range: " + range);
            }
        } while (range > 1E-20 && currentBest.error != 0.0);

        printMatrix("Final " + name, currentBest);
    }

    private Result optimiseWithRange(Result currentBest, double range) {
        double[][] bestMatrix = currentBest.matrix;

        return IntStream.rangeClosed(1, 100)
                .parallel()
                .mapToObj(ignored -> {
                            double[][] candidate = new double[][]{
                                    new double[3],
                                    new double[3],
                                    new double[3]
                            };

                            Result result = currentBest;
                            ThreadLocalRandom random = ThreadLocalRandom.current();

                            for (int i = 0; i < 50_000; i++) {
                                for (int row = 0; row < 3; row++) {
                                    for (int column = 0; column < 3; column++) {
                                        candidate[row][column] = bestMatrix[row][column] + random.nextDouble(-range, range);
                                    }
                                }
                                double totalDeviationFromOriginal = totalDeviationFromOriginal(candidate);
                                if (totalDeviationFromOriginal <= maxDeviation) {
                                    double error = getError(candidate);
                                    if (error < result.error) {
                                        result = new Result(error, copyOf(candidate), totalDeviationFromOriginal);
                                    }
                                }
                            }
                            return result;
                        }
                ).reduce(((result1, result2) -> result1.error < result2.error ?
                        result1 :
                        result2.error < result1.error ?
                                result2 :
                                (result1.totalDeviationFromOriginal < result2.totalDeviationFromOriginal ?
                                        result1 : result2)
                )).orElse(currentBest);
    }

    private double getError(double[][] candidate) {
        if (isOverAllowedDifference(candidate)) {
            return Double.POSITIVE_INFINITY;
        }
        double[] currentOutput = MatrixUtils.createRealMatrix(candidate).operate(input);
        double error0 = currentOutput[0] - targetOutput[0];
        double error1 = currentOutput[1] - targetOutput[1];
        double error2 = currentOutput[2] - targetOutput[2];

        return error0 * error0 + error1 * error1 + error2 * error2;
    }

    private boolean isOverAllowedDifference(double[][] candidate) {
        boolean isOverAllowedDifference = false;
        for (int row = 0; !isOverAllowedDifference && row < 3; row++) {
            for (int column = 0; !isOverAllowedDifference && column < 3; column++) {
                double difference = abs(candidate[row][column] - original[row][column]);
                if (difference != 0) {
                    double magnitude = Double.min(abs(candidate[row][column]), abs(original[row][column]));
                    if (magnitude != 0) {
                        double ratio = difference / magnitude;
                        isOverAllowedDifference = (ratio > maxCoordinateDifferenceRatio);
                    }
                }
            }
        }
        return isOverAllowedDifference;
    }

    private static double[][] copyOf(double[][] original) {
        return new double[][]{
                original[0].clone(),
                original[1].clone(),
                original[2].clone()
        };
    }

    double totalDeviationFromOriginal(double[][] candidate) {
        double totalDeviation = 0;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                double deviation = candidate[row][column] - original[row][column];
                totalDeviation += deviation * deviation;
            }
        }

        totalDeviation = sqrt(totalDeviation / 9);
        return totalDeviation;
    }

    void printMatrix(String description, Result result) {
        System.out.println(description);
        System.out.println(Arrays.toString(result.matrix[0]));
        System.out.println(Arrays.toString(result.matrix[1]));
        System.out.println(Arrays.toString(result.matrix[2]));

        System.out.println();
        System.out.println("Matrix produces squared error sum = " + result.error);

        System.out.println("RMS deviation from original matrix: " + result.totalDeviationFromOriginal);
        System.out.println();
    }

    static class Result {
        double error;
        double[][] matrix;
        private final double totalDeviationFromOriginal;

        Result(double error, double[][] matrix, double totalDeviationFromOriginal) {
            this.error = error;
            this.matrix = matrix;
            this.totalDeviationFromOriginal = totalDeviationFromOriginal;
        }
    }
}