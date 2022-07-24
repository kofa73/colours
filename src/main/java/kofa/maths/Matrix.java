package kofa.maths;

import com.google.common.base.Preconditions;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class Matrix {
    private final float[][] values;
    private final int nRows;
    private final int nColumns;
    private int fillingRow = 0;

    public Matrix(int nRows, int nColumns) {
        this.nRows = nRows;
        this.nColumns = nColumns;
        values = new float[nRows][nColumns];
    }

    public Matrix row(float... row) {
        checkState(fillingRow < nRows, "Already added all %s rows", nRows);
        checkArgument(row.length == nColumns, "Row length must be %s", nColumns);
        values[fillingRow] = row.clone();
        fillingRow++;
        return this;
    }

    public float[] multiply(float... vector) {
        checkArgument(vector.length == nColumns, "Vector must have %s rows", nColumns);
        float[] result = new float[nRows];
        for (int columnIndex = 0; columnIndex < vector.length; columnIndex++) {
            for (int rowIndex = 0; rowIndex < nRows; rowIndex++) {
                result[rowIndex] += values[rowIndex][columnIndex] * vector[columnIndex];
            }
        }
        return result;
    }
}
