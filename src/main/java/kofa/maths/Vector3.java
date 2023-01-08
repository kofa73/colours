package kofa.maths;

import java.util.Objects;
import java.util.function.DoublePredicate;
import java.util.stream.DoubleStream;

public abstract class Vector3 {
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
        return "%s(%.8f, %.8f, %.8f)".formatted(this.getClass().getSimpleName(), coordinate1, coordinate2, coordinate3);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector3 vector3 = (Vector3) o;
        return Double.compare(vector3.coordinate1, coordinate1) == 0 && Double.compare(vector3.coordinate2, coordinate2) == 0 && Double.compare(vector3.coordinate3, coordinate3) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate1, coordinate2, coordinate3);
    }
}
