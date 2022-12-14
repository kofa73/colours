package kofa.maths;

public interface Vector3D extends FloatVector {
    interface ConstructorFromArray<T> {
        T createNew(float[] values);
    }

    interface ConstructorFromValues<T> {
        T createNew(float a, float b, float c);
    }
}
