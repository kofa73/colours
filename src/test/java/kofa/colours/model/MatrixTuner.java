package kofa.colours.model;

import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static org.apache.commons.math3.linear.MatrixUtils.createRealMatrix;

// A brute-force tool that tries to gradually refine a matrix, given an input
// and the desired result multiplying the input by the matrix.
class MatrixTuner {

    private static class VectorTuner {
        private final String outputComponentName;
        private final double[] originalRow;
        private final double[] inputColumn;
        private final double targetOutputColumn;
        private final double originalOutputColumn;
        private final double originalError;

        private VectorTuner(String outputComponentName, double[] originalRow, double[] inputColumn, double targetOutputColumn) {
            this.outputComponentName = outputComponentName;
            this.originalRow = originalRow;
            this.inputColumn = inputColumn;
            this.targetOutputColumn = targetOutputColumn;
            this.originalOutputColumn = dotProduct(originalRow, inputColumn);
            this.originalError = getError(originalOutputColumn);
        }

        private Result optimise() {
            if (originalError == 0) {
                return new Result(originalRow, originalRow, 0, 0);
            }
            return forcedOptimise();
        }

        private Result forcedOptimise() {
            Result result;
            if (targetOutputColumn != 0 && originalOutputColumn != 0) {
                result = scaleOriginalVector();
            } else {
                result = search();
            }
            return result;
        }

        private Result scaleOriginalVector() {
            double[] scaled = new double[3];
            double scalingFactor = targetOutputColumn / originalOutputColumn;
            scaled[0] = originalRow[0] * scalingFactor;
            scaled[1] = originalRow[1] * scalingFactor;
            scaled[2] = originalRow[2] * scalingFactor;

            double scaledOutput = dotProduct(scaled, inputColumn);

            double error = getError(scaledOutput);
            double totalDeviationFromOriginal = totalDeviationFromOriginal(originalRow, scaled);

            return new Result(originalRow, scaled, error, totalDeviationFromOriginal);
        }

        private Result search() {
            double originalError = getError(originalOutputColumn);
            var currentBest = new Result(originalRow, originalRow, originalError, 0);

            double range = 0.00000002;
            System.out.println("For " + outputComponentName + " " + format(currentBest.tuned) + " has error " + currentBest.error + ", deviation: " + currentBest.totalDeviationFromOriginal);
            int remainingRounds = 10;
            while (remainingRounds > 0) {
                Result newBest = optimiseWithRange(currentBest, range);
                //noinspection ObjectEquality
                if (newBest != currentBest) {
                    currentBest = newBest;
                    System.out.println("For " + outputComponentName + " " + format(currentBest.tuned) + " has error " + currentBest.error + ", deviation: " + currentBest.totalDeviationFromOriginal);
                    remainingRounds = 10;
                } else {
                    remainingRounds--;
                }

                range /= 2;
                System.out.println("Range: " + range + ", remaining rounds: " + remainingRounds);
            }

            return currentBest;
        }

