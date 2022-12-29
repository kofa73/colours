package kofa.maths;

import java.util.stream.DoubleStream;

public abstract class Vector3 {
    protected final double coordinate1;
    protected final double coordinate2;
    protected final double coordinate3;

    protected Vector3(double coordinate1, double coordinate2, double coordinate3) {
        this.coordinate1 = coordinate1;
        this.coordinate2 = coordinate2;
        this.coordinate3 = coordinate3;
    }

    public DoubleStream coordinates() {
        return DoubleStream.of(coordinate1, coordinate2, coordinate3);
    }
}
