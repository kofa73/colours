package kofa.colours.model;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;
import static org.apache.commons.math3.linear.MatrixUtils.inverse;

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

//    public static void main(String[] args) {
////        optimiseLmsPrimeToLab();
//        optimiseLmsToXYZ(CIEXYZ.D65_WHITE_ASTM_E308_01, OkLAB.LMS_TO_XYZ_D65_ASTM_E308_01.values(), "D65_WHITE_ASTM_E308_01");
////        optimiseLmsToXYZ(CIEXYZ.D65_WHITE_IEC_61966_2_1, OkLAB.LMS_TO_XYZ_D65_IEC_61966_2_1.values(), "D65_WHITE_IEC_61966_2_1");
////        optimiseLmsToXYZ(CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER, OkLAB.LMS_TO_XYZ_D65_2DEGREE_STANDARD_OBSERVER.values(), "D65_WHITE_2DEGREE_STANDARD_OBSERVER");
////        optimiseLmsToXYZ(CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER, OkLAB.LMS_TO_XYZ_D65_10DEGREE_SUPPLEMENTARY_OBSERVER.values(), "D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER");
//    }

    private static void optimiseLmsToXYZ(CIEXYZ whiteReference, double[][] matrix, String name) {
//        double[][] originalXyzToLms = new double[][]{
//                {+0.8189330101, +0.3618667424, -0.1288597137},
//                {+0.0329845436, +0.9293118715, +0.0361456387},
//                {+0.0482003018, +0.2643662691, +0.6338517070}
//        };
//        double[] xyzWhite = whiteReference.coordinates().toArray();
//        double[] lmsWhite = {1, 1, 1};
//        double maxDeviation = 1E-3;
//        double maxCoordinateDifferencePercent = 1;
//        new MatrixTuner(
//                originalXyzToLms,
//                xyzWhite,
//                lmsWhite,
//                maxDeviation,
//                maxCoordinateDifferencePercent / 100.0
//        ).printMatrix("XYZ -> LMS " + name, new Result(0, originalXyzToLms, 0));
        double[] lmsWhite = {1, 1, 1};
        double[] targetOutput = whiteReference.coordinates().toArray();
        double[] scalingFactors = scalingFactors(lmsWhite, targetOutput, matrix);

        System.out.println("Optimising " + name);
        System.out.println(format(matrix));

        double[][] scaled = copyOf(matrix);
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                scaled[row][column] *= scalingFactors[row];
            }
        }

        System.out.println("Scaled " + name);
        System.out.println(format(scaled));

        double[] originalOutput = createRealMatrix(matrix).operate(lmsWhite);
        double[] scaledOutput = createRealMatrix(scaled).operate(lmsWhite);

        System.out.println(name);
        double[][] tuned = new double[3][];
        for (int i = 0; i < 3; i++) {
            tuned[i] = fineTuneScaling(1E-5, matrix[i], lmsWhite, targetOutput[i], scalingFactors[i]);
        }

        System.out.println("tuned:");
        System.out.println(format(tuned));

        for (int i = 0; i < 3; i++) {
            if (tuned[i] == null) {
                System.out.println("Tuning failed for row " + i);
                tuned[i] = scaled[i];
            }
        }
        System.out.println("final:");
        System.out.println(format(tuned));

        double[] tunedOutput = createRealMatrix(tuned).operate(lmsWhite);

        System.out.println("target   output = " + format(targetOutput));
        System.out.println("original output = " + format(originalOutput));
        System.out.println("scaled   output = " + format(scaledOutput));
        System.out.println("tuned    output = " + format(tunedOutput));

        System.out.println("=============================");
    }

    private static double[] fineTuneScaling(double range, double[] originalMatrixRow, double[] input, double targetOutput, double scalingFactorFromDivision) {
        int steps = 1000_000;
        for (int step = -steps; step <= steps; step++) {
            double tuning = (range / steps) * step;
            double[] scaledMatrixRow = new double[3];
            double output = 0;
            double tunedScalingFactor = scalingFactorFromDivision * (1 + tuning);
            for (int i = 0; i < 3; i++) {
                scaledMatrixRow[i] = originalMatrixRow[i] * tunedScalingFactor;
                output += scaledMatrixRow[i] * input[i];
            }
            if (output == targetOutput) {
//                System.out.println("step = " + step + ", tuning = " + tuning + ", tunedScalingFactor = " + tunedScalingFactor);
                return scaledMatrixRow;
            }
            if (output > targetOutput) {
//                System.out.println("skipped over");
                if (step != -steps) {
                    return fineTuneScaling(range / 2, scaledMatrixRow.clone(), input, targetOutput, 1);
                }
                return null;
            }
        }
        System.out.println("range not broad enough");
        return null;
    }

    private static void optimiseLmsPrimeToLab(double[][] lmsPrimeToOkLab) {
        double[] lmsWhite = {1, 1, 1};
        double[] labWhite = {1, 0, 0};

        for (double scale = 0.000000001; scale < 0.00000001; scale += 1E-16) {
            double[][] scaled = copyOf(lmsPrimeToOkLab);
            scaled[0][0] += scaled[0][0] * scale;
            scaled[0][1] += scaled[0][1] * scale;
            scaled[0][2] += scaled[0][2] * scale;
            double[] resultOutput = createRealMatrix(scaled).operate(lmsWhite);
            if (resultOutput[0] == 1) {
                new MatrixTuner(lmsPrimeToOkLab, lmsWhite, labWhite, 0, 0).printMatrix("Fitted", new Result(0, scaled, 0));
                System.exit(0);
            }
            if (resultOutput[0] >= 1) {
                System.out.println("%.20f".formatted(scale));
                System.exit(0);
            }
        }
//        double maxDeviation = 1E-8;
//        double maxCoordinateDifferencePercent = 0.000003;
//        new MatrixTuner(
//                lmsPrimeToOkLab,
//                lmsWhite,
//                labWhite,
//                maxDeviation,
//                maxCoordinateDifferencePercent / 100.0
//        ).printMatrix("L'M'S' -> LAB", new Result(0, lmsPrimeToOkLab, 0));
    }

    private void optimise(String name) {
        double originalError = getError(original);
        var currentBest = new Result(originalError, original, totalDeviationFromOriginal(original));
        double range = 0.05;
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
        double[] currentOutput = createRealMatrix(candidate).operate(input);
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

        System.out.println("Scaling factors compared to original matrix:");
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                System.out.print("%.20f, ".formatted(result.matrix[row][column] / original[row][column]));
            }
            System.out.println();
        }

        double[] originalOutput = createRealMatrix(original).operate(input);
        System.out.println("Output of original matrix:");
        System.out.println(format(originalOutput));

        double[] resultOutput = createRealMatrix(result.matrix).operate(input);
        System.out.println("Output of result matrix:");
        System.out.println(format(resultOutput));

        System.out.println("Target output:");
        System.out.println(format(targetOutput));

        double[][] scaled = copyOf(original);
        double[] scalingFactors = new double[3];
        for (int row = 0; row < 3; row++) {
            scalingFactors[row] = targetOutput[row] / originalOutput[row];
            for (int column = 0; column < 3; column++) {
                scaled[row][column] *= scalingFactors[row];
            }
        }
        System.out.println("Scaled matrix:");
        System.out.println(format(scaled));

        System.out.println("Scaling factors from original to target:");
        for (int i = 0; i < 3; i++) {
            System.out.print("%.20f, ".formatted(1 / scalingFactors[i]));
        }
        System.out.println();

        double[] scaledOutput = createRealMatrix(scaled).operate(input);
        System.out.println("Output of scaled matrix:");
        System.out.println(format(scaledOutput));


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

    private static double[] scalingFactors(double[] input, double[] targetOutput, double[][] matrix) {
        double[] output = createRealMatrix(matrix).operate(input);

        double[] scalingFactors = new double[3];
        for (int row = 0; row < 3; row++) {
            scalingFactors[row] = targetOutput[row] / output[row];
        }
        return scalingFactors;
    }

    private static String format(double[] vector) {
        return "{%.20f, %.20f, %.20f}".formatted(vector[0], vector[1], vector[2]);
    }

    private static String format(double[][] matrix) {
        return "{%n\t%s%n\t%s%n\t%s%n}".formatted(format(matrix[0]), format(matrix[1]), format(matrix[2]));
    }

    private static double[][] tune(String name, double[][] originalMatrix, double[] input, double[] targetOutput) {
        System.out.println("=====%n%s%n=====".formatted(name));
        System.out.println("Original matrix:");
        System.out.println(format(originalMatrix));
        System.out.println("Target output: " + format(targetOutput));

        double[] originalOutput = multiply(originalMatrix, input);
        System.out.println("Initial output: " + format(originalOutput));

        double[][] scaledMatrix = copyOf(originalMatrix);
        for (int row = 0; row < 3; row++) {
            if (targetOutput[row] != 0 && originalOutput[row] != 0) {
                double scalingFactor = targetOutput[row] / originalOutput[row];
                scaledMatrix[row][0] *= scalingFactor;
                scaledMatrix[row][1] *= scalingFactor;
                scaledMatrix[row][2] *= scalingFactor;
            }
        }

        double[] scaledOutput = multiply(scaledMatrix, input);

        System.out.println("Scaled matrix:");
        System.out.println(format(scaledMatrix));
        System.out.println("Output of scaled matrix: " + format(scaledOutput));
        System.out.println();

        return scaledMatrix;
    }

    private static double[] multiply(double[][] matrix, double[] vector) {
        return createRealMatrix(matrix).operate(vector);
    }

    public static void main(String[] args) {

//        double[][] scaledLmsPrimeToLab = tune("OkLAB.LMS_PRIME_TO_LAB",
//                OkLAB.LMS_PRIME_TO_LAB_ORIGINAL.values(),
//                new double[]{1, 1, 1},
//                new double[]{1, 0, 0}
//        );
//
//        optimiseLmsPrimeToLab(scaledLmsPrimeToLab);
//
        tune("OkLAB.LAB_TO_LMS_PRIME",
                OkLAB.LAB_TO_LMS_PRIME.values(),
                new double[]{1, 0, 0},
                new double[]{1, 1, 1}
        );
//
//        tune("OkLAB.LMS_TO_XYZ_D65_ASTM_E308_01",
//                OkLAB.LMS_TO_XYZ_D65_ASTM_E308_01.values(),
//                new double[]{1, 1, 1},
//                CIEXYZ.D65_WHITE_ASTM_E308_01.coordinates().toArray()
//        );
//
//        tune("OkLAB.XYZ_TO_LMS_D65_ASTM_E308_01",
//                OkLAB.XYZ_TO_LMS_D65_ASTM_E308_01.values(),
//                CIEXYZ.D65_WHITE_ASTM_E308_01.coordinates().toArray(),
//                new double[]{1, 1, 1}
//        );
//
//        tune("OkLAB.LMS_TO_XYZ_D65_IEC_61966_2_1",
//                OkLAB.LMS_TO_XYZ_D65_IEC_61966_2_1.values(),
//                new double[]{1, 1, 1},
//                CIEXYZ.D65_WHITE_IEC_61966_2_1.coordinates().toArray()
//        );
//
//        tune("OkLAB.XYZ_TO_LMS_D65_IEC_61966_2_1",
//                OkLAB.XYZ_TO_LMS_D65_IEC_61966_2_1.values(),
//                CIEXYZ.D65_WHITE_IEC_61966_2_1.coordinates().toArray(),
//                new double[]{1, 1, 1}
//        );
//
//        tune("OkLAB.LMS_TO_XYZ_D65_2DEGREE_STANDARD_OBSERVER",
//                OkLAB.LMS_TO_XYZ_D65_2DEGREE_STANDARD_OBSERVER.values(),
//                new double[]{1, 1, 1},
//                CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER.coordinates().toArray()
//        );
//
//        tune("OkLAB.XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER",
//                OkLAB.XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER.values(),
//                CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER.coordinates().toArray(),
//                new double[]{1, 1, 1}
//        );
//
//        tune("OkLAB.LMS_TO_XYZ_D65_10DEGREE_SUPPLEMENTARY_OBSERVER",
//                OkLAB.LMS_TO_XYZ_D65_10DEGREE_SUPPLEMENTARY_OBSERVER.values(),
//                new double[]{1, 1, 1},
//                CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER.coordinates().toArray()
//        );
//
//        tune("OkLAB.XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER",
//                OkLAB.XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER.values(),
//                CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER.coordinates().toArray(),
//                new double[]{1, 1, 1}
//        );
//
//
//        tune("OkLAB.LAB_TO_LMS_PRIME with inversion",
//                inverse(createRealMatrix(OkLAB.LMS_PRIME_TO_LAB.values())).getData(),
//                new double[]{1, 0, 0},
//                new double[]{1, 1, 1}
//        );
        System.out.println(format(createRealMatrix(OkLAB.LAB_TO_LMS_PRIME.values()).multiply(createRealMatrix(OkLAB.LMS_PRIME_TO_LAB.values())).getData()));


        System.out.println(format(
                inverse(createRealMatrix(OkLAB.LMS_PRIME_TO_LAB.values()))
                        .multiply(createRealMatrix(OkLAB.LMS_PRIME_TO_LAB.values())).getData())
        );


    }

}