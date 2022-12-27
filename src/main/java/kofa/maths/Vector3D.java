package kofa.maths;

public interface Vector3D extends DoubleVector {
    interface ConstructorFromArray<T> {
        T createNew(double[] values);
    }

    interface ConstructorFromComponents<T> {
        T createNew(double coordinate1, double coordinate2, double coordinate3);
    }
}
