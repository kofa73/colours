package kofa.maths;

import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

public abstract class Vector3<V extends Vector3<V>> {
    protected Vector3(double coordinate1, double coordinate2, double coordinate3) {
        if (Double.isNaN(coordinate1) || Double.isNaN(coordinate2) || Double.isNaN(coordinate3)) {
            throw new IllegalArgumentException(format(this, coordinate1, coordinate2, coordinate3));
        }
    }

    protected abstract <T extends Vector3<T>> T multiplyBy(SpaceConversionMatrix<V, T> conversionMatrix);

    protected <T extends Vector3<T>> T multiplyBy(SpaceConversionMatrix<V, T> conversionMatrix, double coordinate1, double coordinate2, double coordinate3) {
        return conversionMatrix.multiply(coordinate1, coordinate2, coordinate3);
    }

    public boolean anyCoordinateMatches(DoublePredicate predicate) {
        return coordinates().anyMatch(predicate);
    }

    public abstract DoubleStream coordinates();

    public abstract String toString();

    protected static String format(Vector3<?> vector, double coordinate1, double coordinate2, double coordinate3) {
        return "%s(%.8f, %.8f, %.8f))".formatted(vector.getClass().getSimpleName(), coordinate1, coordinate2, coordinate3);
    }
}
