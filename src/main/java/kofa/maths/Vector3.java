package kofa.maths;

import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

public abstract class Vector3<V extends Vector3<V>> {
    protected final double coordinate1;
    protected final double coordinate2;
    protected final double coordinate3;

    protected Vector3(double coordinate1, double coordinate2, double coordinate3) {
        this.coordinate1 = coordinate1;
        this.coordinate2 = coordinate2;
        this.coordinate3 = coordinate3;
        if (Double.isNaN(coordinate1) || Double.isNaN(coordinate2) || Double.isNaN(coordinate3)) {
            throw new IllegalArgumentException(toString());
        }
    }

    public boolean anyCoordinateMatches(DoublePredicate predicate) {
        return coordinates().anyMatch(predicate);
    }

    public final DoubleStream coordinates() {
        return DoubleStream.of(coordinate1, coordinate2, coordinate3);
    }

    public String toString() {
        return "%s(%.8f, %.8f, %.8f))".formatted(this.getClass().getSimpleName(), coordinate1, coordinate2, coordinate3);
    }
}
