package kofa.maths;

public class Matrix3x3<I extends Vector3D, O extends Vector3D> {
    private final double[][] values;
    private final Vector3D.ConstructorFromArray<O> resultConstructor;

    public Matrix3x3(
            Vector3D.ConstructorFromArray<O> resultConstructor,
            double value00, double value01, double value02,
            double value10, double value11, double value12,
            double value20, double value21, double value22
    ) {
        values = new double[][]{
                {value00, value01, value02},
                {value10, value11, value12},
                {value20, value21, value22}
        };
        this.resultConstructor = resultConstructor;
    }

    public O multiply(I vector) {
        var doubles = vector.values();
        var result = new double[3];
        result[0] = values[0][0] * doubles[0] + values[0][1] * doubles[1] + values[0][2] * doubles[2];
        result[1] = values[1][0] * doubles[0] + values[1][1] * doubles[1] + values[1][2] * doubles[2];
        result[2] = values[2][0] * doubles[0] + values[2][1] * doubles[1] + values[2][2] * doubles[2];
        return resultConstructor.createNew(result);
    }

    public <S extends Vector3D> Matrix3x3<S, O> multiply(Matrix3x3<S, I> multiplicand) {
        double[][] otherValues = multiplicand.values;

        var cell00 = values[0][0] * otherValues[0][0] + values[0][1] * otherValues[1][0] + values[0][2] * otherValues[2][0];
        var cell01 = values[0][0] * otherValues[0][1] + values[0][1] * otherValues[1][1] + values[0][2] * otherValues[2][1];
        var cell02 = values[0][0] * otherValues[0][2] + values[0][1] * otherValues[1][2] + values[0][2] * otherValues[2][2];

        var cell10 = values[1][0] * otherValues[0][0] + values[1][1] * otherValues[1][0] + values[1][2] * otherValues[2][0];
        var cell11 = values[1][0] * otherValues[0][1] + values[1][1] * otherValues[1][1] + values[1][2] * otherValues[2][1];
        var cell12 = values[1][0] * otherValues[0][2] + values[1][1] * otherValues[1][2] + values[1][2] * otherValues[2][2];

        var cell20 = values[2][0] * otherValues[0][0] + values[2][1] * otherValues[1][0] + values[2][2] * otherValues[2][0];
        var cell21 = values[2][0] * otherValues[0][1] + values[2][1] * otherValues[1][1] + values[2][2] * otherValues[2][1];
        var cell22 = values[2][0] * otherValues[0][2] + values[2][1] * otherValues[1][2] + values[2][2] * otherValues[2][2];

        return new Matrix3x3<>(
                resultConstructor,
                cell00, cell01, cell02,
                cell10, cell11, cell12,
                cell20, cell21, cell22
        );
    }

    public double[][] values() {
        double[] row0 = values[0].clone();
        double[] row1 = values[1].clone();
        double[] row2 = values[2].clone();
        return new double[][]{row0, row1, row2};
    }
}
