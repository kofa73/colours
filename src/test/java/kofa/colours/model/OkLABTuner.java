package kofa.colours.model;

import kofa.maths.SpaceConversionMatrix;
import kofa.maths.Vector3;
import kofa.maths.Vector3Constructor;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import static java.lang.Math.abs;
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

    private static final double MAX_DEVIATION = 0.00227;
    private final SpaceConversionMatrix<LMS, CIEXYZ> lmsToXyz;

    private static final double[][] LMS_PRIME_TO_LAB_VALUES = {
            {0.21045425666795267, 0.7936177901585156, -0.004072046826468304},
            {1.977998495812036, -2.428592208389442, 0.45059371257740577},
            {0.025904036510579237, 0.7827717514307554, -0.8086757879413347}
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
        double[][] originalMatrix = OkLAB.XYZ_TO_LMS_D65_IEC_61966_2_1.values();

        double originalError = getError(originalMatrix);
        var currentBest = new Result(originalError, originalMatrix, totalDeviationFromOriginal(originalMatrix));
        double range = 0.05;
        printMatrix("Initial", currentBest);
        double lastPrint = 0;
        do {
            var newBest = optimise(currentBest, range);
            if (newBest != currentBest) {
                currentBest = newBest;
            }

            range *= 0.5;

            if (lastPrint < System.currentTimeMillis() - 10000) {
                lastPrint = System.currentTimeMillis();
                printMatrix("current best", currentBest);
                System.out.println("Range: " + range);
            }
        } while (range > 1E-20 && currentBest.error != 0.0);

        printMatrix("Final bestResult", currentBest);
    }

    static void printMatrix(String description, Result result) {
        System.out.println(description);
        System.out.println(Arrays.toString(result.matrix[0]));
        System.out.println(Arrays.toString(result.matrix[1]));
        System.out.println(Arrays.toString(result.matrix[2]));

        System.out.println();
        System.out.println("Matrix produces squared XYZ white error sum = " + result.error);

        System.out.println("RMS deviation from original matrix: " + result.totalDeviationFromOriginal);
        System.out.println();
    }

    static double totalDeviationFromOriginal(double[][] matrix) {
        double[][] originalMatrix = OkLAB.XYZ_TO_LMS_ORIGINAL.values();
        double totalDeviation = 0;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                double deviation = matrix[row][column] - originalMatrix[row][column];
                totalDeviation += deviation * deviation;
            }
        }

        totalDeviation = sqrt(totalDeviation / 9);
        return totalDeviation;
    }

    private static Result optimise(Result currentBest, double range) {
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
                                if (totalDeviationFromOriginal <= MAX_DEVIATION) {
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

    private static double getError(double[][] xyzToLmsMatrix) {
        CIEXYZ standardWhite = CIEXYZ.D65_WHITE_IEC_61966_2_1;
        double[][] original = OkLAB.XYZ_TO_LMS_ORIGINAL.values();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                double difference = abs(xyzToLmsMatrix[row][column] - original[row][column]);
                if (difference != 0) {
                    double magnitude = Double.min(abs(xyzToLmsMatrix[row][column]), abs(original[row][column]));
                    if (magnitude != 0) {
                        double ratio = difference / magnitude;
                        if (ratio > 0.01) {
                            return Double.POSITIVE_INFINITY;
                        }
                    }
                }
            }
        }
        double errorLabToWhite = getError_lab_to_white(xyzToLmsMatrix, standardWhite);
        double errorWhiteToLab = getError_white_to_lab(xyzToLmsMatrix, standardWhite);
        return errorLabToWhite + errorWhiteToLab;
    }

    static double getError_white_to_lab(double[][] xyzToLmsMatrix, CIEXYZ standardWhite) {
        double error;
        try {
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

    static double getError_lab_to_white(double[][] xyzToLmsMatrix, CIEXYZ standardWhite) {
        double error;
        try {
            OkLABTuner labWhite = new OkLABTuner(1, 0, 0, xyzToLmsMatrix);
            CIEXYZ whiteXYZ = labWhite.toXyz();
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
        private final double totalDeviationFromOriginal;

        Result(double error, double[][] matrix, double totalDeviationFromOriginal) {
            this.error = error;
            this.matrix = matrix;
            this.totalDeviationFromOriginal = totalDeviationFromOriginal;
        }
    }
}

/*
ASTM - optimised for white Lab -> XYZ and XYZ -> Lab, maxRange = 0.00001;
[0.8189185058849386, 0.3619250232288143, -0.12883783525960107]
[0.032989744106165446, 0.929280894117544, 0.03615198924272081]
[0.04818496599041629, 0.2642778949982382, 0.6336378538376899]

Matrix produces squared XYZ white error sum = 6.162975822039155E-32
RMS deviation from original matrix: 8.089156682114042E-5

---

D65_WHITE_IEC_61966_2_1 - optimised for white Lab -> XYZ and XYZ -> Lab, maxRange = 0.00001;
[0.819000675034942, 0.3619184812039237, -0.12886598814434513]
[0.03297134500099032, 0.9293121018200567, 0.03613225619255389]
[0.04818266855902102, 0.2642341625206832, 0.6335480999143406]

Matrix produces squared XYZ white error sum = 6.162975822039155E-32
RMS deviation from original matrix: 1.1430393471478513E-4
---

D65_WHITE_2DEGREE_STANDARD_OBSERVER - optimised for white Lab -> XYZ and XYZ -> Lab, maxRange = 0.00001;
[0.8189625335336425, 0.36193051420879846, -0.12884180415807572]
[0.032984437518689624, 0.9293038863085773, 0.03613442553692302]
[0.048183751157980376, 0.26426142935535324, 0.6336149207409002]

Matrix produces squared XYZ white error sum = 6.162975822039155E-32
RMS deviation from original matrix: 8.992698661337344E-5

---

D65_WHITE_10DEGREE_SUPPLEMENTARY_OBSERVER
MAX_DEVIATION = 0.00227
double maxRange = 0.05;
[0.8188705673458518, 0.3621356674814235, -0.1290750293357169]
[0.033080872867520816, 0.9297527092351892, 0.036236338024257374]
[0.048681834435854915, 0.2670038020756251, 0.6400823878597188]

Matrix produces squared XYZ white error sum = 6.162975822039155E-32
RMS deviation from original matrix: 0.0022692206708345686
*/