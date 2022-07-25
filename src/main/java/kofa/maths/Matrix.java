package kofa.maths;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class Matrix<M extends Matrix<M>> {
    private final float[][] values;
    private final int nRows;
    private final int nColumns;
    private int fillingRow = 0;

    public Matrix(int nRows, int nColumns) {
        this.nRows = nRows;
        this.nColumns = nColumns;
        values = new float[nRows][nColumns];
    }

    public M row(float... row) {
        checkState(fillingRow < nRows, "Already added all %s rows", nRows);
        checkArgument(row.length == nColumns, "Row length must be %s", nColumns);
        values[fillingRow] = row.clone();
        fillingRow++;
        return (M) this;
    }

    public float[] multipliedBy(float... vector) {
        checkArgument(vector.length == nColumns, "Vector must have %s rows", nColumns);
        var result = new float[nRows];
        for (int columnIndex = 0; columnIndex < vector.length; columnIndex++) {
            for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
                result[rowIndex] += values[rowIndex][columnIndex] * vector[columnIndex];
            }
        }
        return result;
    }

    public Matrix<?> multipliedBy(Matrix<?> other) {
        checkArgument(this.nColumns == other.nRows, "other matrix should have %s rows", this.nColumns);
        var result = new Matrix<>(this.nRows, other.nColumns);
        for (int resultRowIndex = 0; resultRowIndex < result.nRows; resultRowIndex++) {
            var resultRow = new float[other.nColumns];
            for (int resultColumnIndex = 0; resultColumnIndex < other.nColumns; resultColumnIndex++) {
                resultRow[resultColumnIndex] = dotProduct(this.values[resultRowIndex], other.column(resultRowIndex));
            }
            result.row(resultRow);
        }
        return result;
    }

    public float[] column(int columnIndex) {
        checkArgument(columnIndex < nColumns, "columnIndex %s >= %s", columnIndex, nColumns);
        var columnValues = new float[nRows];
        for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
            columnValues[rowIndex] = values[rowIndex][columnIndex];
        }
        return columnValues;
    }

    private float dotProduct(float[] vector1, float[] vector2) {
        checkArgument(vector1.length == vector2.length, "vector1.length = %s, but vector2.length = %s");
        float result = 0;
        for (int index = 0; index < vector1.length; index++) {
            result += vector1[index] * vector2[index];
        }
        return result;
    }

    public int nColumns() {
        return nColumns;
    }
}