        private Result optimiseWithRange(Result currentBest, double range) {
            return IntStream.rangeClosed(1, 100)
                    .parallel()
                    .mapToObj(ignored -> {
                                double[] candidateRow = new double[3];

                                Result result = currentBest;
                                ThreadLocalRandom random = ThreadLocalRandom.current();

                                for (int i = 0; i < 50_000_000; i++) {
                                    for (int column = 0; column < 3; column++) {
                                        candidateRow[column] = originalRow[column] + random.nextDouble(-range, range);
                                    }
                                    double candidateOutput = dotProduct(candidateRow, inputColumn);
                                    double error = getError(candidateOutput);
                                    double totalDeviationFromOriginal = totalDeviationFromOriginal(originalRow, candidateRow);
                                    if (error < result.error || error == result.error && totalDeviationFromOriginal < result.totalDeviationFromOriginal) {
                                        result = new Result(originalRow, candidateRow.clone(), error, totalDeviationFromOriginal);
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

        private double getError(double candidateOutput) {
            return abs(candidateOutput - targetOutputColumn);
        }

    }

    record Result(double[] original, double[] tuned, double error, double totalDeviationFromOriginal) {
    }

    static double totalDeviationFromOriginal(double[] originalVector, double[] candidate) {
        double totalDeviation = 0;
        for (int column = 0; column < 3; column++) {
            double deviation = candidate[column] - originalVector[column];
            totalDeviation += deviation * deviation;
        }

        return sqrt(totalDeviation / 3);
    }

    private static String format(double[] vector) {
        return "{%+.20f, %+.20f, %+.20f}".formatted(vector[0], vector[1], vector[2]);
    }

    private static double dotProduct(double[] rowVector, double[] columnVector) {
        return rowVector[0] * columnVector[0] + rowVector[1] * columnVector[1] + rowVector[2] * columnVector[2];
    }

    public static void main(String[] args) {
        new MatrixTuner(
                "LMS_PRIME_TO_LAB",
                OkLAB.LMS_PRIME_TO_LAB_ORIGINAL.values(),
                new String[]{"L", "a", "b"},
                new double[]{1, 1, 1},
                new double[]{1, 0, 0}
        ).optimise();

        new MatrixTuner(
                "XYZ_TO_LMS_D65_ASTM_E308_01",
                OkLAB.XYZ_TO_LMS_ORIGINAL.values(),
                new String[]{"L", "M", "S"},
                CIEXYZ.D65_WHITE_ASTM_E308_01.coordinates().toArray(),
                new double[]{1, 1, 1}
        ).optimise();

        new MatrixTuner(
                "XYZ_TO_LMS_D65_IEC_61966_2_1",
                OkLAB.XYZ_TO_LMS_ORIGINAL.values(),
                new String[]{"L", "M", "S"},
                CIEXYZ.D65_WHITE_IEC_61966_2_1.coordinates().toArray(),
                new double[]{1, 1, 1}
        ).optimise();

        new MatrixTuner(
                "XYZ_TO_LMS_D65_2DEGREE_STANDARD_OBSERVER",
                OkLAB.XYZ_TO_LMS_ORIGINAL.values(),
                new String[]{"L", "M", "S"},
                CIEXYZ.D65_WHITE_2DEGREE_STANDARD_OBSERVER.coordinates().toArray(),
                new double[]{1, 1, 1}
        ).optimise();

        new MatrixTuner(
                "XYZ_TO_LMS_D65_10DEGREE_SUPPLEMENTARY_OBSERVER",
                OkLAB.XYZ_TO_LMS_ORIGINAL.values(),
                new String[]{"L", "M", "S"},
                CIEXYZ.D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER.coordinates().toArray(),
                new double[]{1, 1, 1}
        ).optimise();
    }

    private final String name;
    private final double[][] original;
    private final String[] outputComponentNames;
    private final double[] input;
    private final double[] targetOutput;

    private MatrixTuner(String name, double[][] original, String[] outputComponentNames, double[] input, double[] targetOutput) {
        this.name = name;
        this.original = original;
        this.outputComponentNames = outputComponentNames;
        this.input = input;
        this.targetOutput = targetOutput;
    }

    private void optimise() {
        Result[] results = new Result[3];
        for (int row = 0; row < 3; row++) {
            results[row] = new VectorTuner(name + "." + outputComponentNames[row], original[row], input, targetOutput[row]).optimise();
        }

        printMatrix(results);
    }

    void printMatrix(Result[] result) {
        System.out.println(name);
        System.out.println("Original");
        System.out.println(format(result[0].original) + ",");
        System.out.println(format(result[1].original) + ",");
        System.out.println(format(result[2].original));

        System.out.println("Tuned");
        System.out.println(format(result[0].tuned) + ",");
        System.out.println(format(result[1].tuned) + ",");
        System.out.println(format(result[2].tuned));

        System.out.println("Deviation: " + format(new double[]{
                result[0].totalDeviationFromOriginal,
                result[1].totalDeviationFromOriginal,
                result[2].totalDeviationFromOriginal}
        ));

        double[] originalOutput = createRealMatrix(original).operate(input);
        System.out.println("Output of original matrix:");
        System.out.println(format(originalOutput));

        double[] resultOutput = createRealMatrix(new double[][]{
                result[0].tuned,
                result[1].tuned,
                result[2].tuned
        }).operate(input);
        System.out.println("Output of result matrix:");
        System.out.println(format(resultOutput));

        System.out.println("Target output:");
        System.out.println(format(targetOutput));
    }
}
