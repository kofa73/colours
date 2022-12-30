package kofa.maths;

import org.apache.commons.math3.linear.MatrixUtils;

public class SpaceConversionMatrix<I extends Vector3, O extends Vector3> {
    private final double[][] matrix;
    private final Vector3Constructor<O> resultConstructor;

    public SpaceConversionMatrix(
            Vector3Constructor<O> resultConstructor,
            double[][] matrix
    ) {
        this.resultConstructor = resultConstructor;
        this.matrix = matrix;
    }

    public SpaceConversionMatrix<O, I> invert(Vector3Constructor<I> resultConstructor) {
        return new SpaceConversionMatrix<>(
                resultConstructor,
                MatrixUtils.inverse(MatrixUtils.createRealMatrix(matrix)).getData()
        );
    }

    public O multiply(I vector) {
        double r1 = matrix[0][0] * vector.coordinate1 + matrix[0][1] * vector.coordinate2 + matrix[0][2] * vector.coordinate3;
        double r2 = matrix[1][0] * vector.coordinate1 + matrix[1][1] * vector.coordinate2 + matrix[1][2] * vector.coordinate3;
        double r3 = matrix[2][0] * vector.coordinate1 + matrix[2][1] * vector.coordinate2 + matrix[2][2] * vector.coordinate3;
        return resultConstructor.createFrom(r1, r2, r3);
    }

//    public O multiplyFloat(I vector) {
//        double r1 = (float) matrix[0][0] * (float) vector.coordinate1 + (float) matrix[0][1] * (float) vector.coordinate2 + (float) matrix[0][2] * (float) vector.coordinate3;
//        double r2 = (float) matrix[1][0] * (float) vector.coordinate1 + (float) matrix[1][1] * (float) vector.coordinate2 + (float) matrix[1][2] * (float) vector.coordinate3;
//        double r3 = (float) matrix[2][0] * (float) vector.coordinate1 + (float) matrix[2][1] * (float) vector.coordinate2 + (float) matrix[2][2] * (float) vector.coordinate3;
//        return resultConstructor.createFrom(r1, r2, r3);
//    }

    public <S extends Vector3> SpaceConversionMatrix<S, O> multiply(SpaceConversionMatrix<S, I> multiplicand) {
        double[][] otherValues = multiplicand.matrix;

        var cell00 = matrix[0][0] * otherValues[0][0] + matrix[0][1] * otherValues[1][0] + matrix[0][2] * otherValues[2][0];
        var cell01 = matrix[0][0] * otherValues[0][1] + matrix[0][1] * otherValues[1][1] + matrix[0][2] * otherValues[2][1];
        var cell02 = matrix[0][0] * otherValues[0][2] + matrix[0][1] * otherValues[1][2] + matrix[0][2] * otherValues[2][2];

        var cell10 = matrix[1][0] * otherValues[0][0] + matrix[1][1] * otherValues[1][0] + matrix[1][2] * otherValues[2][0];
        var cell11 = matrix[1][0] * otherValues[0][1] + matrix[1][1] * otherValues[1][1] + matrix[1][2] * otherValues[2][1];
        var cell12 = matrix[1][0] * otherValues[0][2] + matrix[1][1] * otherValues[1][2] + matrix[1][2] * otherValues[2][2];

        var cell20 = matrix[2][0] * otherValues[0][0] + matrix[2][1] * otherValues[1][0] + matrix[2][2] * otherValues[2][0];
        var cell21 = matrix[2][0] * otherValues[0][1] + matrix[2][1] * otherValues[1][1] + matrix[2][2] * otherValues[2][1];
        var cell22 = matrix[2][0] * otherValues[0][2] + matrix[2][1] * otherValues[1][2] + matrix[2][2] * otherValues[2][2];

        return new SpaceConversionMatrix<>(
                resultConstructor,
                new double[][]{
                        {cell00, cell01, cell02},
                        {cell10, cell11, cell12},
                        {cell20, cell21, cell22}
                }
        );
    }

    public double[][] values() {
        double[] row0 = matrix[0].clone();
        double[] row1 = matrix[1].clone();
        double[] row2 = matrix[2].clone();
        return new double[][]{row0, row1, row2};
    }
}
