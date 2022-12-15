package kofa.maths;

import static com.google.common.base.Preconditions.checkState;

public class Matrix3<I extends Vector3D, O extends Vector3D> {
    private final double[][] values;
    private final Vector3D.ConstructorFromArray<O> resultConstructor;
    private int fillingRow = 0;

    public Matrix3(Vector3D.ConstructorFromArray<O> resultConstructor) {
        values = new double[3][3];
        this.resultConstructor = resultConstructor;
    }

    public Matrix3<I, O> row(double a, double b, double c) {
        checkState(fillingRow < 3, "Already added all 3 rows");
        values[fillingRow][0] = a;
        values[fillingRow][1] = b;
        values[fillingRow][2] = c;
        fillingRow++;
        return this;
    }

    public O multipliedBy(I vector) {
        var doubles = vector.values();
        var result = new double[3];
        for (int columnIndex = 0; columnIndex < 3; columnIndex++) {
            for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
                result[rowIndex] += values[rowIndex][columnIndex] * doubles[columnIndex];
            }
        }
        return resultConstructor.createNew(result);
    }
}
