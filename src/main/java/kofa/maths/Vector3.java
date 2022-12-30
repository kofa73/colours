package kofa.maths;

import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

public abstract class Vector3 {
    final double coordinate1;
    final double coordinate2;
    final double coordinate3;

    protected Vector3(double coordinate1, double coordinate2, double coordinate3) {
        this.coordinate1 = coordinate1;
        this.coordinate2 = coordinate2;
        this.coordinate3 = coordinate3;
        if (Double.isNaN(coordinate1) || Double.isNaN(coordinate2) || Double.isNaN(coordinate3)) {
            throw new IllegalArgumentException(this.toString());
        }
    }

    public boolean anyCoordinateMatches(DoublePredicate predicate) {
        return coordinates().anyMatch(predicate);
    }

    public abstract DoubleStream coordinates();

    public abstract String toString();

    protected static String format(Vector3 vector, double coordinate1, double coordinate2, double coordinate3) {
        return "%s(%.8f, %.8f, %.8f))".formatted(vector.getClass().getSimpleName(), coordinate1, coordinate2, coordinate3);
    }
}
