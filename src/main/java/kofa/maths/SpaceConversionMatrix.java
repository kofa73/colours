package kofa.maths;

import org.apache.commons.math3.linear.MatrixUtils;

public class SpaceConversionMatrix<I extends Vector3D, O extends Vector3D> {
    private final double[][] matrix;
    private final Vector3D.ConstructorFromArray<O> resultConstructor;

    public SpaceConversionMatrix(
            Vector3D.ConstructorFromArray<O> resultConstructor,
            double[][] matrix
    ) {
        this.resultConstructor = resultConstructor;
        this.matrix = matrix;
    }

    public SpaceConversionMatrix<O, I> invert(Vector3D.ConstructorFromArray<I> resultConstructor) {
        return new SpaceConversionMatrix<>(
                resultConstructor,
                MatrixUtils.inverse(MatrixUtils.createRealMatrix(matrix)).getData()
        );
    }

    public O multiply(I vector) {
        var doubles = vector.coordinates();
        var result = new double[3];
        result[0] = matrix[0][0] * doubles[0] + matrix[0][1] * doubles[1] + matrix[0][2] * doubles[2];
        result[1] = matrix[1][0] * doubles[0] + matrix[1][1] * doubles[1] + matrix[1][2] * doubles[2];
        result[2] = matrix[2][0] * doubles[0] + matrix[2][1] * doubles[1] + matrix[2][2] * doubles[2];
        return resultConstructor.createNew(result);
    }

    public <S extends Vector3D> SpaceConversionMatrix<S, O> multiply(SpaceConversionMatrix<S, I> multiplicand) {
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
