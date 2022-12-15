package kofa.maths;

public interface Vector3D extends DoubleVector {
    interface ConstructorFromArray<T> {
        T createNew(double[] values);
    }

    interface ConstructorFromValues<T> {
        T createNew(double a, double b, double c);
    }
}
